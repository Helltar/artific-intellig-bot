package com.helltar.aibot.database.dao

import com.helltar.aibot.database.CachedSet
import com.helltar.aibot.database.Database.dbTransaction
import com.helltar.aibot.database.fit
import com.helltar.aibot.database.models.SudoersData
import com.helltar.aibot.database.tables.AdminsTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertIgnore
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll

class SudoersDao {

    // checked on every command
    private val adminIds = CachedSet { dbTransaction { AdminsTable.select(AdminsTable.userId).map { it[AdminsTable.userId] }.toList() } }

    // the row exists after an insert or an ignored duplicate alike, so the cache follows either way
    suspend fun add(userId: Long, username: String?): Boolean =
        dbTransaction {
            AdminsTable
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
            AdminsTable
                .deleteWhere { this.userId eq userId } > 0
        }.also { adminIds.remove(userId) }

    suspend fun list(): List<SudoersData> = dbTransaction {
        AdminsTable
            .selectAll()
            .map {
                SudoersData(
                    it[AdminsTable.userId],
                    it[AdminsTable.username],
                    it[AdminsTable.createdAt].toInstant()
                )
            }.toList()
    }
}

val sudoersDao = SudoersDao()
