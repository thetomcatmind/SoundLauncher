package com.thetomcatmind.soundlauncher.firestore

import android.annotation.SuppressLint
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thetomcatmind.soundlauncher.SoundButton
import com.thetomcatmind.soundlauncher.User

object FirestoreManager {

    // Obtenemos la instancia de Firestore y de FirebaseAuth
    @SuppressLint("StaticFieldLeak")
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Método para agregar un sonido favorito a Firestore
    fun addFavoriteSound(soundId: Int) {
        // Obtén el usuario actual
        val user = auth.currentUser

        // Si el usuario está autenticado (no es nulo), procedemos a guardar el sonido
        if (user != null) {
            // Creamos un mapa para el sonido favorito con el userID, el userEmail y el soundId
            val favoriteSound = hashMapOf(
                "userId" to user.uid,
                "userEmail" to user.email,
                "soundId" to soundId
            )

            // Agregamos el sonido favorito a la colección "favoriteSounds" en Firestore
            firestore.collection("favoriteSounds")
                .add(favoriteSound)
                .addOnSuccessListener { documentReference ->
                    // Imprime el ID del documento en caso de éxito
                    println("DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    // Imprime el error en caso de fallo
                    println("Error adding document $e")
                }
        } else {
            // Si no hay usuario autenticado, imprimimos un mensaje de error
            println("No user is signed in.")
        }
    }

    // Método para remover un sonido favorito de Firestore
    fun removeFavoriteSound(soundId: Int) {
        // Obtén el usuario actual
        val user = auth.currentUser

        // Si el usuario está autenticado (no es nulo), procedemos a remover el sonido
        if (user != null) {
            // Obtenemos la colección de sonidos favoritos
            val favoriteSounds = firestore.collection("favoriteSounds")

            // Realizamos una consulta para obtener los sonidos favoritos del usuario actual
            favoriteSounds.whereEqualTo("userId", user.uid)
                .whereEqualTo("soundId", soundId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // Borramos el sonido favorito de la colección "favoriteSounds" en Firestore
                        favoriteSounds.document(document.id).delete()
                    }
                }
                .addOnFailureListener { e ->
                    // Imprime el error en caso de fallo
                    println("Error removing document $e")
                }
        } else {
            // Si no hay usuario autenticado, imprimimos un mensaje de error
            println("No user is signed in.")
        }
    }

    // Método para obtener los sonidos favoritos de un usuario en particular
    fun getFavoriteSoundsForUser(userId: String?, onSuccess: (List<Int>) -> Unit) {
        // Si el usuario está autenticado (no es nulo), procedemos a obtener los sonidos favoritos
        if (userId != null) {
            // Obtenemos la colección de sonidos favoritos
            val favoriteSounds = firestore.collection("favoriteSounds")

            // Realizamos una consulta para obtener los sonidos favoritos del usuario actual
            favoriteSounds.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val favoriteSoundList = mutableListOf<Int>()
                    for (document in documents) {
                        document.getLong("soundId")?.toInt()?.let { soundId ->
                            favoriteSoundList.add(soundId)
                        }
                    }
                    onSuccess(favoriteSoundList)
                }
                .addOnFailureListener { e ->
                    // Imprime el error en caso de fallo
                    println("Error getting documents: $e")
                }
        } else {
            // Si no hay usuario autenticado, imprimimos un mensaje de error
            println("No user is signed in.")
        }
    }

    fun addUserData(user: User) {

        firestore.collection("users")
            .add(user)

    }

    fun saveSoundButtonConfig(buttonConfig: SoundButton) {
        // Obtén el usuario actual
        val user = auth.currentUser

        // Si el usuario está autenticado (no es nulo), procedemos a guardar la configuración del botón
        if (user != null) {
            val userId = user.uid
            val volume = buttonConfig.getVolumeSeekBar().progress

            // Creamos un mapa para la configuración del botón con el buttonId, soundUri, iconUrl, volume y userId
            val docData = hashMapOf(
                "soundUri" to buttonConfig.getSoundUri().toString(),
                "iconUrl" to (buttonConfig.getIconUrl() ?: ""), // Si la URL del icono es nula, guardamos una cadena vacía
                "volume" to volume,
                "userId" to userId
            )

            // Agregamos o actualizamos la configuración del botón en la colección "botonSetup" en Firestore
            firestore.collection("botonSetup")
                .document("$userId-${buttonConfig.getButtonId()}")
                .set(docData)
                .addOnSuccessListener {
                    // Imprime un mensaje en caso de éxito
                    println("DocumentSnapshot successfully written!")
                }
                .addOnFailureListener { e ->
                    // Imprime el error en caso de fallo
                    println("Error writing document $e")
                }
        } else {
            // Si no hay usuario autenticado, imprimimos un mensaje de error
            println("No user is signed in.")
        }
    }
    // Método para obtener la configuración de un botón en particular para un usuario específico
    // Función para obtener la configuración del botón para un usuario específico
    fun getButtonConfigForUser(buttonId: String, userId: String, onSuccess: (soundUri: Uri?, iconUrl: String?, volume: Int) -> Unit) {
        val defaultSoundUri = Uri.parse("android.resource://com.thetomcatmind.soundlauncher/raw/sonidopordefecto")
        val defaultIconUrl = "https://firebasestorage.googleapis.com/v0/b/soundlauncher-843f0.appspot.com/o/iconopordefecto.png?alt=media&token=30b3617c-439e-4f9c-9eb0-c855c6bd5363"

        if (userId.isNotEmpty()) {
            val buttonSetupCollection = firestore.collection("botonSetup")

            buttonSetupCollection.document("$userId-$buttonId")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val soundUriString = document.getString("soundUri")
                        val volume = document.getLong("volume")?.toInt() ?: 0

                        val soundUri = if (soundUriString != null && soundUriString.isNotBlank()) Uri.parse(soundUriString) else defaultSoundUri
                        val iconUrl = document.getString("iconUrl")

                        if (iconUrl != null && iconUrl.isNotBlank()) {
                            onSuccess(soundUri, iconUrl, volume)
                        } else {
                            onSuccess(soundUri, defaultIconUrl, volume)
                        }
                    } else {
                        val volume = 100
                        onSuccess(defaultSoundUri, defaultIconUrl, volume)
                    }
                }
                .addOnFailureListener { e ->
                    println("Error getting documents: $e")
                }
        } else {
            println("No user is signed in.")
        }
    }

}
