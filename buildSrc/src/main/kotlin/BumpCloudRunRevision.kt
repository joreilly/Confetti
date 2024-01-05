import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.run.v2.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayInputStream
import java.util.*

abstract class BumpCloudRunRevision : DefaultTask() {
    @get:Input
    abstract val imageName: Property<String>

    @TaskAction
    fun taskAction() {
        val imageName = imageName.get()
        val servicesClient = ServicesClient.create(ServicesSettings.newBuilder()
            .setCredentialsProvider(
                GoogleCredentials.fromStream(
                    ByteArrayInputStream(gcpServiceAccountJson.encodeToByteArray())
                ).let {
                    FixedCredentialsProvider.create(it)
                }
            )
            .build())

        val name = ServiceName.of(gcpProjectName, gcpRegion, imageName).toString()
        val existingService = servicesClient.getService(name)
        val newService = Service.newBuilder()
            .setName(name)
            .setTemplate(
                existingService.template
                    .toBuilder()
                    .setRevision("$name-${revision()}")
                    .build()
            )
            .build()

        servicesClient.updateServiceAsync(newService).get()
    }
}

/**
 * We need to force something new or else no new revision is created
 */
private fun revision(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    return String.format(
        Locale.ROOT,
        "%4d-%02d-%02d-%02d%02d%02d",
        now.year,
        now.monthNumber,
        now.dayOfMonth,
        now.hour,
        now.minute,
        now.second
    )
}
