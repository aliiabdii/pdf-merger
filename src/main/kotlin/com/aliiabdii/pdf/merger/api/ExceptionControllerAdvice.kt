package com.aliiabdii.pdf.merger.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException

data class ErrorMessageModel(
    var status: Int? = null,
    var message: String? = null
)

@ControllerAdvice
class ExceptionControllerAdvice(
    @Value($$"${spring.servlet.multipart.max-file-size}") private val maxFileSize: String
) {

    @ExceptionHandler
    fun handleMaxUploadSideException(ex: MaxUploadSizeExceededException): ResponseEntity<ErrorMessageModel> {

        val errorMessage = ErrorMessageModel(
            HttpStatus.CONTENT_TOO_LARGE.value(),
            "File too large! Maximum allowed size for each file is $maxFileSize"
        )
        return ResponseEntity(errorMessage, HttpStatus.CONTENT_TOO_LARGE)
    }

    @ExceptionHandler
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorMessageModel> {

        val errorMessage = ErrorMessageModel(
            HttpStatus.BAD_REQUEST.value(),
            ex.message
        )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleIllegalStateException(ex: Exception): ResponseEntity<ErrorMessageModel> {

        val errorMessage = ErrorMessageModel(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.message
        )
        return ResponseEntity(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}