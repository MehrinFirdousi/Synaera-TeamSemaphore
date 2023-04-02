package com.example.synaera

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synaera.databinding.FragmentFilesBinding
import okhttp3.internal.notifyAll
import java.io.File
import kotlin.math.roundToInt

class FilesFragment() : Fragment() {

    private var list: ArrayList<VideoItem> = ArrayList()
    var mAdapter = VideoRecyclerAdapter(list)
    private lateinit var binding : FragmentFilesBinding

    constructor(list : ArrayList<VideoItem>) : this() {
        this.list = list
    }

    companion object {
        @JvmStatic
        fun newInstance(list : ArrayList<VideoItem>) : FilesFragment {
            return FilesFragment(list)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.videoRV.layoutManager = layoutManager

        binding.videosDeleteButton.visibility = View.GONE
        mAdapter = VideoRecyclerAdapter(list)
        binding.videoRV.adapter = mAdapter
        val mainActivity = requireActivity() as MainActivity

        binding.uploadButton.setOnClickListener {
            if (list.size > 0) {
                if (list[0].deleteMode) {
                    return@setOnClickListener
                }
            }
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_PICK
            mainActivity.selectVideoIntent.launch(intent)
        }
        binding.videosEditButton.setOnClickListener {
            if (list.size > 0){
                if (list[0].deleteMode) {
                    binding.videosEditButton.setImageResource(R.drawable.outline_edit_24)
                    for (i in 0 until list.size) {
                        setDeleteMode(false, i)
                    }
                    slideView(binding.videosDeleteButton, dpToPx(48), 1)
                    binding.videosDeleteButton.visibility = View.GONE
                    mAdapter.selectedValues.clear()
                }
                else { // enable video selection
                    binding.videosEditButton.setImageResource(R.drawable.outline_cancel_24)
                    for (i in 0 until list.size) {
                        setDeleteMode(true, i)
                    }
                    binding.videosDeleteButton.visibility = View.VISIBLE
                    slideView(binding.videosDeleteButton, 1, dpToPx(48))
                }
            }
        }
        binding.videosDeleteButton.setOnClickListener {
            Log.d("FILES", "delete button clicked")
            if (mAdapter.selectedValues.size == 0)
                Toast.makeText(context, "No videos selected", Toast.LENGTH_SHORT).show()
            for (pos in mAdapter.selectedValues) {
                Log.d("FILES", "deleting $pos")
                list.removeAt(pos)
                mAdapter.notifyItemRemoved(pos)
            }
            mAdapter.selectedValues.clear()
            if (list.size == 0) {
                binding.videosEditButton.setImageResource(R.drawable.outline_edit_24)
                slideView(binding.videosDeleteButton, dpToPx(48), 1)
                binding.videosDeleteButton.visibility = View.GONE
                mAdapter.selectedValues.clear()
            }
        }
    }

    private fun scrollToPos(pos: Int) {
        (binding.videoRV.layoutManager as LinearLayoutManager).scrollToPosition(pos)
    }
    private fun slideView(view: View, currentHeight: Int, newHeight: Int) {
        val slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(200)
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

    fun dpToPx(dp: Int): Int {
        val density: Float = resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    fun addItem (item: VideoItem) {
        list.add(item)
        mAdapter.notifyItemInserted(list.size - 1)
        scrollToPos(list.size - 1)

    }
    fun changeStatus(status : String) {
        val lastPos = list.size - 1
        if (lastPos < 0)
            return
        list[lastPos].status = status
        mAdapter.notifyItemChanged(lastPos)
    }

    fun addTranscript(transcript: String) {
        val lastPos = list.size - 1
        if (lastPos < 0)
            return
        list[lastPos].transcript = transcript
        mAdapter.notifyItemChanged(lastPos)
    }

    private fun setDeleteMode(mode: Boolean, position: Int) {
        list[position].deleteMode = mode
        mAdapter.notifyItemChanged(position)
    }
}