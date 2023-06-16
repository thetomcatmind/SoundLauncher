package com.thetomcatmind.soundlauncher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    // Variable para almacenar la instancia de FirebaseAuth
    private lateinit var auth: FirebaseAuth

    // Request code para el intent de inicio de sesión
    private lateinit var googleSignInClient: GoogleSignInClient

    // Google sign-in client
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Encontrar los elementos de la vista en el diseño
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signinButton = findViewById<Button>(R.id.SigninButton)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val googleSignInButton = findViewById<Button>(R.id.googleSignInButton)

        // Establecer OnClickListener para el botón de inicio de sesión
        loginButton.setOnClickListener {
            // Obtener el correo electrónico y la contraseña ingresados por el usuario
            val email = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Verificar si el correo electrónico y la contraseña no están vacíos
            if (email.isNotBlank() && password.isNotBlank()) {
                // Intentar iniciar sesión con FirebaseAuth
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Si el inicio de sesión es exitoso, mostrar un mensaje y navegar a la pantalla principal
                            Toast.makeText(this, "Login correcto", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Si el inicio de sesión falla, mostrar un mensaje con la razón del error
                            Toast.makeText(this, "Login fallido: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener(this) { exception ->
                        // Aquí se maneja el fallo
                        Toast.makeText(this, "Fallo de autenticación: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Si el correo electrónico o la contraseña están vacíos, mostrar un mensaje solicitando al usuario que los complete
                Toast.makeText(this, "Introduce email y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        //OnClickListener para pasar a la activity de registro
        signinButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //OnClickListener para olvidar la contraseña
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotPasswordTextView)
        forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, PasswordResetActivity::class.java)
            startActivity(intent)

        }
        // Configurar Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Si el acceso con Google es valido, hace la auth con Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Acceso con Google fallido. Código de estado: ${e.statusCode}, Detalle: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Si el acceso es correcto, llamamos al método updateUI y le mandamos el usuario
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // Si el acceso falla mandamos mensaje de error
                    Toast.makeText(this, "Acceso Fallido.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Navigate to home screen
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Stay in the login screen and show error message
        }
    }
}
