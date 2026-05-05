package com.example.myapplication.labs.ghost.tiles

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.StudentRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * GhostQuickLogTileService: A Quick Settings Tile for rapid behavioral feedback.
 *
 * This service allows teachers to log a "Positive Participation" event for the
 * most recently active student with a single tap from the Android Quick Settings.
 * It's designed for "eyes-free" classroom management where the teacher can
 * reward behavior without opening the app or looking away from the class.
 *
 * BOLT: Uses [StudentRepository.getLastActiveStudentId] for O(1) target identification.
 */
@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class GhostQuickLogTileService : TileService() {

    @Inject
    lateinit var studentRepository: StudentRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onStartListening() {
        super.onStartListening()
        job?.cancel()
        job = serviceScope.launch {
            studentRepository.getLastActiveStudentIdFlow()
                .flatMapLatest { lastActiveId ->
                    if (lastActiveId != null) {
                        studentRepository.getStudentByIdFlow(lastActiveId)
                    } else {
                        flowOf(null)
                    }
                }
                .collect { student ->
                    updateTileState(student)
                }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        job?.cancel()
        job = null
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch {
            val lastActiveId = studentRepository.getLastActiveStudentId() ?: return@launch

            val event = BehaviorEvent(
                studentId = lastActiveId,
                type = "Positive Participation",
                comment = "Quick logged via Ghost Tile",
                timestamp = System.currentTimeMillis()
            )

            studentRepository.insertBehaviorEvent(event)

            // Visual confirmation on the tile
            val tile = qsTile ?: return@launch
            val originalLabel = tile.label
            tile.label = "Logged!"
            tile.updateTile()

            delay(1500)
            tile.label = originalLabel
            tile.updateTile()
        }
    }

    private fun updateTileState(student: com.example.myapplication.data.Student?) {
        val tile = qsTile ?: return

        if (student != null) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Log ${student.firstName}"
        } else {
            tile.state = Tile.STATE_DISABLED
            tile.label = "No Active Student"
        }
        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
