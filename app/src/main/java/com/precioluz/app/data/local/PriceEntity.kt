package com.precioluz.app.data.local

import androidx.room.Entity

@Entity(tableName = "prices", primaryKeys = ["date", "hour"])
data class PriceEntity(
    val date: String,
    val hour: Int,
    val priceKwh: Double,
    val priceMwh: Double,
)
