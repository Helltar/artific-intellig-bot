package com.helltar.aibot.exceptions

import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException

/**
 * Thrown when Telegram rejects a message because of broken HTML markup
 * (e.g. an unclosed or unsupported tag).
 *
 * Telegram reports this as a generic 400 [TelegramApiRequestException] with a
 * description like:
 *   "Bad Request: can't parse entities: Can't find end tag corresponding to start tag "b""
 *
 * Wrapping it in a dedicated type lets callers handle formatting failures
 * specifically instead of catching every [TelegramApiException].
 */
class TelegramFormattingException(cause: TelegramApiException) :
    TelegramApiException("telegram rejected message formatting: ${cause.message}", cause) {

    companion object {
        private const val PARSE_ENTITIES_MARKER = "can't parse entities"

        fun isFormattingError(e: TelegramApiException): Boolean =
            e is TelegramApiRequestException &&
                e.errorCode == 400 &&
                e.message?.contains(PARSE_ENTITIES_MARKER, ignoreCase = true) == true
    }
}
