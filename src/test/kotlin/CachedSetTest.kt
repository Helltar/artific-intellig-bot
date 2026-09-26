import com.helltar.aibot.database.CachedSet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CachedSetTest {

    @Test
    fun `the values are loaded once, even by concurrent first checks`() = runBlocking {
        val loads = AtomicInteger()
        val set = CachedSet { loads.incrementAndGet(); delay(50); listOf(1L, 2L) }

        val results = (1..20).map { async { set.contains(1L) } }.awaitAll()

        assertTrue(results.all { it })
        assertEquals(1, loads.get())
    }

    @Test
    fun `adds and removes change the loaded values without loading again`() = runBlocking {
        val loads = AtomicInteger()
        val set = CachedSet { loads.incrementAndGet(); listOf(1L) }

        set.add(2L)
        set.remove(1L)

        assertTrue(set.contains(2L))
        assertFalse(set.contains(1L))
        assertEquals(1, loads.get(), "an add before the first check must load first, not replace the values")
    }
}
