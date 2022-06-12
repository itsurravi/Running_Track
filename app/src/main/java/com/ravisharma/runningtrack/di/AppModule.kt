package com.ravisharma.runningtrack.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.ravisharma.runningtrack.db.RunningDatabase
import com.ravisharma.runningtrack.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.ravisharma.runningtrack.other.Constants.KEY_NAME
import com.ravisharma.runningtrack.other.Constants.KEY_WEIGHT
import com.ravisharma.runningtrack.other.Constants.RUNNING_DATABASE_NAME
import com.ravisharma.runningtrack.other.Constants.SHARE_PREFRENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARE_PREFRENCES_NAME, MODE_PRIVATE)


    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "") ?: ""


    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 70f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}