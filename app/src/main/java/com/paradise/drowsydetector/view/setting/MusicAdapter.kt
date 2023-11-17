package com.paradise.drowsydetector.view.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.databinding.ItemMusicBinding

class MusicAdapter(
    private val musicList: MutableList<Music>,
    private val onLayoutClick: (Music) -> Unit,
    private val onEditClick: (Music) -> Unit,
    private val onDeleteClick: (Music) -> Unit,
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(music: Music) {
            with(binding) {
                tvItemmusicNum.text = "${absoluteAdapterPosition + 1}"

                tvItemmusicMusicname.text = music.title

                layoutItemmusic.setOnClickListener {
                    onLayoutClick(music)
                }

                ivItemmusicEdit.setOnClickListener {
                    onEditClick(music)
                }

                ivItemmusicDelete.setOnClickListener {
                    onDeleteClick(music)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding =
            ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MusicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(musicList[position])
    }

    override fun getItemCount() = musicList.size

    fun addItem(music: Music) {
        musicList.add(music)
        notifyItemInserted(musicList.size - 1)
    }

    fun removeItem(music: Music) {
        val idx = musicList.indexOf(music)
        musicList.remove(music)
        notifyItemRemoved(idx)
    }
}