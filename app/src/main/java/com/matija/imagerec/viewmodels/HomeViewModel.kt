package com.matija.imagerec.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.matija.imagerec.util.LiveDataEvent

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