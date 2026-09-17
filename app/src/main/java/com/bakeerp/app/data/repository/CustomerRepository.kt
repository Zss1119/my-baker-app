package com.bakeerp.app.data.repository

import com.bakeerp.app.data.dao.CustomerDao
import com.bakeerp.app.data.database.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * 客户（邻居）仓储。
 */
class CustomerRepository(
    private val customerDao: CustomerDao
) {

    fun observeAll(): Flow<List<CustomerEntity>> = customerDao.observeAll()

    suspend fun upsert(entity: CustomerEntity): Long = customerDao.upsert(entity)

    suspend fun findByName(name: String): CustomerEntity? {
        if (name.isBlank()) return null
        return customerDao.findByName(name)
    }
}