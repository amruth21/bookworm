package com.example.bookworm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookAdapter(private val context: Context) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
    private var books = listOf<Book>()
    private val sharedPrefs = context.getSharedPreferences("SavedBooks", Context.MODE_PRIVATE)

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.bookTitle)
        val imageView: ImageView = itemView.findViewById(R.id.bookImage)
        val saveSwitch: SwitchCompat = itemView.findViewById(R.id.saveSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.titleTextView.text = book.title

        Glide.with(holder.imageView.context).load(book.imageUrl).centerCrop().into(holder.imageView)
        holder.saveSwitch.setOnCheckedChangeListener(null)
        holder.saveSwitch.isChecked = sharedPrefs.contains(book.id)

        holder.saveSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sharedPrefs.edit().putString(book.id, book.title).apply()
                Toast.makeText(context, "Book Added to Favorites", Toast.LENGTH_SHORT).show()
            } else {
                sharedPrefs.edit().remove(book.id).apply()
                Toast.makeText(context, "Book Removed from Favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = books.size

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}
