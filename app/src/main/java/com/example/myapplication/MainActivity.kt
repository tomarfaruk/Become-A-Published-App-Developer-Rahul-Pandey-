package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMovies: TextView
    private lateinit var tvNumParis: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard = findViewById(R.id.rvBoard)
        tvNumMovies = findViewById(R.id.tvNumMovies)
        tvNumParis =findViewById(R.id.tvNumParis)
        tvNumParis.setText("hdfjsgjdfhgh")

        rvBoard.adapter = MemoryBoardAdaper(this, 8)
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this,  2)
    }
}