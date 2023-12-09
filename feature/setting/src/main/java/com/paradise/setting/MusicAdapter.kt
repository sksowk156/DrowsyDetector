package com.paradise.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.model.musicItem
import com.paradise.setting.databinding.ItemMusicBinding

class MusicAdapter(
    private val musicList: MutableList<musicItem>,
    private val onLayoutClick: (musicItem) -> Unit,
    private val onEditClick: (musicItem) -> Unit,
    private val onDeleteClick: (musicItem) -> Unit,
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(music: musicItem) {
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

    fun addItem(music: musicItem) {
        musicList.add(music)
        notifyItemInserted(musicList.size - 1)
    }

    fun removeItem(music: musicItem) {
        val idx = musicList.indexOf(music)
        musicList.remove(music)
        notifyItemRemoved(idx)
    }
}