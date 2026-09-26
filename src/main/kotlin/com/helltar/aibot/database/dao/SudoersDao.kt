package com.helltar.aibot.database.dao

import com.helltar.aibot.database.CachedSet
import com.helltar.aibot.database.Database.dbTransaction
import com.helltar.aibot.database.fit
import com.helltar.aibot.database.models.SudoersData
import com.helltar.aibot.database.tables.SudoersTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertIgnore
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll

class SudoersDao {

    // checked on every command
    private val adminIds = CachedSet { dbTransaction { SudoersTable.select(SudoersTable.userId).map { it[SudoersTable.userId] }.toList() } }

    // the row exists after an insert or an ignored duplicate alike, so the cache follows either way
    suspend fun add(userId: Long, username: String?): Boolean =
        dbTransaction {
            SudoersTable
                .insertIgnore {
                    it[this.userId] = userId
                    it[this.username] = username?.let(this.username::fit)
                }
                .insertedCount > 0
        }.also { adminIds.add(userId) }

    suspend fun isAdmin(userId: Long): Boolean =
        adminIds.contains(userId)

    suspend fun remove(userId: Long): Boolean =
        dbTransaction {
            SudoersTable
                .deleteWhere { this.userId eq userId } > 0
        }.also { adminIds.remove(userId) }

    suspend fun list(): List<SudoersData> = dbTransaction {
        SudoersTable
            .selectAll()
            .map {
                SudoersData(
                    it[SudoersTable.userId],
                    it[SudoersTable.username],
                    it[SudoersTable.createdAt]
                )
            }.toList()
    }
}

val sudoersDao = SudoersDao()
