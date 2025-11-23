package com.aliiabdii

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import java.io.ByteArrayOutputStream

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PdfMergerIntegrationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `merge endpoint returns merged pdf`() {
        val pdf1 = createPdf(1)
        val pdf2 = createPdf(2)

        val file1 = MockMultipartFile("files", "a.pdf", MediaType.APPLICATION_PDF_VALUE, pdf1)
        val file2 = MockMultipartFile("files", "b.pdf", MediaType.APPLICATION_PDF_VALUE, pdf2)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/merge")
                .file(file1)
                .file(file2)
                .characterEncoding("utf-8")
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Disposition", containsString("merged.pdf")))
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn()

        val mergedBytes = result.response.contentAsByteArray
        Loader.loadPDF(mergedBytes).use { doc ->
            assertEquals(3, doc.numberOfPages)
        }
    }

    private fun createPdf(pages: Int): ByteArray {
        PDDocument().use { doc ->
            repeat(pages) { doc.addPage(PDPage()) }
            ByteArrayOutputStream().use { baos ->
                doc.save(baos)
                return baos.toByteArray()
            }
        }
    }
}
