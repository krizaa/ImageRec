package com.matija.imagerec.view

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.matija.imagerec.R

open class BaseFragment : Fragment() {

    private var progressDialog: AlertDialog? = null
    private var alertDialog: AlertDialog? = null

    protected fun showProgressDialog() {
        if (progressDialog != null)
            hideProgressDialog()
        val progressLayout = layoutInflater.inflate(R.layout.progress_dialog_layout, null)
        val progressDialogBuilder = AlertDialog.Builder(context ?: return)
        progressDialogBuilder.setCancelable(false)
        progressDialogBuilder.setView(progressLayout)
        progressDialog = progressDialogBuilder.create()
        progressDialog?.show()
    }

    protected fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    protected fun showAlert(title: String, message: String, onPositiveButtonClick: () -> Any? = {}) {
        context?.let {
            if (alertDialog?.isShowing == true) return
            val adBuilder = AlertDialog.Builder(it)
            adBuilder.setTitle(title)
            adBuilder.setMessage(message)
            adBuilder.setPositiveButton(it.getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
                onPositiveButtonClick()
            }
            alertDialog = adBuilder.create()
            alertDialog?.show()
        }
    }
}