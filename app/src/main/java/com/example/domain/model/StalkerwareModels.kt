package com.example.domain.model

enum class ThreatCategory(val label: String, val minScore: Int, val maxScore: Int) {
    LOW("Low Concern", 0, 19),
    REVIEW_RECOMMENDED("Review Recommended", 20, 39),
    ELEVATED_RISK("Elevated Risk", 40, 59),
    HIGH_RISK("High Risk — Investigate Promptly", 60, 79),
    VERY_HIGH_RISK("Very High Risk Indicators", 80, 100);

    companion object {
        fun fromScore(score: Int): ThreatCategory = when {
            score >= 80 -> VERY_HIGH_RISK
            score >= 60 -> HIGH_RISK
            score >= 40 -> ELEVATED_RISK
            score >= 20 -> REVIEW_RECOMMENDED
            else -> LOW
        }
    }
}

data class ThreatIndicator(
    val id: String,
    val title: String,
    val score: Int,
    val evidence: String,
    val explanation: String
)

data class StalkerwareIoc(
    val packageId: String,
    val certSha256: String? = null,
    val knownName: String,
    val classification: String,
    val source: String,
    val notes: String
)

data class IocMatchResult(
    val ioc: StalkerwareIoc,
    val isCertCorroborated: Boolean,
    val matchDescription: String
)
