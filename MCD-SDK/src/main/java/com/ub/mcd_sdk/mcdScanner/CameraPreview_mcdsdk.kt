package com.ub.mcd_sdk.mcdScanner

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.JsonObject
import com.ub.mcd_sdk.API.Client
import com.ub.mcd_sdk.R
import kotlinx.android.synthetic.main.activity_camera_preview_mcdsdk.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*
import kotlin.collections.HashMap

class CameraPreview_mcdsdk : AppCompatActivity() {
    private lateinit var currentCroppedBitmap: Bitmap

    private lateinit var filePath: String

    private var mLastClickTime: Long = 0

    private var headers = HashMap<String, String>()
    private var bodyMap = HashMap<String, Any>()

    private val DEFAULT_COMPRESS_QUALITY = 100
    private var isCroppedMode = false
    private var isfront = false

    private var imageName = ""

    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_preview_mcdsdk)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        filePath = intent.extras!!["filePath"].toString()
        file = File(filePath)

        headers = intent.getSerializableExtra("headers") as HashMap<String, String>
        bodyMap = intent.getSerializableExtra("bodyMap") as HashMap<String, Any>


        if (bodyMap["isFrontImage"]!!.equals(true)) {
            imageName = "Front"
            isfront = true
        } else {
            imageName = "Back"
            isfront = false
        }

        tv_done.setOnClickListener {
            onClickedDone()
        }
        tv_cancel.setOnClickListener {
            file.delete()
            finish()
        }
        onCroppingMode()
    }

    private fun onClickedDone() {
        if (imageViewCrop.canRightCrop()) {
            currentCroppedBitmap = imageViewCrop.crop()
            exitCroppingMode()
        }
    }

    private fun onCroppingMode() {
        isCroppedMode = true
        invalidateOptionsMenu()
        imageViewCrop.visibility = View.VISIBLE
        iv_preview.visibility = View.GONE
        useThisBtn.visibility = View.GONE
        tv_done.visibility = View.VISIBLE
        tv_edit.visibility = View.GONE

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inJustDecodeBounds = false
        imageViewCrop.setImageToCrop(BitmapFactory.decodeFile(filePath, options))
    }

    private fun exitCroppingMode() {
        isCroppedMode = false
        invalidateOptionsMenu()
        iv_preview.visibility = View.VISIBLE
        imageViewCrop.visibility = View.GONE
        useThisBtn.visibility = View.VISIBLE
        useThisBtn.isEnabled = true
        tv_done.visibility = View.GONE
        tv_edit.visibility = View.VISIBLE


        iv_preview.loadImage(currentCroppedBitmap)
        iv_preview.postDelayed(
                {
                    iv_preview.zoomOut()
                }, 100
        )

        tv_edit.setOnClickListener {
            onCroppingMode()
        }

        val path = saveFile(rotateImage(currentCroppedBitmap))

        file = File(path)

        val checkImage = MultipartBody.Part.createFormData(
                "image", file.name,
                RequestBody.create(MediaType.parse("image/*"), file)
        )

        useThisBtn.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            useThisBtn.isEnabled = false
            progress_bar.visibility = View.VISIBLE
            useThis(checkImage)
        }

    }

    private fun useThis(checkImage: MultipartBody.Part) {
        tv_cancel.isEnabled = false
        tv_edit.isEnabled = false

        val uniqueID: String = UUID.randomUUID().toString()
        val date = System.currentTimeMillis()
        val senderRefId = "$uniqueID-$date"

        val body = hashMapOf(
                "senderRefId" to senderRefId,
                "tranRequestDate" to bodyMap["tranRequestDate"].toString(),
                "deviceId" to bodyMap["deviceId"].toString(),
                "referenceId" to bodyMap["referenceId"].toString()
        )

        Client.getAPI.validateImage(headers, body, isfront, checkImage).enqueue(object :
                Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                progress_bar.visibility = View.GONE
                tv_cancel.isEnabled = true
                tv_edit.isEnabled = true
                showDialog(t!!.message.toString())
            }

            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                val intent2 = Intent()

                progress_bar.visibility = View.GONE
                tv_edit.isEnabled = false
                tv_cancel.isEnabled = true

                if (response!!.isSuccessful) {
                    Toast.makeText(
                            this@CameraPreview_mcdsdk,
                            "${response.body()["description"]}",
                            Toast.LENGTH_SHORT
                    ).show()
                    response.body().addProperty("imagePath", file.path)
                    intent2.putExtra("CheckDetails", response.body().toString())
                    setResult(RESULT_OK, intent2)
                    finish()

                } else {
                    val responseBody = response.errorBody().string()
                    val jsonObject = JSONObject(responseBody)
                    val errorArray = jsonObject.getJSONArray("errors")
                    val errorBody = errorArray.getJSONObject(0)

                    when (errorBody["code"]) {
                        500 -> {
                            showDialog(errorBody["description"].toString())
                        }

                        "409" -> {
                            val errorMessage = errorBody.getJSONObject("details")
                            showDialog(errorMessage["message"].toString())
                        }

                        409 -> {
                            val errorMessage = errorBody.getJSONObject("details")
                            showDialog(errorMessage["message"].toString())
                        }

                        -20 -> {
                            showDialog(errorBody["message"].toString())
                        }
                        else -> {
                            if (!response.message().isNullOrBlank()) {
                                showDialog(response.message())
                            } else {
                                val message = "Unexpected error occurred"
                                showDialog(message)
                            }
                        }
                    }
                    progress_bar.visibility = View.GONE
                    tv_cancel.isEnabled = true
                }
            }
        })

    }

    private fun saveFile(bitmap: Bitmap): String? {
        val file = saveBitmap(
                bitmap,
                filename = "${imageName}_CROPPED",
                format = Bitmap.CompressFormat.JPEG
        )
        return file?.path
    }

    private fun rotateImage(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(-90f)
        return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
        )
    }

    private fun saveBitmap(
            bitmap: Bitmap,
            directory: String? = null,
            filename: String,
            format: Bitmap.CompressFormat
    ): File? {
        var directory = directory
        var filename = filename
        if (directory == null) {
            directory = this.externalCacheDir!!.absolutePath
        } else {
            // Check if the given directory exists or try to create it.
            val file = File(directory)
            if (!file.isDirectory && !file.mkdirs()) {
                return null
            }
        }
        var file: File? = null
        var os: OutputStream? = null
        try {
            filename =
                    if (format == Bitmap.CompressFormat.PNG) "$filename.png" else "$filename.jpeg"
            file = File(directory, filename)
            os = FileOutputStream(file)
            bitmap.compress(format, DEFAULT_COMPRESS_QUALITY, os)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            closeStream(os)
        }
//        val  save = FileOutputStream(file)
//        save.flush()
//        save.close()
//        MediaStore.Images.Media.insertImage(contentResolver, bitmap,"frontImage ","yourDescription");
        return file
    }

    private fun closeStream(stream: Closeable?) {
        if (stream != null) {
            try {
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun ImageView.loadImage(image: Any?) = Glide.with(this)
            .asBitmap()
            .load(image)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(this)

    private fun showDialog(message: String) {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Error")
        alertDialog.setMessage(message)
        alertDialog.setCancelable(false)
        alertDialog.setButton(
                AlertDialog.BUTTON_POSITIVE,
                "Retake"
        ) { dialog, which ->
            file.delete()
            finish()
        }

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Edit") { dialog, which ->
            onCroppingMode()
            dialog.dismiss()
        }
        alertDialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        file.delete()
    }
}