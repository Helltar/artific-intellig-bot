package com.helltar.aibot.utils

object StringUtils {

    private val whitespaceRegex = Regex("\\s+")

    fun String.singleLineTruncated(maxLength: Int): String {
        val singleLine = replace(whitespaceRegex, " ").trim()
        return singleLine.takeIf { it.length <= maxLength } ?: "${singleLine.take(maxLength)}…"
    }
}
