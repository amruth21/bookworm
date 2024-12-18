package com.example.bookworm


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class FeedFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.booksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        bookAdapter = BookAdapter(requireContext())
        recyclerView.adapter = bookAdapter

        loadBooks()
    }

    private fun loadBooks() {
        firestore.collection("books")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(8)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val books = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Book::class.java)?.copy(id = doc.id)
                    }
                    bookAdapter.updateBooks(books)
                }
            }
    }
}