// OrderDao.kt
package com.example.cryptotrader.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getOrders(): Flow<List<OrderEntity>>

    /**
     * 插入一条订单记录，返回生成的主键 ID。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(order: OrderEntity): Long
}