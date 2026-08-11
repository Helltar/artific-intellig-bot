import com.helltar.aibot.health.Heartbeat
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class HeartbeatTest {

    @Test
    fun `heartbeat file is absent until the first poll cycle`() {
        withHeartbeat { file, _ ->
            sleep(SETTLE)

            assertFalse(Files.exists(file), "a bot whose polling never started must not pass the healthcheck")
        }
    }

    @Test
    fun `heartbeat file is refreshed while poll cycles keep coming`() {
        withHeartbeat { file, heartbeat ->
            heartbeat.markPoll()
            sleep(SETTLE)

            assertTrue(Files.exists(file))
            val first = Files.readString(file)

            heartbeat.markPoll()
            sleep(SETTLE)

            assertNotEquals(first, Files.readString(file), "a live polling loop must keep the file fresh")
        }
    }

    @Test
    fun `heartbeat file goes stale once poll cycles stop`() {
        withHeartbeat { file, heartbeat ->
            heartbeat.markPoll()
            sleep(SETTLE)
            assertTrue(Files.exists(file))

            // no further markPoll: past the staleness window the file must stop being touched, which is
            // what turns a silently dead polling loop into a failing healthcheck
            sleep(STALE_AFTER + SETTLE)
            val frozen = Files.readString(file)

            sleep(SETTLE)

            assertEquals(frozen, Files.readString(file), "a stalled polling loop must stop refreshing the file")
        }
    }

    @Test
    fun `awaiting the first poll gives up when polling never starts`() {
        withHeartbeat { _, heartbeat ->
            assertFalse(
                heartbeat.awaitFirstPoll(SETTLE),
                "a swallowed registration failure must be reported, so the process can exit and be restarted"
            )
        }
    }

    @Test
    fun `awaiting the first poll returns once the loop cycles`() {
        withHeartbeat { _, heartbeat ->
            heartbeat.markPoll()

            assertTrue(heartbeat.awaitFirstPoll(SETTLE))
        }
    }

    private fun withHeartbeat(block: (Path, Heartbeat) -> Unit) {
        val dir = Files.createTempDirectory("aibot-heartbeat-test")
        val file = dir.resolve("health")
        val heartbeat = Heartbeat(file, staleAfter = STALE_AFTER, interval = INTERVAL)
        val thread = heartbeat.start()

        try {
            block(file, heartbeat)
        } finally {
            thread.interrupt()
            thread.join()
            dir.toFile().deleteRecursively()
        }
    }

    private fun sleep(duration: Duration) = Thread.sleep(duration.inWholeMilliseconds)

    private companion object {
        val INTERVAL = 20.milliseconds
        val STALE_AFTER = 200.milliseconds

        // several write attempts, so a single slow tick does not decide the assertion
        val SETTLE = 100.milliseconds
    }
}
