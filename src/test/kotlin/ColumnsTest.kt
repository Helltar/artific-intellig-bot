import com.helltar.aibot.database.fit
import com.helltar.aibot.database.tables.BannedUsersTable
import kotlin.test.Test
import kotlin.test.assertEquals

class ColumnsTest {

    /* reason is varchar(150) */
    private val column = BannedUsersTable.reason

    @Test
    fun `a value within the column length is kept`() {
        assertEquals("spam", column.fit("spam"))
        assertEquals("a".repeat(150), column.fit("a".repeat(150)))
    }

    @Test
    fun `a longer value is cut to the column length`() {
        assertEquals("a".repeat(150), column.fit("a".repeat(300)))
    }

    @Test
    fun `the length is counted in characters, an emoji is not split`() {
        val emoji = "😀" // one character, two utf-16 units
        val value = emoji.repeat(200)

        val fitted = column.fit(value)

        assertEquals(150, fitted.codePointCount(0, fitted.length))
        assertEquals(emoji.repeat(150), fitted)
    }
}
