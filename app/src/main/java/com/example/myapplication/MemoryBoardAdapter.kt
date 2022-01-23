package com.example.myapplication

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.models.BoardSize
import com.example.myapplication.models.MemoryCard
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>,
    private val cardClickListener: CardClickListener
) :
    RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    companion object{
        private const val  MARGIN_SIZE=10
        private const val  TAG="MemoryBoardAdaper"
    }

    interface CardClickListener{
        fun onCardClicked(position:Int)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            MemoryBoardAdapter.ViewHolder {
        val cardWidth = parent.width/boardSize.getWidth() - (2*MARGIN_SIZE)
        val cardHeight = parent.height/boardSize.getHeight() -(2*MARGIN_SIZE)
        val cardSideLenght = min(cardHeight,cardWidth)

       val view =LayoutInflater.from(context).inflate(R.layout.memory_card,parent, false)

        val cardLayoutParam = view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        cardLayoutParam.width=cardSideLenght
        cardLayoutParam.height=cardSideLenght
        cardLayoutParam.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:  ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int{
        return boardSize.numCard
    }


    inner class  ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        private val imageButton =itemView.findViewById<ImageButton>(R.id.imageButton)

        fun  bind(position: Int){
            val card = cards[position]
            imageButton.setImageResource(if(card.isFaceUp) card.identifier else R.drawable.ic_launcher_background)
            imageButton.alpha=if(card.isMatched) 0.4f else 1.0f

            val colorStaleLess = if(card.isMatched) ContextCompat.getColorStateList(context, R.color.color_grey) else null
            ViewCompat.setBackgroundTintList(imageButton, colorStaleLess)

            imageButton.setOnClickListener{
                Log.i(TAG,"Click position $position")
                cardClickListener.onCardClicked(position)
            }
        }
    }
}