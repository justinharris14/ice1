package com.example.snake2025


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snake2025.models.Score
import com.google.firebase.firestore.FirebaseFirestore


class LeaderboardActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)


        val recycler = findViewById<RecyclerView>(R.id.recyclerScores)
        recycler.layoutManager = LinearLayoutManager(this)


        db.collection("scores")
            .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Score::class.java) }
                recycler.adapter = ScoresAdapter(list)
            }
            .addOnFailureListener { e ->
                recycler.adapter = ScoresAdapter(emptyList())
            }
    }
}