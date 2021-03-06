package com.taramathers.dotoday

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import android.os.Parcel
import android.os.Parcelable


@Database(entities = arrayOf(ItemData::class), version = 1)
abstract class ItemDatabase() : RoomDatabase() {


    abstract fun itemDataDao(): ItemDataDAO

    companion object {
        private var INSTANCE: ItemDatabase? = null

        fun getInstance(context: Context): ItemDatabase? {
            if (INSTANCE == null) {
                synchronized(ItemDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ItemDatabase::class.java, "items.db")
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}