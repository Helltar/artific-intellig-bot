package com.helltar.aibot.database

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.VarCharColumnType

/**
 * Cuts [value] to the length of this varchar column: postgres rejects a longer value instead of cutting it.
 *
 * The length is counted in characters, as postgres counts it, so a character outside the basic
 * plane (an emoji) is never split in half.
 */
fun Column<*>.fit(value: String): String {
    val length = (columnType as VarCharColumnType).colLength

    return if (value.codePointCount(0, value.length) <= length)
        value
    else
        value.substring(0, value.offsetByCodePoints(0, length))
}
