package com.example.synaera

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract.Data
import android.speech.tts.TextToSpeech
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterViewFlipper
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.example.synaera.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.*
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), ServerResultCallback, IVideoFrameExtractor, TextToSpeech.OnInitListener {
    lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService

//    private var url : String = "http://192.168.1.16:5000/sendImg"
//    private var url : String = "http://synaera-api.centralindia.cloudapp.azure.com:5000/sendImg"

    private var translationOngoing : Boolean = false
    private var cameraFacing : Int = CameraSelector.LENS_FACING_FRONT
    private var chatList = ArrayList<ChatBubble>()
    private var videoList = ArrayList<VideoItem>()
    private lateinit var chatFragment: ChatFragment
    private lateinit var filesFragment: FilesFragment
    private lateinit var homeFragment: HomeFragment

    private lateinit var mServer: ServerClient
    private lateinit var mCameraPreview: PreviewView
//    private lateinit var mCameraPreview2: PreviewView

    // Camera Use-Cases
    private var mPreview : Preview? = null
    private var mPreview2 : Preview? = null

    private val mTargetWidth = 640
    private val mTargetHeight = 480
    private var mLastTime: Long = 0
    private val mUploadDelay: Long = 100
    private var mIsStreaming = false

    private var isRealText = false
    private lateinit var circleView: CircleView
    private lateinit var recordButton: CardView
    private lateinit var handler: Handler
    private var runnable: Runnable? = null
    private var recordAnimation: ButtonAnimator = ButtonAnimator()
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var videoThumbnail: Bitmap
    private var videoFrames = ArrayList<ByteArray>()
    lateinit var selectVideoIntent : ActivityResultLauncher<Intent>
    private var transcriptGenerated : Boolean = false

    private var tts: TextToSpeech? = null
    private var soundOn: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        mServer = ServerClient.getInstance()
        mServer.init("user", "pass", "20.193.159.90", 5000)
        mServer.connect()

//        mCameraPreview2 = viewBinding.viewFinder2
        mCameraPreview = viewBinding.viewFinder

        //Info screen
        viewBinding.infoButton.setOnClickListener{
            viewBinding.infoBg.visibility = View.VISIBLE
            viewBinding.personOutline.visibility = View.VISIBLE
            viewBinding.guideMsg.visibility = View.VISIBLE
        }

        viewBinding.infoBg.setOnClickListener{
            viewBinding.infoBg.visibility = View.GONE
            viewBinding.personOutline.visibility = View.GONE
            viewBinding.guideMsg.visibility = View.GONE
        }

        val db = DatabaseHelper(this)

        /** sender = true for system, false for user */
//        chatList.add(ChatBubble("Hello", true))
//        chatList.add(ChatBubble("hi", false))

        /** list for the videos*/
//        videoList.add(VideoItem("Video1", "Processing...", getDummyBitmap(100,100,123) ,"123", false))
//        videoList.add(VideoItem("Video2", "View Transcript", getDummyBitmap(120,120,50) ,"123", false))

        chatFragment = ChatFragment.newInstance(chatList)
        filesFragment = FilesFragment.newInstance(videoList)
        homeFragment = HomeFragment.newInstance(db)
        tts = TextToSpeech(this, this)

        // Request camera permissions
        if (allPermissionsGranted()) {
//            startCameraPreview2()
            startCameraPreview()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up adapters and bottom navigation
        setViewPagerAdapter()
        setBottomNavigation()
        setViewPagerListener()
//        viewBinding.bottomNavBar.selectedItemId = R.id.camera_menu_id

        // allow video selection from gallery
        selectVideoIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val dataUri = data?.data
                if (dataUri != null) {
                    val uriPathHelper = URIPathHelper()
                    val videoInputPath = uriPathHelper.getPath(this, dataUri).toString()
                    val videoInputFile = File(videoInputPath)
//                    setThumbnailRecentVideo()
                    Log.d(TAG, "videoInputPath=$videoInputPath")
                    val frameExtractor = FrameExtractor(this)
                    executorService.execute {
                        try {
                            frameExtractor.extractFrames(videoInputFile.absolutePath)
//                            this.runOnUiThread {
//                                filesFragment.changeStatus("Extraction complete")
//                            }
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            Log.d(TAG, "Failed!!!")
                            this.runOnUiThread {
                                Toast.makeText(
                                    this,
                                    "Failed to extract frames",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    viewBinding.bottomNavBar.selectedItemId = R.id.gallery_menu_id // switches to gallery fragment

                } else {
                    Toast.makeText(this, "Video input error!", Toast.LENGTH_LONG).show()
                }

            }

        }

        // Set up the listeners for record, flip camera and open gallery buttons
        viewBinding.openGalleryButton.setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_PICK
            selectVideoIntent.launch(intent)
        }

        viewBinding.flipCameraButton.setOnClickListener {
            if (translationOngoing) {
                stopStreaming()
                circleView.animateRadius(
                    circleView.getmMinRadius(),
                    circleView.getmMinStroke()
                )
                recordAnimation.stopAnimationOfSquare(resources, recordButton)
                handler.removeCallbacks(runnable!!)
                recordAnimation.resetAnimation(circleView)
            }
            if (cameraFacing == CameraSelector.LENS_FACING_FRONT)
                cameraFacing = CameraSelector.LENS_FACING_BACK
            else
                cameraFacing = CameraSelector.LENS_FACING_FRONT
//            startCameraPreview2()
            startCameraPreview()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        viewBinding.muteButton.setOnClickListener {
            if (soundOn) {
                viewBinding.muteButton.setImageResource(R.drawable.outline_volume_off_24)
            }
            else {
                viewBinding.muteButton.setImageResource(R.drawable.outline_volume_up_24)
            }
            soundOn = !soundOn
        }

        circleView = viewBinding.recordCircle
        recordButton = viewBinding.recordButton

        circleView.setOnClickListener {
            viewBinding.openGalleryButton.visibility = View.VISIBLE

            if (!translationOngoing) {
                startStreaming()
                recordAnimation.startAnimationOfSquare(resources, circleView, recordButton)
                circleView.animateRadius(
                    circleView.getmMaxRadius(),
                    circleView.getmMinStroke()
                )
                handler.postDelayed(runnable!!, 80)
                slideView(viewBinding.bottomNavBar, viewBinding.bottomNavBar.layoutParams.height, 1)
                viewBinding.openGalleryButton.visibility = View.INVISIBLE
                viewBinding.flipCameraButton.visibility = View.INVISIBLE
                viewBinding.infoButton.visibility = View.INVISIBLE
                viewBinding.muteButton.visibility = View.INVISIBLE
            }
            else {
                stopStreaming()
                circleView.animateRadius(
                    circleView.getmMinRadius(),
                    circleView.getmMinStroke()
                )
                recordAnimation.stopAnimationOfSquare(resources, recordButton)
                handler.removeCallbacks(runnable!!)
                recordAnimation.resetAnimation(circleView)
                slideView(viewBinding.bottomNavBar, 1, dpToPx(55))
                viewBinding.openGalleryButton.visibility = View.VISIBLE
                viewBinding.flipCameraButton.visibility = View.VISIBLE
                viewBinding.infoButton.visibility = View.VISIBLE
                viewBinding.muteButton.visibility = View.VISIBLE
            }
            translationOngoing = !translationOngoing
        }

        recordAnimation.resetAnimation(circleView)

        handler = Handler()
        runnable = Runnable {
            recordAnimation.animateStroke(circleView)
            handler.postDelayed(runnable!!, 130)
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        mServer.registerCallback(this)
        mServer.connect()
//        setThumbnailRecentVideo()
    }
    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        if (mIsStreaming) stopStreaming()
//        mServer!!.unregisterCallback()
//        mServer!!.disconnect()
    }

    private fun slideView(view: View, currentHeight: Int, newHeight: Int) {
        val slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(400)
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

    private fun dpToPx(dp: Int): Int {
        val density: Float = resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    private fun setViewPagerListener() {
        viewBinding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when(position) {
                    0 -> {
                        viewBinding.viewPager.currentItem = 0
                        homeFragment.updateName()
                        disableCameraButtons()
                        viewBinding.bottomNavBar.selectedItemId = R.id.home_menu_id
                    }
                    1 -> {
                        viewBinding.viewPager.currentItem = 1
                        disableCameraButtons()
                        viewBinding.bottomNavBar.selectedItemId = R.id.gallery_menu_id
                    }
                    2 -> {
                        viewBinding.viewPager.currentItem = 2
                        enableCameraButtons()
                        viewBinding.bottomNavBar.selectedItemId = R.id.camera_menu_id
                    }
                    3 -> {
                        viewBinding.viewPager.currentItem = 3
                        disableCameraButtons()
                        viewBinding.bottomNavBar.selectedItemId = R.id.chat_menu_id
                    }
                    4 -> {
                        viewBinding.viewPager.currentItem = 4
                        disableCameraButtons()
                        viewBinding.bottomNavBar.selectedItemId = R.id.profile_menu_id
                    }
                    else -> viewBinding.viewPager.currentItem = 2
                }
                super.onPageSelected(position)
            }
        })
    }

    private fun setBottomNavigation() {
        viewBinding.bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home_menu_id -> {
                    if (viewBinding.viewPager.currentItem != 0) {
                        viewBinding.viewPager.setCurrentItem(0, false)
                        disableCameraButtons()
                    }
                }
                R.id.gallery_menu_id -> {
                    if (viewBinding.viewPager.currentItem != 1) {
                        viewBinding.viewPager.setCurrentItem(1, false)
                        disableCameraButtons()
                    }
                }
                R.id.camera_menu_id -> {
                    if (viewBinding.viewPager.currentItem != 2) {
                        viewBinding.viewPager.setCurrentItem(2, false)
                        enableCameraButtons()
                    }
                }
                R.id.chat_menu_id -> {
                    if (viewBinding.viewPager.currentItem != 3) {
                        viewBinding.viewPager.setCurrentItem(3, false)
                        disableCameraButtons()
                    }
                }
                R.id.profile_menu_id -> {
                    if (viewBinding.viewPager.currentItem != 4) {
                        viewBinding.viewPager.setCurrentItem(4, false)
                        disableCameraButtons()
                    }
                }
                else -> viewBinding.viewPager.currentItem = 2
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun disableCameraButtons() {
        circleView.visibility = View.INVISIBLE
        recordButton.visibility = View.INVISIBLE
        viewBinding.openGalleryButton.visibility = View.INVISIBLE
        viewBinding.flipCameraButton.visibility = View.INVISIBLE
        viewBinding.infoButton.visibility = View.INVISIBLE
        viewBinding.muteButton.visibility = View.INVISIBLE
        viewBinding.textView.visibility = View.INVISIBLE
    }

    private fun enableCameraButtons() {
        circleView.visibility = View.VISIBLE
        recordButton.visibility = View.VISIBLE
        viewBinding.openGalleryButton.visibility = View.VISIBLE
        viewBinding.flipCameraButton.visibility = View.VISIBLE
        viewBinding.infoButton.visibility = View.VISIBLE
        viewBinding.muteButton.visibility = View.VISIBLE
        if (viewBinding.textView.text.isNotEmpty())
            viewBinding.textView.visibility = View.VISIBLE
    }

    private fun setViewPagerAdapter() {
        val curItem = viewBinding.viewPager.currentItem
        Log.d(TAG, "current item in viewpager = $curItem")
        viewBinding.viewPager.adapter = ViewPagerAdapter(this, chatFragment, filesFragment, homeFragment)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
    }

    companion object {
        private const val TAG = "SynaeraApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
//                startCameraPreview2()
                startCameraPreview()
//                setThumbnailRecentVideo()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCameraPreview() {
        Log.d(TAG, "startCameraPreview")
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                // do nothing
            } catch (_: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(this))
    }

//    private fun startCameraPreview2() {
//        Log.d(TAG, "startCameraPreview")
//        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
//            ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            try {
//                val cameraProvider = cameraProviderFuture.get()
//                bindPreview2(cameraProvider)
//            } catch (e: ExecutionException) {
//                // do nothing
//            } catch (_: InterruptedException) {
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        mPreview = Preview.Builder().build()

        mPreview!!.setSurfaceProvider(mCameraPreview.createSurfaceProvider())
        val cameraSelector: CameraSelector = if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        cameraProvider.unbindAll()
//        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, mPreview)
    }

//    private fun bindPreview2(cameraProvider: ProcessCameraProvider) {
//        mPreview2 = Preview.Builder().build()
//
//        mPreview2!!.setSurfaceProvider(mCameraPreview2.createSurfaceProvider())
//        val cameraSelector: CameraSelector = if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
//            CameraSelector.DEFAULT_FRONT_CAMERA
//        } else {
//            CameraSelector.DEFAULT_BACK_CAMERA
//        }
//        cameraProvider.unbindAll()
////        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
//        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, mPreview2)
//    }

    private fun startStreaming() {
        mIsStreaming = true
        viewBinding.textView.text = ""
        viewBinding.textView.visibility = View.INVISIBLE
        if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
            Thread {
                while (mIsStreaming) {
                    val elapsedTime: Long = System.currentTimeMillis() - mLastTime
                    if (elapsedTime > mUploadDelay && mUploadDelay != 0L) {   // Bound the image upload based on the user-defined frequency
                        var byteArray: ByteArray
                        val bmp = mCameraPreview.bitmap
                        val bmpScaled = Bitmap.createScaledBitmap(bmp!!, mTargetWidth, mTargetHeight, false)
                        val bmpFlipped = bmpScaled.flipHorizontally()
                        byteArray = ImageConverter.BitmaptoJPEG(bmpFlipped)
                        mServer.sendImage(byteArray)
                        mLastTime = System.currentTimeMillis()
                    }
                }
            }.start()
        }
        else {
            Thread {
                while (mIsStreaming) {
                    val elapsedTime: Long = System.currentTimeMillis() - mLastTime
                    if (elapsedTime > mUploadDelay && mUploadDelay != 0L) {   // Bound the image upload based on the user-defined frequency
                        var byteArray: ByteArray
                        val bmp = mCameraPreview.bitmap
                        val bmpScaled = Bitmap.createScaledBitmap(bmp!!, mTargetWidth, mTargetHeight, false)
                        byteArray = ImageConverter.BitmaptoJPEG(bmpScaled)
                        mServer.sendImage(byteArray)
                        mLastTime = System.currentTimeMillis()
                    }
                }
            }.start()
        }
    }

    private fun stopStreaming() {
        mIsStreaming = false
        mServer.getPrediction()
    }

    private fun Bitmap.flipHorizontally(): Bitmap {
        val matrix = Matrix().apply { postScale(-1f, 1f, width / 2f, height / 2f) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    override fun onConnected(success: Boolean) {
        Log.d(TAG, "ServerResultCallback-onConnected: $success")
    }

    override fun displayResponse(result: String, isGloss: Boolean) {
        Log.d(TAG, "displayResponse in main act: $result")
        runOnUiThread {
            viewBinding.textView.visibility = View.VISIBLE
            if (isGloss) {
                if (viewBinding.textView.text.length < 40 && !isRealText) {
                    viewBinding.textView.append(" $result")
                    speakOut(result)
                }
                else {
                    viewBinding.textView.text = result
                    speakOut(result)
                }
                isRealText = false
            } else {
                chatFragment.addItem(ChatBubble(result, true))
                viewBinding.textView.text = result
                speakOut(result)
                isRealText = true
            }
        }
    }

    override fun addNewTranscript(result: String?) {
        if (result != null) {
            Log.d(TAG, "result is $result")
            if (result.isNotEmpty() || result.compareTo("") != 0) {
                transcriptGenerated = true
                runOnUiThread {
                    filesFragment.addTranscript(result)
                }
            }
        }
    }

    private fun fromBufferToBitmap(buffer: ByteBuffer, width: Int, height: Int): Bitmap? {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        buffer.rewind()
        result.copyPixelsFromBuffer(buffer)
        val transformMatrix = Matrix()
        val outputBitmap = Bitmap.createBitmap(result, 0, 0, result.width, result.height, transformMatrix, false)
        outputBitmap.density = DisplayMetrics.DENSITY_DEFAULT
        return outputBitmap
    }
    override fun onCurrentFrameExtracted(currentFrame: Frame, decodeCount: Int) {
//        Thread {
            // 1. Convert frame byte buffer to bitmap
        val imageBitmap = fromBufferToBitmap(currentFrame.byteBuffer, currentFrame.width, currentFrame.height)
        val byteArray = ImageConverter.BitmaptoJPEG(imageBitmap)

        videoFrames.add(byteArray)
//        mServer.sendVideoFrame(byteArray)
//        mLastTime = System.currentTimeMillis()
        if (decodeCount == 0) {
            videoThumbnail = imageBitmap!!
            this.runOnUiThread {
                val itemCount = filesFragment.mAdapter.itemCount + 1
                filesFragment.addItem(VideoItem("Video$itemCount", "Processing...", videoThumbnail, "", false))
                viewBinding.openGalleryButton.setImageBitmap(videoThumbnail)
            }
        }
    }

    override fun onAllFrameExtracted(processedFrameCount: Int, processedTimeMs: Long) {
        this.runOnUiThread {
            filesFragment.changeStatus("Uploading...")
        }
        mIsStreaming = true
        Thread {
            var frameNo = 0
            while (mIsStreaming && frameNo < processedFrameCount) {
                val elapsedTime: Long = System.currentTimeMillis() - mLastTime
                if (elapsedTime > mUploadDelay && mUploadDelay != 0L) {
                    mServer.sendVideoFrame(videoFrames[frameNo++], processedFrameCount)
                    mLastTime = System.currentTimeMillis()
                    Log.d(TAG, "sending frame $frameNo")
                }
            }
            this.runOnUiThread {
                filesFragment.changeStatus("Translating...")
            }
            println("suspending execution")
            Thread.sleep(15000)
            println("resuming execution")

            mServer.checkTranscript()
            mLastTime = System.currentTimeMillis()
            while (!transcriptGenerated) {
                val elapsedTime: Long = System.currentTimeMillis() - mLastTime
                if (elapsedTime > 3000) {
                    mServer.checkTranscript()
                    mLastTime = System.currentTimeMillis()
                    Log.d(TAG, "Checking for transcript...")
                }
            }

            mIsStreaming = false
            this.runOnUiThread {
                filesFragment.changeStatus("Transcript generated")
            }
            videoFrames.clear()
            transcriptGenerated = false
        }.start()
        Log.d(TAG, "Save: $processedFrameCount frames in: $processedTimeMs ms.")
    }
    private fun getDummyBitmap(
        targetWidth: Int, targetHeight: Int,
        color: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            targetWidth, targetHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        canvas.drawPaint(paint)
        return bitmap
    }

    private fun speakOut(text: String) {
        if (soundOn) {
            tts!!.speak(text, TextToSpeech.QUEUE_ADD, null, "")
        }
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language for text to speech not supported!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "TextToSpeech initialization failed")
        }
    }
}
