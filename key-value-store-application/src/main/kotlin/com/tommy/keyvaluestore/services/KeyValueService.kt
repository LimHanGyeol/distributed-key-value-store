package com.tommy.keyvaluestore.services

import com.tommy.keyvaluestore.dtos.KeyValueGetResponse
import com.tommy.keyvaluestore.dtos.KeyValueSaveRequest
import com.tommy.keyvaluestore.dtos.KeyValueSaveResponse
import com.tommy.store.MemoryStore
import org.springframework.stereotype.Service

@Service
class KeyValueService {

    fun put(keyValueSaveRequest: KeyValueSaveRequest): KeyValueSaveResponse {
        val savedKey = MemoryStore.put(keyValueSaveRequest.key, keyValueSaveRequest.value)
        return KeyValueSaveResponse(savedKey)
    }

    fun get(key: String): KeyValueGetResponse {
        val savedValue = MemoryStore.get(key)
        return KeyValueGetResponse(savedValue)
    }
}
