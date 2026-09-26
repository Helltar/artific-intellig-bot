package com.helltar.aibot.database

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * An in-memory copy of a small table that only this bot writes: the admins, the bans, the allowed chats.
 *
 * Every command checks those, so the checks are answered from memory instead of a query each. The set
 * is loaded on first use and then kept in sync by [add] and [remove], which the DAO calls after its
 * write succeeded. It relies on a single bot per database: a row changed by hand needs a restart.
 */
class CachedSet<T : Any>(private val load: suspend () -> Collection<T>) {

    private val mutex = Mutex()

    @Volatile
    private var values: MutableSet<T>? = null

    suspend fun contains(value: T): Boolean =
        values().contains(value)

    suspend fun add(value: T) {
        values().add(value)
    }

    suspend fun remove(value: T) {
        values().remove(value)
    }

    private suspend fun values(): MutableSet<T> =
        values ?: mutex.withLock {
            values ?: ConcurrentHashMap.newKeySet<T>().apply { addAll(load()) }.also { values = it }
        }
}
