package com.aliiabdii.pdf.merger.service

import com.aliiabdii.pdf.merger.config.TurnstileConfig
import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

data class TurnstileVerifyResponse(
    val success: Boolean,
    @JsonAlias("challenge_ts")
    val challengeTS: String?,
    val hostname: String?,
    val error: List<String>?
)

@Service
class TurnstileService(
    private val webClient: WebClient,
    private val config: TurnstileConfig
) {
    fun isEnabled(): Boolean {
        return config.enabled
    }

    fun getSiteKey(): String {
        return config.siteKey
    }

    fun validate(remoteAddress: String, captchaToken: String): Boolean {
        if (!config.enabled) return true

        return verifyToken(captchaToken, remoteAddress)
    }

    private fun verifyToken(token: String, remoteIp: String?): Boolean {
        val body = mapOf(
            "secret" to config.secretKey,
            "response" to token,
            "remoteip" to (remoteIp ?: "")
        )

        val response = webClient.post()
            .uri("https://challenges.cloudflare.com/turnstile/v0/siteverify")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(TurnstileVerifyResponse::class.java)
            .block()

        return response?.success == true
    }
}
