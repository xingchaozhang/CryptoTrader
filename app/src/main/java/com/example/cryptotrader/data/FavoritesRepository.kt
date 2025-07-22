package com.example.cryptotrader.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 持久化收藏仓库（Room）。
 * 需在 Application#onCreate 调用 init(context)。
 */
object FavoritesRepository {

    /** 热流：UI 直接 collectAsState() 即可 */
    private val _symbols = MutableStateFlow<Set<String>>(emptySet())
    val symbols: StateFlow<Set<String>> = _symbols.asStateFlow()

    private lateinit var dao: FavoriteDao
    private val scope = CoroutineScope(Dispatchers.IO)

    /** 在 Application 启动时初始化 */
    fun init(context: Context) {
        dao = AppDatabase.get(context).favoriteDao()
        /** 数据库 → StateFlow 同步 */
        scope.launch {
            dao.observeAll()
                .map { it.toSet() }
                .collect { _symbols.value = it }
        }
    }

    /** 覆盖保存 */
    fun replaceAll(newSet: Set<String>) {
        scope.launch { dao.replaceAll(newSet) }
    }

    /** 切换单个 symbol */
    fun toggle(symbol: String) {
        scope.launch {
            if (symbol in _symbols.value) dao.delete(FavoritePair(symbol))
            else dao.insertMany(listOf(FavoritePair(symbol)))
        }
    }
}
