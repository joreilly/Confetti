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
    abstract val serviceName: Property<String>

    @TaskAction
    fun taskAction() {
        val serviceName = serviceName.get()
        val servicesClient = ServicesClient.create(ServicesSettings.newBuilder()
            .setCredentialsProvider(
                GoogleCredentials.fromStream(
                    ByteArrayInputStream(gcpServiceAccountJson.encodeToByteArray())
                ).let {
                    FixedCredentialsProvider.create(it)
                }
            )
            .build())

        val fullName = ServiceName.of(gcpProjectName, gcpRegion, serviceName).toString()
        val existingService = servicesClient.getService(fullName)
        val newService = Service.newBuilder()
            .setName(fullName)
            .setTemplate(
                existingService.template
                    .toBuilder()
                    .setRevision("$serviceName-${revision()}")
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
