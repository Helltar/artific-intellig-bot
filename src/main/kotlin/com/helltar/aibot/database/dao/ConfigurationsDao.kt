package com.helltar.aibot.database.dao

import com.helltar.aibot.database.Database.dbTransaction
import com.helltar.aibot.database.tables.ConfigurationsTable
import com.helltar.aibot.utils.DateTimeUtils.utcNow
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.insertIgnore
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.concurrent.ConcurrentHashMap

class ConfigurationsDao {

    private companion object {
        const val KEY_CHAT_MODEL = "chat_model"
        const val KEY_VISION_MODEL = "vision_model"
        const val KEY_IMAGE_GEN_MODEL = "image_gen_model"

        const val KEY_SLOWMODE_MAX_USAGE_COUNT = "global_slowmode_max_usage_count"
        const val KEY_LOADING_GIF_FILE_ID = "loading_gif_file_id"

        const val NULL_MARKER = "<NULL>"

        const val DEFAULT_CHAT_MODEL = "gpt-5.2"
        const val DEFAULT_VISION_MODEL = "gpt-5.2"
        const val DEFAULT_IMAGE_GEN_MODEL = "gpt-image-1.5"
    }

    private val cache = ConcurrentHashMap<String, String>()

    suspend fun loadingGifFileId(): String? =
        getCached(KEY_LOADING_GIF_FILE_ID)

    suspend fun updateLoadingGifFileId(fileId: String): Boolean =
        setAndCache(KEY_LOADING_GIF_FILE_ID, fileId)

    suspend fun slowmodeMaxUsageCount(): Int =
        getCached(KEY_SLOWMODE_MAX_USAGE_COUNT)?.toIntOrNull() ?: 10

    suspend fun updateSlowmodeMaxUsageCount(newMax: Int): Boolean =
        setAndCache(KEY_SLOWMODE_MAX_USAGE_COUNT, newMax)

    suspend fun chatModel(): String =
        getCached(KEY_CHAT_MODEL) ?: DEFAULT_CHAT_MODEL

    suspend fun updateChatModel(model: String): Boolean =
        setAndCache(KEY_CHAT_MODEL, model)

    suspend fun visionModel(): String =
        getCached(KEY_VISION_MODEL) ?: DEFAULT_VISION_MODEL

    suspend fun updateVisionModel(model: String): Boolean =
        setAndCache(KEY_VISION_MODEL, model)

    suspend fun imageGenModel(): String =
        getCached(KEY_IMAGE_GEN_MODEL) ?: DEFAULT_IMAGE_GEN_MODEL

    suspend fun updateImageGenModel(model: String): Boolean =
        setAndCache(KEY_IMAGE_GEN_MODEL, model)

    private suspend fun getCached(key: String): String? =
        when (val cached = cache[key]) {
            null -> {
                val value = getConfigValue(key)
                cache[key] = value ?: NULL_MARKER
                value
            }

            NULL_MARKER -> null
            else -> cached
        }

    private suspend fun setAndCache(key: String, value: Any): Boolean =
        setConfiguration(key, value.toString()).also { if (it) cache[key] = value.toString() }

    private suspend fun getConfigValue(key: String): String? = dbTransaction {
        ConfigurationsTable
            .select(ConfigurationsTable.value)
            .where { ConfigurationsTable.key eq key }
            .singleOrNull()
            ?.getOrNull(ConfigurationsTable.value)
    }

    private suspend fun setConfiguration(key: String, value: String): Boolean = dbTransaction {
        (ConfigurationsTable
            .update({ ConfigurationsTable.key eq key }) {
                it[this.value] = value
                it[this.updatedAt] = utcNow()
            }.takeIf { it > 0 }
            ?: ConfigurationsTable
                .insertIgnore {
                    it[this.key] = key
                    it[this.value] = value
                    it[this.createdAt] = utcNow()
                }.insertedCount) > 0
    }
}

val configurationsDao = ConfigurationsDao()
