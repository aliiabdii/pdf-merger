package com.aliiabdii

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PdfMergerApplication

fun main(args: Array<String>) {
	runApplication<PdfMergerApplication>(*args)
}
