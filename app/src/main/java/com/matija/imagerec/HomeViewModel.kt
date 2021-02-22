package com.matija.imagerec

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val uiState: MutableLiveData<LiveDataEvent<UIEvent>> = MutableLiveData()


    fun takePhotoClicked() {
        uiState.value = LiveDataEvent(UIEvent.TakePhotoClicked)
    }

    fun choosePhotoClicked() {
        uiState.value = LiveDataEvent(UIEvent.ChoosePhotoClicked)
    }



    open class UIEvent {
        object TakePhotoClicked : UIEvent()
        object ChoosePhotoClicked : UIEvent()
    }
}