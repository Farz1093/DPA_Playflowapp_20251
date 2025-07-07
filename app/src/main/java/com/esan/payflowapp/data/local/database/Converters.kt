package com.esan.payflowapp.data.local.database

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }
    @TypeConverter fun dateToTimestamp(date: Date?): Long? = date?.time
}