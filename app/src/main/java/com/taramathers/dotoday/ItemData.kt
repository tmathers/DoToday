package com.taramathers.dotoday

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "itemData")
data class ItemData(@PrimaryKey(autoGenerate = true) var id: Long? = 0,
                    @ColumnInfo(name = "text") var text: String,
                    @ColumnInfo(name = "checked") var checked: Boolean

){
    constructor():this(null,"",false)
}
