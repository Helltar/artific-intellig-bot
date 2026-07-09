import com.helltar.aibot.utils.StringUtils.singleLineTruncated
import kotlin.test.Test
import kotlin.test.assertEquals

class StringUtilsTest {

    @Test
    fun `collapses whitespace and newlines into single spaces`() {
        assertEquals("one two three", "one\n\ntwo\t  three".singleLineTruncated(100))
    }

    @Test
    fun `trims surrounding whitespace`() {
        assertEquals("text", "  text \n".singleLineTruncated(100))
    }

    @Test
    fun `returns string unchanged when within limit`() {
        assertEquals("short", "short".singleLineTruncated(5))
    }

    @Test
    fun `truncates with ellipsis when over limit`() {
        assertEquals("12345…", "1234567890".singleLineTruncated(5))
    }

    @Test
    fun `truncation applies to length after collapsing whitespace`() {
        assertEquals("a b…", "a\n\n\nb c".singleLineTruncated(3))
    }
}
