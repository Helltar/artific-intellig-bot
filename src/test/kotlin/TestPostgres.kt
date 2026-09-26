import com.helltar.aibot.database.Database
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.junit.jupiter.api.Assumptions
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile
import java.io.File

/* A postgres for the database tests, started on first use and shared by them: every test takes a
   database of its own in it. Without docker the database tests are skipped. */
class TestPostgres private constructor(image: String) {

    companion object {
        const val CREATOR_ID = 1L
        val COMMANDS = listOf("chat", "imgen")

        private const val USER = "aibot"
        private const val PASSWORD = "secret"

        private val dockerAvailable by lazy { DockerClientFactory.instance().isDockerAvailable }

        // the version compose.yaml runs
        val current by lazy {
            TestPostgres(
                File("compose.yaml").readLines()
                    .map { it.trim() }
                    .first { it.startsWith("image: postgres:") }
                    .removePrefix("image: ")
            )
        }

        // the migrations read catalogs that differ between 17 and 18, and older installations still run 17
        val previous by lazy { TestPostgres("postgres:17") }

        fun assumeDocker() {
            Assumptions.assumeTrue(dockerAvailable, "docker is not available, the database tests are skipped")
        }
    }

    private val container by lazy {
        GenericContainer(image)
            .withEnv("POSTGRES_USER", USER)
            .withEnv("POSTGRES_PASSWORD", PASSWORD)
            .withExposedPorts(5432)
            // postgres restarts once after its init scripts, so the second message is the ready one
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
            .also { it.start() }
    }

    fun createDatabase(name: String) {
        psql("postgres", "CREATE DATABASE $name")
    }

    // what the bot does on startup, against the database of the given name
    fun initBot(database: String): R2dbcDatabase =
        Database.init(url(database), USER, PASSWORD, CREATOR_ID, COMMANDS)

    fun psql(database: String, sql: String): String {
        val result = container.execInContainer("psql", "-U", USER, "-d", database, "-v", "ON_ERROR_STOP=1", "-q", "-tAc", sql)
        check(result.exitCode == 0) { "psql failed: ${result.stderr}" }
        return result.stdout.trim()
    }

    fun psqlFile(database: String, resource: String) {
        container.copyFileToContainer(MountableFile.forClasspathResource(resource), "/tmp/$resource")
        val result = container.execInContainer("psql", "-U", USER, "-d", database, "-v", "ON_ERROR_STOP=1", "-q", "-f", "/tmp/$resource")
        check(result.exitCode == 0) { "psql failed: ${result.stderr}" }
    }

    // the schema only, without the comments and settings pg_dump puts around it, to compare two databases
    fun schema(database: String): String {
        val result = container.execInContainer("pg_dump", "-U", USER, "-d", database, "--schema-only", "--no-owner", "--no-privileges")
        check(result.exitCode == 0) { "pg_dump failed: ${result.stderr}" }

        return result.stdout.lines()
            .filterNot { it.isBlank() || it.startsWith("--") || it.startsWith("SET ") || it.startsWith("SELECT pg_catalog") || it.startsWith("\\") }
            .joinToString("\n")
    }

    private fun url(database: String) =
        "r2dbc:pool:postgresql://${container.host}:${container.getMappedPort(5432)}/$database?initialSize=1&maxSize=5"
}
