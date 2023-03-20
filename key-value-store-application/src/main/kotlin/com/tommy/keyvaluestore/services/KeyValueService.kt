package com.tommy.keyvaluestore.services

import com.tommy.keyvaluestore.dtos.KeyValueRequest
import com.tommy.keyvaluestore.dtos.KeyValueResponse
import com.tommy.store.MemoryStore
import org.springframework.stereotype.Service

@Service
class KeyValueService {

    fun put(keyValueRequest: KeyValueRequest): KeyValueResponse {
        val savedKey = MemoryStore.put(keyValueRequest.key, keyValueRequest.value)
        return KeyValueResponse(savedKey)
    }
}
