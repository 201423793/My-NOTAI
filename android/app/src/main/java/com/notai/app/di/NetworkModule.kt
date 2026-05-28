package com.notai.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // OpenCV is initialized in NotaiApplication
    // No network dependencies needed - all processing is local
}
