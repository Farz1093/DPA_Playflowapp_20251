package com.esan.payflowapp.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.esan.payflowapp.R
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.NumberFormat
import java.util.Locale

object AdminNotificationsManager {

    private const val NORMAL_CH = "DEPOSITS_NORMAL"
    private const val URGENT_CH = "DEPOSITS_URGENT"
    private const val URGENT_LIMIT = 3
    private var lastPendingCount = 0

    private val fs = FirebaseFirestore.getInstance()
    private var registration: ListenerRegistration? = null


    fun startListening(context: Context) {
        if (registration != null) return

        // Es crucial crear los canales antes de intentar notificar
        createNotificationChannels(context)

        registration = fs.collection("deposit_data")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snaps, err ->
                if (err != null || snaps == null) return@addSnapshotListener

                val docs = snaps.documents
                val count = docs.size

                // Esta lógica se mantiene: notificar ruidosamente por cada depósito nuevo.
                for (change in snaps.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        notifyNewDeposit(context, change.document)
                    }
                }

                // Esta lógica también se mantiene: actualizar el resumen si el contador cambia.
                if (count > 0 && count != lastPendingCount) {
                    notifyPending(context, docs, count)
                }
                lastPendingCount = count
            }
    }

    fun stopListening() {
        registration?.remove()
        registration = null
    }

    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val normalChannel = NotificationChannel(
                NORMAL_CH, "Depósitos Nuevos", NotificationManager.IMPORTANCE_DEFAULT
            )
            val urgentChannel = NotificationChannel(
                URGENT_CH, "Depósitos Urgentes", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(normalChannel)
            manager.createNotificationChannel(urgentChannel)
        }
    }

    // ESTA NOTIFICACIÓN ES PARA UN EVENTO ÚNICO Y DEBE SONAR
    private fun notifyNewDeposit(context: Context, doc: DocumentSnapshot) {
        val amount = doc.getDouble("amount") ?: 0.0
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        val formattedAmount = formatter.format(amount)

        val notif = NotificationCompat.Builder(context, NORMAL_CH)
            .setSmallIcon(R.drawable.ic_launcher_round) // Reemplaza con tu ícono
            .setContentTitle("Nuevo depósito por validar")
            .setContentText("Has recibido un nuevo depósito de $formattedAmount.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Usamos un ID dinámico para que cada notificación sea nueva y suene.
        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notif)
    }

    // ESTA NOTIFICACIÓN ES UN RESUMEN CONTINUO Y SOLO DEBE SONAR LA PRIMERA VEZ
    private fun notifyPending(
        context: Context,
        docs: List<DocumentSnapshot>,
        count: Int
    ) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        val inboxStyle = NotificationCompat.InboxStyle().also { style ->
            docs.take(5).forEach { doc ->
                val amount = doc.getDouble("amount") ?: 0.0
                style.addLine("• Depósito de ${formatter.format(amount)}")
            }
            style.setSummaryText("$count depósitos en total")
        }

        val channel = if (count >= URGENT_LIMIT) URGENT_CH else NORMAL_CH
        val priority = if (count >= URGENT_LIMIT) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT

        val notif = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_round)
            .setContentTitle("$count Depósitos Pendientes")
            .setContentText("Tienes depósitos que requieren tu atención.")
            .setStyle(inboxStyle)
            .setPriority(priority)
            // <<< ¡ESTA ES LA LÍNEA CLAVE! >>>
            // Solo alertará (sonido/vibración) la primera vez que se muestre esta notificación.
            // Las actualizaciones posteriores serán silenciosas.
            .setOnlyAlertOnce(true)
            .setAutoCancel(true) // Se cierra si el usuario la toca
            .build()

        // Usamos un ID estático (1000) para que esta notificación se actualice a sí misma.
        NotificationManagerCompat.from(context)
            .notify(1000, notif)
    }
}