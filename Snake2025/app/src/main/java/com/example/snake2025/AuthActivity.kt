package com.example.snake2025

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.Executor

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnGoogle: com.google.android.gms.common.SignInButton
    private lateinit var btnBiometricLogin: Button
    private lateinit var tvStatus: TextView
    private lateinit var cbSaveBiometric: CheckBox

    private lateinit var googleClient: GoogleSignInClient
    private val RC_GOOGLE_SIGN_IN = 9001

    private lateinit var biometricHelper: BiometricHelper

    // Encrypted prefs keys
    private val PREFS_NAME = "secure_prefs"
    private val KEY_EMAIL = "saved_email"
    private val KEY_PASSWORD = "saved_password" // NOTE: better to store token — explained below

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnBiometricLogin = findViewById(R.id.btnBiometricLogin)
        tvStatus = findViewById(R.id.tvStatus)
        cbSaveBiometric = findViewById(R.id.cbSaveBiometric)

        biometricHelper = BiometricHelper(this)

        // GOOGLE SIGN IN setup
        // You must replace "YOUR_WEB_CLIENT_ID" with the OAuth client id for your Android app
        // found in Firebase Console -> Project Settings -> Web API Key / OAuth 2.0 Client IDs
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID") // <-- replace
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        btnGoogle.setOnClickListener {
            val signInIntent = googleClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.length < 6) {
                tvStatus.text = "Enter a valid email and password (min 6 chars)."
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    tvStatus.text = "Registered: ${auth.currentUser?.email}"
                    maybeSaveCredentialsForBiometric(email, pass)
                    finish()
                } else {
                    tvStatus.text = "Error: ${task.exception?.localizedMessage}"
                }
            }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    tvStatus.text = "Logged in: ${auth.currentUser?.email}"
                    maybeSaveCredentialsForBiometric(email, pass)
                    finish()
                } else {
                    tvStatus.text = "Error: ${task.exception?.localizedMessage}"
                }
            }
        }

        btnBiometricLogin.setOnClickListener {
            // Launch biometric prompt and on success attempt login using saved credentials
            if (!biometricHelper.isHardwareAvailable()) {
                tvStatus.text = "Biometric unavailable on this device."
                return@setOnClickListener
            }

            biometricHelper.authenticate("Login with biometrics",
                onSuccess = {
                    // read encrypted prefs and login
                    val prefs = getEncryptedPrefs()
                    val savedEmail = prefs.getString(KEY_EMAIL, null)
                    val savedPass = prefs.getString(KEY_PASSWORD, null)
                    if (!savedEmail.isNullOrEmpty() && !savedPass.isNullOrEmpty()) {
                        auth.signInWithEmailAndPassword(savedEmail, savedPass).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                tvStatus.text = "Biometric login successful: ${auth.currentUser?.email}"
                                finish()
                            } else {
                                tvStatus.text = "Auto-login failed: ${task.exception?.localizedMessage}"
                            }
                        }
                    } else {
                        tvStatus.text = "No saved credentials. Login with email first and opt-in."
                    }
                },
                onError = { err ->
                    tvStatus.text = "Biometric failed: $err"
                })
        }
    }

    private fun maybeSaveCredentialsForBiometric(email: String, password: String) {
        if (cbSaveBiometric.isChecked) {
            // Save securely
            val prefs = getEncryptedPrefs()
            prefs.edit().putString(KEY_EMAIL, email).putString(KEY_PASSWORD, password).apply()
            tvStatus.text = (tvStatus.text.toString() + "\nCredentials saved for biometric login.")
        }
    }

    private fun getEncryptedPrefs(): EncryptedSharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Google Sign-In activity result
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("AuthActivity", "Google sign in failed", e)
                tvStatus.text = "Google sign-in failed: ${e.localizedMessage}"
            }
        } else {
            tvStatus.text = "Google sign-in cancelled"
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                tvStatus.text = "Google sign-in success: ${auth.currentUser?.email}"
                finish()
            } else {
                tvStatus.text = "Google sign-in failed: ${task.exception?.localizedMessage}"
            }
        }
    }
}
