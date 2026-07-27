package com.helltar.aibot.openai

import java.security.MessageDigest

/**
 * Per-user identifiers sent along with the api requests.
 *
 * The telegram user id is hashed, as the api docs recommend, so it is not sent as is:
 * `safety_identifier` only has to be stable and unique per user, not readable.
 * The same hash is used for `prompt_cache_key` — requests with an equal key are routed to the same
 * cache, and it is the history of one user that repeats as a prompt prefix between requests.
 */
object ApiUser {

    // safety_identifier is limited to 64 characters, half of a sha-256 digest is more than enough
    private const val HASH_LENGTH = 32

    fun safetyIdentifier(userId: Long): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(userId.toString().toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(HASH_LENGTH)

    fun promptCacheKey(prefix: String, userId: Long): String =
        "$prefix-${safetyIdentifier(userId)}"
}
