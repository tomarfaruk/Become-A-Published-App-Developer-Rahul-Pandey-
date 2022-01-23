package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.models.BoardSize
import com.example.myapplication.models.MemoryCard
import com.example.myapplication.models.MemoryGame
import com.example.myapplication.utils.DEFAULT_ICON

class MainActivity : AppCompatActivity() {
    companion object{
       private const val TAG="MainActivity"
    }

    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMovies: TextView
    private lateinit var tvNumParis: TextView

    private var boardSize:BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard = findViewById(R.id.rvBoard)
        tvNumMovies = findViewById(R.id.tvNumMovies)
        tvNumParis =findViewById(R.id.tvNumParis)

        memoryGame = MemoryGame(boardSize)

        adapter = MemoryBoardAdapter(this, boardSize,memoryGame.cards,
            object :MemoryBoardAdapter.CardClickListener{
                override fun onCardClicked(position: Int) {
                    Log.i(TAG,"Position: $position")
                    updateMemoryBoard(position)
                }
            })
        rvBoard.adapter=adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this,  boardSize.getWidth())
    }

    private fun updateMemoryBoard(position: Int) {
        memoryGame.flipCard(position)
        adapter.notifyDataSetChanged()
    }
}