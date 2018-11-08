package com.taramathers.dotoday

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface ItemDataDAO {

    @Query("SELECT * from itemData")
    fun getAll(): List<ItemData>

    @Insert(onConflict = REPLACE)
    fun insert(itemData: ItemData) : Long

    @Query("DELETE from itemData")
    fun deleteAll()
}