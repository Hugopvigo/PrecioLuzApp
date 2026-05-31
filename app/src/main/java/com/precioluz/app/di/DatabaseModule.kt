package com.precioluz.app.di

import android.content.Context
import androidx.room.Room
import com.precioluz.app.data.local.PrecioLuzDatabase
import com.precioluz.app.data.local.PriceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PrecioLuzDatabase {
        return Room.databaseBuilder(
            context,
            PrecioLuzDatabase::class.java,
            "precioluz.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun providePriceDao(db: PrecioLuzDatabase): PriceDao = db.priceDao()
}
