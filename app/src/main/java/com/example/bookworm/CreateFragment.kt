package com.example.bookworm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CreateFragment : Fragment() {
    private lateinit var bookImageView: ImageView
    private lateinit var bookTitleInput: TextInputEditText
    private lateinit var uploadBookButton: Button
    private lateinit var booksReadView: TextView
    private var photoPath: String = ""
    private var selectedImageUri: Uri? = null

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val PREFS_NAME = "BookWormPrefs"
        private const val KEY_BOOKS_UPLOADED = "books_uploaded"
    }

    private fun getSharedPreferences() =
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun updateBooks() {
        val prefs = getSharedPreferences()
        val currentCount = prefs.getInt(KEY_BOOKS_UPLOADED, 0)
        prefs.edit().putInt(KEY_BOOKS_UPLOADED, currentCount + 1).apply()
        val bookCount = getSharedPreferences().getInt(KEY_BOOKS_UPLOADED, 0)
        booksReadView.text = "Books Read: $bookCount"
    }

    private fun incrementBooksUploaded() {
        val prefs = getSharedPreferences()
        val currentCount = prefs.getInt(KEY_BOOKS_UPLOADED, 0)
        prefs.edit().putInt(KEY_BOOKS_UPLOADED, currentCount + 1).apply()
        updateBooksReadCount()
    }

    private fun getBooksUploadedCount(): Int {
        return getSharedPreferences().getInt(KEY_BOOKS_UPLOADED, 0)
    }

    private fun updateBooksReadCount() {
        val bookCount = getBooksUploadedCount()
        booksReadView.text = "Books Read: $bookCount"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookImageView = view.findViewById(R.id.imageView)
        bookTitleInput = view.findViewById(R.id.edtBookTitle)
        uploadBookButton = view.findViewById(R.id.btnUpload)
        booksReadView = view.findViewById(R.id.tvBooksRead)

        updateBooksReadCount()

        view.findViewById<View>(R.id.btnTakePhoto).setOnClickListener {
            checkCameraPermission()
        }

        view.findViewById<View>(R.id.btnGallery).setOnClickListener {
            checkGalleryPermission()
        }

        uploadBookButton.setOnClickListener {
            uploadBook()
        }

    }


private fun uploadBook() {
    val bookData = getBookData()
    if (bookData != null) {
        uploadBookButton.isEnabled = false

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "book_images/${timestamp}_${UUID.randomUUID()}.jpg"

        val imageRef = storage.reference.child(imageFileName)

        imageRef.putFile(bookData.imageUri)
            .addOnSuccessListener { _ ->
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val book = hashMapOf(
                        "title" to bookData.title,
                        "imageUrl" to downloadUri.toString(),
                        "createdAt" to Date(),
                        "userUploadCount" to getBooksUploadedCount() + 1
                    )

                    firestore.collection("books")
                        .add(book)
                        .addOnSuccessListener { _ ->
//                            val intent = Intent(requireContext(), MainActivity::class.java).apply {
//                                putExtra("BOOK_TITLE", bookData.title)
//                            }
//                            startActivity(intent)
//                            parentFragmentManager.setFragmentResult("book_upload", bundleOf(
//                                "BOOK_TITLE" to bookData.title
//                            ))
// Then navigate to favorites
                            //findNavController().navigate(R.id.action_createFragment_to_favoritesFragment)
                            updateBooks()
                            clearForm()
                        }
                        .addOnFailureListener { e ->
                            uploadBookButton.isEnabled = true
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to upload image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                uploadBookButton.isEnabled = true
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                uploadBookButton.text = "Uploading ${progress.toInt()}%"
            }
    }
}



    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            takePicture()
        } else {
            Toast.makeText(requireContext(), "Require camera permission ", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(requireContext(), "Require gallery permission", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = Uri.parse(photoPath)
            bookImageView.setImageURI(selectedImageUri)
            checkUploadButtonVisibility()
        }
    }


    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                bookImageView.setImageURI(uri)
                checkUploadButtonVisibility()
            }
        }
    }

    private fun clearForm() {
        bookTitleInput.text?.clear()
        bookImageView.setImageResource(android.R.color.transparent)
        selectedImageUri = null
        uploadBookButton.isEnabled = true
        uploadBookButton.text = "Upload"
        checkUploadButtonVisibility()
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }
    // pass through to model

    private fun checkUploadButtonVisibility() {
        val hasImage = selectedImageUri != null
        if(hasImage) {
            uploadBookButton.visibility = View.VISIBLE
        } else {
            uploadBookButton.visibility = View.GONE
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePicture()
        } else {
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            galleryPermissionRequest.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }


    private fun takePicture() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        val photoFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        val photoURI = FileProvider.getUriForFile(
            requireContext(),
            "com.example.bookworm.fileprovider",
            photoFile
        )
        photoPath = photoURI.toString()

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
        takePictureLauncher.launch(takePictureIntent)
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)

    }

    private fun getBookData(): BookData? {
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Select an image", Toast.LENGTH_SHORT).show()
            return null
        }

        var title = "Untitled"
        if (bookTitleInput.text != null) {
            val titleText = bookTitleInput.text.toString()
            if (titleText.isNotBlank()) {
                title = titleText
            }
        }
        return BookData(title, selectedImageUri!!)
    }
}

data class BookData(
    val title: String,
    val imageUri: Uri
)