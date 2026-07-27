import com.helltar.aibot.chat.SystemPrompt
import java.io.File
import java.time.Instant
import kotlin.test.*

class SystemPromptTest {

    private fun withPromptFile(text: String, block: (String) -> Unit) {
        val file = File.createTempFile("system-prompt-test", ".md").apply { writeText(text) }
        try {
            block(file.absolutePath)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `built-in prompt is used when no custom file is set`() {
        val instructions = SystemPrompt.buildInstructions(null)

        assertContains(instructions, "You are a friendly and intelligent AI assistant")
        assertContains(instructions, "# Output Format")
    }

    @Test
    fun `custom file replaces the personality and keeps the output rules`() = withPromptFile("You are Marvin, a paranoid android.") { path ->
        val instructions = SystemPrompt.buildInstructions(path)

        assertContains(instructions, "You are Marvin, a paranoid android.")
        assertFalse(instructions.contains("Your source code is available on GitHub"), "the built-in personality must be replaced")
        assertContains(instructions, "parse_mode=\"HTML\"", message = "telegram output rules must always be appended")
        assertTrue(instructions.startsWith("You are Marvin"), "the custom prompt must go first")
    }

    @Test
    fun `blank custom file falls back to the built-in personality`() = withPromptFile("   \n  ") { path ->
        assertContains(SystemPrompt.buildInstructions(path), "You are a friendly and intelligent AI assistant")
    }

    @Test
    fun `instructions have no placeholders left from the old template`() {
        val instructions = SystemPrompt.buildInstructions(null)

        assertFalse(instructions.contains("{room_name}"))
        assertFalse(instructions.contains("{user_name}"))
        assertFalse(instructions.contains("{user_id}"))
    }

    @Test
    fun `context carries the room, the user and the current time`() {
        val context = SystemPrompt.context("Kotlin Chat", "tester", 42, Instant.parse("2026-07-27T10:15:30Z"))

        assertContains(context, "- Telegram room name: Kotlin Chat")
        assertContains(context, "- User's Telegram name: tester")
        assertContains(context, "- User's Telegram ID: 42")

        /* the time is formatted in the local zone of the bot, so the day can shift by one */
        assertContains(context, "- Current date and time: 2026-07-2")
    }
}
