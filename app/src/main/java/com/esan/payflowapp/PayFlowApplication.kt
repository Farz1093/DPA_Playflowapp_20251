package com.esan.payflowapp

import android.app.Application
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.esan.payflowapp.data.local.database.AppDatabase
import com.esan.payflowapp.data.local.sync.SyncWorker
import java.util.concurrent.TimeUnit

class PayFlowApplication : Application() {

    companion object {
        // Instancia singleton de la BD accesible desde cualquier parte
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        // Aquí inicializamos Room con el esquema que definimos
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "payflow.db"
        )
            .fallbackToDestructiveMigration() // Si cambias de version, recrea la BD
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "sync_work",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
    }
}