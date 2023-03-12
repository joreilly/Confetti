import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.gradle.api.Project
import java.io.File

fun Project.gcpServiceAccountFile() = rootProject.file("backend/datastore/src/jvmMain/resources/gcp_service_account_key.json")

fun Project.uploadLandingPage() {
    val storage: Storage = StorageOptions.newBuilder()
        .setCredentials(gcpServiceAccountFile().inputStream().use {
            GoogleCredentials.fromStream(it)
        })
        .build()
        .service

    val bucketName = "confetti-landing-page"

    println("uploading landing page...")
    val base = file("public")
    base.walk().filter { it.isFile }
        .forEach { file ->
            file.inputStream().use {
                val blobId = BlobId.of(bucketName, file.relativeTo(base).path)
                val blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.contentType()).build()

                storage.createFrom(blobInfo, it)
            }
        }
}

private fun File.contentType() : String{
    return when (this.extension) {
        "html", "htm" -> "text/html"
        "css" -> "text/css"
        else -> "application/octet-stream"
    }
}