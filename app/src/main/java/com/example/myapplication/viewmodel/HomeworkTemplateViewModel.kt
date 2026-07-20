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

/**
 * HomeworkTemplateViewModel: Manages CRUD operations and reactive streams for assignment checklists.
 *
 * This ViewModel handles the configuration of homework checklist schemas. Unlike flat lists,
 * homework templates define multi-step evaluation workflows (e.g., Checkboxes, Score scales, and Comments)
 * which are serialized to JSON in the database.
 *
 * ### Architectural Roles:
 * - **AndroidViewModel**: Extends [AndroidViewModel] to securely access the [Application] context,
 *   which is required for initializing the local Room database instance [AppDatabase].
 * - **Thread Safety**: Offloads write and delete operations directly to [Dispatchers.IO] to keep the Main (UI)
 *   thread fully unblocked, maintaining fluid 60fps performance during configuration editing.
 *
 * @param application The current application environment, used to fetch the Room database context.
 */
class HomeworkTemplateViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Singleton SQLite database instance of the application.
     */
    private val db = AppDatabase.getDatabase(application)

    /**
     * Data Access Object (DAO) for interacting with the `homework_templates` database table.
     */
    private val homeworkTemplateDao = db.homeworkTemplateDao()

    /**
     * Reactive, read-only [StateFlow] list of all custom homework templates.
     * Uses [SharingStarted.Lazily] to pre-warm the cache and start loading on the first subscriber.
     */
    val homeworkTemplates: StateFlow<List<HomeworkTemplate>> = homeworkTemplateDao.getAllHomeworkTemplates()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Inserts a new homework template definition into the local database.
     *
     * @param template The [HomeworkTemplate] instance containing name and serialized step specifications.
     */
    fun insert(template: HomeworkTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            homeworkTemplateDao.insert(template)
        }
    }

    /**
     * Updates an existing homework template definition.
     *
     * @param template The [HomeworkTemplate] record with revised fields/step settings.
     */
    fun update(template: HomeworkTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            homeworkTemplateDao.update(template)
        }
    }

    /**
     * Deletes a homework template definition.
     * Due to foreign key cascades, deleting a template may also nullify related assignments' template references.
     *
     * @param template The [HomeworkTemplate] entity to remove.
     */
    fun delete(template: HomeworkTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            homeworkTemplateDao.delete(template)
        }
    }
}
