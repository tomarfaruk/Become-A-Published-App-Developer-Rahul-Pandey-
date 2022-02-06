package com.example.myapplication

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.models.BoardSize
import com.example.myapplication.models.MemoryGame
import com.example.myapplication.models.UserImageList
import com.example.myapplication.utils.EXTRA_BOARD_SIZE
import com.example.myapplication.utils.EXTRA_GAME_NAME
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 248
    }

    private val db = Firebase.firestore
    private var gameName: String? = null
    private var createdGameImages: List<String>? = null
    private lateinit var clRoot: CoordinatorLayout
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMovies: TextView
    private lateinit var tvNumParis: TextView

    private var boardSize: BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMovies = findViewById(R.id.tvNumMovies)
        tvNumParis = findViewById(R.id.tvNumParis)

        val intent = Intent(this, CreateActivity::class.java)
        intent.putExtra(EXTRA_BOARD_SIZE, BoardSize.EASY)
//        startActivityForResult(intent,CREATE_REQUEST_CODE )
        setUpBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()) {
                    showAlertDialog("Quit your game?", null, View.OnClickListener {
                        //call setup idle value for all option
                        setUpBoard()
                    })
                } else {
                    //call setup idle value for all option
                    setUpBoard()
                }
                return true
            }
            R.id.mi_chose_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.mi_custom -> {
                showCreationDialog()
                return true
            }
            R.id.mi_download_game -> {
                showDownloadDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDownloadDialog() {
        val downloadBoardVIew = LayoutInflater.from(this).inflate(R.layout.download_dialoga, null)
        showAlertDialog("Get game", downloadBoardVIew, View.OnClickListener {

            val etDownloadName = downloadBoardVIew.findViewById<EditText>(R.id.etDownloadGameName)
            val gameToDownload = etDownloadName.text.toString()
            downloadGame(gameToDownload)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(TAG, "call from $requestCode and Result code $resultCode ")
        if (requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val createdGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if (createdGameName == null) {
                Log.e(TAG, "Got game name empty")
                Snackbar.make(clRoot, "can't get game name", Snackbar.LENGTH_LONG).show()
                return
            }
            downloadGame(createdGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downloadGame(createdGameName: String) {
        db.collection("games").document(createdGameName).get()
            .addOnSuccessListener { doc ->
                val userImageList = doc.toObject(UserImageList::class.java)
                if (userImageList?.images == null) {
                    Snackbar.make(
                        clRoot,
                        "There is no game name $createdGameName",
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }
                createdGameImages = userImageList.images
                val numCards = createdGameImages!!.size * 2
                boardSize = BoardSize.getByValue(numCards)
                gameName = createdGameName
                for (uri in userImageList.images) {
                    Picasso.get().load(uri).fetch()
                }
                setUpBoard()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "game download error", exception)
            }
    }

    private fun setUpBoard() {
        supportActionBar?.title = gameName ?: getString(R.string.app_name)
        when (boardSize) {
            BoardSize.EASY -> {
                tvNumParis.text = "Pairs: 0 / 4"
                tvNumMovies.text = "Easy 4 X 2"
            }
            BoardSize.MEDIUM -> {
                tvNumParis.text = "Pairs: 0 / 9"
                tvNumMovies.text = "Medium 6 X 3"
            }
            BoardSize.HARD -> {
                tvNumParis.text = "Pairs: 0 / 12"
                tvNumMovies.text = "Hard 6 X 6"
            }
        }

        tvNumParis.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize, createdGameImages)
        memoryGame.numPairsFound = 0

        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards,
            object : MemoryBoardAdapter.CardClickListener {
                override fun onCardClicked(position: Int) {
                    Log.i(TAG, "Position: $position")
                    Log.i(TAG, "${memoryGame.cards.first().imageUri} ................")
                    updateMemoryBoard(position)
                }
            })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun showCreationDialog() {

        val boardSizeView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        when (boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Create your own memory game", boardSizeView, View.OnClickListener {
            val desierdBoardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desierdBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })

    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater
            .from(this)
            .inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        when (boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }

        showAlertDialog("Chose new size", boardSizeView, View.OnClickListener {
            boardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setUpBoard()
        })
    }

    private fun showAlertDialog(
        title: String,
        view: View?,
        positiveClickListener: View.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun updateMemoryBoard(position: Int) {

        if (memoryGame.haveWonGame()) {
            //alert user with snackbar
            Snackbar.make(clRoot, "You already won", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position)) {
            //alert user wuth snackbar
            Snackbar.make(clRoot, "Invalid move", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (memoryGame.flipCard(position)) {
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full),
            ) as Int
            tvNumParis.setTextColor(color)
            tvNumParis.setText("Paris: ${memoryGame.numPairsFound}/${boardSize.getNumPairs()}")
            if (memoryGame.haveWonGame()) {
                CommonConfetti.rainingConfetti(
                    clRoot,
                    intArrayOf(Color.YELLOW, Color.GREEN, Color.BLUE)
                ).oneShot()
                Snackbar.make(clRoot, "You won", Snackbar.LENGTH_LONG).show()
            }
        }
        tvNumMovies.setText("Movies: ${memoryGame.getNumPairs()}")
        adapter.notifyDataSetChanged()
    }
}