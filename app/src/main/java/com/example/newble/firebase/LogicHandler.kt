package com.example.newble.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseHelper {
    private val db = FirebaseFirestore.getInstance()

    fun writeToFirebase(
        username: String,
        date: String,
        time: String,
        type: String, // "GYRO" or "ACCEL"
        data: Map<String, Any>,
        onSuccess: () -> Unit = { Log.d("FirebaseHelper", "Document written successfully") },
        onFailure: (Exception) -> Unit = { e -> Log.e("FirebaseHelper", "Error writing document", e) }
    ) {
        val documentPath = "$username/$date/$time" // Path to the time document

        val dataWithType = data.toMutableMap()
        dataWithType["type"] = type

        db.collection(username).document(date).collection(time).document(type).set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}