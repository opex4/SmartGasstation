package com.example.smartgasstation.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RefuelDao {
    @Query("SELECT * FROM refuel_records ORDER BY odometer ASC")
    fun getAll(): LiveData<List<RefuelRecordEntity>>

    @Query("SELECT * FROM refuel_records ORDER BY odometer ASC")
    suspend fun getAllList(): List<RefuelRecordEntity>

    @Insert
    suspend fun insert(record: RefuelRecordEntity)

    @Update
    suspend fun update(record: RefuelRecordEntity)

    @Delete
    suspend fun delete(record: RefuelRecordEntity)

    @Query("DELETE FROM refuel_records")
    suspend fun clear()
}