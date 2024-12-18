package com.example.bookworm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SavedBookAdapter : RecyclerView.Adapter<SavedBookAdapter.ViewHolder>() {
    private var bookTitles = listOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.savedBookTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.saved_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleTextView.text = bookTitles[position]
    }

    override fun getItemCount() = bookTitles.size

    fun updateBooks(titles: List<String>) {
        bookTitles = titles
        notifyDataSetChanged()
    }
}