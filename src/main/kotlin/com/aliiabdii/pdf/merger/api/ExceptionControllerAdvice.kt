package com.aliiabdii.pdf.merger.api

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.multipart.MaxUploadSizeExceededException

data class ErrorMessageModel(
    var status: Int? = null,
    var message: String? = null
)

private val logger = KotlinLogging.logger {}

@ControllerAdvice
class ExceptionControllerAdvice(
    @Value($$"${spring.servlet.multipart.max-request-size}") private val maxRequestSize: String
) {

    @ExceptionHandler
    fun handleMaxUploadSideException(ex: MaxUploadSizeExceededException): ResponseEntity<ErrorMessageModel> {
        logger.error { ex }

        val errorMessage = ErrorMessageModel(
            HttpStatus.CONTENT_TOO_LARGE.value(),
            "Request too large! Maximum allowed size for each request is $maxRequestSize"
        )
        return ResponseEntity(errorMessage, HttpStatus.CONTENT_TOO_LARGE)
    }

    @ExceptionHandler
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorMessageModel> {
        logger.error { ex }

        val errorMessage = ErrorMessageModel(
            HttpStatus.BAD_REQUEST.value(),
            ex.message
        )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        request: ServletWebRequest): ResponseEntity<ErrorMessageModel>
    {
        logger.error { "Unsupported method '${ex.method}' for path '${request.request.requestURI}'" }

        val errorMessage = ErrorMessageModel(
            HttpStatus.BAD_REQUEST.value(),
            ex.message
        )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleIllegalStateException(ex: Exception): ResponseEntity<ErrorMessageModel> {
        logger.error { ex }
        
        val errorMessage = ErrorMessageModel(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.message
        )
        return ResponseEntity(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}