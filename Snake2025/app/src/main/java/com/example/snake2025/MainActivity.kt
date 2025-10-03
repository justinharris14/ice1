package com.example.snake2025


import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var auth: FirebaseAuth
    private lateinit var tvUser: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()


        tvUser = findViewById(R.id.tvUser)
        findViewById<ImageButton>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnLeaderboard).setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }


        gameView = findViewById(R.id.gameView)

        findViewById<ImageButton>(R.id.btnUp).setOnClickListener { gameView.setDirection(GameView.Direction.UP) }
        findViewById<ImageButton>(R.id.btnDown).setOnClickListener { gameView.setDirection(GameView.Direction.DOWN) }
        findViewById<ImageButton>(R.id.btnLeft).setOnClickListener { gameView.setDirection(GameView.Direction.LEFT) }
        findViewById<ImageButton>(R.id.btnRight).setOnClickListener { gameView.setDirection(GameView.Direction.RIGHT) }



        val api = ApiClient.create()

        api.getJokes().enqueue(object : retrofit2.Callback<List<JokeResponse>> {
            override fun onResponse(call: retrofit2.Call<List<JokeResponse>>, response: retrofit2.Response<List<JokeResponse>>) {
                if (response.isSuccessful) {
                    val jokes = response.body()
                    if (!jokes.isNullOrEmpty()) {
                        val randomJoke = jokes.first().joke
                        textView.text = randomJoke // make sure you have a TextView in your layout
                    } else {
                        textView.text = "No jokes found"
                    }
                } else {
                    textView.text = "Error: ${response.message()}"
                }
            }

            override fun onFailure(call: retrofit2.Call<List<JokeResponse>>, t: Throwable) {
                textView.text = "Failed: ${t.message}"
            }
        })

    }


    override fun onResume() {
        super.onResume()
        val user = auth.currentUser
        tvUser.text = user?.email ?: "Not logged in"
    }
}