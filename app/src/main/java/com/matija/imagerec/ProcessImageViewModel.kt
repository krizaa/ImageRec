package com.matija.imagerec

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudmersive.client.FaceApi
import com.cloudmersive.client.RecognizeApi
import com.cloudmersive.client.invoker.ApiClient
import com.cloudmersive.client.invoker.ApiException
import com.cloudmersive.client.invoker.Configuration
import com.cloudmersive.client.invoker.auth.ApiKeyAuth
import com.cloudmersive.client.model.FaceLocateResponse
import com.cloudmersive.client.model.ImageDescriptionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class ProcessImageViewModel : ViewModel() {

    var defaultClient: ApiClient = Configuration.getDefaultApiClient()
    val uiState: MutableLiveData<LiveDataEvent<UIEvent>> = MutableLiveData()
    val faceSquares: MutableLiveData<FaceLocateResponse> = MutableLiveData()
    val imageDescription: MutableLiveData<ImageDescriptionResponse> = MutableLiveData()
    lateinit var filePath: String
    private val faceApiInstance = FaceApi()
    private val recognizeApiInstance = RecognizeApi()

    init {
        val apiKey = defaultClient.getAuthentication("Apikey") as ApiKeyAuth
        //TODO secure store of api key
        apiKey.apiKey = "d4001d0b-ba5a-426c-9593-c2f383f8ef15"
    }

    fun action(action: Action) = viewModelScope.launch(Dispatchers.IO) {
        uiState.postValue(LiveDataEvent(UIEvent.Loading))
        //TODO check internet connection
        val imageFile = File(filePath)
        when (action) {
            Action.RECOGNIZE_FACES -> recognizeFaces(imageFile)
            Action.DESCRIBE_IMAGE -> describeImage(imageFile)
        }
        uiState.postValue(LiveDataEvent(UIEvent.StopLoading))
    }

    private suspend fun describeImage(imageFile: File) {
        try {
            val result = recognizeApiInstance.recognizeDescribe(imageFile)
            imageDescription.postValue(result)
        } catch (e: ApiException) {
            System.err.println("Exception when calling RecognizeApi#recognizeDescribe")
            e.printStackTrace()
        }
    }

    private suspend fun recognizeFaces(imageFile: File)  {
        try {
            val result = faceApiInstance.faceLocate(imageFile)
            faceSquares.postValue(result)
        } catch (e: ApiException) {
            System.err.println("Exception when calling FaceApi#faceLocate")
            e.printStackTrace()
        }
    }

    fun copyFileFromGallery(context: Context?) = viewModelScope.launch {
        context?.let {
            val newFile = createImageFile(it)
            val parcelFileDescriptor = context.contentResolver?.openFileDescriptor(Uri.parse(filePath) , "r")
            val fileDescriptor = parcelFileDescriptor?.fileDescriptor
            val inStream = FileInputStream(fileDescriptor)
            val outStream = FileOutputStream(newFile)

            inStream.channel.use { inC ->
                outStream.channel.use { outC ->
                    inC.transferTo(0, inC.size(), outC)
                }
            }

            parcelFileDescriptor?.close()

            filePath = newFile.absolutePath
        }

    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val storageDir: File? = context.getExternalFilesDir(null)
        val name = "newImageFile"
        return File("${storageDir?.absolutePath}/$name${System.currentTimeMillis()}.jpg")
    }

    open class UIEvent {
        object Loading : UIEvent()
        object StopLoading : UIEvent()
    }

    enum class Action{
        RECOGNIZE_FACES,
        DESCRIBE_IMAGE
    }
}