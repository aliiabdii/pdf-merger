package com.aliiabdii.pdf.merger.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PDFServiceTests {
    private val pdfService = PDFService()

    @Test
    fun `every compression level returns a valid PDF no larger than the input`() {
        val original = createImagePdf()

        CompressionLevel.entries.forEach { level ->
            val result = pdfService.compressPdf(pdfFile(original), level)

            assertTrue(result.bytes.size <= original.size)
            Loader.loadPDF(result.bytes).use { document ->
                assertEquals(1, document.numberOfPages)
            }
        }
    }

    @Test
    fun `original PDF is returned when optimization does not reduce its size`() {
        val original = createEmptyPdf()

        val result = pdfService.compressPdf(pdfFile(original), CompressionLevel.LIGHT)

        assertFalse(result.isCompressed)
        assertContentEquals(original, result.bytes)
    }

    private fun pdfFile(bytes: ByteArray) = MockMultipartFile(
        "file",
        "document.pdf",
        MediaType.APPLICATION_PDF_VALUE,
        bytes
    )

    private fun createImagePdf(): ByteArray {
        val random = Random(0)
        val image = BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                image.setRGB(x, y, random.nextInt(1 shl 24))
            }
        }

        PDDocument().use { document ->
            val page = PDPage()
            document.addPage(page)
            val imageXObject = LosslessFactory.createFromImage(document, image)

            PDPageContentStream(document, page).use { content ->
                content.drawImage(imageXObject, 0f, 0f, 612f, 459f)
            }

            return ByteArrayOutputStream().use { output ->
                document.save(output)
                output.toByteArray()
            }
        }
    }

    private fun createEmptyPdf(): ByteArray {
        PDDocument().use { document ->
            document.addPage(PDPage())

            return ByteArrayOutputStream().use { output ->
                document.save(output)
                output.toByteArray()
            }
        }
    }
}
