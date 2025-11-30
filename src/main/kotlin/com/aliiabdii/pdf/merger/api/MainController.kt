package com.aliiabdii.pdf.merger.api

import com.aliiabdii.pdf.merger.service.PDFService
import com.aliiabdii.pdf.merger.service.TurnstileService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
class MainController(
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
        return "page/index"
    }

    @GetMapping("/about")
    fun about(): String {
        return "page/about"
    }

    @PostMapping(value = ["/merge"], consumes = ["multipart/form-data"])
    fun merge(
        @RequestParam("files") files: Array<MultipartFile>,
        @RequestParam("cf-turnstile-response") captchaToken: String,
        request: HttpServletRequest
    ): ResponseEntity<ByteArrayResource> {
        val clientIp = request.remoteAddr
        val isValid = turnstileService.validate(captchaToken, clientIp)

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
            e.printStackTrace()
            return ResponseEntity.status(500).build()
        }
    }
}