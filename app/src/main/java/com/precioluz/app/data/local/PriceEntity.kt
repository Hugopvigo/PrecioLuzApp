package com.precioluz.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class PriceEntity(
    val date: String,
    @PrimaryKey val hour: Int,
    val priceKwh: Double,
    val priceMwh: Double
)
