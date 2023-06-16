package com.thetomcatmind.soundlauncher


import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import android.net.Uri

class FirebaseStorageManager {

    private val storage = Firebase.storage

    fun uploadIconToFirebaseStorage(uri: Uri, userId: String, onSuccess: (downloadUrl: Uri) -> Unit) {
        val ref = storage.reference.child("icons/$userId/${uri.lastPathSegment}")
        val uploadTask = ref.putFile(uri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri) // Llama a la función onSuccess con la URL de descarga
            }
        }.addOnFailureListener { e ->
            println("Failed to upload icon to Firebase Storage: $e")
        }
    }

}