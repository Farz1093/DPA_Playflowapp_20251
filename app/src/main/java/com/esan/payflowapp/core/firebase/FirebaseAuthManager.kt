package com.esan.payflowapp.core.firebase

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.esan.payflowapp.core.firebase.model.Transaction
import com.esan.payflowapp.core.firebase.model.UserData
import com.esan.payflowapp.core.pref.SharedPreferencesManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

object FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    //Login
    suspend fun loginUser(email: String, password: String): Result<Unit> { return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(): UserData {
        var uid = auth.currentUser?.uid.orEmpty()
        var snapshot = db.collection("user_data").document(uid).get().await()
        val name = snapshot.getString("name").orEmpty()
        val accountNumber = snapshot.getString("account_number").orEmpty()
        val isAdmin = snapshot.getBoolean("is_admin") ?: false
        val balance = snapshot.getDouble("balance") ?: 0.0
        return UserData(name, accountNumber, isAdmin, balance)
    }

    suspend fun transferMoney(
        accountNumberDestiny: String,
        amount: Double
    ): Boolean {
        val fromUID = getCurrentUserUid()
        if (amount <= 0) throw Exception("El monto debe ser mayor a 0.")

        val fromRef = db.collection("user_data").document(fromUID)
        val fromSnapshot = fromRef.get().await()
//        if (!fromSnapshot.exists()) throw Exception("Usuario origen no encontrado.")

        val isAdminFrom = fromSnapshot.getBoolean("is_admin") ?: false
        if (isAdminFrom) throw Exception("Admins no pueden transferir")

        val destinyQuery = db.collection("user_data")
            .whereEqualTo("account_number", accountNumberDestiny)
            .limit(1)
            .get()
            .await()

        if (destinyQuery.isEmpty) throw Exception("No existe usuario con esa cuenta.")

        val toSnapshot = destinyQuery.documents[0]
        val toUID = toSnapshot.id

        if (fromUID == toUID) throw Exception("No puedes transferirte a ti mismo.")

        val isAdminTo = toSnapshot.getBoolean("is_admin") ?: false
        if (isAdminTo) throw Exception("No puedes transferir a un usuario administrador.")

        val fromName = fromSnapshot.getString("name") ?: ""
        val toName = toSnapshot.getString("name") ?: ""

        val toRef = db.collection("user_data").document(toUID)

        db.runTransaction { transaction ->
            val liveFromSnap = transaction.get(fromRef)
            val liveToSnap = transaction.get(toRef)

            val fromBalance = liveFromSnap.getDouble("balance") ?: 0.0
            val toBalance = liveToSnap.getDouble("balance") ?: 0.0

            if (fromBalance < amount) throw Exception("Saldo insuficiente")

            transaction.update(fromRef, "balance", fromBalance - amount)
            transaction.update(toRef, "balance", toBalance + amount)

            val trxRef = db.collection("trx_data").document()
            transaction.set(
                trxRef, mapOf(
                    "from_uid" to fromUID,
                    "from_name" to fromName,
                    "to_uid" to toUID,
                    "to_name" to toName,
                    "amount" to amount,
                    "date" to Timestamp.now()
                )
            )
        }.await()

        return true
    }

    suspend fun createDeposit(
        amount: Double
    ): Boolean {
        if (amount <= 0) throw Exception("El monto debe ser mayor a 0.")

        db.runTransaction { transaction ->
            val depositReference = db.collection("deposit_data").document()
            transaction.set(
                depositReference, mapOf(
                    "uid" to getCurrentUserUid(),
                    "amount" to amount,
                    "date" to Timestamp.now(),
                    "is_validated" to false,
                    "is_approved" to false,
                    // NUEVOS CAMPOS
                    "type" to "DEPOSIT",
                    "status"       to "PENDING",
                    "createdAt"    to System.currentTimeMillis(), // o usa tu propio valor
                    "currency"     to "PEN",
                    "updatedAt"    to null,
                    "validatedBy"  to null,
                    "validatedAt"  to null
                )
            )
        }.await()

        return true
    }

    suspend fun listUsers(): List<Pair<String, String>> {
        val users = db.collection("user_data")
            .whereEqualTo("is_admin", false)
            .get()
            .await()

        return users.documents
            .filter { it.id != getCurrentUserUid() }
            .map { it.id to (it.getString("name") ?: "Sin nombre") }
    }

    suspend fun getBalance(): Double {
        val doc = db.collection("user_data").document(getCurrentUserUid()).get().await()
        return doc.getDouble("balance") ?: 0.0
    }

    suspend fun getLastTrx(): List<Transaction> {
        val depositQuery = db.collection("deposit_data")
            .whereEqualTo("uid", getCurrentUserUid())
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()
        val trxQueryFrom = db.collection("trx_data")
            .whereEqualTo("from_uid", getCurrentUserUid())
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()
        val trxQueryTo = db.collection("trx_data")
            .whereEqualTo("to_uid", getCurrentUserUid())
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()

        val depositRows = depositQuery.documents.map {
            Transaction(
                type = "deposit",
                amount = it.getDouble("amount") ?: 0.0,
                date = it.getTimestamp("date")?.toDate()?.time ?: 0L,
                isValidated = it.getBoolean("is_validated") ?: false,
                isApproved = it.getBoolean("is_approved") ?: false
            )
        }
        val transferFromRows = trxQueryFrom.documents.map {
            Transaction(
                type = "transfer_sent",
                amount = it.getDouble("amount") ?: 0.0,
                date = it.getTimestamp("date")?.toDate()?.time ?: 0L,
                isValidated = false,
                isApproved = false
            )
        }
        val transferToRows = trxQueryTo.documents.map {
            Transaction(
                type = "transfer_received",
                amount = it.getDouble("amount") ?: 0.0,
                date = it.getTimestamp("date")?.toDate()?.time ?: 0L,
                isValidated = false,
                isApproved = false
            )
        }

        Log.e("WAA", "depositRows=${depositRows.size}")
        Log.e("WAA", "transferFromRows=${transferFromRows.size}")
        Log.e("WAA", "transferToRows=${transferToRows.size}")

        return (depositRows + transferFromRows + transferToRows)
            .sortedByDescending { it.date }
            .take(5)
    }

    suspend fun getTransactionHistory(
        from: Long,
        to: Long
    ): List<Transaction> {
        val uid = getCurrentUserUid()

        Log.e("WAA", "getTransactionHistory-from=$from")
        Log.e("WAA", "getTransactionHistory-to=$to")

        val fromTimeStamp = Timestamp(Date(from))
        val toTimeStamp = Timestamp(Date(to))

        Log.e("WAA", "getTransactionHistory-fromTimeStamp=$fromTimeStamp")
        Log.e("WAA", "getTransactionHistory-toTimeStamp=$toTimeStamp")

        val ancientDate = Timestamp(Date(0)) // 1 de enero 1970
        val futureDate = Timestamp(Date(System.currentTimeMillis() + 100L * 365 * 24 * 60 * 60 * 1000)) // 100 años al futuro

        Log.e("WAA", "getTransactionHistory-ancientDate=$ancientDate")
        Log.e("WAA", "getTransactionHistory-futureDate=$futureDate")

        val depositQuery = db.collection("deposit_data")
            .whereEqualTo("uid", uid)
            .whereGreaterThanOrEqualTo("date", fromTimeStamp)
            .whereLessThanOrEqualTo("date", toTimeStamp)
//            .whereGreaterThanOrEqualTo("date", ancientDate)
//            .whereLessThanOrEqualTo("date", futureDate)
            .get()
            .await()

        val sentQuery = db.collection("trx_data")
            .whereEqualTo("from_uid", uid)
            .whereGreaterThanOrEqualTo("date", fromTimeStamp)
            .whereLessThanOrEqualTo("date", toTimeStamp)
            .get()
            .await()

        val receivedQuery = db.collection("trx_data")
            .whereEqualTo("to_uid", uid)
            .whereGreaterThanOrEqualTo("date", fromTimeStamp)
            .whereLessThanOrEqualTo("date", toTimeStamp)
            .get()
            .await()

        val depositList = depositQuery.documents.map {
            Transaction(
                type = "deposit",
                isValidated = it.getBoolean("is_validated") ?: false,
                isApproved = it.getBoolean("is_approved") ?: false,
                amount = it.getDouble("amount") ?: 0.0,
                date = it.getTimestamp("date")?.toDate()?.time ?: 0L
            )
        }
        val transferSentList = sentQuery.documents.map {
            Transaction(
                type = "transfer_sent",
                isValidated = true, // Puedes omitir si tu modelo no lo usa para transferencias
                isApproved = true,
                amount = it.getDouble("amount") ?: 0.0,
                date = it.getTimestamp("date")?.toDate()?.time ?: 0L
            )
        }
        val transferReceivedList = receivedQuery.documents.map {
            Transaction(
                type = "transfer_received",
                isValidated = true,
                isApproved = true,
                amount = it.getDouble("amount") ?: 0.0,
                date = it.getTimestamp("date")?.toDate()?.time ?: 0L
            )
        }
        Log.e("WAA", "getTransactionHistory-depositList=${depositList.size}")
        Log.e("WAA", "getTransactionHistory-transferSentList=${transferSentList.size}")
        Log.e("WAA", "getTransactionHistory-transferReceivedList=${transferReceivedList.size}")

        return (depositList + transferSentList + transferReceivedList).sortedByDescending { it.date }
    }

    suspend fun logoutUser(context: Context) {
        auth.signOut()
        SharedPreferencesManager.clearData(context)
    }

    fun getCurrentUserUid(): String {
        return auth.currentUser?.uid.orEmpty()
    }

}