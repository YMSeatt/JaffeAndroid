package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Guide
import com.example.myapplication.data.GuideDao
import com.example.myapplication.data.GuideType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuideViewModel @Inject constructor(
    private val guideDao: GuideDao
) : ViewModel() {

    val guides = guideDao.getAllGuides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGuide(type: GuideType) {
        viewModelScope.launch {
            guideDao.insert(Guide(type = type, position = 0f))
        }
    }

    fun updateGuide(guide: Guide) {
        viewModelScope.launch {
            guideDao.update(guide)
        }
    }

    fun deleteGuide(guide: Guide) {
        viewModelScope.launch {
            guideDao.delete(guide)
        }
    }

    fun updateGuidePosition(guideId: Long, newPosition: Float) {
        viewModelScope.launch {
            val guide = guides.value.find { it.id == guideId }
            guide?.let {
                updateGuide(it.copy(position = newPosition))
            }
        }
    }
}