package com.thetomcatmind.soundlauncher

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.thetomcatmind.soundlauncher.firestore.FirestoreManager


class SoundButton(
    private val context: Context,
    private val buttonId: String,  // Identificador único para este botón de sonido // NUEVO
    private val buttonFrame: FrameLayout,
    private val iconView: ImageView,
    private val volumeSeekBar: SeekBar,
    private val soundActivityResultLauncher: ActivityResultLauncher<Intent>,
    private val iconActivityResultLauncher: ActivityResultLauncher<Intent>
) : PopupMenu.OnMenuItemClickListener {

    companion object {
        private const val REQUEST_CODE_SOUND = 101
        private const val REQUEST_CODE_ICON = 102
    }

    private var mediaPlayer: MediaPlayer? = null // MediaPlayer para reproducir el sonido
    private var soundUri: Uri? = null // URI del sonido que se va a reproducir
    private var iconUri: Uri? = null // URI del icono del botón //// NUEVO
    private var pendingIconUri: Uri? = null// // Variable para almacenar el URI del icono mientras esperamos el permiso
    private val firebaseStorageManager = FirebaseStorageManager()
    private var iconUrl: String? = null

    init {

        // Configuración inicial del volumen
        volumeSeekBar.max = 100 // El volumen máximo es 100
        volumeSeekBar.progress = 100 // El volumen inicial es 100

        // Configuración inicial del sonido
        soundUri = Uri.parse("android.resource://com.thetomcatmind.soundlauncher/raw/sonidopordefecto")

        // Configuración inicial del icono
       // iconView.setImageResource(R.drawable.iconopordefecto)
        iconUri?.let { uri ->
            Glide.with(context)
                .load(uri)
                .into(iconView)
        } ?: iconView.setImageResource(R.drawable.iconopordefecto)


        // Configurar el listener del SeekBar para cambiar el volumen
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                mediaPlayer?.setVolume(volume, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Configurar el click listener para el botón de sonido
        buttonFrame.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer?.release()
                mediaPlayer = null
            } else {
                mediaPlayer = MediaPlayer.create(context, soundUri)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setVolume(volumeSeekBar.progress / 100f, volumeSeekBar.progress / 100f)
                    start()
                }
            }
        }

        // Configurar el long click listener para el botón de sonido
        buttonFrame.setOnLongClickListener {
            showPopupMenu()
            true
        }
    }


    // Método para mostrar el menú emergente
    private fun showPopupMenu() {
        val popupMenu = PopupMenu(context, buttonFrame)
        popupMenu.inflate(R.menu.sound_button_menu)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    // Método invocado cuando se hace clic en un elemento del menú emergente
    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_load_sound -> {
                if (Permisos.checkReadExternalStoragePermission(context)) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "audio/*"
                    }
                    soundActivityResultLauncher.launch(intent)
                } else {
                    Permisos.requestReadExternalStoragePermission(context as Activity)
                }
                return true
            }

            R.id.action_change_sound -> {
                val intent = Intent(context, SoundSelectActivity::class.java)
                soundActivityResultLauncher.launch(intent)
                return true
            }

            R.id.action_change_icon -> {
                if (Permisos.checkReadExternalStoragePermission(context)) {
                    //AlertDialog de recomendación
                    AlertDialog.Builder(context)
                        .setTitle(R.string.icon_change_title)
                        .setMessage(R.string.icon_change_message)
                        .setPositiveButton(R.string.aceptar) { _, _ ->
                            // Abrir el selector de archivos después de que el usuario presione "OK"
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "image/*"
                            }
                            iconActivityResultLauncher.launch(intent)
                        }
                        .show()
                } else {
                    Permisos.requestReadExternalStoragePermission(context as Activity)
                }
                return true
            }

            R.id.action_show_favorites -> {
                val intent = Intent(context, FavoritesActivity::class.java)
                context.startActivity(intent)
                return true
            }
            else -> return false
        }

    }

    // Método para cambiar el sonido del botón
    fun updateButtonSound(uri: Uri) {
        Log.d("SoundButton", "Updating sound to: $uri")
        soundUri = uri
        FirestoreManager.saveSoundButtonConfig(this) // Guardar la configuración en Firestore
    }

    // Método para cambiar el icono del botón
    fun updateButtonIcon(uri: Uri) {
        if (Permisos.checkReadExternalStoragePermission(context)) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                firebaseStorageManager.uploadIconToFirebaseStorage(uri, userId) { downloadUrl ->
                    Glide.with(context)
                        .load(downloadUrl)
                        .into(iconView)
                    iconUri = downloadUrl
                    FirestoreManager.saveSoundButtonConfig(this)
                }
            } else {
                Log.e("SoundButton", "User is not signed in.")
            }
        } else {
            pendingIconUri = uri
            Permisos.requestReadExternalStoragePermission(context as Activity)
        }
    }
    //Getter para getPendingIconUri
    fun getPendingIconUri(): Uri? {
        return pendingIconUri
    }
    //Setter para getPendingIconUri
    fun setPendingIconUri(uri: Uri?) {
        pendingIconUri = uri
    }
    // Getter para buttonId
    fun getButtonId(): String {
        return this.buttonId
    }

    // Getter para soundUri
    fun getSoundUri(): Uri? {
        return this.soundUri
    }

    // Getter para iconUri
    fun getIconUri(): String? {
        return this.iconUri?.toString() // Devuelve la representación en cadena del Uri
    }

    // Getter para volumeSeekBar
    fun getVolumeSeekBar(): SeekBar {
        return this.volumeSeekBar
    }

    // Getter para iconUrl
    fun getIconUrl(): String? {
        return this.iconUri?.toString() // Devuelve la representación en cadena del Uri
    }

    fun getIconView(): ImageView {
        return iconView
    }
}
