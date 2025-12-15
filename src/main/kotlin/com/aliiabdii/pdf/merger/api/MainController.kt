package com.aliiabdii.pdf.merger.api

import com.aliiabdii.pdf.merger.service.PDFService
import com.aliiabdii.pdf.merger.service.TurnstileService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.unit.DataSize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

private val logger = KotlinLogging.logger {}

@Controller
class MainController(
    @Value($$"${service.max-files:5}") private val maxFiles: Int,
    @Value($$"${service.max-files:5}") private val maxSize: DataSize,
    private val turnstileService: TurnstileService,
    private val pdfService: PDFService) {

    @GetMapping("/actuator/health")
    fun healthCheck(): ResponseEntity<String> {
        return ResponseEntity.ok("OK")
    }

    @GetMapping("/", "/index")
    fun index(model: Model): String {
        model.addAttribute("siteKey", turnstileService.getSiteKey())
        model.addAttribute("captchaEnabled", turnstileService.isEnabled())
        model.addAttribute("maxFiles", maxFiles)
        model.addAttribute("maxSize", maxSize.toMegabytes())
        return "page/index"
    }

    @GetMapping("/about")
    fun about(): String {
        return "page/about"
    }

    @PostMapping(value = ["/merge"], consumes = ["multipart/form-data"])
    fun merge(
        @RequestParam("files") files: Array<MultipartFile>,
        @RequestParam("cf-turnstile-response", required = false) captchaToken: String?,
        request: HttpServletRequest
    ): ResponseEntity<ByteArrayResource> {
        if (files.size > maxFiles) {
            throw IllegalStateException("Only up to $maxFiles files are allowed")
        }
        val clientIp = request.remoteAddr
        val isValid = turnstileService.validate(captchaToken ?: "", clientIp)

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        if (files.isEmpty()) return ResponseEntity.badRequest().build()
        try {
            val mergedBytes = pdfService.mergePdfFiles(files)
            val resource = ByteArrayResource(mergedBytes)
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"merged.pdf\"")
                .contentLength(mergedBytes.size.toLong())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource)
        } catch (e: Exception) {
            logger.error { e }
            return ResponseEntity.status(500).build()
        }
    }
}