package com.matija.imagerec

import android.app.Activity
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.matija.imagerec.databinding.HomeFragmentBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    lateinit var currentPhotoPath: String

    private val imageCaptureResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            navigateToImageProcessing()
            galleryAddPic()
        }
    }

    private val choosePhotoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.dataString?.let { navigateToImageProcessing(it) }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<HomeFragmentBinding>(inflater, R.layout.home_fragment, container, false)
        binding.viewmodel = viewModel
        return binding.root
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePhotoIntent() {
        context?.let { ctx ->
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(ctx.packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(ctx, ctx.packageName, it)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        imageCaptureResult.launch(takePictureIntent)
                    }
                }
            }


        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.uiState.observe(viewLifecycleOwner) {
            when (it.getContentIfNotHandled()) {
                HomeViewModel.UIEvent.TakePhotoClicked -> dispatchTakePhotoIntent()
                HomeViewModel.UIEvent.ChoosePhotoClicked -> dispatchChoosePhotoIntent()
            }
        }
    }

    private fun dispatchChoosePhotoIntent() {
        context?.let { context ->
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { intent ->
                intent.resolveActivity(context.packageManager)?.also {
                    choosePhotoResult.launch(intent)
                }
            }
        }
    }

    private fun navigateToImageProcessing(path: String = "") {
        findNavController().navigate(HomeFragmentDirections.actionHomeToProcessImage(if (path.isBlank()) currentPhotoPath else path))
    }

    //TODO not working hehe
    private fun galleryAddPic() {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(currentPhotoPath),
            arrayOf("image/*"),
            null
        )

    }
}