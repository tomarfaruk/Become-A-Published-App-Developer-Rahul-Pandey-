package com.example.myapplication

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class MemoryBoardAdaper(private val context: Context,private val numPices: Int) :
    RecyclerView.Adapter<MemoryBoardAdaper.ViewHolder>() {

    companion object{
        private const val  MARGIN_SIZE=10
        private const val  TAG="MemoryBoardAdaper"
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryBoardAdaper.ViewHolder {
        val cardWidth = parent.width/2-(2*MARGIN_SIZE)
        val cardHeight = parent.height/4-(2*MARGIN_SIZE)
        val cardSideLenght = min(cardHeight,cardHeight)

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
        return numPices
    }

    inner class  ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        private val imageButton =itemView.findViewById<ImageButton>(R.id.imageButton)

        fun  bind(position: Int){
            imageButton.setOnClickListener{
                Log.i(TAG,"Click position $position")
            }
        }
    }

}


