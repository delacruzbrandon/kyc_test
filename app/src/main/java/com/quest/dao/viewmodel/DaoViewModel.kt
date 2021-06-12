package com.quest.dao.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quest.dao.dataservice.DaoClient
import com.quest.dao.model.CardHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DaoViewModel(): ViewModel() {

    private val _cardHolder = MutableLiveData<CardHolder>()
    val cardHolder: LiveData<CardHolder>
        get() = _cardHolder

    private var mCodeReference: String = ""

    init {
    }

    fun getData() {
        viewModelScope.launch {
            _cardHolder.value = getDocument()
        }
    }

    private suspend fun getDocument(): CardHolder {
        return getCardDocuments("465c92ac-4f64-4295-bd83-5b6e2c7d9982")
//        return retrofitBuilder.getCardDocuments(mCodeReference)
    }

    private suspend fun getCardDocuments(codeReference: String): CardHolder =
        withContext(Dispatchers.IO) {
            DaoClient.getAPI.getHolderDocuments(codeReference)
        }
}