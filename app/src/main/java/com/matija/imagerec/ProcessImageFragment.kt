package com.matija.imagerec

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.cloudmersive.client.model.FaceLocateResponse
import com.matija.imagerec.databinding.ProcessImageFragmentBinding
import kotlinx.android.synthetic.main.process_image_fragment.*
import java.io.FileInputStream

class ProcessImageFragment : Fragment() {

    private val viewModel: ProcessImageViewModel by viewModels()
    private val args: ProcessImageFragmentArgs by navArgs()

    private var progressDialog: AlertDialog? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<ProcessImageFragmentBinding>(inflater, R.layout.process_image_fragment, container, false)
        binding.viewModel = viewModel
        viewModel.filePath = args.filePath
        if(args.gallery) viewModel.copyFileFromGallery(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(image).load(args.filePath).into(image)
        viewModel.uiState.observe(viewLifecycleOwner) {
            when (val liveData = it.getContentIfNotHandled()) {
                ProcessImageViewModel.UIEvent.Loading -> showProgressDialog()
                ProcessImageViewModel.UIEvent.StopLoading -> hideProgressDialog()
            }
        }
        viewModel.faceSquares.observe(viewLifecycleOwner){
            it?.let {
                drawFaceSquares(it)
            }
        }
        viewModel.imageDescription.observe(viewLifecycleOwner){
            it?.let {
                showDialog(it.bestOutcome.description)
            }
        }
        setImageSize()
    }

    private fun showDialog(description: String?) {
        if(description?.isNotBlank() == true){
            showAlert("Description", description)
        }
        else{
            showAlert("Error", "There has been an error.")
        }
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

    private fun showProgressDialog() {
        if (progressDialog != null)
            hideProgressDialog()
        val progressLayout = layoutInflater.inflate(R.layout.progress_dialog_layout, null)
        val progressDialogBuilder = AlertDialog.Builder(context ?: return)
        progressDialogBuilder.setCancelable(false)
        progressDialogBuilder.setView(progressLayout)
        progressDialog = progressDialogBuilder.create()
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun showAlert(title: String, message: String) {
        context?.let {
            if (alertDialog?.isShowing == true) return
            val adBuilder = AlertDialog.Builder(it)
            adBuilder.setTitle(title)
            adBuilder.setMessage(message)
            adBuilder.setPositiveButton(it.getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
            }
            alertDialog = adBuilder.create()
            alertDialog?.show()
        }
    }
}