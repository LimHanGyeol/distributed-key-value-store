package com.tommy.store

import java.util.concurrent.ConcurrentHashMap

object MemoryStore {

    private val store: ConcurrentHashMap<String, Any> = ConcurrentHashMap()

    fun put(key: String, value: Any): String {
        store[key] = value
        return key
    }

    fun get(key: String): Any? {
        if (store.containsKey(key)) {
            return store[key]
        }
        return null
    }

    fun clear() {
        store.clear()
    }
}
