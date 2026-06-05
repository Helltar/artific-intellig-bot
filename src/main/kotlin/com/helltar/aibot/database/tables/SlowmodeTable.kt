package com.helltar.aibot.database.tables

import com.helltar.aibot.utils.DateTimeUtils.instantNow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object SlowmodeTable : Table() {

    val userId = long("user_id")
    val usageCount = integer("usage_count").default(1)
    val updatedAt = timestamp("updated_at").clientDefault { instantNow() }
    val createdAt = timestamp("created_at").clientDefault { instantNow() }

    override val primaryKey = PrimaryKey(userId)
}
