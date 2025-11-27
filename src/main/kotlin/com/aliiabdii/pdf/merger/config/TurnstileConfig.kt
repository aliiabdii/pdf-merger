package com.aliiabdii.pdf.merger.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "turnstile")
class TurnstileConfig {
    var enabled: Boolean = true
    lateinit var siteKey: String
    lateinit var secretKey: String
}
