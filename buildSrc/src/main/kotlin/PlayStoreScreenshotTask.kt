import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

abstract class PlayStoreScreenshotTask : DefaultTask() {
    @get:InputFiles
    abstract val selectedImages: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @TaskAction
    fun generateImages() {
        selectedImages.forEach {
            checkSourceScreenshotExists(it)
        }

        deleteAllExisting()

        selectedImages.forEachIndexed { index, file ->
            copyScreenshot(file, index)
        }
    }

    private fun checkSourceScreenshotExists(it: File) {
        check(it.exists()) {
            "Source file $it does not exist"
        }
    }

    private fun deleteAllExisting() {
        val existing = output.asFileTree.files
        logger.info("Deleting " + existing.map { it.name })
        project.delete(existing)
    }

    private fun copyScreenshot(file: File, index: Int) {
        val sourceImage = ImageIO.read(file)
        val destImage = BufferedImage(sourceImage.width, sourceImage.height, sourceImage.type)
        val g = destImage.createGraphics()

        g.setColor(Color.BLACK)
        g.fillRect(0, 0, destImage.width, destImage.height)

        g.drawImage(sourceImage, null, 0, 0)

        val destinationName = "${'a' + index}_${file.name.replace("\\W+.*".toRegex(), "")}.png"
        ImageIO.write(destImage, "png", output.file(destinationName).get().asFile)
    }
}