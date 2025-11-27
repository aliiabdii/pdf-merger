package com.aliiabdii.pdf.merger.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

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
}