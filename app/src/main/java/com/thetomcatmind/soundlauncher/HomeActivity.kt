package com.thetomcatmind.soundlauncher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.thetomcatmind.soundlauncher.firestore.FirestoreManager
import com.google.firebase.auth.FirebaseAuth
class HomeActivity : AppCompatActivity() {

    // Declaración de los SoundButtons
    private val soundButtons = arrayOfNulls<SoundButton>(9)

    // Obtén la instancia de FirestoreManager y el ID del usuario
    private val firestoreManager = FirestoreManager
    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        // IDs de los componentes de la interfaz para cada botón
        val buttonFrameIDs = intArrayOf(
            R.id.button_frame_1, R.id.button_frame_2, R.id.button_frame_3,
            R.id.button_frame_4, R.id.button_frame_5, R.id.button_frame_6,
            R.id.button_frame_7, R.id.button_frame_8, R.id.button_frame_9
        )

        val iconViewIDs = intArrayOf(
            R.id.button_icon_1, R.id.button_icon_2, R.id.button_icon_3,
            R.id.button_icon_4, R.id.button_icon_5, R.id.button_icon_6,
            R.id.button_icon_7, R.id.button_icon_8, R.id.button_icon_9
        )

        val volumeSeekBarIDs = intArrayOf(
            R.id.volume_seek_bar_1, R.id.volume_seek_bar_2, R.id.volume_seek_bar_3,
            R.id.volume_seek_bar_4, R.id.volume_seek_bar_5, R.id.volume_seek_bar_6,
            R.id.volume_seek_bar_7, R.id.volume_seek_bar_8, R.id.volume_seek_bar_9
        )

        // Declaración de los ActivityResultLauncher para manejar las respuestas de las actividades lanzadas
        // Se registra una nueva instancia para cada SoundButton
        val iconActivityResultLaunchers = Array<ActivityResultLauncher<Intent>>(9) { index ->
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        // Actualiza el icono del SoundButton con el nuevo URI
                        soundButtons[index]?.updateButtonIcon(uri)
                    }
                }
            }
        }

        val soundActivityResultLaunchers = Array<ActivityResultLauncher<Intent>>(9) { index ->
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        // Actualiza el sonido del SoundButton con el nuevo URI
                        soundButtons[index]?.updateButtonSound(uri)
                    }
                }
            }
        }

        //Inicialización de las instancias de SoundButton
        for (i in 0 until 9) {
            // Crea una nueva instancia de SoundButton
            val newSoundButton = SoundButton(
                this,
                i.toString(), // Usar la posición del array como identificador
                findViewById(buttonFrameIDs[i]),
                findViewById(iconViewIDs[i]),
                findViewById(volumeSeekBarIDs[i]),
                soundActivityResultLaunchers[i],
                iconActivityResultLaunchers[i]
            )

            // Guarda la instancia de SoundButton en el array
            soundButtons[i] = newSoundButton

            // Recuperamos la configuración del botón de Firestore
            // y actualizamos la configuración del botón con los datos recuperados
            userId?.let {
                firestoreManager.getButtonConfigForUser(i.toString(), it) { soundUri, iconUrl, volume ->
                    newSoundButton.updateButtonSound(soundUri ?: return@getButtonConfigForUser)
                    Glide.with(this).load(iconUrl).into(newSoundButton.getIconView())
                    newSoundButton.getVolumeSeekBar().progress = volume
                }
            } ?: throw IllegalStateException("User ID is null.")
        }
    }

    override fun onStop() {
        super.onStop()

        // Guarda la configuración del botón en Firestore cuando la actividad se detiene
        soundButtons.forEach { soundButton ->
            soundButton?.let { firestoreManager.saveSoundButtonConfig(it) }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (Permisos.isReadExternalStoragePermissionGranted(requestCode, permissions, grantResults)) {
            // Si se concede el permiso, actualiza el SoundButton que tiene un pendingIconUri no nulo
            soundButtons.firstOrNull { it?.getPendingIconUri() != null }?.apply {
                getPendingIconUri()?.let { updateButtonIcon(it) } // Notación de llamada segura
                // Aquí llamas al setter de pendingIconUri en SoundButton
                setPendingIconUri(null)
            }
        } else {
            // El permiso fue denegado. Maneja esta situación como mejor te parezca.
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Sobrescribe este método para inflar tu menú en HomeActivity
    // Este método es llamado por el sistema Android cuando necesita dibujar el menú
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Obtiene una instancia del MenuInflater, que se utiliza para inflar el menú
        val inflater: MenuInflater = menuInflater
        // Infla el menú. Reemplaza 'menu' con el nombre de tu archivo de menú
        inflater.inflate(R.menu.sound_button_menu, menu)
        return true
    }

    // Sobrescribe este método para manejar las selecciones de elementos del menú
    // Este método es llamado por el sistema Android cuando el usuario selecciona un ítem del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Chequea qué ítem fue seleccionado
        return when (item.itemId) {
            R.id.action_show_favorites -> {
                // Si el ítem seleccionado es "Mostrar Favoritos", inicia la FavoritesActivity
                val intent = Intent(this, FavoritesActivity::class.java)
                startActivity(intent)
                true
            }
            // Aquí van los otros casos para los otros ítems del menú
            else -> super.onOptionsItemSelected(item)
        }
    }


}
