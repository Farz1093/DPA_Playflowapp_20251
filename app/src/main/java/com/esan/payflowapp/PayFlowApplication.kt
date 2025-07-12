package com.esan.payflowapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build


class PayFlowApplication : Application() {

    companion object {

    }


    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            // Canal normal (pendientes ≤ LIMITE)
            nm.createNotificationChannel(
                NotificationChannel(
                    "DEPOSITS_NORMAL",
                    "Depósitos pendientes",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones de depósitos por validar"
                }
            )

            // Canal urgente (pendientes > LIMITE)
            nm.createNotificationChannel(
                NotificationChannel(
                    "DEPOSITS_URGENT",
                    "Depósitos URGENTES",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerta de alta carga de depósitos pendientes"
                    // sonido personalizado opcional
                    val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    setSound(sound, AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    )
                }
            )
        }
    }

    override fun onCreate() {
        super.onCreate()


        createNotificationChannels()

    }
}