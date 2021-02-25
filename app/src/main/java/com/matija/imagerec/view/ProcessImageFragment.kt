package com.matija.imagerec.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.cloudmersive.client.model.FaceLocateResponse
import com.matija.imagerec.R
import com.matija.imagerec.databinding.ProcessImageFragmentBinding
import com.matija.imagerec.viewmodels.ProcessImageViewModel
import kotlinx.android.synthetic.main.process_image_fragment.*
import java.io.FileInputStream

class ProcessImageFragment : BaseFragment() {

    private val viewModel: ProcessImageViewModel by viewModels()
    private val args: ProcessImageFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<ProcessImageFragmentBinding>(inflater, R.layout.process_image_fragment, container, false)
        binding.viewModel = viewModel
        viewModel.filePath = args.filePath
        if (args.gallery) viewModel.copyFileFromGallery(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(image).load(args.filePath).into(image)
        viewModel.uiState.observe(viewLifecycleOwner) {
            when (val result = it.getContentIfNotHandled()) {
                ProcessImageViewModel.UIEvent.Loading -> showProgressDialog()
                ProcessImageViewModel.UIEvent.StopLoading -> hideProgressDialog()
                is ProcessImageViewModel.UIEvent.ShowAlert -> showDialog(result.message)
            }
        }
        viewModel.faceSquares.observe(viewLifecycleOwner) {
            it?.let {
                if (!it.isSuccessful) {
                    showAlert("Error", "Something went wrong.")
                }
                if (it.faceCount == 0) {
                    showAlert("Error", "No faces recognized.")
                } else {
                    drawFaceSquares(it)
                }
            }
        }
        viewModel.imageDescription.observe(viewLifecycleOwner) {
            it?.let {
                showDialog(it.bestOutcome.description)
            }
        }
        setImageSize()
    }

    private fun setImageSize() {
        val ei = FileInputStream(viewModel.filePath).use { ExifInterface(it) }
        image.imageWidth = ei.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 1)
        image.imageHeight = ei.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 1)
    }

    private fun drawFaceSquares(result: FaceLocateResponse) {
        image.faces = result.faces
        image.invalidate()
    }

    private fun showDialog(description: String?) {
        if (description?.isNotBlank() == true) {
            showAlert("Description", description)
        } else {
            showAlert("Error", "There has been an error.")
        }
    }
}