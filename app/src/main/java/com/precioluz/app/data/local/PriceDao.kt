package com.precioluz.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PriceDao {

    @Query("SELECT * FROM prices WHERE date = :date ORDER BY hour ASC")
    suspend fun getPricesForDate(date: String): List<PriceEntity>

    @Query("SELECT COUNT(*) FROM prices WHERE date = :date")
    suspend fun countPricesForDate(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<PriceEntity>)

    @Query("DELETE FROM prices WHERE date = :date")
    suspend fun deletePricesForDate(date: String)
}
