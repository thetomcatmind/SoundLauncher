package com.thetomcatmind.soundlauncher

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.thetomcatmind.soundlauncher.firestore.FirestoreManager
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import com.google.firebase.Timestamp
import java.text.ParseException
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val usernameEditText = findViewById<EditText>(R.id.username_edittext)
        val emailEditText = findViewById<EditText>(R.id.email_edittext)
        val passwordEditText = findViewById<EditText>(R.id.password_edittext)
        val passwordEditTextCon = findViewById<EditText>(R.id.password_edittext_con)
        val nameEditText = findViewById<EditText>(R.id.name_edittext)
        val surnameEditText = findViewById<EditText>(R.id.surname_edittext)
        val phoneEditText = findViewById<EditText>(R.id.phone_edittext)
        val birthdayEditText = findViewById<EditText>(R.id.birthday_edittext)
        val registerButton = findViewById<Button>(R.id.register_button)

        birthdayEditText.setOnClickListener {
            val currentDate = Calendar.getInstance()
            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH)
            val day = currentDate.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format(
                        "%02d-%02d-%04d",
                        selectedDay,
                        selectedMonth + 1,
                        selectedYear
                    )
                    birthdayEditText.setText(formattedDate)
                }, year, month, day)

            datePickerDialog.show()
        }

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val passwordCon = passwordEditTextCon.text.toString()
            val name = nameEditText.text.toString()
            val surname = surnameEditText.text.toString()
            val phone = phoneEditText.text.toString().toIntOrNull()
            val birthdayString = birthdayEditText.text.toString()

            val birthday: Date? = if (birthdayString.isEmpty()) {
                null
            } else {
                try {
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(birthdayString)
                } catch (e: ParseException) {
                    null
                }
            }

            if (password != passwordCon) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else if (username.isEmpty() || email.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, completa todos los campos obligatorios",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: ""
                            val user = User(
                                username,
                                email,
                                name,
                                surname,
                                phone,
                                birthday?.let { Timestamp(it) })
                            FirestoreManager.addUserData(user)

                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Error en el registro: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}

