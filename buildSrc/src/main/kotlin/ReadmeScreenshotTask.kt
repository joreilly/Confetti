import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import javax.imageio.ImageIO
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import java.awt.geom.Point2D

abstract class ReadmeScreenshotTask : DefaultTask() {
    @get:InputFiles
    abstract val selectedImages: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val output: RegularFileProperty

    @TaskAction
    fun generateImages() {
        selectedImages.forEach {
            checkSourceScreenshotExists(it)
        }

        generateCombinedScreenshot()
    }

    private fun checkSourceScreenshotExists(it: java.io.File) {
        check(it.exists()) {
            "Source file $it does not exist"
        }
    }

    private fun generateCombinedScreenshot() {
        val destImage = BufferedImage(1000, 1000, TYPE_INT_ARGB)
        val g = destImage.createGraphics()

        selectedImages.forEachIndexed { index, file ->
            val sourceImage = ImageIO.read(file)
            val columns = 2
            val x = (index % columns) * 500
            val y = (index / columns) * 500
            g.drawImage(sourceImage, null, x, y)
        }

        ImageIO.write(destImage, "png", output.get().asFile)
    }
}