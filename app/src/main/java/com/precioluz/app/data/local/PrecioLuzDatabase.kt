package com.precioluz.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PriceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PrecioLuzDatabase : RoomDatabase() {
    abstract fun priceDao(): PriceDao
}
