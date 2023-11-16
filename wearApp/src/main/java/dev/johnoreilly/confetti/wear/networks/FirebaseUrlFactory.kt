package dev.johnoreilly.confetti.wear.networks

/*
* Copyright (C) 2014 Square, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Handshake
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import okio.BufferedSink
import okio.Pipe
import okio.Timeout
import okio.buffer
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.Proxy
import java.net.SocketPermission
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory
import java.security.Permission
import java.security.Principal
import java.security.cert.Certificate
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.TreeMap
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

/**
 * OkHttp 3.14 dropped support for the long-deprecated OkUrlFactory class, which allows you to use
 * the HttpURLConnection API with OkHttp's implementation. This class does the same thing using only
 * public APIs in OkHttp. It requires OkHttp 3.14 or newer.
 *
 *
 * Rather than pasting this 1100 line gist into your source code, please upgrade to OkHttp's
 * request/response API. Your code will be shorter, easier to read, and you'll be able to use
 * interceptors.
 */
class FirebaseUrlFactory(private val client: Call.Factory) : URLStreamHandlerFactory, Cloneable {
    /**
     * Returns a copy of this stream handler factory that includes a shallow copy of the internal
     * [HTTP client][OkHttpClient].
     */
    public override fun clone(): FirebaseUrlFactory {
        return FirebaseUrlFactory(client)
    }

    fun open(url: URL): HttpURLConnection {
        if (url.protocol == "http") return OkHttpURLConnection(url, client)
        if (url.protocol == "https") return OkHttpsURLConnection(url, client)
        throw IllegalArgumentException("Unexpected protocol: $url.protocol")
    }

