package com.aliiabdii.pdf.merger

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
class MainController(private val pdfService: PDFService) {

    @GetMapping("/actuator/health")
    fun healthCheck(): ResponseEntity<String> {
        return ResponseEntity.ok("OK")
    }

    @GetMapping("/", "/index")
    fun index(): String {
        return "page/index"
    }

    @GetMapping("/about")
    fun about(): String {
        return "page/about"
    }

    @PostMapping(value = ["/merge"], consumes = ["multipart/form-data"])
    fun merge(
        @RequestParam("files") files: Array<MultipartFile>
    ): ResponseEntity<ByteArrayResource> {
        if (files.isEmpty()) return ResponseEntity.badRequest().build()
        try {
            val mergedBytes = pdfService.mergePdfFiles(files)
            val resource = ByteArrayResource(mergedBytes)
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"merged.pdf\"")
                .contentLength(mergedBytes.size.toLong())
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(resource)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(500).build()
        }
    }
}