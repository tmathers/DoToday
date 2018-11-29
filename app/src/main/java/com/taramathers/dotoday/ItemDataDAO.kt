package com.taramathers.dotoday

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItemDataDAO {

    @Query("SELECT * from itemData")
    fun getAll(): List<ItemData>

    @Insert(onConflict = REPLACE)
    fun insert(itemData: ItemData) : Long

    @Query("DELETE from itemData")
    fun deleteAll()

    @Update
    fun update(item : ItemData)
}