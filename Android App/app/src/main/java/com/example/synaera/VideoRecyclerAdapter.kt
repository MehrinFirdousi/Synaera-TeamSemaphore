package com.example.synaera

import android.R
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.synaera.databinding.VideoItemBinding
import kotlinx.coroutines.selects.select


open class VideoRecyclerAdapter(var items: ArrayList<VideoItem>):
    RecyclerView.Adapter<VideoRecyclerAdapter.ViewHolder>() {

    private lateinit var context : Context
    var selectedValues: ArrayList<Int> = ArrayList<Int>()

    inner class ViewHolder(b: VideoItemBinding) : RecyclerView.ViewHolder(b.root) {
        var binding = b

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context

        return ViewHolder(
            VideoItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }
        override fun getItemCount(): Int {
            return items.size
    }

    private fun slideView(view: View, currentHeight: Int, newHeight: Int) {
        val slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(350)
        slideAnimator.addUpdateListener { animation1 ->
            val value = animation1.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }

        val animationSet = AnimatorSet()
        animationSet.interpolator = AccelerateDecelerateInterpolator()
        animationSet.play(slideAnimator)
        animationSet.start()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currItem = items[position]

        holder.binding.videoThumbnail.setImageBitmap(currItem.bitmap)
        holder.binding.titleTextView.text = currItem.title
        holder.binding.statusTextView.text = currItem.status

        if (currItem.deleteMode) {
            holder.binding.videoProgressBar.visibility = View.GONE
            holder.binding.transcriptResult.visibility = View.GONE
            holder.binding.expandButton.visibility = View.GONE
            holder.binding.collapseButton.visibility = View.GONE

            holder.binding.expandCollapseButton.visibility = View.VISIBLE
            holder.binding.videoCheckBox.visibility = View.VISIBLE

            holder.binding.videoCheckBox.setOnClickListener {
                if(holder.binding.videoCheckBox.isChecked) {
                    selectedValues.add(position)
                    println("added $position to selected values")
                }
                else {
                    selectedValues.remove(position)
                    println("removed $position from selected values")
                }
            }
        }
        else {
            holder.binding.videoCheckBox.visibility = View.GONE
            holder.binding.expandCollapseButton.visibility = View.GONE
            if (holder.binding.videoProgressBar.isVisible)
                holder.binding.videoItemContainer.visibility = View.VISIBLE
//            holder.binding.expandButton.visibility = View.GONE
//            holder.binding.collapseButton.visibility = View.GONE
//            holder.binding.transcriptResult.visibility = View.GONE
        }

        if (!currItem.deleteMode) {
            if (currItem.transcript.isNotEmpty()) {
//                holder.binding.videoThumbnail.alpha = 1f
                holder.binding.videoProgressBar.visibility = View.GONE
                holder.binding.collapseButton.visibility = View.GONE
                holder.binding.transcriptResult.visibility = View.GONE
                holder.binding.expandCollapseButton.visibility = View.VISIBLE
                holder.binding.transcriptResult.text = currItem.transcript

                // get current height
                val widthSpec = MeasureSpec.makeMeasureSpec(holder.binding.videoDetails.width, MeasureSpec.EXACTLY)
                val heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                holder.binding.transcriptResult.measure(widthSpec, heightSpec)
                val height: Int = holder.binding.transcriptResult.measuredHeight

                // make expand button appear and set click listeners for expand and collapse
                holder.binding.expandButton.visibility = View.VISIBLE
                holder.binding.expandButton.setOnClickListener {
                    holder.binding.expandButton.visibility = View.GONE
                    holder.binding.collapseButton.visibility = View.VISIBLE
                    holder.binding.transcriptResult.visibility = View.VISIBLE
                    slideView(holder.binding.transcriptResult, 1, height)
                }
                holder.binding.collapseButton.setOnClickListener {
                    holder.binding.collapseButton.visibility = View.GONE
                    holder.binding.expandButton.visibility = View.VISIBLE
                    slideView(holder.binding.transcriptResult, height, 1)
                }
            }
            else {
//                holder.binding.expandButton.visibility = View.GONE
//                holder.binding.collapseButton.visibility = View.GONE
//                holder.binding.transcriptResult.visibility = View.GONE
//                holder.binding.videoThumbnail.alpha = 0.5f
                holder.binding.videoProgressBar.visibility = View.VISIBLE
            }
        }


    }

}