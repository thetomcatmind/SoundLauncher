package com.thetomcatmind.soundlauncher

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.thetomcatmind.soundlauncher.firestore.FirestoreManager
class FavoritesActivity : AppCompatActivity() {

    // Instancia de FirebaseAuth y FirestoreManager.
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreManager: FirestoreManager

    // Instancia del SoundAdapter.
    private lateinit var soundAdapter: SoundAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.favorite_sound_select)

        // Inicializamos FirebaseAuth y FirestoreManager.
        auth = FirebaseAuth.getInstance()
        firestoreManager = FirestoreManager

        // Encuentras la vista del RecyclerView en tu layout y la asignas a la variable recyclerView.
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Llamamos a nuestro nuevo método de inicialización del adaptador
        initializeAdapter(recyclerView)
    }

    private fun initializeAdapter(recyclerView: RecyclerView) {
        // Obtenemos los sonidos favoritos del usuario.
        firestoreManager.getFavoriteSoundsForUser(auth.currentUser?.uid) { favoriteSoundList ->

            // Inicializamos el SoundAdapter con la lista de sonidos favoritos
            soundAdapter = SoundAdapter(
                favoriteSoundList.toMutableList(),
                firestoreManager,
                onSoundSelected = { soundResId ->

                    // Estableces el resultado de la actividad con el soundResId seleccionado
                    val resultIntent = Intent().apply {
                        // Convertimos el soundResId a un Uri y lo pasamos como datos del Intent
                        data = Uri.parse("android.resource://com.thetomcatmind.soundlauncher/$soundResId")
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    // Cierras la actividad
                    finish()
                },
                onSoundPlay = { soundResId ->
                    // Creas un MediaPlayer y lo configuras para reproducir el sonido
                    val mediaPlayer = MediaPlayer.create(this, soundResId)
                    mediaPlayer.start()
                }
            )

            // Configuramos el RecyclerView.
            recyclerView.adapter = soundAdapter

            // Actualizamos la lista de sonidos favoritos en el adaptador.
            soundAdapter.updateFavoriteSounds(favoriteSoundList)
            soundAdapter.updateSounds(favoriteSoundList)
        }
    }
}
