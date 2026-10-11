package com.aliiabdii.pdf.merger.service

import org.apache.pdfbox.cos.COSBase
import org.apache.pdfbox.Loader
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.pdfwriter.compress.CompressParameters
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Collections
import java.util.IdentityHashMap

enum class CompressionLevel(
    val jpegQuality: Float,
    val maxImageWidth: Int
) {
    LIGHT(0.85f, 2400),
    BALANCED(0.60f, 1600),
    STRONG(0.40f, 1200)
}

data class CompressionResult(
    val bytes: ByteArray,
    val isCompressed: Boolean
)

@Service
class PDFService {

    fun mergePdfFiles(pdfFiles: Array<MultipartFile>): ByteArray {
        PDDocument().use { mergedDocument ->
            pdfFiles.forEach { file ->
                Loader.loadPDF(file.bytes).use {
                    document -> PDFMergerUtility().appendDocument(mergedDocument, document)
                }
            }

            ByteArrayOutputStream().use { outputStream ->
                mergedDocument.save(outputStream)
                return outputStream.toByteArray()
            }
        }
    }

    fun compressPdf(
        file: MultipartFile,
        compressionLevel: CompressionLevel
    ): CompressionResult {
        val originalBytes = file.bytes

        Loader.loadPDF(originalBytes).use { document ->
            val processedResources = Collections.newSetFromMap(IdentityHashMap<COSBase, Boolean>())
            val replacementImages = IdentityHashMap<COSBase, PDImageXObject>()

            for (page in document.pages) {
                page.resources?.let {
                    compressResources(
                        document,
                        it,
                        compressionLevel,
                        processedResources,
                        replacementImages
                    )
                }
            }

            val compressedBytes = ByteArrayOutputStream().use { output ->
                document.save(output, CompressParameters.DEFAULT_COMPRESSION)
                output.toByteArray()
            }

            return if (compressedBytes.size < originalBytes.size) {
                CompressionResult(compressedBytes, true)
            } else {
                CompressionResult(originalBytes, false)
            }
        }
    }

    private fun compressResources(
        document: PDDocument,
        resources: PDResources,
        compressionLevel: CompressionLevel,
        processedResources: MutableSet<COSBase>,
        replacementImages: IdentityHashMap<COSBase, PDImageXObject>
    ) {
        if (!processedResources.add(resources.cosObject)) return

        for (name in resources.xObjectNames) {
            val xObject = resources.getXObject(name)

            if (xObject is PDImageXObject) {
                val replacement = replacementImages.getOrPut(xObject.cosObject) {
                    val image = resize(xObject.image, compressionLevel.maxImageWidth)

                    if (image.colorModel.hasAlpha()) {
                        LosslessFactory.createFromImage(document, image)
                    } else {
                        JPEGFactory.createFromImage(document, image, compressionLevel.jpegQuality)
                    }
                }

                resources.put(name, replacement)
            } else if (xObject is PDFormXObject) {
                xObject.resources?.let {
                    compressResources(
                        document,
                        it,
                        compressionLevel,
                        processedResources,
                        replacementImages
                    )
                }
            }
        }
    }

    private fun resize(image: BufferedImage, maxWidth: Int): BufferedImage {
        if (image.width <= maxWidth) return image

        val scale = maxWidth.toDouble() / image.width
        val newHeight = (image.height * scale).toInt()

        val type = if (image.colorModel.hasAlpha())
            BufferedImage.TYPE_INT_ARGB
        else
            BufferedImage.TYPE_INT_RGB

        val resized = BufferedImage(maxWidth, newHeight, type)

        val g = resized.createGraphics()
        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC
        )
        g.drawImage(image, 0, 0, maxWidth, newHeight, null)
        g.dispose()

        return resized
    }
}