package com.example.cryptotrader.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist")
    fun getAll(): Flow<List<WatchlistEntity>>

    /**
     * 插入一条自选记录，返回新行的 ID。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pair: WatchlistEntity): Long

    /**
     * 删除一条自选记录，返回删除的行数。
     */
    @Delete
    fun delete(pair: WatchlistEntity): Int
}