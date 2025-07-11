package com.esan.payflowapp.core.firebase

import android.annotation.SuppressLint
import android.util.Log
import com.esan.payflowapp.core.firebase.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    //Login
    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(): Triple<String, Double, Boolean> {
        var uid = auth.currentUser?.uid.orEmpty()
        var snapshot = db.collection("user_data").document(uid).get().await()
        val name = snapshot.getString("name").orEmpty()
        val isAdmin = snapshot.getBoolean("is_admin") ?: false
        val balance = snapshot.getDouble("balance") ?: 0.0
        return Triple(name, balance, isAdmin)
    }

    suspend fun transferMoney(toUID: String, amount: Double): Result<String> {
        val fromUid = auth.currentUser?.uid.orEmpty()
        val fromRef = db.collection("user_data").document(fromUid)
        val toRef = db.collection("user_data").document(toUID)

        db.runTransaction { transaction ->
            val fromSnapshot = transaction.get(fromRef)
            val toSnapshot = transaction.get(toRef)

            val fromBalance = fromSnapshot.getDouble("balance") ?: 0.0
            val isAdmin = fromSnapshot.getBoolean("is_admin") ?: false
            if (isAdmin) throw Exception("Admins no pueden transferir")
            if (fromBalance < amount) throw Exception("Saldo insuficiente")

            // Actualiza saldos
            transaction.update(fromRef, "balance", fromBalance - amount)
            val toBalance = toSnapshot.getDouble("balance") ?: 0.0
            transaction.update(toRef, "balance", toBalance + amount)

            // Agrega registro a trx_data
            val fromName = fromSnapshot.getString("name") ?: ""
            val toName = toSnapshot.getString("name") ?: ""
            val trxRef = db.collection("trx_data").document()
            transaction.set(
                trxRef, mapOf(
                    "from_uid" to fromUid,
                    "from_name" to fromName,
                    "to_uid" to toUID,
                    "to_name" to toName,
                    "amount" to amount,
                    "date" to System.currentTimeMillis()
                )
            )
        }.let {
            return Result.success("Transferencia realizada")
        }
    }

    suspend fun listUsers(): List<Pair<String, String>> {
        val fromUid = auth.currentUser?.uid.orEmpty()
        val users = db.collection("user_data")
            .whereEqualTo("is_admin", false)
            .get()
            .await()

        return users.documents
            .filter { it.id != fromUid }
            .map { it.id to (it.getString("name") ?: "Sin nombre") }
    }

    suspend fun getBalance(): Double {
        val fromUid = auth.currentUser?.uid.orEmpty()
        val doc = db.collection("user_data").document(fromUid).get().await()
        return doc.getDouble("balance") ?: 0.0
    }

    suspend fun getLastTrx(): List<Transaction> {
        val fromUid = auth.currentUser?.uid.orEmpty()

        val depositQuery = db.collection("deposit_data")
            .whereEqualTo("uid", fromUid)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()
        val trxQueryFrom = db.collection("trx_data")
            .whereEqualTo("from_uid", fromUid)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()
        val trxQueryTo = db.collection("trx_data")
            .whereEqualTo("to_uid", fromUid)
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

    suspend fun logoutUser() {
        auth.signOut()
    }

    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

}