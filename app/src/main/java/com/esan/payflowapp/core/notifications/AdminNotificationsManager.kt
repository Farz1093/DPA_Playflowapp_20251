package com.esan.payflowapp.core.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.esan.payflowapp.R
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.core.app.NotificationCompat.InboxStyle

object AdminNotificationsManager {

    private const val NORMAL_CH     = "DEPOSITS_NORMAL"
    private const val URGENT_CH     = "DEPOSITS_URGENT"
    private const val URGENT_LIMIT  = 3
    private var lastPendingCount = 0

    private val fs = FirebaseFirestore.getInstance()
    private var registration: ListenerRegistration? = null


    fun startListening(context: Context) {
        if (registration != null) return
        registration = fs.collection("transactions")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snaps, err ->
                if (err != null || snaps == null) return@addSnapshotListener

                val docs = snaps.documents
                val count = docs.size

                // Detecta depósitos NUEVOS
                for (change in snaps.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        notifyNewDeposit(context, change.document)
                    }
                }

                // Actualiza resumen de pendientes si la cantidad cambia
                if (count > 0 && count != lastPendingCount) {
                    notifyPending(context, docs, count)
                }
                lastPendingCount = count
            }
    }

    /** Llamar al hacer logout del admin */
    fun stopListening() {
        registration?.remove()
        registration = null
    }

    private fun notifyNewDeposit(context: Context, doc: DocumentSnapshot) {
        val amount = doc.getLong("amount") ?: 0L
        val type = doc.getString("type") ?: "DEPÓSITO"

        val notif = NotificationCompat.Builder(context, NORMAL_CH)
            .setSmallIcon(R.drawable.ic_launcher_round)
            .setContentTitle("Nuevo depósito pendiente")
            .setContentText("S/ $amount - ($type)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notif)
    }

    private fun notifyPending(
        context: Context,
        docs: List<DocumentSnapshot>,
        count: Int
    ) {

        val inboxStyle = NotificationCompat.InboxStyle().also  { style ->
            docs.take(5).forEach { doc ->
                val amt = (doc.getLong("amount") ?: 0L)
                val type = doc.getString("type") ?: "DESCONOCIDO"
                style.addLine("• S/ $amt  - ($type)")
            }
            style.setSummaryText("$count depósitos pendientes")
        }


        val channel = if (count > URGENT_LIMIT) URGENT_CH else NORMAL_CH

        // 3) Construye y dispara la notificación con ese style
        val notif = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_round)
            .setContentTitle("Depósitos pendientes")
            .setContentText("Tienes $count depósitos por validar")
            .setStyle(inboxStyle)      // aquí le pasas el InboxStyle
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(1000, notif)
    }
}