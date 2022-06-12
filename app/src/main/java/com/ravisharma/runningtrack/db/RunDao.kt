package com.ravisharma.runningtrack.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM runningTable ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM runningTable ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM runningTable ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM runningTable ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM runningTable ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMillis) FROM runningTable")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) FROM runningTable")
    fun getTotalCaloriesBurned(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) FROM runningTable")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKMH) FROM runningTable")
    fun getTotalAvgSpeed(): LiveData<Float>
}