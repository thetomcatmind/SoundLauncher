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
import java.util.*

class RegisterActivity : AppCompatActivity() {

    // Inicializamos las instancias de FirebaseAuth y FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Asignamos las instancias de FirebaseAuth y FirebaseDatabase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Asignamos los campos de texto y el botón de registro a variables
        val usernameEditText = findViewById<EditText>(R.id.username_edittext)
        val emailEditText = findViewById<EditText>(R.id.email_edittext)
        val passwordEditText = findViewById<EditText>(R.id.password_edittext)
        val nameEditText = findViewById<EditText>(R.id.name_edittext)
        val surnameEditText = findViewById<EditText>(R.id.surname_edittext)
        val phoneEditText = findViewById<EditText>(R.id.phone_edittext)
        val birthdayEditText = findViewById<EditText>(R.id.birthday_edittext)
        val registerButton = findViewById<Button>(R.id.register_button)

        // Establecemos un listener para el campo de cumpleaños (birthdayEditText)
        birthdayEditText.setOnClickListener {
            // Obtenemos la fecha actual para establecerla como fecha inicial en el selector de fecha
            val currentDate = Calendar.getInstance()
            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH)
            val day = currentDate.get(Calendar.DAY_OF_MONTH)

            // Crea un DatePickerDialog que permita al usuario seleccionar una fecha
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Actualiza el campo de texto con la fecha seleccionada por el usuario
                val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                birthdayEditText.setText(formattedDate)
            }, year, month, day)

            // Muestra el selector de fecha
            datePickerDialog.show()
        }

        // Establecemos un listener para el botón de registro
        registerButton.setOnClickListener {
            // Obtenemos el texto de los campos de texto
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            val surname = surnameEditText.text.toString()
            val phone = phoneEditText.text.toString().toIntOrNull()
            val birthdayString = birthdayEditText.text.toString()
            val birthday = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(birthdayString)

            // Verificamos si los campos obligatorios están vacíos
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                // Si alguno de los campos obligatorios está vacío, mostramos un mensaje de error
                Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                // Si todos los campos obligatorios están llenos, procedemos con el registro
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Si el registro es exitoso, obtenemos el ID del usuario y guardamos sus datos en la base de datos
                            val userId = auth.currentUser?.uid ?: ""
                            val user = User(username, email, name, surname, phone, birthday?.let { Timestamp(it) })
                            FirestoreManager.addUserData(user)

                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            // Si el registro falla, mostramos un mensaje de error
                            Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }


    }
}