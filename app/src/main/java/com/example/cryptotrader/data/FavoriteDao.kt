package com.example.cryptotrader.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    /* ---------- 查询 ---------- */

    /** 观察全部收藏：返回 Flow<String> 列表 */
    @Query("SELECT symbol FROM favorite_pairs")
    fun observeAll(): Flow<List<String>>

    /* ---------- 写：Insert / Delete / Clear ---------- */

    /** 批量插入；返回每条行 id */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMany(list: List<FavoritePair>
    ): List<Long>

    /** 删除单条；返回删除行数 */
    @Delete
    suspend fun delete(item: FavoritePair): Int

    /** 清空表；返回删除行数 (必须是 Int) */
    @Query("DELETE FROM favorite_pairs")
    suspend fun clear(): Int

    /* ---------- 事务：覆盖保存 ---------- */

    @Transaction
    suspend fun replaceAll(symbols: Set<String>) {
        clear()
        if (symbols.isNotEmpty()) {
            insertMany(symbols.map { FavoritePair(it) })
        }
    }
}
