package com.example.bookworm

import com.google.firebase.FirebaseApp
import com.google.firebase.Firebase

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this);

        val navHostFragment : NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_frag) as NavHostFragment
        val navController = navHostFragment.navController

        findViewById<BottomNavigationView>(R.id.nav)
            .setupWithNavController(navController)

        intent.getStringExtra("BOOK_TITLE")?.let { bookTitle ->
            val bundle = Bundle().apply {
                putString("BOOK_TITLE", bookTitle)
            }
            navController.navigate(R.id.favoritesFragment, bundle)
        }
    }
}
