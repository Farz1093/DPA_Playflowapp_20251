package com.esan.payflowapp.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.esan.payflowapp.R // Asegúrate de que apunte a tu R
import com.esan.payflowapp.core.firebase.FirebaseAuthManager // Tu clase de auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.NumberFormat
import java.util.Locale

object UserNotificationsManager {

    private const val VALIDATION_CHANNEL_ID = "DEPOSIT_VALIDATION_CHANNEL"
    private var listenerRegistration: ListenerRegistration? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val notifiedDepositIds = mutableSetOf<String>()
    // Debes llamar a esta función cuando el usuario inicie sesión
    fun startListeningForDepositUpdates(context: Context) {
        if (listenerRegistration != null) return
        val uid = FirebaseAuthManager.getCurrentUserUid() ?: return

        createNotificationChannel(context)

        listenerRegistration = firestore.collection("deposit_data")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                // <<< LÓGICA CORREGIDA Y MEJORADA >>>
                for (change in snapshot.documentChanges) {
                    // Solo nos interesa cuando un documento fue MODIFICADO
                    if (change.type == DocumentChange.Type.MODIFIED) {
                        val modifiedDoc = change.document
                        val newStatus = modifiedDoc.getString("status")
                        val docId = modifiedDoc.id

                        // La condición clave es si el estado es final Y no hemos notificado antes sobre este depósito.
                        if ((newStatus == "COMPLETED" || newStatus == "FAILED") && !notifiedDepositIds.contains(docId)) {
                            showValidationNotification(context, modifiedDoc)
                            // Agregamos el ID a la lista para no volver a notificar
                            notifiedDepositIds.add(docId)
                        }
                    } else if (change.type == DocumentChange.Type.REMOVED) {
                        // Si un depósito se elimina, lo quitamos de nuestra lista de notificados
                        notifiedDepositIds.remove(change.document.id)
                    }
                }
            }
    }

    // Al cerrar sesión, también limpiamos la lista de notificaciones mostradas.
    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
        notifiedDepositIds.clear() // Limpiar el set
    }

    private fun showValidationNotification(context: Context, document: com.google.firebase.firestore.DocumentSnapshot) {
        val amount = document.getDouble("amount") ?: 0.0
        val status = document.getString("status")
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        val formattedAmount = formatter.format(amount)

        val title: String
        val text: String
        val icon: Int

        when (status) {
            "COMPLETED" -> {
                title = "¡Depósito Aprobado!"
                text = "Tu depósito de $formattedAmount ha sido procesado."
                icon = R.drawable.ic_launcher_round
            }
            "FAILED" -> {
                title = "Depósito Rechazado"
                text = "Hubo un problema con tu depósito de $formattedAmount."
                icon = R.drawable.ic_launcher_round
            }
            else -> return // No notificar si el estado es otro
        }

        val notification = NotificationCompat.Builder(context, VALIDATION_CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Usamos el hash del ID del documento para que cada notificación sea única
        NotificationManagerCompat.from(context).notify(document.id.hashCode(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Estado de Depósitos"
            val descriptionText = "Notificaciones sobre la validación de tus depósitos."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(VALIDATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}