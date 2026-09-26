package com.helltar.aibot.database.tables

import com.helltar.aibot.database.utcNow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object SlowmodeUsageTable : Table("slowmode_usage") {

    val userId = long("user_id")
    val usageCount = integer("usage_count").default(1)
    val lastUsedAt = timestampWithTimeZone("last_used_at").clientDefault { utcNow() }
    val createdAt = timestampWithTimeZone("created_at").clientDefault { utcNow() }

    override val primaryKey = PrimaryKey(userId)
}
