package com.example.bookworm

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class FavoritesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedBookAdapter
    private lateinit var adView: AdView
    private lateinit var tvMostRecent: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //ads
        MobileAds.initialize(requireContext()) {}

        recyclerView = view.findViewById(R.id.savedBooksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = SavedBookAdapter()
        recyclerView.adapter = adapter

        adView = view.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        loadFavBooks()
    }

    override fun onResume() {
        super.onResume()
        loadFavBooks()
        adView.resume()
    }

    override fun onPause() {
        super.onPause()
        adView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        adView.destroy()
    }

    private fun loadFavBooks() {
        val sharedPrefs = requireContext().getSharedPreferences("SavedBooks", Context.MODE_PRIVATE)
        val favBooks = sharedPrefs.all.values.map { it.toString() }
        adapter.updateBooks(favBooks)
    }
}