package dev.johnoreilly.confetti.agent

import ai.koog.rag.base.files.FileMetadata
import ai.koog.rag.base.files.FileSystemProvider
import kotlinx.io.Sink
import kotlinx.io.Source
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

class OkioFileSystemProvider(
    private val fs: FileSystem,
) : FileSystemProvider.ReadWrite<Path> {

    override fun toAbsolutePathString(path: Path): String = path.normalized().toString()

    override fun fromAbsolutePathString(path: String): Path {
        val resolved = path.toPath().normalized()
        require(resolved.isAbsolute) { "Resolved path must be absolute: $path" }
        return resolved
    }

    override fun joinPath(base: Path, vararg parts: String): Path {
        return parts.fold(base) { acc, part ->
            val partPath = part.toPath()
            require(!partPath.isAbsolute) { "Path must be relative, but was absolute: $part" }
            acc.resolve(partPath)
        }.normalized()
    }

    override fun name(path: Path): String = path.name

    override fun extension(path: Path): String {
        val n = path.name
        val dot = n.lastIndexOf('.')
        return if (dot <= 0) "" else n.substring(dot + 1)
    }

    override suspend fun metadata(path: Path): FileMetadata? {
        val m = fs.metadataOrNull(path) ?: return null
        val type = when {
            m.isRegularFile -> FileMetadata.FileType.File
            m.isDirectory -> FileMetadata.FileType.Directory
            else -> return null
        }
        return FileMetadata(type, hidden = path.name.startsWith("."))
    }

    override suspend fun getFileContentType(path: Path): FileMetadata.FileContentType {
        require(fs.exists(path)) { "Path must exist: $path" }
        return FileMetadata.FileContentType.Text
    }

    override suspend fun list(directory: Path): List<Path> {
        require(fs.exists(directory)) { "Path must exist: $directory" }
        return fs.list(directory).sortedBy { it.name }
    }

    override fun parent(path: Path): Path? = path.parent

    override fun relativize(root: Path, path: Path): String? {
        val rootParts = root.normalized().segments
        val pathParts = path.normalized().segments
        if (pathParts.size < rootParts.size) return null
        for (i in rootParts.indices) {
            if (rootParts[i] != pathParts[i]) return null
        }
        return pathParts.drop(rootParts.size).joinToString("/")
    }

    override suspend fun exists(path: Path): Boolean = fs.exists(path)

    override suspend fun readBytes(path: Path): ByteArray =
        fs.read(path) { readByteArray() }

    override suspend fun inputStream(path: Path): Source =
        throw UnsupportedOperationException("inputStream is not used by FileVectorStorageBackend")

    override suspend fun size(path: Path): Long = fs.metadata(path).size ?: 0L

    override suspend fun create(path: Path, type: FileMetadata.FileType) {
        path.parent?.let { fs.createDirectories(it) }
        when (type) {
            FileMetadata.FileType.Directory -> fs.createDirectories(path)
            FileMetadata.FileType.File -> fs.write(path) { /* touch */ }
        }
    }

    override suspend fun move(source: Path, target: Path) {
        target.parent?.let { fs.createDirectories(it) }
        fs.atomicMove(source, target)
    }

    override suspend fun copy(source: Path, target: Path) {
        target.parent?.let { fs.createDirectories(it) }
        fs.copy(source, target)
    }

    override suspend fun writeBytes(path: Path, data: ByteArray) {
        path.parent?.let { fs.createDirectories(it) }
        fs.write(path) { write(data) }
    }

    override suspend fun outputStream(path: Path, append: Boolean): Sink =
        throw UnsupportedOperationException("outputStream is not used by FileVectorStorageBackend")

    override suspend fun delete(path: Path) {
        fs.deleteRecursively(path)
    }
}
