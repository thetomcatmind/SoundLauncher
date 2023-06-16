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

// Clase que representa la actividad de selección de sonido.
class SoundSelectActivity : AppCompatActivity() {

    // Esta es una lista de identificadores de recursos para los sonidos que quieres mostrar.
    // R.raw.kick es el identificador de recurso para el sonido "kick" en la carpeta de recursos raw.
    // Estos son los sonidos que el usuario podrá seleccionar.
    private val soundList = mutableListOf(
        R.raw.kick,
        R.raw.clap,
        R.raw.crash,
        R.raw.hat,
        R.raw.shaker,
        R.raw.vocal,
        R.raw.vocal2,
        R.raw.siren,
        R.raw.sample,
        // Agrega más identificadores de recurso de sonido aquí
    )

    // Este es el método que se llama cuando se crea la actividad. Aquí es donde configuras tu vista y la inicializas.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_select)

        // Encuentras la vista del RecyclerView en tu layout y la asignas a la variable recyclerView.
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // Configuras el administrador de diseño del RecyclerView para que sea un LinearLayoutManager.
        // Esto significa que tus elementos se mostrarán en una lista vertical.
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configuras el adaptador del RecyclerView.
        // El adaptador es el que proporciona los datos y sabe cómo crear cada vista de elemento en la lista.
        // SoundAdapter es una clase personalizada que has creado, y pasas tu lista de sonidos a su constructor.
        // También pasas una función lambda que se llama cuando el usuario selecciona un sonido.
        recyclerView.adapter = SoundAdapter(soundList as MutableList<Int>, FirestoreManager, { soundResId ->
            val resultIntent = Intent().apply {
                data = Uri.parse("android.resource://com.thetomcatmind.soundlauncher/raw/$soundResId")
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }, { soundResId ->
            // Aquí puedes manejar el evento de reproducción de sonido, o simplemente dejarlo en blanco si no necesitas hacer nada específico cuando se reproduce un sonido.
            MediaPlayer.create(this, soundResId).apply {
                start() // no need to call prepare(); create() does that for you
                setOnCompletionListener { mp ->
                    mp.release()
                }
            }
        })
        // Recuperar sonidos favoritos
        val adapter = recyclerView.adapter as SoundAdapter
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        FirestoreManager.getFavoriteSoundsForUser(userId) { favoriteSounds ->
            adapter.updateFavoriteSounds(favoriteSounds)
            adapter.notifyDataSetChanged() // Esto le indica al adaptador que los datos han cambiado
        }
    }
}
