package com.thetomcatmind.soundlauncher

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.thetomcatmind.soundlauncher.firestore.FirestoreManager

// Definimos la clase SoundAdapter que extiende de RecyclerView.Adapter.
class SoundAdapter(

    private val soundList: MutableList<Int>,
    private val firestoreManager: FirestoreManager,  // Gestor para interactuar con Firestore
    private val onSoundSelected: (Int) -> Unit,  // Callback para cuando se selecciona un sonido
    private val onSoundPlay: (Int) -> Unit,  // Callback para cuando se reproduce un sonido
) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {

    // Instancia de FirebaseAuth.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Lista para mantener el seguimiento de los sonidos favoritos.
    private val favoriteSoundList = mutableListOf<Int>()


    // ViewHolder que representa un único ítem de la lista. En este caso, un TextView.
    class SoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val favoriteImageView: ImageView = itemView.findViewById(R.id.favoriteImageView)
        val playButton: Button = itemView.findViewById(R.id.playButton)
        val addButton: Button = itemView.findViewById(R.id.addButton)
    }

    // Este método es llamado cuando el RecyclerView necesita un nuevo ViewHolder para representar un ítem.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sound, parent, false)
        return SoundViewHolder(view)
    }

    // Este método es llamado para vincular datos con un ViewHolder.
    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        // Obteniendo el identificador del recurso de sonido para esta posición.
        val soundResId = soundList[position]
        // Obteniendo el nombre del recurso de sonido.
        val soundName = holder.itemView.context.resources.getResourceEntryName(soundResId)
        // Estableciendo el nombre del recurso de sonido en el TextView.
        holder.textView.text = soundName

        // Establecer un listener de clic para el botón de reproducción
        holder.playButton.setOnClickListener {
                        onSoundPlay(soundResId)
                    }

        // Estableciendo un listener de clic para el botón de añadir.
        holder.addButton.setOnClickListener {
            // Llamando al callback cuando se selecciona un sonido.
            onSoundSelected(soundResId)
        }

        // Establecer el icono correcto para el sonido en función de si es favorito o no
        holder.favoriteImageView.setImageResource(
            if (favoriteSoundList.contains(soundResId)) R.drawable.ic_heart_filled
            else R.drawable.ic_heart_outline
        )

        // Establecer un listener de clic para el icono de favorito
        holder.favoriteImageView.setOnClickListener {
            if (favoriteSoundList.contains(soundResId)) {
                // Si el sonido es favorito, lo removemos de la lista de favoritos
                favoriteSoundList.remove(soundResId)
                holder.favoriteImageView.setImageResource(R.drawable.ic_heart_outline)
                firestoreManager.removeFavoriteSound(soundResId)
                Toast.makeText(holder.itemView.context, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
            } else {
                // Si el sonido no es favorito, lo añadimos a la lista de favoritos
                favoriteSoundList.add(soundResId)
                holder.favoriteImageView.setImageResource(R.drawable.ic_heart_filled)
                firestoreManager.addFavoriteSound(soundResId)
                Toast.makeText(holder.itemView.context, "Añadido a favoritos", Toast.LENGTH_SHORT).show()

            }
            notifyDataSetChanged() // Notificamos que los datos han cambiado
        }
    }

    // Método para actualizar la lista de sonidos favoritos.
    fun updateFavoriteSounds(newFavoriteSoundList: List<Int>) {
        favoriteSoundList.clear()
        favoriteSoundList.addAll(newFavoriteSoundList)
    }

    // Método para actualizar la lista general de sonidos.
    fun updateSounds(newSoundList: List<Int>) {
        soundList.clear()
        soundList.addAll(newSoundList)
        notifyDataSetChanged() // Notificamos que los datos han cambiado
    }


    // Este método retorna el total de ítems en los datos.
    override fun getItemCount() = soundList.size
}

