package com.example.scanner

import com.example.domain.model.IocMatchResult
import com.example.domain.model.StalkerwareIoc

object StalkerwareIocDatabase {

    const val DATABASE_VERSION = "2026.10.1-offline"
    const val DATABASE_SOURCE = "Echap Stalkerware Indicators & Mobile Security Research Archive"
    const val PUBLICATION_DATE = "2026-10-01"

    // Bundled offline indicators cataloged by privacy research groups
    private val BUNDLED_IOCS = listOf(
        StalkerwareIoc(
            packageId = "com.mspy.android",
            certSha256 = "6F:73:9A:82:5E:4C:9D:61:74:02:44:E2:B0:81:49:15:37:92:A9:39:DF:44:A2:72:08:C1:28:FE:8B:13:95:C7",
            knownName = "mSpy Mobile Tracker",
            classification = "Commercial Stalkerware",
            source = "Echap Indicators / Security Research",
            notes = "Commercial surveillance tool marketed for clandestine device tracking, call interception, and keystroke logging."
        ),
        StalkerwareIoc(
            packageId = "com.trackview",
            certSha256 = null,
            knownName = "TrackView Spy Tool",
            classification = "Dual-Use Surveillance",
            source = "Security Research Archive",
            notes = "Dual-use tool frequently deployed covertly for background camera recording, GPS tracking, and remote access."
        ),
        StalkerwareIoc(
            packageId = "com.flexispy",
            certSha256 = "91:E7:62:3B:AA:89:D4:58:34:61:99:A2:27:E1:8B:25:29:43:CE:39:41:BC:07:95:90:54:33:DE:7E:6F:09:A1",
            knownName = "FlexiSPY Client",
            classification = "Commercial Stalkerware",
            source = "Echap Indicators",
            notes = "High-threat surveillance package supporting ambient audio recording, call interception, and hidden operation."
        ),
        StalkerwareIoc(
            packageId = "com.hoverwatch.android",
            certSha256 = null,
            knownName = "Hoverwatch Spy App",
            classification = "Commercial Stalkerware",
            source = "Echap Indicators",
            notes = "Covert tracker capable of periodic screenshot capture, front camera stealth photos, and message monitoring."
        ),
        StalkerwareIoc(
            packageId = "org.cocos2dx.Ghost",
            certSha256 = null,
            knownName = "Cerberus Disguised Client",
            classification = "Stalkerware Disguise",
            source = "Security Research Archive",
            notes = "Cerberus anti-theft modified or disguised under faux system package names for unauthorized remote control."
        ),
        StalkerwareIoc(
            packageId = "com.clevguard.kidsguard",
            certSha256 = null,
            knownName = "KidsGuard Pro Tracker",
            classification = "Dual-Use Surveillance",
            source = "Echap Indicators",
            notes = "Marketed as parental control, frequently sideloaded for covert adult tracking and keylogging."
        ),
        StalkerwareIoc(
            packageId = "com.spyier.app",
            certSha256 = null,
            knownName = "Spyier Tracker",
            classification = "Commercial Stalkerware",
            source = "Security Research Archive",
            notes = "Sideloaded surveillance utility capable of GPS tracking, message exfiltration, and browser log extraction."
        ),
        StalkerwareIoc(
            packageId = "com.thetruthspy",
            certSha256 = null,
            knownName = "TheTruthSpy Monitor",
            classification = "Commercial Stalkerware",
            source = "Echap Indicators",
            notes = "Well-documented stalkerware package with history of remote mic listening and GPS relay."
        ),
        StalkerwareIoc(
            packageId = "com.xgpy.android",
            certSha256 = null,
            knownName = "XNSPY Surveillance Tool",
            classification = "Commercial Stalkerware",
            source = "Echap Indicators",
            notes = "Surveillance suite capable of reading WhatsApp, SMS, call recordings, and continuous location updates."
        ),
        StalkerwareIoc(
            packageId = "com.spybubble",
            certSha256 = null,
            knownName = "SpyBubble",
            classification = "Commercial Stalkerware",
            source = "Security Research Archive",
            notes = "Stealth tracker designed to hide launcher icon and transmit background location and audio clips."
        )
    )

    fun findMatch(packageName: String, certSha256: String?): IocMatchResult? {
        val matchingIoc = BUNDLED_IOCS.firstOrNull { it.packageId.equals(packageName.trim(), ignoreCase = true) }
            ?: return null

        val isCorroborated = if (!matchingIoc.certSha256.isNullOrEmpty() && !certSha256.isNullOrEmpty()) {
            normalizeSha(matchingIoc.certSha256) == normalizeSha(certSha256)
        } else {
            false
        }

        val desc = if (isCorroborated) {
            "Corroborated match: Package identifier and signing certificate SHA-256 fingerprint match known stalkerware artifact (${matchingIoc.knownName})."
        } else {
            "Package identifier match: Matches known indicator for '${matchingIoc.knownName}'. (Signing certificate fingerprint uncorroborated or pending)."
        }

        return IocMatchResult(
            ioc = matchingIoc,
            isCertCorroborated = isCorroborated,
            matchDescription = desc
        )
    }

    fun getAllIndicators(): List<StalkerwareIoc> = BUNDLED_IOCS

    private fun normalizeSha(sha: String): String {
        return sha.replace(":", "").replace(" ", "").uppercase()
    }
}
