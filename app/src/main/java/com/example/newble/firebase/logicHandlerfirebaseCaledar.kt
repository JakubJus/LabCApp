package com.example.newble.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

object FirebaseHelperCalendar {
    private val db = FirebaseFirestore.getInstance()

    data class Event(
        val startdate: String = "",
        val starttime: String = "",
        val enddate: String = "",
        val endtime: String = ""
    )

    fun addEvent(
        event: Event,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("Events")
            .add(event)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun fetchEvents(
        onSuccess: (List<Event>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("Events")
            .get()
            .addOnSuccessListener { result ->
                val events = result.map { it.toObject<Event>() }
                onSuccess(events)
            }
            .addOnFailureListener { onFailure(it) }
    }
}
