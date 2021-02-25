package com.matija.imagerec.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.matija.imagerec.R
import com.matija.imagerec.databinding.HomeFragmentBinding
import com.matija.imagerec.viewmodels.HomeViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : BaseFragment() {

    private val viewModel: HomeViewModel by viewModels()
    lateinit var currentPhotoPath: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<HomeFragmentBinding>(inflater, R.layout.home_fragment, container, false)
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.uiState.observe(viewLifecycleOwner) {
            when (it.getContentIfNotHandled()) {
                HomeViewModel.UIEvent.TakePhotoClicked -> takePhoto()
                HomeViewModel.UIEvent.ChoosePhotoClicked -> choosePhoto()
            }
        }
    }

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

    private val requestCaptureResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result[Manifest.permission.CAMERA] == true && result[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
            dispatchTakePhotoIntent()
        } else {
            showAlert(getString(R.string.permission_needed), getString(R.string.requested_permissions_are_needed_to_take_photo))
        }
    }

    private val requestChooseResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            dispatchChoosePhotoIntent()
        } else {
            showAlert(getString(R.string.permission_needed), getString(R.string.requested_permissions_are_needed_to_choose_photo))
        }
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

    private fun takePhoto() {
        context?.let { ctx ->
            when {
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    dispatchTakePhotoIntent()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    showAlert(getString(R.string.permission_needed), getString(R.string.requested_permissions_are_needed_to_take_photo)) {
                        requestCaptureResult.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    }
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestCaptureResult.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }
        }
    }

    private fun choosePhoto() {
        context?.let { ctx ->
            when {
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    dispatchChoosePhotoIntent()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showAlert(getString(R.string.permission_needed), getString(R.string.requested_permissions_are_needed_to_take_photo)) {
                        requestChooseResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
                else -> {
                    requestChooseResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
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
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToProcessImage(
                if (path.isBlank()) currentPhotoPath else path,
                path.isNotBlank()
            )
        )
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