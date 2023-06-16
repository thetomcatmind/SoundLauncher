package com.thetomcatmind.soundlauncher

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Permisos {

    // Código de solicitud que usaremos para identificar nuestro permiso de almacenamiento
    const val READ_EXTERNAL_STORAGE_REQUEST = 0x01

    // Función para verificar si ya tenemos el permiso de almacenamiento
    fun checkReadExternalStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Función para solicitar el permiso de almacenamiento
    fun requestReadExternalStoragePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_EXTERNAL_STORAGE_REQUEST
        )
    }

    // Función para verificar el resultado de la solicitud del permiso de almacenamiento
    // Devolverá true si el permiso se ha otorgado

    fun isReadExternalStoragePermissionGranted(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        return requestCode == READ_EXTERNAL_STORAGE_REQUEST && grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
}
