package com.notai.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.notai.app.data.local.NotaiDatabase
import com.notai.app.data.local.dao.FavoriteDao
import com.notai.app.data.local.dao.HistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notai_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotaiDatabase =
        Room.databaseBuilder(context, NotaiDatabase::class.java, "notai_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideHistoryDao(db: NotaiDatabase): HistoryDao = db.historyDao()

    @Provides
    fun provideFavoriteDao(db: NotaiDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
