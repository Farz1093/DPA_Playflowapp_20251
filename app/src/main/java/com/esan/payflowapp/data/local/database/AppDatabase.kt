package com.esan.payflowapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.esan.payflowapp.data.local.dao.*
import com.esan.payflowapp.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        NotificationEntity::class,
        SyncMetadata::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun syncMetadataDao(): SyncMetadataDao
}