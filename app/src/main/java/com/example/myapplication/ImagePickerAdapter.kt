package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.models.BoardSize
import kotlin.math.min

class ImagePickerAdapter(
    private val context: Context,
    private val chosenImageUris: MutableList<Uri>,
    private val boardSize: BoardSize,
    private val imageClickListener: ImageClickListener,
    ): RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    interface ImageClickListener{
        fun onPlaceholderClicked()
    }

    companion object{
        private const val  MARGIN_SIZE = 8
        private const val  TAG="MemoryBoardAdaper"
    }

    inner class ViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        val ivCustomImage = itemView.findViewById<ImageView>(R.id.ivCustomImage)

        fun bind(uri: Uri) {
            ivCustomImage.setImageURI(uri)
            ivCustomImage.setOnClickListener(null)
        }
        fun bind() {
            ivCustomImage.setOnClickListener{
                imageClickListener.onPlaceholderClicked()
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePickerAdapter.ViewHolder {
        val view =LayoutInflater.from(context).inflate(R.layout.card_image,null)
        val cardWidth = parent.width/boardSize.getWidth() - (2* MARGIN_SIZE)
        val cardHeight = parent.height/boardSize.getHeight() -(2* MARGIN_SIZE)
        val cardSideLength = min(cardHeight,cardWidth)

        val layoutParams = view.findViewById<ImageView>(R.id.ivCustomImage).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       if(position<chosenImageUris.size){
           holder.bind(chosenImageUris[position])
       }else {
           holder.bind()
       }
    }

    override fun getItemCount()= boardSize.getNumPairs()

}