    /**
     * Creates a URLStreamHandler as a [java.net.URL.setURLStreamHandlerFactory].
     *
     *
     * This code configures OkHttp to handle all HTTP and HTTPS connections
     * created with [java.net.URL.openConnection]: <pre>   `OkHttpClient okHttpClient = new OkHttpClient();
     * URL.setURLStreamHandlerFactory(new ObsoleteUrlFactory(okHttpClient));
    `</pre> *
     */
    override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
        return if (protocol != "http" && protocol != "https") {
            null
        } else {
            object : URLStreamHandler() {
                override fun openConnection(url: URL): URLConnection {
                    return open(url)
                }

                override fun openConnection(url: URL, proxy: Proxy): URLConnection {
                    if (proxy.type() != Proxy.Type.DIRECT)
                        throw UnsupportedOperationException()
                    return open(url)
                }

                override fun getDefaultPort(): Int {
                    if (protocol == "http") return 80
                    if (protocol == "https") return 443
                    throw AssertionError()
                }
            }
        }
    }

    internal class OkHttpURLConnection(
        url: URL?, // These fields are confined to the application thread that uses HttpURLConnection.
        var client: Call.Factory
    ) : HttpURLConnection(url), Callback {
        var requestHeaders: Headers.Builder = Headers.Builder()
        var responseHeaders: Headers? = null
        var executed = false
        var call: Call? = null

        // These fields are guarded by lock.
        private val lock = Any()
        private var response: Response? = null
        private var callFailure: Throwable? = null
        var networkResponse: Response? = null
        var connectPending = true
        var handshake: Handshake? = null
        
        override fun connect() {
            if (executed) return
            val call = buildCall()
            executed = true
            call.enqueue(this)
            synchronized(lock) {
                try {
                    while (connectPending && response == null && callFailure == null) {
                        (lock as Object).wait() // Wait 'til the network interceptor is reached or the call fails.
                    }
                    if (callFailure != null) {
                        throw propagate(callFailure)
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt() // Retain interrupted status.
                    throw InterruptedIOException()
                }
            }
        }

        override fun disconnect() {
            // Calling disconnect() before a connection exists should have no effect.
            if (call == null) return
            call!!.cancel()
        }

        override fun getErrorStream(): InputStream? {
            return try {
                val response = getResponse(true)
                if (hasBody(response) && response.code >= HTTP_BAD_REQUEST) {
                    response.body.byteStream()
                } else null
            } catch (e: IOException) {
                null
            }
        }

        
        val headers: Headers
            get() {
                if (responseHeaders == null) {
                    val response = getResponse(true)
                    val headers = response.headers
                    responseHeaders = headers.newBuilder()
                        .add(SELECTED_PROTOCOL, response.protocol.toString())
                        .add(RESPONSE_SOURCE, responseSourceHeader(response))
                        .build()
                }
                return responseHeaders!!
            }

        override fun getHeaderField(position: Int): String? {
            return try {
                val headers = headers
                if (position < 0 || position >= headers.size) null else headers.value(position)
            } catch (e: IOException) {
                null
            }
        }

        override fun getHeaderField(fieldName: String?): String? {
            return try {
                if (fieldName == null) statusLineToString(getResponse(true)) else headers[fieldName]!!
            } catch (e: IOException) {
                null
            }
        }

        override fun getHeaderFieldKey(position: Int): String? {
            return try {
                val headers = headers
                if (position < 0 || position >= headers.size) null else headers.name(position)
            } catch (e: IOException) {
                null
            }
        }

        override fun getHeaderFields(): Map<String, List<String>> {
            return try {
                toMultimap(headers, statusLineToString(getResponse(true)))
            } catch (e: IOException) {
                emptyMap()
            }
        }

        override fun getRequestProperties(): Map<String, List<String>> {
            check(!connected) { "Cannot access request header fields after connection is set" }
            return toMultimap(requestHeaders.build(), null)
        }

        
        override fun getInputStream(): InputStream {
            if (!doInput) {
                throw ProtocolException("This protocol does not support input")
            }
            val response = getResponse(false)
            if (response.code >= HTTP_BAD_REQUEST) throw FileNotFoundException(url.toString())
            return response.body.byteStream()
        }

        
        override fun getOutputStream(): OutputStream {
            val requestBody = buildCall().request().body as OutputStreamRequestBody?
                ?: throw ProtocolException("method does not support a request body: $method")
            if (requestBody.closed) {
                throw ProtocolException("cannot write request body after response has been read")
            }
            return requestBody.outputStream!!
        }

        override fun getPermission(): Permission {
            val url = getURL()
            var hostname = url.host
            var hostPort = if (url.port != -1) url.port else HttpUrl.defaultPort(url.protocol)
            return SocketPermission("$hostname:$hostPort", "connect, resolve")
        }

        override fun getRequestProperty(field: String?): String? {
            return if (field == null) null else requestHeaders.get(field)
        }

        override fun setConnectTimeout(timeoutMillis: Int) {
            // ignored
        }

        override fun setInstanceFollowRedirects(followRedirects: Boolean) {
            // ignored
        }

        override fun getInstanceFollowRedirects(): Boolean {
            TODO()
        }

        override fun getConnectTimeout(): Int {
            TODO()
        }

        override fun setReadTimeout(timeoutMillis: Int) {
            // ignored
        }

        override fun getReadTimeout(): Int {
            TODO()
        }

        private fun buildCall(): Call {
            if (call != null) {
                return call!!
            }
            connected = true
            if (doOutput) {
                if (method == "GET") {
                    method = "POST"
                } else if (!permitsRequestBody(method)) {
                    throw ProtocolException("$method does not support writing")
                }
            }
            var requestBody: OutputStreamRequestBody? = null
            if (permitsRequestBody(method)) {
                var contentType: String? = requestHeaders.get("Content-Type")
                if (contentType == null) {
                    contentType = "application/x-www-form-urlencoded"
                    requestHeaders.add("Content-Type", contentType)
                }
                val stream = fixedContentLengthLong != -1L || chunkLength > 0
                var contentLength = -1L
                val contentLengthString: String? = requestHeaders.get("Content-Length")
                if (fixedContentLengthLong != -1L) {
                    contentLength = fixedContentLengthLong
                } else if (contentLengthString != null) {
                    contentLength = contentLengthString.toLong()
                }
                requestBody = if (stream) StreamedRequestBody(contentLength) else BufferedRequestBody(contentLength)
            }

            val url = getURL().toHttpUrlOrNull() ?: throw MalformedURLException("URL = ${getURL()}")

            val request: Request = Request.Builder()
                .url(url)
                .headers(requestHeaders.build())
                .method(method, requestBody)
                .apply {
                    // If we're currently not using caches, make sure the engine's client doesn't have one.
                    if (!getUseCaches()) {
                        cacheControl(CacheControl.FORCE_NETWORK)
                    }
                }
                .build()

            return client.newCall(request).also {
                call = it
            }
        }

        
        private fun getResponse(networkResponseOnError: Boolean): Response {
            synchronized(lock) {
                if (response != null) return response!!
                if (callFailure != null) {
                    if (networkResponseOnError && networkResponse != null) return networkResponse!!
                    throw propagate(callFailure)
                }
            }
            val call = buildCall()
            val requestBody = call.request().body as OutputStreamRequestBody?
            if (requestBody != null) requestBody.outputStream!!.close()
            if (executed) {
                synchronized(lock) {
                    try {
                        while (response == null && callFailure == null) {
                            (lock as Object).wait() // Wait until the response is returned or the call fails.
                        }
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt() // Retain interrupted status.
                        throw InterruptedIOException()
                    }
                }
            } else {
                executed = true
                try {
                    onResponse(call, call.execute())
                } catch (e: IOException) {
                    onFailure(call, e)
                }
            }
            synchronized(lock) {
                if (callFailure != null) throw propagate(callFailure)
                if (response != null) return response!!
            }
            throw AssertionError()
        }

        override fun usingProxy(): Boolean {
            return false
        }

        
        override fun getResponseMessage(): String {
            return getResponse(true).message
        }

        
        override fun getResponseCode(): Int {
            return getResponse(true).code
        }

        override fun setRequestProperty(field: String, newValue: String) {
            check(!connected) { "Cannot set request property after connection is made" }
            requestHeaders[field] = newValue
        }

        override fun setIfModifiedSince(newValue: Long) {
            super.setIfModifiedSince(newValue)
            if (ifModifiedSince != 0L) {
                requestHeaders["If-Modified-Since"] = format(Date(ifModifiedSince))
            } else {
                requestHeaders.removeAll("If-Modified-Since")
            }
        }

        override fun addRequestProperty(field: String, value: String) {
            check(!connected) { "Cannot add request property after connection is made" }
            requestHeaders.add(field, value)
        }

        
        override fun setRequestMethod(method: String) {
            if (!METHODS.contains(method)) {
                throw ProtocolException("Expected one of $METHODS but was $method")
            }
            this.method = method
        }

        override fun setFixedLengthStreamingMode(contentLength: Int) {
            setFixedLengthStreamingMode(contentLength.toLong())
        }

        override fun setFixedLengthStreamingMode(contentLength: Long) {
            check(!super.connected) { "Already connected" }
            check(chunkLength <= 0) { "Already in chunked mode" }
            require(contentLength >= 0) { "contentLength < 0" }
            this.fixedContentLengthLong = contentLength
        }

        override fun onFailure(call: Call, e: IOException) {
            synchronized(lock) {
                callFailure = e
                (lock as Object).notifyAll()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            synchronized(lock) {
                this.response = response
                handshake = response.handshake
                url = response.request.url.toUrl()
                (lock as Object).notifyAll()
            }
        }
    }

    internal abstract class OutputStreamRequestBody : RequestBody() {
        var timeout: Timeout? = null
        var expectedContentLength: Long = 0
        var outputStream: OutputStream? = null
        var closed = false
        fun initOutputStream(sink: BufferedSink, expectedContentLength: Long) {
            timeout = sink.timeout()
            this.expectedContentLength = expectedContentLength

            // An output stream that writes to sink. If expectedContentLength is not -1, then this expects
            // exactly that many bytes to be written.
            outputStream = object : OutputStream() {
                private var bytesReceived: Long = 0
                
                override fun write(b: Int) {
                    write(byteArrayOf(b.toByte()), 0, 1)
                }

                
                override fun write(source: ByteArray, offset: Int, byteCount: Int) {
                    if (closed) throw IOException("closed") // Not IllegalStateException!
                    if (expectedContentLength != -1L && bytesReceived + byteCount > expectedContentLength) {
                        throw ProtocolException(
                            "expected " + expectedContentLength
                                + " bytes but received " + bytesReceived + byteCount
                        )
                    }
                    bytesReceived += byteCount.toLong()
                    try {
                        sink.write(source, offset, byteCount)
                    } catch (e: InterruptedIOException) {
                        throw SocketTimeoutException(e.message)
                    }
                }

                
                override fun flush() {
                    if (closed) return  // Weird, but consistent with historical behavior.
                    sink.flush()
                }

                
                override fun close() {
                    closed = true
                    if (expectedContentLength != -1L && bytesReceived < expectedContentLength) {
                        throw ProtocolException(
                            "expected " + expectedContentLength
                                + " bytes but received " + bytesReceived
                        )
                    }
                    sink.close()
                }
            }
        }

        override fun contentLength(): Long {
            return expectedContentLength
        }

        override fun contentType(): MediaType? {
            return null // Let the caller provide this in a regular header.
        }

        
        open fun prepareToSendRequest(request: Request): Request {
            return request
        }
    }

    internal class BufferedRequestBody(expectedContentLength: Long) : OutputStreamRequestBody() {
        val buffer = Buffer()
        var contentLength = -1L

        init {
            initOutputStream(buffer, expectedContentLength)
        }

        override fun contentLength(): Long {
            return contentLength
        }

        
        override fun prepareToSendRequest(request: Request): Request {
            if (request.header("Content-Length") != null) return request
            outputStream!!.close()
            contentLength = buffer.size
            return request.newBuilder()
                .removeHeader("Transfer-Encoding")
                .header("Content-Length", buffer.size.toString())
                .build()
        }

        override fun writeTo(sink: BufferedSink) {
            buffer.copyTo(sink.buffer, 0, buffer.size)
        }
    }

    internal class StreamedRequestBody(expectedContentLength: Long) : OutputStreamRequestBody() {
        private val pipe = Pipe(8192)

        init {
            initOutputStream(pipe.sink.buffer(), expectedContentLength)
        }

        override fun isOneShot(): Boolean {
            return true
        }

        
        override fun writeTo(sink: BufferedSink) {
            val buffer = Buffer()
            while (pipe.source.read(buffer, 8192) != -1L) {
                sink.write(buffer, buffer.size)
            }
        }
    }

    internal abstract class DelegatingHttpsURLConnection(val delegate: HttpURLConnection) : HttpsURLConnection(
        delegate.url
    ) {
        protected abstract fun handshake(): Handshake?
        abstract override fun setHostnameVerifier(hostnameVerifier: HostnameVerifier)
        abstract override fun getHostnameVerifier(): HostnameVerifier
        abstract override fun setSSLSocketFactory(sslSocketFactory: SSLSocketFactory)
        abstract override fun getSSLSocketFactory(): SSLSocketFactory
        override fun getCipherSuite(): String? {
            return handshake()?.cipherSuite?.javaName
        }

        override fun getLocalCertificates(): Array<Certificate>? {
            val handshake = handshake() ?: return null
            val result = handshake.localCertificates
            return if (result.isNotEmpty()) result.toTypedArray<Certificate>() else null
        }

        override fun getServerCertificates(): Array<Certificate>? {
            val handshake = handshake() ?: return null
            val result = handshake.peerCertificates
            return if (result.isNotEmpty()) result.toTypedArray<Certificate>() else null
        }

        override fun getPeerPrincipal(): Principal? {
            return handshake()?.peerPrincipal
        }

        override fun getLocalPrincipal(): Principal? {
            return handshake()?.localPrincipal
        }

        
        override fun connect() {
            connected = true
            delegate.connect()
        }

        override fun disconnect() {
            delegate.disconnect()
        }

        override fun getErrorStream(): InputStream {
            return delegate.errorStream
        }

        override fun getRequestMethod(): String {
            return delegate.requestMethod
        }

        
        override fun getResponseCode(): Int {
            return delegate.getResponseCode()
        }

        
        override fun getResponseMessage(): String {
            return delegate.getResponseMessage()
        }

        
        override fun setRequestMethod(method: String) {
            delegate.setRequestMethod(method)
        }

        override fun usingProxy(): Boolean {
            return delegate.usingProxy()
        }

        override fun getInstanceFollowRedirects(): Boolean {
            return delegate.instanceFollowRedirects
        }

        override fun setInstanceFollowRedirects(followRedirects: Boolean) {
            delegate.instanceFollowRedirects = followRedirects
        }

        override fun getAllowUserInteraction(): Boolean {
            return delegate.allowUserInteraction
        }

        
        override fun getContent(): Any {
            return delegate.getContent()
        }

        
        override fun getContent(types: Array<Class<*>?>?): Any {
            return delegate.getContent(types)
        }

        override fun getContentEncoding(): String {
            return delegate.contentEncoding
        }

        override fun getContentLength(): Int {
            return delegate.getContentLength()
        }

        // Should only be invoked on Java 8+ or Android API 24+.
        override fun getContentLengthLong(): Long {
            return delegate.contentLengthLong
        }

        override fun getContentType(): String {
            return delegate.contentType
        }

        override fun getDate(): Long {
            return delegate.date
        }

        override fun getDefaultUseCaches(): Boolean {
            return delegate.defaultUseCaches
        }

        override fun getDoInput(): Boolean {
            return delegate.doInput
        }

        override fun getDoOutput(): Boolean {
            return delegate.doOutput
        }

        override fun getExpiration(): Long {
            return delegate.expiration
        }

        override fun getHeaderField(pos: Int): String {
            return delegate.getHeaderField(pos)
        }

        override fun getHeaderFields(): Map<String, List<String>> {
            return delegate.headerFields
        }

        override fun getRequestProperties(): Map<String, List<String>> {
            return delegate.getRequestProperties()
        }

        override fun addRequestProperty(field: String, newValue: String) {
            delegate.addRequestProperty(field, newValue)
        }

        override fun getHeaderField(key: String): String {
            return delegate.getHeaderField(key)
        }

        // Should only be invoked on Java 8+ or Android API 24+.
        override fun getHeaderFieldLong(field: String, defaultValue: Long): Long {
            return delegate.getHeaderFieldLong(field, defaultValue)
        }

        override fun getHeaderFieldDate(field: String, defaultValue: Long): Long {
            return delegate.getHeaderFieldDate(field, defaultValue)
        }

        override fun getHeaderFieldInt(field: String, defaultValue: Int): Int {
            return delegate.getHeaderFieldInt(field, defaultValue)
        }

        override fun getHeaderFieldKey(position: Int): String {
            return delegate.getHeaderFieldKey(position)
        }

        override fun getIfModifiedSince(): Long {
            return delegate.getIfModifiedSince()
        }

        
        override fun getInputStream(): InputStream {
            return delegate.inputStream
        }

        override fun getLastModified(): Long {
            return delegate.lastModified
        }

        
        override fun getOutputStream(): OutputStream {
            return delegate.outputStream
        }

        
        override fun getPermission(): Permission {
            return delegate.getPermission()
        }

        override fun getRequestProperty(field: String): String {
            return delegate.getRequestProperty(field)
        }

        override fun getURL(): URL {
            return delegate.url
        }

        override fun getUseCaches(): Boolean {
            return delegate.useCaches
        }

        override fun setAllowUserInteraction(newValue: Boolean) {
            delegate.setAllowUserInteraction(newValue)
        }

        override fun setDefaultUseCaches(newValue: Boolean) {
            delegate.defaultUseCaches = newValue
        }

        override fun setDoInput(newValue: Boolean) {
            delegate.setDoInput(newValue)
        }

        override fun setDoOutput(newValue: Boolean) {
            delegate.setDoOutput(newValue)
        }

        // Should only be invoked on Java 8+ or Android API 24+.
        override fun setFixedLengthStreamingMode(contentLength: Long) {
            delegate.setFixedLengthStreamingMode(contentLength)
        }

        override fun setIfModifiedSince(newValue: Long) {
            delegate.setIfModifiedSince(newValue)
        }

        override fun setRequestProperty(field: String, newValue: String) {
            delegate.setRequestProperty(field, newValue)
        }

        override fun setUseCaches(newValue: Boolean) {
            delegate.setUseCaches(newValue)
        }

        override fun setConnectTimeout(timeoutMillis: Int) {
            delegate.setConnectTimeout(timeoutMillis)
        }

        override fun getConnectTimeout(): Int {
            return delegate.connectTimeout
        }

        override fun setReadTimeout(timeoutMillis: Int) {
            delegate.setReadTimeout(timeoutMillis)
        }

        override fun getReadTimeout(): Int {
            return delegate.readTimeout
        }

        override fun toString(): String {
            return delegate.toString()
        }

        override fun setFixedLengthStreamingMode(contentLength: Int) {
            delegate.setFixedLengthStreamingMode(contentLength)
        }

        override fun setChunkedStreamingMode(chunkLength: Int) {
            delegate.setChunkedStreamingMode(chunkLength)
        }
    }

    internal class OkHttpsURLConnection(url: URL?, client: Call.Factory) : DelegatingHttpsURLConnection(
        OkHttpURLConnection(url, client)
    ) {

        override fun handshake(): Handshake? {
            checkNotNull((delegate as OkHttpURLConnection).call) { "Connection has not yet been established" }
            return delegate.handshake
        }

        override fun setHostnameVerifier(hostnameVerifier: HostnameVerifier) {
            // ignored
        }

        override fun getHostnameVerifier(): HostnameVerifier {
            TODO()
        }

        override fun setSSLSocketFactory(sslSocketFactory: SSLSocketFactory) {
            // ignored
        }

        override fun getSSLSocketFactory(): SSLSocketFactory {
            TODO()
        }
    }

    companion object {
        const val SELECTED_PROTOCOL = "ObsoleteUrlFactory-Selected-Protocol"
        const val RESPONSE_SOURCE = "ObsoleteUrlFactory-Response-Source"
        val METHODS: Set<String> =
            LinkedHashSet(mutableListOf("OPTIONS", "GET", "HEAD", "POST", "PUT", "DELETE", "TRACE", "PATCH"))
        val UTC = TimeZone.getTimeZone("GMT")
        const val HTTP_CONTINUE = 100
        val STANDARD_DATE_FORMAT = ThreadLocal.withInitial {

            // Date format specified by RFC 7231 section 7.1.1.1.
            val rfc1123: DateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
            rfc1123.isLenient = false
            rfc1123.timeZone = UTC
            rfc1123
        }
        val FIELD_NAME_COMPARATOR = java.lang.String.CASE_INSENSITIVE_ORDER

        fun format(value: Date?): String {
            return STANDARD_DATE_FORMAT.get().format(value)
        }

        fun permitsRequestBody(method: String): Boolean {
            return !(method == "GET" || method == "HEAD")
        }

        /**
         * Returns true if the response must have a (possibly 0-length) body. See RFC 7231.
         */
        fun hasBody(response: Response): Boolean {
            // HEAD requests never yield a body regardless of the response headers.
            if (response.request.method == "HEAD") {
                return false
            }
            val responseCode = response.code
            if ((responseCode < HTTP_CONTINUE || responseCode >= 200) && responseCode != HttpURLConnection.HTTP_NO_CONTENT && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
                return true
            }

            // If the Content-Length or Transfer-Encoding headers disagree with the response code, the
            // response is malformed. For best compatibility, we honor the headers.
            return if (contentLength(response.headers) != -1L
                || "chunked".equals(response.header("Transfer-Encoding"), ignoreCase = true)
            ) {
                true
            } else false
        }

        fun contentLength(headers: Headers): Long {
            val s = headers["Content-Length"] ?: return -1
            return try {
                s.toLong()
            } catch (e: NumberFormatException) {
                -1
            }
        }

        fun responseSourceHeader(response: Response): String {
            if (response.networkResponse == null) {
                return if (response.cacheResponse == null) "NONE" else "CACHE " + response.code
            }
            return if (response.cacheResponse == null) "NETWORK " + response.code else "CONDITIONAL_CACHE " + response.networkResponse!!
                .code
        }

        fun statusLineToString(response: Response): String {
            return ((if (response.protocol == Protocol.HTTP_1_0) "HTTP/1.0" else "HTTP/1.1")
                + ' ' + response.code
                + ' ' + response.message)
        }

        fun toMultimap(headers: Headers, valueForNullKey: String?): Map<String, List<String>> {
            val result: MutableMap<String?, List<String>> = TreeMap(FIELD_NAME_COMPARATOR)
            var i = 0
            val size = headers.size
            while (i < size) {
                val fieldName = headers.name(i)
                val value = headers.value(i)
                val allValues: MutableList<String> = ArrayList()
                val otherValues = result[fieldName]
                if (otherValues != null) {
                    allValues.addAll(otherValues)
                }
                allValues.add(value)
                result[fieldName] = Collections.unmodifiableList(allValues)
                i++
            }
            if (valueForNullKey != null) {
                result[null] = listOf(valueForNullKey)
            }
            return Collections.unmodifiableMap(result)
        }

        
        fun propagate(throwable: Throwable?): IOException {
            if (throwable is IOException) throw (throwable as IOException?)!!
            if (throwable is Error) throw (throwable as Error?)!!
            if (throwable is RuntimeException) throw (throwable as RuntimeException?)!!
            throw AssertionError()
        }
    }
}
