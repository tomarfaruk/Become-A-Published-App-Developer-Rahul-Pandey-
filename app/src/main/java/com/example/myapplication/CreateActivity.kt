package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.models.BoardSize
import com.example.myapplication.utils.EXTRA_BOARD_SIZE
import com.example.myapplication.utils.EXTRA_GAME_NAME
import com.example.myapplication.utils.isPermissionGrand
import com.example.myapplication.utils.requestPermission
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.rkpandey.mymemory.utils.BitmapScaler
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {
    private var numImageRequired = -1
    private lateinit var boardSize: BoardSize
    private val chosenImageUris = mutableListOf<Uri>()

    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button
    private lateinit var adapter: ImagePickerAdapter

    private val storage = Firebase.storage
    private val db = Firebase.firestore


    companion object {
        private const val TAG = "CreateActivity"
        private const val PICK_PHOTO_CODE = 665
        private const val REQUEST_PHOTO_CODE = 200
        private const val MAX_GAME_NAME_LENGTH = 14
        private const val MIN_GAME_NANE_LENGTH = 3
        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etDownloadGameName)
        btnSave = findViewById(R.id.btnSave)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImageRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics (0 / $numImageRequired)"

        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGTH))

        btnSave.setOnClickListener {
            saveDataToFirebase()
        }
        etGameName.setText("test")

        etGameName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                btnSave.isEnabled = shouldEnable()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        adapter = ImagePickerAdapter(
            this,
            chosenImageUris,
            boardSize,
            object : ImagePickerAdapter.ImageClickListener {
                override fun onPlaceholderClicked() {
                    if (isPermissionGrand(this@CreateActivity, READ_PHOTOS_PERMISSION)) {
                        launchIntentPhotos()
                    } else {
                        requestPermission(
                            this@CreateActivity,
                            READ_PHOTOS_PERMISSION,
                            REQUEST_PHOTO_CODE
                        )
                    }
                }
            })
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())
        rvImagePicker.adapter = adapter
    }

    private fun saveDataToFirebase() {
        val customGameName = etGameName.text.toString()
        db.collection("games").document(customGameName).get()
            .addOnSuccessListener { document ->
                if (document != null && document?.data != null) {
                    AlertDialog.Builder(this)
                        .setTitle("Name taken")
                        .setMessage("A game already exists with this name $customGameName. Please try another name")
                        .setPositiveButton("OK", null)
                        .show()
                    btnSave.isEnabled = true
                } else {
                    handleImageUploading(customGameName)
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "error on saving data")
                Toast.makeText(this, "Error on saving game", Toast.LENGTH_LONG).show()
                btnSave.isEnabled = true
            }
    }

    private fun handleImageUploading(gameName: String) {
        var didEncounterError = false
        val uploadImageUri = mutableListOf<String>()

        Log.i(TAG, "saveDataToFirebase")
        for ((index: Int, photoUri: Uri) in chosenImageUris.withIndex()) {
            val imageByteArray: ByteArray = getImageByteArray(photoUri)
            val filePath = "images/$gameName/${System.currentTimeMillis()}-${index}.jpg"
            val photoReference = storage.reference.child(filePath)
            photoReference.putBytes(imageByteArray).continueWithTask { photoUploadTask ->
                Log.i(TAG, "${photoUploadTask.result?.bytesTransferred}")
                photoReference.downloadUrl
            }.addOnCompleteListener { downloadTask ->
                if (!downloadTask.isSuccessful) {
                    Log.e(TAG, "Exception with Firestore ", downloadTask.exception)
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_LONG).show()
                    didEncounterError = true
                    return@addOnCompleteListener
                }
                if (didEncounterError) {
                    return@addOnCompleteListener
                }
                val downloadUri = downloadTask.result.toString()
                uploadImageUri.add(downloadUri)
                Log.i(
                    TAG,
                    "Finished upload $downloadUri, uploaded image number ${uploadImageUri.size}"
                )

                if (uploadImageUri.size == chosenImageUris.size) {
                    handleAllImagesUploaded(gameName, uploadImageUri)
                }
            }
        }
    }

    private fun handleAllImagesUploaded(gameName: String, imageUri: MutableList<String>) {
        Log.i(TAG, "all images upload done.................")
        db.collection("games").document(gameName).set(mapOf("images" to imageUri))
            .addOnCompleteListener { gameCreationTask ->
                btnSave.isEnabled = true
                if (!gameCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception with game creation")
                    Toast.makeText(this, "Game creation failed", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }
                AlertDialog.Builder(this)
                    .setTitle("$gameName game creation success. Let's play!")
                    .setPositiveButton("OK") { _, _ ->
                        val intent = Intent()
                        intent.putExtra(EXTRA_GAME_NAME, gameName)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }.show()
            }
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {
        var originalBitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)

        Log.i(TAG, "Original width ${originalBitmap.width} and height ${originalBitmap.height}")
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        Log.i(TAG, "Scaled width ${scaledBitmap.width} and height ${scaledBitmap.height}")
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != PICK_PHOTO_CODE || resultCode != Activity.RESULT_OK || data == null) {
            Log.i(TAG, "User no select photos")
            return
        }
        val selectedUri = data.data
        val clipData = data.clipData

        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                val itemClip = clipData.getItemAt(i)
                if (chosenImageUris.size < numImageRequired) {
                    chosenImageUris.add(itemClip.uri)
                } else {
                    break
                }
            }
        } else if (selectedUri != null) {
            chosenImageUris.add(selectedUri)
        }
        adapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose pics ${chosenImageUris.size} / $numImageRequired"
        btnSave.isEnabled = shouldEnable()
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun shouldEnable(): Boolean {
        if (chosenImageUris.size != numImageRequired) {
            return false
        } else if (etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_NANE_LENGTH) {
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PHOTO_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchIntentPhotos()
            } else {
                Toast.makeText(
                    this,
                    "In order to create custom game, you need to provide photo read permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchIntentPhotos() {
        Log.i(TAG, "click photos")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Choose PICS"), PICK_PHOTO_CODE)
    }
}