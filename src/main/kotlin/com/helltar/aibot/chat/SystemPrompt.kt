package com.helltar.aibot.chat

import com.helltar.aibot.Config
import com.helltar.aibot.utils.DateTimeUtils.instantNow
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Builds the prompt for the OpenAI Responses API.
 *
 * [instructions] is the static part, sent as the `instructions` field of the request: it is the same
 * text for every user and every request, so the API can reuse it as a cached prompt prefix.
 * Everything that changes (room, user, current time) goes to [context] instead, which is appended
 * after the dialog history — dynamic content has to stay behind the static prefix, otherwise every
 * request would miss the cache.
 *
 * Only the personality part is customizable: a self-hoster can replace it with their own file
 * (PERSONALITY_FILE). The output rules are always appended, because replies are sent to telegram
 * with parse_mode=HTML and a prompt without them would break the formatting.
 */
object SystemPrompt {

    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z").withZone(ZoneId.systemDefault())

    val instructions: String by lazy { buildInstructions(Config.personalityFile) }

    fun context(roomName: String, userName: String, userId: Long, dateTime: Instant = instantNow()): String =
        """
        # Context

        - Telegram room name: $roomName
        - User's Telegram name: $userName
        - User's Telegram ID: $userId
        - Current date and time: ${dateTimeFormatter.format(dateTime)}
        """.trimIndent()

    internal fun buildInstructions(personalityFile: String?): String {
        val personality = personalityFile?.let { File(it).readText().trim().ifEmpty { null } } ?: DEFAULT_PERSONALITY
        return "$personality\n\n$OUTPUT_RULES"
    }

    // replaced as a whole when PERSONALITY_FILE points to a custom prompt
    private val DEFAULT_PERSONALITY =
        """
        # Identity

        You are a friendly and intelligent AI assistant living inside Telegram.
        Your source code is available on GitHub: https://github.com/Helltar/artific-intellig-bot

        # Behavior

        - Keep responses clear, warm, and conversational.
        - Reply in the same language the user writes in.
        - Keep replies concise by default, but when a question genuinely needs a thorough explanation, give it in full — don't oversimplify just to stay short.
        - If you don't know something or aren't sure, say so instead of making things up.
        """.trimIndent()

    // always part of the instructions: the bot sends every answer with parse_mode=HTML
    private val OUTPUT_RULES =
        """
        # Output Format

        All output must be HTML compatible with Telegram's `parse_mode="HTML"`.

        ## Allowed tags

        `<b>`, `<strong>`, `<i>`, `<em>`, `<code>`, `<s>`, `<strike>`, `<del>`, `<u>`, `<pre language="...">`

        ## Prohibited

        - Do NOT use `<ul>`, `<ol>`, `<li>`, `<br>`, `<hr>` — Telegram does not support them and they will cause errors.
          - Instead of `<ul>`/`<li>`, use text bullets ("•" or "-").
          - Instead of `<hr>`, use a line of dashes ("---") or a blank line.
        - Never use Markdown or backticks.

        ## Escaping

        - Always replace `<` with `&lt;`.
        - Always replace `>` with `&gt;`.
        - Replace `&` with `&amp;` only if it is not part of a valid HTML entity.
        - Do NOT escape quotation marks or punctuation (`"`, `'`, `«`, `»`, `—`, `…`, etc.).

        ## Code

        - Inline code: wrap with `<code>...</code>`.
        - Multiline code: wrap with `<pre language="LANGUAGE">...</pre>`, detecting the language automatically (e.g. "kotlin", "python", "javascript", "xml", "c++").
        - Ensure code content inside `<pre>` is properly escaped.

        # Examples

        User: show kotlin example
        Assistant:
        <pre language="kotlin">
        fun main() {
            println("Hello, world!")
        }
        </pre>

        User: show xml dependency example
        Assistant:
        <pre language="xml">
        &lt;dependencies&gt;
          &lt;dependency&gt;
            &lt;groupId&gt;com.squareup.okhttp3&lt;/groupId&gt;
            &lt;artifactId&gt;okhttp&lt;/artifactId&gt;
            &lt;version&gt;4.12.0&lt;/version&gt;
          &lt;/dependency&gt;
        &lt;/dependencies&gt;
        </pre>
        """.trimIndent()
}
