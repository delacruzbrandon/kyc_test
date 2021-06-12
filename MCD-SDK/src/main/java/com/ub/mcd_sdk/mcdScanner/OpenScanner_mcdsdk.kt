package com.ub.mcd_sdk.mcdScanner

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.PictureResult
import com.ub.mcd_sdk.R
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_open_scanner_mcdsdk.*
import java.io.File


class OpenScanner_mcdsdk : AppCompatActivity() {
    private val TAKE_PICTURE = 1
    private val SELECT_PICTURE = 2
    private val REQUEST_WRITE_EXTERNAL_STORAGE = 3
    var isFront = false
    var imageText = ""
    private var mLastClickTime: Long = 0
    private lateinit var token: String
    var headers = HashMap<String, String>()
    var bodyMap = HashMap<String, Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_scanner_mcdsdk)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        headers = intent.getSerializableExtra("headers") as HashMap<String, String>
        bodyMap = intent.getSerializableExtra("bodyMap") as HashMap<String, Any>

        if (bodyMap["isFrontImage"]!! == true) {
            imageScan.setText("Front of Check")
            isFront = true
        } else {
            imageScan.setText("Back of Check")
            isFront = false
        }
        initCameraView()
    }

    private fun initCameraView() {
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        with(camera) {
            setLifecycleOwner(this@OpenScanner_mcdsdk)
            addCameraListener(cameraViewListener)
        }
        onInitializeListener()
    }

    private var cameraViewListener = object : CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            result.toFile(File(filesDir, "$imageText.jpeg")) { file ->
                Compressor(this@OpenScanner_mcdsdk)
                        .setMaxWidth(1112)
                        .setMaxHeight(1316)
                        .setQuality(100)
                        .compressToFileAsFlowable(file)
                        .doOnSubscribe {
                            progress_bar.visibility = View.VISIBLE
                        }.doFinally {
                            progress_bar.visibility = View.GONE
                        }.subscribe({
                            launchNextScreen(it.path, isFront)
                        }, {
                            Toast.makeText(this@OpenScanner_mcdsdk, "$it", Toast.LENGTH_SHORT).show()
                        })
            }
        }


        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            Toast.makeText(this@OpenScanner_mcdsdk, "${exception}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        camera.addCameraListener(object : CameraListener() {})
    }

    fun launchNextScreen(filePath: String, isFront: Boolean) {
        val intent = Intent(this@OpenScanner_mcdsdk, CameraPreview_mcdsdk::class.java)
        intent.putExtra("filePath", filePath)
        intent.putExtra("headers", headers)
        intent.putExtra("bodyMap", bodyMap)
        startActivityForResult(intent, 1)
    }

    private fun onInitializeListener() {
        imageViewCapture.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            camera.takePicture()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            TAKE_PICTURE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeRequest()
            }
            REQUEST_WRITE_EXTERNAL_STORAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(
                        applicationContext,
                        "Please allow permission to use the external storage",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun makeRequest() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    TAKE_PICTURE
            )
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            SELECT_PICTURE
                    )
                } else {
                    openCamera()
                }
            } else {
                openCamera()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intent3 = Intent()
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                intent3.putExtra("CheckDetails", data!!.getStringExtra("CheckDetails"))
                setResult(Activity.RESULT_OK, intent3)
                finish()
            }
        }
    }
}