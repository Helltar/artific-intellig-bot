package com.helltar.aibot.database.dao

import com.helltar.aibot.database.Database.dbTransaction
import com.helltar.aibot.database.models.SlowmodeStatusData
import com.helltar.aibot.database.tables.SlowmodeUsageTable
import com.helltar.aibot.database.utcNow
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.r2dbc.insertIgnore
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.update

class SlowmodeDao {

    suspend fun registerUser(userId: Long): Boolean = dbTransaction {
        SlowmodeUsageTable
            .insertIgnore {
                it[this.userId] = userId
            }
            .insertedCount > 0
    }

    suspend fun incrementUsageCount(userId: Long): Boolean = dbTransaction {
        SlowmodeUsageTable
            .update({ SlowmodeUsageTable.userId eq userId }) {
                it[usageCount] = usageCount + 1
                it[lastUsedAt] = utcNow()
            } > 0
    }

    suspend fun resetUsageCount(userId: Long): Boolean = dbTransaction {
        SlowmodeUsageTable
            .update({ SlowmodeUsageTable.userId eq userId }) {
                it[usageCount] = 1
                it[lastUsedAt] = utcNow()
            } > 0
    }

    suspend fun slowmodeStatus(userId: Long): SlowmodeStatusData? = dbTransaction {
        SlowmodeUsageTable
            .select(SlowmodeUsageTable.usageCount, SlowmodeUsageTable.lastUsedAt)
            .where { SlowmodeUsageTable.userId eq userId }
            .singleOrNull()
            ?.let {
                SlowmodeStatusData(
                    it[SlowmodeUsageTable.usageCount],
                    it[SlowmodeUsageTable.lastUsedAt].toInstant()
                )
            }
    }
}

val slowmodeDao = SlowmodeDao()
