package com.taramathers.dotoday

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity(tableName = "itemData")
data class ItemData(@PrimaryKey(autoGenerate = true) var id: Long?,
                    @ColumnInfo(name = "text") var text: String,
                    @ColumnInfo(name = "checked") var checked: Boolean

){
    constructor():this(null,"",false)
}
