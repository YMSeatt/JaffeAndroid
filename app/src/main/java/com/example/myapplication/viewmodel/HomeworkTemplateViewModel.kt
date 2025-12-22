package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.HomeworkTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeworkTemplateViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val homeworkTemplateDao = db.homeworkTemplateDao()

    val homeworkTemplates: StateFlow<List<HomeworkTemplate>> = homeworkTemplateDao.getAllHomeworkTemplates()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insert(template: HomeworkTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            homeworkTemplateDao.insert(template)
        }
    }

    fun update(template: HomeworkTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            homeworkTemplateDao.update(template)
        }
    }

    fun delete(template: HomeworkTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            homeworkTemplateDao.delete(template)
        }
    }
}
