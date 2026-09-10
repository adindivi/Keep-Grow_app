package com.example.data.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_settings")
data class UserSetting(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "portfolio_stocks")
data class PortfolioStock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val name: String,
    val price: Double,
    val changePct: Double,
    val quantity: Int,
    val isCustom: Boolean = false
)

@Entity(tableName = "watchlist_stocks")
data class WatchlistStock(
    @PrimaryKey val ticker: String,
    val name: String,
    val price: Double,
    val changePct: Double,
    val isPositive: Boolean = true
)

@Entity(tableName = "cached_screener_stocks")
data class CachedScreenerStock(
    @PrimaryKey val ticker: String,
    val name: String,
    val price: Double,
    val changePct: Double,
    val isPositive: Boolean = true,
    val upProb3M: Double,
    val upProb6M: Double,
    val upProb12M: Double,
    val return3M: Double = 0.0,
    val return6M: Double = 0.0,
    val return12M: Double = 0.0,
    val dailyReturnsCsv: String
)

@Dao
interface UserSettingDao {
    @Query("SELECT * FROM user_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): UserSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: UserSetting)

    @Query("SELECT * FROM user_settings")
    fun getAllSettingsFlow(): Flow<List<UserSetting>>
}

@Dao
interface PortfolioStockDao {
    @Query("SELECT * FROM portfolio_stocks ORDER BY id DESC")
    fun getAllStocksFlow(): Flow<List<PortfolioStock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: PortfolioStock)

    @Update
    suspend fun updateStock(stock: PortfolioStock)

    @Query("DELETE FROM portfolio_stocks WHERE id = :id")
    suspend fun deleteStockById(id: Int)

    @Query("DELETE FROM portfolio_stocks WHERE ticker = :ticker")
    suspend fun deleteStockByTicker(ticker: String)
}

@Dao
interface WatchlistStockDao {
    @Query("SELECT * FROM watchlist_stocks ORDER BY name ASC")
    fun getAllWatchlistFlow(): Flow<List<WatchlistStock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(stock: WatchlistStock)

    @Query("DELETE FROM watchlist_stocks WHERE ticker = :ticker")
    suspend fun deleteWatchlistByTicker(ticker: String)

    @Query("SELECT EXISTS(SELECT * FROM watchlist_stocks WHERE ticker = :ticker)")
    suspend fun isWatched(ticker: String): Boolean
}

@Dao
interface CachedScreenerStockDao {
    @Query("SELECT * FROM cached_screener_stocks")
    fun getAllScreenerStocksFlow(): Flow<List<CachedScreenerStock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenerStocks(stocks: List<CachedScreenerStock>)

    @Query("DELETE FROM cached_screener_stocks")
    suspend fun clearScreenerStocks()
}

@Database(entities = [UserSetting::class, PortfolioStock::class, WatchlistStock::class, CachedScreenerStock::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userSettingDao(): UserSettingDao
    abstract fun portfolioStockDao(): PortfolioStockDao
    abstract fun watchlistStockDao(): WatchlistStockDao
    abstract fun cachedScreenerStockDao(): CachedScreenerStockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "keep_grow_database"
                )
                  .fallbackToDestructiveMigration()
                  .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
