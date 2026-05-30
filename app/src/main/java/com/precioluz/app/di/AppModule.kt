package com.precioluz.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Módulo vacío — el cliente HTTP y JSON son provistos por NetworkModule
@Module
@InstallIn(SingletonComponent::class)
object AppModule
