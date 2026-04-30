package com.example.myapplication.ui.screens

import android.app.Activity
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.labs.ghost.GhostInsight
import com.example.myapplication.labs.ghost.GhostEchoEngine
import com.example.myapplication.labs.ghost.GhostEchoLayer
import com.example.myapplication.labs.ghost.GhostChronosLayer
import com.example.myapplication.labs.ghost.NeuralMapLayer
import com.example.myapplication.labs.ghost.GhostInsightDialog
import com.example.myapplication.labs.ghost.GhostInsightEngine
import com.example.myapplication.labs.ghost.GhostOracle
import com.example.myapplication.labs.ghost.GhostHUDLayer
import com.example.myapplication.labs.ghost.GhostHUDViewModel
import com.example.myapplication.labs.ghost.GhostOracleDialog
import com.example.myapplication.labs.ghost.GhostVoiceAssistant
import com.example.myapplication.labs.ghost.GhostVoiceVisualizer
import com.example.myapplication.labs.ghost.GhostHologramEngine
import com.example.myapplication.labs.ghost.GhostHologramLayer
import com.example.myapplication.labs.ghost.GhostBlueprintEngine
import com.example.myapplication.labs.ghost.GhostPhantasmLayer
import com.example.myapplication.labs.ghost.GhostPhantasmEngine
import com.example.myapplication.labs.ghost.GhostPortalLayer
import com.example.myapplication.labs.ghost.GhostSpectraLayer
import com.example.myapplication.labs.ghost.GhostFluxLayer
import com.example.myapplication.labs.ghost.GhostSingularityLayer
import com.example.myapplication.labs.ghost.GhostAuroraLayer
import com.example.myapplication.labs.ghost.silhouette.GhostSilhouetteLayer
import com.example.myapplication.labs.ghost.ion.GhostIonLayer
import com.example.myapplication.labs.ghost.sync.GhostSyncLayer
import com.example.myapplication.labs.ghost.sync.GhostSyncEngine
import com.example.myapplication.labs.ghost.GhostSparkEngine
import com.example.myapplication.labs.ghost.GhostSparkLayer
import com.example.myapplication.labs.ghost.GhostFutureLayer
import com.example.myapplication.labs.ghost.GhostNebulaLayer
import com.example.myapplication.labs.ghost.GhostPulseLayer
import com.example.myapplication.labs.ghost.pulsar.GhostPulsarLayer
import com.example.myapplication.labs.ghost.magnetar.GhostMagnetarLayer
import com.example.myapplication.labs.ghost.magnetar.GhostMagnetarEngine
import com.example.myapplication.labs.ghost.warp.GhostWarpLayer
import com.example.myapplication.labs.ghost.GhostLensEngine
import com.example.myapplication.labs.ghost.GhostLensLayer
import com.example.myapplication.labs.ghost.lattice.GhostLatticeLayer
import com.example.myapplication.labs.ghost.phasing.GhostPhasingEngine
import com.example.myapplication.labs.ghost.phasing.GhostPhasingLayer
import com.example.myapplication.labs.ghost.vector.GhostVectorLayer
import com.example.myapplication.labs.ghost.synapse.GhostSynapseDialog
import com.example.myapplication.labs.ghost.glitch.GhostGlitchLayer
import com.example.myapplication.labs.ghost.osmosis.GhostOsmosisLayer
import com.example.myapplication.labs.ghost.osmosis.GhostOsmosisEngine
import com.example.myapplication.labs.ghost.entanglement.GhostEntanglementLayer
import com.example.myapplication.labs.ghost.entanglement.GhostEntanglementEngine
import com.example.myapplication.labs.ghost.entropy.GhostEntropyLayer
import com.example.myapplication.labs.ghost.emergence.GhostEmergenceEngine
import com.example.myapplication.labs.ghost.emergence.GhostEmergenceLayer
import com.example.myapplication.labs.ghost.catalyst.GhostCatalystLayer
import com.example.myapplication.labs.ghost.flora.GhostFloraLayer
import com.example.myapplication.labs.ghost.tectonics.GhostTectonicLayer
import com.example.myapplication.labs.ghost.adaptive.GhostAdaptiveLayer
import com.example.myapplication.labs.ghost.adaptive.GhostAdaptiveEngine
import com.example.myapplication.labs.ghost.GhostHorizonEngine
import com.example.myapplication.labs.ghost.GhostHorizonLayer
import com.example.myapplication.labs.ghost.zenith.GhostZenithEngine
import com.example.myapplication.labs.ghost.zenith.GhostZenithLayer
import com.example.myapplication.labs.ghost.cortex.GhostCortexEngine
import com.example.myapplication.labs.ghost.cortex.GhostCortexLayer
import com.example.myapplication.labs.ghost.ray.GhostRayEngine
import com.example.myapplication.labs.ghost.ray.GhostRayLayer
import com.example.myapplication.labs.ghost.cortex.GhostCortexActivity
import com.example.myapplication.labs.ghost.quasar.GhostQuasarLayer
import com.example.myapplication.labs.ghost.helix.GhostHelixLayer
import com.example.myapplication.labs.ghost.helix.GhostHelixEngine
import com.example.myapplication.labs.ghost.lasso.GhostLassoLayer
import com.example.myapplication.labs.ghost.vortex.GhostVortexLayer
import com.example.myapplication.labs.ghost.navigator.GhostNavigatorLayer
import com.example.myapplication.labs.ghost.orbit.GhostOrbitLayer
import com.example.myapplication.labs.ghost.architect.GhostArchitectLayer
import com.example.myapplication.labs.ghost.architect.GhostArchitectEngine
import com.example.myapplication.labs.ghost.glyph.GhostGlyphLayer
import com.example.myapplication.labs.ghost.glyph.GhostGlyphEngine
import com.example.myapplication.labs.ghost.vision.GhostVisionEngine
import com.example.myapplication.labs.ghost.vision.GhostVisionLayer
import com.example.myapplication.labs.ghost.vision.GhostVisionActivity
import com.example.myapplication.labs.ghost.strategist.GhostStrategistLayer
import com.example.myapplication.labs.ghost.strategist.GhostStrategistEngine
import com.example.myapplication.labs.ghost.filtering.GhostFilterActivity
import com.example.myapplication.labs.ghost.glance.GhostGlanceSurface
import com.example.myapplication.labs.ghost.glance.GhostGlanceEngine
import com.example.myapplication.labs.ghost.spotlight.GhostSpotlightLayer
import com.example.myapplication.labs.ghost.hub.GhostHubLayer
import com.example.myapplication.labs.ghost.hub.GhostStudentHubLayer
import com.example.myapplication.labs.ghost.hub.GhostAction
import com.example.myapplication.labs.ghost.GhostLinkEngine
import com.example.myapplication.labs.ghost.preferences.GhostPreferencesViewModel
import com.example.myapplication.labs.ghost.util.GhostHapticManager
import com.example.myapplication.labs.ghost.util.GhostShakeDetector
import android.hardware.SensorManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.GuideType
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.ui.components.FurnitureDraggableIcon
import com.example.myapplication.ui.components.GridAndRulers
import com.example.myapplication.ui.components.StudentDraggableIcon
import com.example.myapplication.ui.dialogs.AddEditFurnitureDialog
import com.example.myapplication.ui.dialogs.AddEditStudentDialog
import com.example.myapplication.ui.dialogs.AdvancedHomeworkLogDialog
import com.example.myapplication.ui.dialogs.AssignTaskDialog
import com.example.myapplication.ui.dialogs.BehaviorDialog
import com.example.myapplication.ui.dialogs.BehaviorLogViewerDialog
import com.example.myapplication.ui.dialogs.ChangeBoxSizeDialog
import com.example.myapplication.ui.dialogs.EmailDialog
import com.example.myapplication.ui.dialogs.ExportDialog
import com.example.myapplication.ui.dialogs.LiveHomeworkMarkDialog
import com.example.myapplication.ui.dialogs.LiveQuizMarkDialog
import com.example.myapplication.ui.dialogs.LoadLayoutDialog
import com.example.myapplication.ui.dialogs.LogQuizScoreDialog
import com.example.myapplication.ui.dialogs.SaveLayoutDialog
import com.example.myapplication.ui.dialogs.StudentStyleScreen
import com.example.myapplication.ui.dialogs.UndoHistoryDialog
import com.example.myapplication.commands.ItemType
import com.example.myapplication.ui.model.ChartItemId
import com.example.myapplication.ui.model.SessionType
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.util.EmailException
import com.example.myapplication.util.EmailUtil
import com.example.myapplication.util.captureComposable
import com.example.myapplication.util.findActivity
import com.example.myapplication.util.toTitleCase
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.StudentGroupsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SeatingChartScreen(
    seatingChartViewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    studentGroupsViewModel: StudentGroupsViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToDataViewer: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onHelpClick: () -> Unit,
    onImportJson: () -> Unit,
    onImportStudentsFromExcel: () -> Unit,
    onOpenAppDataFolder: () -> Unit,
    createDocumentLauncher: ActivityResultLauncher<String>,
    showEmailDialog: Boolean,
    onShowEmailDialogChange: (Boolean) -> Unit
) {
    val students by seatingChartViewModel.studentsForDisplay.observeAsState(initial = emptyList())
    val furniture by seatingChartViewModel.furnitureForDisplay.observeAsState(initial = emptyList())
    val layouts by seatingChartViewModel.allLayoutTemplates.observeAsState(initial = emptyList())
    val selectedItemIds by seatingChartViewModel.selectedItemIds.observeAsState(initial = emptySet())
    val allProphecies by seatingChartViewModel.prophecies.collectAsState()
    val allBehaviorEvents by seatingChartViewModel.allBehaviorEvents.observeAsState(initial = emptyList())
    val allQuizLogs by seatingChartViewModel.allQuizLogs.observeAsState(initial = emptyList())
    val allHomeworkLogs by seatingChartViewModel.allHomeworkLogs.observeAsState(initial = emptyList())

    val chronosHeatmap by seatingChartViewModel.chronosHeatmap.collectAsState()
    val spectralDensity by seatingChartViewModel.spectralDensity.collectAsState()
    val agitation by seatingChartViewModel.agitation.collectAsState()
    val latticeEdges by seatingChartViewModel.latticeEdges.collectAsState()
    val socialVectors by seatingChartViewModel.socialVectors.collectAsState()
    val vortices by seatingChartViewModel.vortices.collectAsState()
    val entangledLinks by seatingChartViewModel.entangledLinks.collectAsState()
    val syncLinks by seatingChartViewModel.syncLinks.collectAsState()
    val catalystReactions by seatingChartViewModel.catalystReactions.collectAsState()
    val futureEvents by seatingChartViewModel.futureEvents.collectAsState()
    val adaptiveZones by seatingChartViewModel.adaptiveZones.collectAsState()
    val draggingSilhouettes by seatingChartViewModel.draggingSilhouettes.collectAsState()
    val glitchIntensity by seatingChartViewModel.glitchIntensity.collectAsState()
    val strategistInterventions by seatingChartViewModel.strategistInterventions.collectAsState()
    val isStrategistThinking by seatingChartViewModel.isStrategistThinking.collectAsState()
    val strategistGoal by seatingChartViewModel.strategistGoal.collectAsState()

    var showGhostInsightDialog by remember { mutableStateOf(false) }
    var showGhostSynapseDialog by remember { mutableStateOf(false) }
    var currentGhostInsight by remember { mutableStateOf<GhostInsight?>(null) }
    var showGhostOracleDialog by remember { mutableStateOf(false) }
    var isHudActive by remember { mutableStateOf(false) }
    var isChronosActive by remember { mutableStateOf(false) }
    var isHologramActive by remember { mutableStateOf(false) }
    var isVectorActive by remember { mutableStateOf(false) }
    var isSpectraActive by remember { mutableStateOf(false) }
    var isFluxActive by remember { mutableStateOf(false) }
    var isAuroraActive by remember { mutableStateOf(false) }
    var isNebulaActive by remember { mutableStateOf(false) }
    var isPulseActive by remember { mutableStateOf(false) }
    var isLensActive by remember { mutableStateOf(false) }
    var isSingularityActive by remember { mutableStateOf(false) }
    var isWarpActive by remember { mutableStateOf(false) }
    var isFutureActive by remember { mutableStateOf(false) }
    var isSparkActive by remember { mutableStateOf(false) }
    var isSyncActive by remember { mutableStateOf(false) }
    var isOsmosisActive by remember { mutableStateOf(false) }
    var isEntanglementActive by remember { mutableStateOf(false) }
    var isIonActive by remember { mutableStateOf(false) }
    var isEntropyActive by remember { mutableStateOf(false) }
    var isZenithActive by remember { mutableStateOf(false) }
    var isEmergenceActive by remember { mutableStateOf(false) }
    var isCatalystActive by remember { mutableStateOf(false) }
    var isFloraActive by remember { mutableStateOf(false) }
    var isTectonicsActive by remember { mutableStateOf(false) }
    var isPulsarActive by remember { mutableStateOf(false) }
    var isMagnetarActive by remember { mutableStateOf(false) }
    var isHorizonActive by remember { mutableStateOf(false) }
    var isHelixActive by remember { mutableStateOf(false) }
    var isSupernovaActive by remember { mutableStateOf(false) }
    var isVortexActive by remember { mutableStateOf(false) }
    var isOrbitActive by remember { mutableStateOf(false) }
    var isArchitectActive by remember { mutableStateOf(false) }
    var isVisionActive by remember { mutableStateOf(false) }
    var isGlitchActive by remember { mutableStateOf(GhostConfig.GHOST_MODE_ENABLED && GhostConfig.GLITCH_MODE_ENABLED) }
    var isGlyphActive by remember { mutableStateOf(false) }
    var isSpotlightActive by remember { mutableStateOf(false) }
    var isNavigatorActive by remember { mutableStateOf(false) }
    var isAdaptiveActive by remember { mutableStateOf(false) }
    var isStrategistActive by remember { mutableStateOf(false) }
    var isLassoActive by remember { mutableStateOf(false) }
    var architectGoal by remember { mutableStateOf(GhostArchitectEngine.StrategicGoal.COLLABORATION) }
    var isRayActive by remember { mutableStateOf(false) }
    var isCortexActive by remember { mutableStateOf(false) }
    var isQuasarActive by remember { mutableStateOf(false) }
    var isPhasingActive by remember { mutableStateOf(false) }
    var isIrisActive by remember { mutableStateOf(false) }
    var isSilhouetteActive by remember { mutableStateOf(GhostConfig.GHOST_MODE_ENABLED && GhostConfig.SILHOUETTE_MODE_ENABLED) }
    var isPhantasmActive by remember { mutableStateOf(GhostConfig.GHOST_MODE_ENABLED && GhostConfig.PHANTASM_MODE_ENABLED) }
    var isScreenRecording by remember { mutableStateOf(false) }
    var activeGlanceStudentId by remember { mutableStateOf<Long?>(null) }
    var isGhostHubVisible by remember { mutableStateOf(false) }
    var ghostHubPosition by remember { mutableStateOf(Offset.Zero) }
    var isStudentHubVisible by remember { mutableStateOf(false) }
    var studentHubPosition by remember { mutableStateOf(Offset.Zero) }
    /**
     * SHIELD: Track the last shared artifact (screenshot/blueprint) for cleanup.
     * This ensures that temporary files created for sharing don't persist longer than necessary.
     */
    var lastSharedArtifactUri by remember { mutableStateOf<Uri?>(null) }
    val hudViewModel: GhostHUDViewModel = viewModel()
    val ghostPrefsViewModel: GhostPreferencesViewModel = viewModel()
    val context = LocalContext.current
    val hapticManager = remember { GhostHapticManager(context) }

    val shakeToRecenterEnabled by ghostPrefsViewModel.shakeToRecenterEnabled.collectAsState()

    // Portal State: Manages the visibility and position of the Ghost Portal during drag-and-drop.
    var isDraggingPortalActive by remember { mutableStateOf(false) }
    var portalPosition by remember { mutableStateOf(Offset.Zero) }

    /**
     * Implementation of [DragAndDropTarget] that facilitates inter-app data transfer
     * via the Ghost Portal.
     *
     * ### Inter-app Teleportation:
     * When a drag event containing "student_data" enters the portal, it triggers a visual
     * response. Dropping the item on the portal completes the "teleportation," simulating
     * data transfer between compatible classroom management apps.
     */
    val portalTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isDraggingPortalActive = true
            }

            override fun onEntered(event: DragAndDropEvent) {
                // Potential to change portal color or size
            }

            override fun onMoved(event: DragAndDropEvent) {
                val dragEvent = event.toAndroidDragEvent()
                portalPosition = Offset(dragEvent.x, dragEvent.y)
            }

            override fun onExited(event: DragAndDropEvent) {
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDraggingPortalActive = false
                val dragEvent = event.toAndroidDragEvent()
                val clipData = dragEvent.clipData
                if (clipData.description.label == "student_data") {
                    Toast.makeText(context, "Student teleported through Ghost Portal! 🌀", Toast.LENGTH_SHORT).show()
                    return true
                }
                return false
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDraggingPortalActive = false
            }
        }
    }

    var showBehaviorDialog by remember { mutableStateOf(false) }
    var showLogQuizScoreDialog by remember { mutableStateOf(false) }
    var showLiveQuizMarkDialog by remember { mutableStateOf(false) }
    var showAdvancedHomeworkLogDialog by remember { mutableStateOf(false) }
    var showLiveHomeworkMarkDialog by remember { mutableStateOf(false) }
    var showStudentActionMenu by remember { mutableStateOf(false) }
    var showSaveLayoutDialog by remember { mutableStateOf(false) }
    var showLoadLayoutDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showUndoHistoryDialog by remember { mutableStateOf(false) }

    // Submenu States
    var showAlignSubMenu by remember { mutableStateOf(false) }

    var selectMode by remember { mutableStateOf(false) }
    var showChangeBoxSizeDialog by remember { mutableStateOf(false) }
    var showStudentStyleDialog by remember { mutableStateOf(false) }
    var showAssignTaskDialog by remember { mutableStateOf(false) }

    var selectedStudentUiItemForAction by remember { mutableStateOf<StudentUiItem?>(null) }

    var showAddEditStudentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<com.example.myapplication.data.Student?>(null) }
    var showAddEditFurnitureDialog by remember { mutableStateOf(false) }
    var editingFurniture by remember { mutableStateOf<com.example.myapplication.data.Furniture?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val userPreferences by seatingChartViewModel.userPreferences.collectAsState()
    val behaviorTypes by settingsViewModel.customBehaviors.observeAsState(initial = emptyList())
    val behaviorTypeNames = remember(behaviorTypes) { behaviorTypes.map { it.name } }

    // Ghost Voice Assistant State
    var isGhostListening by remember { mutableStateOf(false) }
    var ghostAmplitude by remember { mutableFloatStateOf(0f) }
    var ghostCurrentText by remember { mutableStateOf("") }

    val ghostVoiceAssistant = remember(behaviorTypeNames) {
        GhostVoiceAssistant(
            context = context,
            viewModel = seatingChartViewModel,
            onAmplitudeChange = { ghostAmplitude = it },
            onListeningStateChange = { isGhostListening = it },
            onResult = { ghostCurrentText = it },
            customBehaviors = behaviorTypeNames,
            onCommand = { cmd ->
                if (cmd == "toggle_hologram") {
                    isHologramActive = !isHologramActive
                }
            }
        )
    }

    val ghostEchoEngine = remember { GhostEchoEngine() }
    val ghostSparkEngine = remember { GhostSparkEngine() }
    val ghostHologramEngine = remember { GhostHologramEngine(context) }
    val ghostHorizonEngine = remember { GhostHorizonEngine(context) }
    val ghostVisionEngine = remember { GhostVisionEngine(context) }
    val ghostMagnetarEngine = remember { GhostMagnetarEngine(context) }
    val ghostZenithEngine = remember { GhostZenithEngine(context) }
    val ghostEmergenceEngine = remember { GhostEmergenceEngine() }
    val ghostCortexEngine = remember { GhostCortexEngine(context) }
    val ghostLensEngine = remember { GhostLensEngine() }
    val ghostPhantasmEngine = remember { GhostPhantasmEngine(context) }
    val ghostPhasingEngine = remember { GhostPhasingEngine(context) }
    val ghostSupernovaEngine = remember { GhostSupernovaEngine() }
    val ghostRayEngine = remember { GhostRayEngine(context) }

    DisposableEffect(Unit, isPhantasmActive, isFutureActive, isVisionActive, isCortexActive, isHudActive, isArchitectActive, shakeToRecenterEnabled) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val shakeDetector = if (shakeToRecenterEnabled && sensorManager != null) {
            GhostShakeDetector {
                scale = 1f
                offset = Offset.Zero
                hapticManager.perform(GhostHapticManager.Pattern.SUCCESS)
                Toast.makeText(context, "Canvas Recentered 👻", Toast.LENGTH_SHORT).show()
            }.apply {
                start(sensorManager)
            }
        } else null

        if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.PHANTASM_MODE_ENABLED) {
            ghostPhantasmEngine.observeScreenRecording(ContextCompat.getMainExecutor(context)) { recording ->
                isScreenRecording = recording
            }
        }

        // HARDEN: Proactively enforce FLAG_SECURE whenever high-PII experiments are active
        val isSensitiveModeActive = isPhantasmActive || isFutureActive || isVisionActive ||
                                   isCortexActive || isHudActive || isArchitectActive
        ghostPhantasmEngine.updatePrivacyShield(context.findActivity(), isSensitiveModeActive)

        if (GhostConfig.GHOST_MODE_ENABLED) {
            if (GhostConfig.ECHO_MODE_ENABLED) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    ghostEchoEngine.start()
                }
            }
            if (GhostConfig.HOLOGRAM_MODE_ENABLED) {
                ghostHologramEngine.start()
            }
            if (GhostConfig.MAGNETAR_MODE_ENABLED) {
                ghostMagnetarEngine.start()
            }
            if (GhostConfig.HORIZON_MODE_ENABLED) {
                ghostHorizonEngine.start()
            }
            if (GhostConfig.VISION_MODE_ENABLED) {
                ghostVisionEngine.start()
            }
        }
        onDispose {
            ghostVoiceAssistant.destroy()
            ghostEchoEngine.stop()
            ghostHologramEngine.stop()
            ghostMagnetarEngine.stop()
            ghostHorizonEngine.stop()
            ghostVisionEngine.stop()
            ghostRayEngine.stop()
            ghostPhantasmEngine.stopObservingScreenRecording()
            sensorManager?.let { shakeDetector?.stop(it) }
            // Ensure privacy shield is cleared on dispose
            ghostPhantasmEngine.updatePrivacyShield(context.findActivity(), false)

            // SHIELD: Clean up the last shared artifact on screen disposal
            lastSharedArtifactUri?.let { uri ->
                try { context.contentResolver.delete(uri, null, null) } catch (e: Exception) { /* Ignore */ }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            ghostVoiceAssistant.startListening()
        } else {
            Toast.makeText(context, "Voice Assistant requires microphone permission", Toast.LENGTH_SHORT).show()
        }
    }

    val showRecentBehavior by settingsViewModel.showRecentBehavior.collectAsState(initial = false)
    val quizLogFontColorStr by settingsViewModel.quizLogFontColor.collectAsState()
    val homeworkLogFontColorStr by settingsViewModel.homeworkLogFontColor.collectAsState()
    val quizLogFontBold by settingsViewModel.quizLogFontBold.collectAsState()
    val homeworkLogFontBold by settingsViewModel.homeworkLogFontBold.collectAsState()

    val quizLogFontColor = remember(quizLogFontColorStr) {
        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(quizLogFontColorStr)) } catch (e: Exception) { androidx.compose.ui.graphics.Color(0xFF006400) }
    }
    val homeworkLogFontColor = remember(homeworkLogFontColorStr) {
        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(homeworkLogFontColorStr)) } catch (e: Exception) { androidx.compose.ui.graphics.Color(0xFF800080) }
    }

    var sessionType by remember { mutableStateOf(SessionType.BEHAVIOR) }
    val editModeEnabled = userPreferences?.editModeEnabled ?: false
    var longPressPosition by remember { mutableStateOf(Offset.Zero) }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(canvasSize.height) {
        seatingChartViewModel.canvasHeight = canvasSize.height
    }

    LaunchedEffect(isHudActive) {
        if (isHudActive) {
            hudViewModel.startTracking()
        } else {
            hudViewModel.stopTracking()
        }
    }

    /**
     * Phasing Transition: Animates the 'phaseLevel' uniform for the Ghost Phasing AGSL shader.
     * This transition creates a glitchy chromatic aberration effect as the UI "phases"
     * into the hidden data layer.
     */
    val phaseLevel by animateFloatAsState(
        targetValue = if (isPhasingActive) 1f else 0f,
        animationSpec = tween(1500, easing = LinearOutSlowInEasing),
        label = "phaseLevel"
    )

    LaunchedEffect(phaseLevel) {
        ghostPhasingEngine.updatePhase(phaseLevel)
    }

    LaunchedEffect(glitchIntensity) {
        if (glitchIntensity > 0.3f && isGlitchActive) {
            hapticManager.perform(GhostHapticManager.Pattern.NEURAL_FRICTION)
        }
    }

    LaunchedEffect(isSupernovaActive, allBehaviorEvents) {
        if (isSupernovaActive) {
            ghostSupernovaEngine.updatePressure(allBehaviorEvents)
        } else {
            ghostSupernovaEngine.reset()
        }
    }

    /**
     * Phantasm Heartbeat: Drives a periodic 'heartbeat' pulse in the Ghost Phantasm meta-ball
     * layer. The pulse frequency and intensity are dynamically scaled based on the ratio
     * of negative behavior events in the classroom (the "Agitation Level").
     */
    LaunchedEffect(isPhantasmActive, allBehaviorEvents) {
        if (isPhantasmActive) {
            val agitation = if (allBehaviorEvents.isEmpty()) 0.2f
            else {
                val negativeCount = allBehaviorEvents.count { it.type.contains("Negative", ignoreCase = true) }
                (negativeCount.toFloat() / allBehaviorEvents.size.coerceAtLeast(1) + 0.1f).coerceAtMost(1.0f)
            }

            while (true) {
                ghostPhantasmEngine.triggerHeartbeat(agitation)
                val delayTime = (2000 - (agitation * 1500)).toLong().coerceAtLeast(500)
                kotlinx.coroutines.delay(delayTime)
            }
        }
    }

    var isFabMenuOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val isSessionActive by seatingChartViewModel.isSessionActive.observeAsState(initial = false)
    val globalIonBalance by seatingChartViewModel.globalIonBalance.collectAsState()
    val lastExportPath by settingsViewModel.lastExportPath.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SeatingChartTopAppBar(
                sessionType = sessionType,
                isSessionActive = isSessionActive,
                selectMode = selectMode,
                selectedItemIds = selectedItemIds,
                behaviorTypeNames = behaviorTypeNames,
                onSessionTypeChange = { sessionType = it },
                onToggleSession = { if (isSessionActive) seatingChartViewModel.endSession() else seatingChartViewModel.startSession() },
                onToggleSelectMode = {
                    selectMode = !selectMode
                    if (!selectMode) seatingChartViewModel.clearSelection()
                },
                onUndo = { seatingChartViewModel.undo() },
                onRedo = { seatingChartViewModel.redo() },
                onShowUndoHistory = { showUndoHistoryDialog = true },
                onBehaviorLog = { type ->
                    val targets = if (selectMode) selectedItemIds.filter { it.type == ItemType.STUDENT }.map { it.id.toLong() } else listOfNotNull(selectedStudentUiItemForAction?.id?.toLong())
                    val behaviorEvents = targets.map { id ->
                        BehaviorEvent(studentId = id, type = type, timestamp = System.currentTimeMillis(), comment = null)
                    }
                    seatingChartViewModel.addBehaviorEvents(behaviorEvents)
                    if (!selectMode) selectedStudentUiItemForAction = null
                    coroutineScope.launch { snackbarHostState.showSnackbar("Logged $type for ${targets.size} student(s)") }
                },
                onLogQuiz = { showLogQuizScoreDialog = true },
                onDeleteSelected = { seatingChartViewModel.deleteSelectedItems(selectedItemIds) },
                onChangeBoxSize = { showChangeBoxSizeDialog = true },
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToDataViewer = onNavigateToDataViewer,
                onNavigateToReminders = onNavigateToReminders,
                onHelpClick = onHelpClick,
                onTakeScreenshot = {
                    coroutineScope.launch {
                        // SHIELD: Clean up previous artifact before creating a new one
                        lastSharedArtifactUri?.let { oldUri ->
                            try { context.contentResolver.delete(oldUri, null, null) } catch (e: Exception) { /* Ignore */ }
                        }

                        val view = (context as Activity).window.decorView
                        val bitmap = captureComposable(view, (context as Activity).window)
                        if (bitmap != null) {
                            val uri = settingsViewModel.saveScreenshot(bitmap)
                            if (uri != null) {
                                lastSharedArtifactUri = uri
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Seating Chart Screenshot"))
                            } else {
                                Toast.makeText(context, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                editModeEnabled = editModeEnabled,
                seatingChartViewModel = seatingChartViewModel,
                settingsViewModel = settingsViewModel,
                onShowSaveLayout = { showSaveLayoutDialog = true },
                onShowLoadLayout = { showLoadLayoutDialog = true },
                onShowExport = { showExportDialog = true },
                onImportJson = onImportJson,
                onImportFromPythonAssets = { seatingChartViewModel.importFromPythonAssets(context) },
                onImportStudentsFromExcel = onImportStudentsFromExcel,
                onOpenLastExportFolder = { path ->
                    lastExportPath?.let { uriPath ->
                        val uri = uriPath.toUri()
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "vnd.android.document/directory")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                    }
                },
                onOpenAppDataFolder = onOpenAppDataFolder,
                onShareDatabase = {
                    coroutineScope.launch {
                        settingsViewModel.shareDatabase()?.let { uri ->
                            val intent = Intent(Intent.ACTION_SEND).apply { type = "application/octet-stream"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                            context.startActivity(Intent.createChooser(intent, "Share Database"))
                        }
                    }
                },
                lastExportPath = lastExportPath,
                selectedStudentUiItemForAction = selectedStudentUiItemForAction,
                onNeuralOracleClick = {
                    showGhostOracleDialog = true
                },
                isHudActive = isHudActive,
                onToggleHud = { isHudActive = !isHudActive },
                isStrategistActive = isStrategistActive,
                onToggleStrategist = {
                    isStrategistActive = !isStrategistActive
                    if (isStrategistActive) seatingChartViewModel.runStrategistSynthesis()
                },
                strategistGoal = strategistGoal,
                onStrategistGoalChange = { seatingChartViewModel.setStrategistGoal(it) },
                isChronosActive = isChronosActive,
                onToggleChronos = { isChronosActive = !isChronosActive },
                isHologramActive = isHologramActive,
                onToggleHologram = { isHologramActive = !isHologramActive },
                isPhantasmActive = isPhantasmActive,
                onTogglePhantasm = { isPhantasmActive = !isPhantasmActive },
                isVectorActive = isVectorActive,
                onToggleVector = { isVectorActive = !isVectorActive },
                isSpectraActive = isSpectraActive,
                onToggleSpectra = { isSpectraActive = !isSpectraActive },
                isAuroraActive = isAuroraActive,
                onToggleAurora = { isAuroraActive = !isAuroraActive },
                isNebulaActive = isNebulaActive,
                onToggleNebula = { isNebulaActive = !isNebulaActive },
                isLensActive = isLensActive,
                onToggleLens = { isLensActive = !isLensActive },
                isFluxActive = isFluxActive,
                onToggleFlux = { isFluxActive = !isFluxActive },
                isSingularityActive = isSingularityActive,
                onToggleSingularity = { isSingularityActive = !isSingularityActive },
                isPhasingActive = isPhasingActive,
                onTogglePhasing = {
                    isPhasingActive = !isPhasingActive
                    if (isPhasingActive) ghostPhasingEngine.triggerPulse()
                },
                isPulseActive = isPulseActive,
                onTogglePulse = { isPulseActive = !isPulseActive },
                isIrisActive = isIrisActive,
                onToggleIris = { isIrisActive = !isIrisActive },
                isWarpActive = isWarpActive,
                onToggleWarp = { isWarpActive = !isWarpActive },
                isFutureActive = isFutureActive,
                onToggleFuture = { isFutureActive = !isFutureActive },
                isSparkActive = isSparkActive,
                onToggleSpark = { isSparkActive = !isSparkActive },
                isOsmosisActive = isOsmosisActive,
                onToggleOsmosis = { isOsmosisActive = !isOsmosisActive },
                isEntanglementActive = isEntanglementActive,
                onToggleEntanglement = { isEntanglementActive = !isEntanglementActive },
                isSyncActive = isSyncActive,
                onToggleSync = { isSyncActive = !isSyncActive },
                isIonActive = isIonActive,
                onToggleIon = { isIonActive = !isIonActive },
                isEntropyActive = isEntropyActive,
                onToggleEntropy = { isEntropyActive = !isEntropyActive },
                isZenithActive = isZenithActive,
                onToggleZenith = { isZenithActive = !isZenithActive },
                isHorizonActive = isHorizonActive,
                onToggleHorizon = { isHorizonActive = !isHorizonActive },
                isGlitchActive = isGlitchActive,
                onToggleGlitch = { isGlitchActive = !isGlitchActive },
                isHelixActive = isHelixActive,
                onToggleHelix = { isHelixActive = !isHelixActive },
                isMagnetarActive = isMagnetarActive,
                onToggleMagnetar = { isMagnetarActive = !isMagnetarActive },
                isEmergenceActive = isEmergenceActive,
                onToggleEmergence = { isEmergenceActive = !isEmergenceActive },
                isCatalystActive = isCatalystActive,
                onToggleCatalyst = { isCatalystActive = !isCatalystActive },
                isFloraActive = isFloraActive,
                onToggleFlora = { isFloraActive = !isFloraActive },
                isTectonicsActive = isTectonicsActive,
                onToggleTectonics = { isTectonicsActive = !isTectonicsActive },
                isPulsarActive = isPulsarActive,
                onTogglePulsar = { isPulsarActive = !isPulsarActive },
                isCortexActive = isCortexActive,
                onToggleCortex = { isCortexActive = !isCortexActive },
                isVortexActive = isVortexActive,
                onToggleVortex = { isVortexActive = !isVortexActive },
                isOrbitActive = isOrbitActive,
                onToggleOrbit = { isOrbitActive = !isOrbitActive },
                isQuasarActive = isQuasarActive,
                onToggleQuasar = { isQuasarActive = !isQuasarActive },
                isSpotlightActive = isSpotlightActive,
                onToggleSpotlight = { isSpotlightActive = !isSpotlightActive },
                isNavigatorActive = isNavigatorActive,
                onToggleNavigator = { isNavigatorActive = !isNavigatorActive },
                isAdaptiveActive = isAdaptiveActive,
                onToggleAdaptive = { isAdaptiveActive = !isAdaptiveActive },
                isGlyphActive = isGlyphActive,
                onToggleGlyph = { isGlyphActive = !isGlyphActive },
                isArchitectActive = isArchitectActive,
                onToggleArchitect = { isArchitectActive = !isArchitectActive },
                onArchitectGoalChange = { architectGoal = it },
                isVisionActive = isVisionActive,
                onToggleVision = { isVisionActive = !isVisionActive },
                isMagnetarActive = isMagnetarActive,
                onToggleMagnetar = { isMagnetarActive = !isMagnetarActive },
                isSupernovaActive = isSupernovaActive,
                onToggleSupernova = { isSupernovaActive = !isSupernovaActive },
                onExportBlueprint = {
                    coroutineScope.launch {
                        // SHIELD: Clean up previous artifact before creating a new one
                        lastSharedArtifactUri?.let { oldUri ->
                            try { context.contentResolver.delete(oldUri, null, null) } catch (e: Exception) { /* Ignore */ }
                        }

                        val svgContent = GhostBlueprintEngine.generateBlueprint(students, furniture)
                        val uri = settingsViewModel.saveBlueprint(svgContent)
                        if (uri != null) {
                            lastSharedArtifactUri = uri
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/svg+xml"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Blueprint SVG"))
                        } else {
                            Toast.makeText(context, "Failed to generate blueprint", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.VOICE_ASSISTANT_ENABLED) {
                    FloatingActionButton(
                        onClick = { /* Handle tap if needed, or just rely on long press */ },
                        containerColor = if (isGhostListening) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        ghostVoiceAssistant.startListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                onTap = {
                                    if (isGhostListening) {
                                        ghostVoiceAssistant.stopListening()
                                    } else {
                                        Toast.makeText(context, "Hold to speak commands", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = "Ghost Voice Assistant")
                    }
                }

                if (editModeEnabled) {
                    AnimatedVisibility(visible = isFabMenuOpen) {
                        FloatingActionButton(onClick = { editingStudent = null; showAddEditStudentDialog = true; isFabMenuOpen = false }) {
                            Icon(Icons.Default.Person, contentDescription = "Add Student")
                        }
                    }
                    AnimatedVisibility(visible = isFabMenuOpen) {
                        FloatingActionButton(onClick = { editingFurniture = null; showAddEditFurnitureDialog = true; isFabMenuOpen = false }) {
                            Icon(Icons.Default.Chair, contentDescription = "Add Furniture")
                        }
                    }
                    FloatingActionButton(onClick = { isFabMenuOpen = !isFabMenuOpen }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                }
            }
        }
    ) { paddingValues ->
        val onStudentClick: (StudentUiItem) -> Unit = { studentItem ->
            if (selectMode) {
                val itemId = ChartItemId(studentItem.id, ItemType.STUDENT)
                val currentSelected = selectedItemIds.toMutableSet()
                if (currentSelected.contains(itemId)) {
                    currentSelected.remove(itemId)
                } else {
                    currentSelected.add(itemId)
                }
                seatingChartViewModel.selectedItemIds.value = currentSelected
            } else {
                selectedStudentUiItemForAction = studentItem
                when (sessionType) {
                    SessionType.BEHAVIOR -> showBehaviorDialog = true
                    SessionType.QUIZ -> if (seatingChartViewModel.isSessionActive.value == true) showLiveQuizMarkDialog = true else showLogQuizScoreDialog = true
                    SessionType.HOMEWORK -> if (seatingChartViewModel.isSessionActive.value == true) showLiveHomeworkMarkDialog = true else showAdvancedHomeworkLogDialog = true
                }
            }
        }

        val onStudentLongClick: (StudentUiItem, Offset) -> Unit = { studentItem, pos ->
            selectedStudentUiItemForAction = studentItem
            studentHubPosition = pos
            isStudentHubVisible = true
        }

        val onFurnitureClick: (com.example.myapplication.ui.model.FurnitureUiItem) -> Unit = { furnitureItem ->
            if (selectMode) {
                val itemId = ChartItemId(furnitureItem.id, ItemType.FURNITURE)
                val currentSelected = selectedItemIds.toMutableSet()
                if (currentSelected.contains(itemId)) {
                    currentSelected.remove(itemId)
                } else {
                    currentSelected.add(itemId)
                }
                seatingChartViewModel.selectedItemIds.value = currentSelected
            }
        }

        val onFurnitureLongClick: (com.example.myapplication.ui.model.FurnitureUiItem) -> Unit = { furnitureItem ->
            coroutineScope.launch {
                editingFurniture = seatingChartViewModel.getFurnitureById(furnitureItem.id)
                showAddEditFurnitureDialog = true
            }
        }

        /**
         * Root Layout Container: Orchestrates the layered composition of the seating chart.
         * The order of items in this Box determines their Z-index.
         *
         * ### Layer Order (Bottom to Top):
         * 1. **Atmospheric Layers**: Horizon, Zenith, Phasing background.
         * 2. **Social Dynamics (Experimental)**: Warp, Future, Aurora, Nebula, etc.
         * 3. **Environmental Framework**: Grid, Rulers, Guides.
         * 4. **Core Content**: Students and Furniture (wrapped in Pan/Zoom container).
         * 5. **Refraction & HUD Layers**: Ghost Lens, HUD, Oracle, Voice Assist overlays.
         */
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            GhostGlitchLayer(
                intensity = glitchIntensity,
                isActive = isGlitchActive
            )
            Box(
                modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { pos ->
                            ghostHubPosition = pos
                            isGhostHubVisible = true
                        }
                    )
                }
                .onSizeChanged { canvasSize = it }
                .then(
                    if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.PORTAL_MODE_ENABLED) {
                        Modifier.dragAndDropTarget(
                            shouldStartDragAndDrop = { event ->
                                event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                            },
                            target = portalTarget
                        )
                    } else Modifier
                )

        ) {
            GhostHorizonLayer(engine = ghostHorizonEngine, isActive = isHorizonActive)
            GhostLassoLayer(
                students = students,
                canvasScale = scale,
                canvasOffset = offset,
                isActive = isLassoActive,
                onSelectionChange = { ids ->
                    selectMode = true
                    val currentSelected = selectedItemIds.toMutableSet()
                    ids.forEach { id ->
                        currentSelected.add(ChartItemId(id.toInt(), ItemType.STUDENT))
                    }
                    seatingChartViewModel.selectedItemIds.value = currentSelected
                }
            )
            GhostAdaptiveLayer(zones = adaptiveZones, isActive = isAdaptiveActive)
            GhostSilhouetteLayer(
                silhouettes = draggingSilhouettes,
                canvasScale = scale,
                canvasOffset = offset,
                isActive = isSilhouetteActive
            )
            GhostArchitectLayer(
                students = students,
                edges = latticeEdges,
                behaviorLogs = allBehaviorEvents,
                goal = architectGoal,
                canvasScale = scale,
                canvasOffset = offset,
                isActive = isArchitectActive
            )
            GhostCortexLayer(engine = ghostCortexEngine, isActive = isCortexActive)
            GhostSupernovaLayer(engine = ghostSupernovaEngine, isActive = isSupernovaActive)
            GhostVortexLayer(
                vortices = vortices,
                canvasScale = scale,
                canvasOffset = offset,
                isActive = isVortexActive
            )
            GhostOrbitLayer(
                students = students,
                behaviorLogs = allBehaviorEvents,
                isActive = isOrbitActive
            )
            GhostRayLayer(
                engine = ghostRayEngine,
                students = students,
                canvasScale = scale,
                canvasOffset = offset,
                isActive = isRayActive
            )
            GhostVisionLayer(
                engine = ghostVisionEngine,
                students = students,
                isActive = isVisionActive
            )
            GhostGlyphLayer(
                students = students,
                canvasScale = scale,
                canvasOffset = offset,
                isActive = isGlyphActive,
                onLogBehavior = { id, type ->
                    seatingChartViewModel.addBehaviorEvent(BehaviorEvent(studentId = id, type = type, timestamp = System.currentTimeMillis()))
                },
                onLogAcademic = { id, type ->
                    seatingChartViewModel.saveQuizLog(com.example.myapplication.data.QuizLog(studentId = id, quizName = type, markValue = 100.0, maxMarkValue = 100.0, timestamp = System.currentTimeMillis()))
                }
            )
            GhostQuasarLayer(
                students = students,
                behaviorLogs = allBehaviorEvents,
                canvasScale = scale,
                canvasOffset = offset,
                isActive = isQuasarActive
            )
            GhostZenithLayer(engine = ghostZenithEngine) { zenithScope ->
            GhostPhasingLayer(engine = ghostPhasingEngine) {
                if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.PORTAL_MODE_ENABLED) {
                GhostPortalLayer(
                    isDraggingActive = isDraggingPortalActive,
                    portalPosition = if (portalPosition == Offset.Zero)
                        Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                        else portalPosition
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.WARP_MODE_ENABLED && isWarpActive) {
                GhostWarpLayer(
                    students = students,
                    behaviorLogs = allBehaviorEvents,
                    canvasScale = scale,
                    canvasOffset = offset
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.SYNC_MODE_ENABLED) {
                GhostSyncLayer(
                    students = students,
                    syncLinks = syncLinks,
                    canvasScale = scale,
                    canvasOffset = offset,
                    isActive = isSyncActive
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.FUTURE_MODE_ENABLED && isFutureActive) {
                GhostFutureLayer(
                    students = students,
                    futureEvents = futureEvents,
                    isFutureActive = isFutureActive,
                    canvasScale = scale,
                    canvasOffset = offset
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.AURORA_MODE_ENABLED && isAuroraActive) {
                GhostAuroraLayer(
                    behaviorLogs = allBehaviorEvents,
                    quizLogs = allQuizLogs,
                    homeworkLogs = allHomeworkLogs
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.NEBULA_MODE_ENABLED && isNebulaActive) {
                GhostNebulaLayer(
                    students = students,
                    behaviorLogs = allBehaviorEvents
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.SPARK_MODE_ENABLED && isSparkActive) {
                GhostSparkLayer(
                    engine = ghostSparkEngine,
                    students = students,
                    canvasScale = scale,
                    canvasOffset = offset
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.OSMOSIS_MODE_ENABLED && isOsmosisActive) {
                val osmoticNodes = remember(students) {
                    students.mapNotNull { it.osmoticNode.value }
                }
                GhostOsmosisLayer(students = osmoticNodes)
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.ENTANGLEMENT_MODE_ENABLED && isEntanglementActive) {
                GhostEntanglementLayer(
                    students = students,
                    entangledLinks = entangledLinks,
                    canvasScale = scale,
                    canvasOffset = offset,
                    isEntanglementActive = isEntanglementActive
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.ION_MODE_ENABLED && isIonActive) {
                GhostIonLayer(
                    students = students,
                    globalBalance = globalIonBalance,
                    canvasScale = scale,
                    canvasOffset = offset
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.CATALYST_MODE_ENABLED && isCatalystActive) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    GhostCatalystLayer(
                        students = students,
                        reactions = catalystReactions,
                        canvasScale = scale,
                        canvasOffset = offset,
                        isActive = isCatalystActive
                    )
                }
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.FLORA_MODE_ENABLED && isFloraActive) {
                GhostFloraLayer(
                    students = students,
                    behaviorLogs = allBehaviorEvents,
                    quizLogs = allQuizLogs,
                    homeworkLogs = allHomeworkLogs,
                    canvasScale = scale,
                    canvasOffset = offset,
                    isActive = isFloraActive
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.TECTONICS_MODE_ENABLED && isTectonicsActive) {
                GhostTectonicLayer(
                    students = students,
                    behaviorLogs = allBehaviorEvents,
                    canvasScale = scale,
                    canvasOffset = offset,
                    isActive = isTectonicsActive
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.PULSAR_MODE_ENABLED && isPulsarActive) {
                GhostPulsarLayer(
                    students = students,
                    behaviorLogs = allBehaviorEvents,
                    canvasScale = scale,
                    canvasOffset = offset,
                    isActive = isPulsarActive
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.MAGNETAR_MODE_ENABLED && isMagnetarActive) {
                GhostMagnetarLayer(
                    engine = ghostMagnetarEngine,
                    students = students,
                    behaviorLogs = allBehaviorEvents,
                    canvasScale = scale,
                    canvasOffset = offset,
                    isActive = isMagnetarActive
                )
            }


            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.SPECTRA_MODE_ENABLED && isSpectraActive) {
                GhostSpectraLayer(
                    density = spectralDensity,
                    agitation = agitation
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.FLUX_MODE_ENABLED && isFluxActive) {
                GhostFluxLayer(
                    students = students,
                    behaviorLogs = allBehaviorEvents,
                    canvasScale = scale,
                    canvasOffset = offset
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.SINGULARITY_MODE_ENABLED && isSingularityActive) {
                GhostSingularityLayer(
                    students = students,
                    isSingularityActive = isSingularityActive
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.PULSE_MODE_ENABLED && isPulseActive) {
                GhostPulseLayer(
                    students = students,
                    behaviorLogs = allBehaviorEvents,
                    canvasScale = scale,
                    canvasOffset = offset
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.PHANTASM_MODE_ENABLED && isPhantasmActive) {
                GhostPhantasmLayer(
                    students = students,
                    behaviorLogs = allBehaviorEvents,
                    canvasScale = scale,
                    canvasOffset = offset,
                    isRecording = isScreenRecording
                )

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.SPOTLIGHT_MODE_ENABLED) {
                GhostSpotlightLayer(
                    targetStudent = selectedStudentUiItemForAction,
                    isActive = isSpotlightActive,
                    canvasScale = scale,
                    canvasOffset = offset
                )
            }
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.LATTICE_MODE_ENABLED) {
                GhostLatticeLayer(
                    students = students,
                    edges = latticeEdges,
                    canvasScale = scale,
                    canvasOffset = offset
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.VECTOR_MODE_ENABLED && isVectorActive) {
                GhostVectorLayer(
                    students = students,
                    vectors = socialVectors,
                    canvasScale = scale,
                    canvasOffset = offset
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.ECHO_MODE_ENABLED) {
                GhostEchoLayer(engine = ghostEchoEngine)
            }

            GridAndRulers(
                settingsViewModel = settingsViewModel,
                seatingChartViewModel = seatingChartViewModel,
                scale = scale,
                offset = offset,
                canvasSize = androidx.compose.ui.geometry.Size(canvasSize.width.toFloat(), canvasSize.height.toFloat())
            )

            // Main Content Rendering
            val chartContent = @Composable {
                SeatingChartContent(
                    scale = scale,
                    offset = offset,
                    onTransformChange = { s, o -> scale = s; offset = o },
                    canvasSize = canvasSize,
                    students = students,
                    furniture = furniture,
                    selectedItemIds = selectedItemIds,
                    selectMode = selectMode,
                    sessionType = sessionType,
                    editModeEnabled = editModeEnabled,
                    userPreferences = userPreferences,
                    showRecentBehavior = showRecentBehavior,
                    quizLogFontColor = quizLogFontColor,
                    homeworkLogFontColor = homeworkLogFontColor,
                    quizLogFontBold = quizLogFontBold,
                    homeworkLogFontBold = homeworkLogFontBold,
                    isIrisActive = isIrisActive,
                    allBehaviorEvents = allBehaviorEvents,
                    allQuizLogs = allQuizLogs,
                    allHomeworkLogs = allHomeworkLogs,
                    onStudentClick = onStudentClick,
                    onStudentLongClick = onStudentLongClick,
                    onFurnitureClick = onFurnitureClick,
                    onFurnitureLongClick = onFurnitureLongClick,
                    seatingChartViewModel = seatingChartViewModel,
                    isZenithActive = isZenithActive,
                    zenithScope = zenithScope
                )
            }

            val seatingChartWithEntropy = @Composable {
                if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.ENTROPY_MODE_ENABLED && isEntropyActive) {
                    GhostEntropyLayer(
                        students = students,
                        behaviorLogs = allBehaviorEvents,
                        quizLogs = allQuizLogs,
                        canvasScale = scale,
                        canvasOffset = offset,
                        isActive = isEntropyActive
                    ) {
                        chartContent()
                    }
                } else {
                    chartContent()
                }
            }

            val seatingChartWithLens = @Composable {
                if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.LENS_MODE_ENABLED && isLensActive) {
                    GhostLensLayer(
                        engine = ghostLensEngine,
                        students = students,
                        allProphecies = allProphecies,
                        canvasScale = scale,
                        canvasOffset = offset
                    ) {
                        if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.HOLOGRAM_MODE_ENABLED && isHologramActive) {
                            GhostHologramLayer(engine = ghostHologramEngine) {
                                seatingChartWithEntropy()
                            }
                        } else {
                            seatingChartWithEntropy()
                        }
                    }
                } else {
                    if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.HOLOGRAM_MODE_ENABLED && isHologramActive) {
                        GhostHologramLayer(engine = ghostHologramEngine) {
                            seatingChartWithEntropy()
                        }
                    } else {
                        seatingChartWithEntropy()
                    }
                }
            }

            seatingChartWithLens()

            // Ghost Overlays
            if (GhostConfig.GHOST_MODE_ENABLED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (GhostConfig.EMERGENCE_MODE_ENABLED && isEmergenceActive) {
                        GhostEmergenceLayer(
                            engine = ghostEmergenceEngine,
                            students = students,
                            behaviorLogs = allBehaviorEvents
                        )
                    }
                }

                if (GhostConfig.CHRONOS_MODE_ENABLED && isChronosActive) {
                    GhostChronosLayer(
                        heatmapGrid = chronosHeatmap,
                        canvasScale = scale,
                        canvasOffset = offset
                    )
                }

                NeuralMapLayer(
                    students = students,
                    behaviorLogs = allBehaviorEvents,
                    canvasScale = scale,
                    canvasOffset = offset
                )

                if (isHudActive) {
                    GhostHUDLayer(
                        hudViewModel = hudViewModel,
                        students = students,
                        prophecies = allProphecies
                    )
                }

                GhostStrategistLayer(
                    interventions = strategistInterventions,
                    isActive = isStrategistActive,
                    isThinking = isStrategistThinking
                )
            }

            if (showGhostSynapseDialog) {
                selectedStudentUiItemForAction?.let { student ->
                    val behavior = allBehaviorEvents.filter { it.studentId == student.id.toLong() }
                    val quiz = allQuizLogs.filter { it.studentId == student.id.toLong() }
                    val homework = allHomeworkLogs.filter { it.studentId == student.id.toLong() }

                    GhostSynapseDialog(
                        studentName = student.fullName.value,
                        behaviorLogs = behavior,
                        quizLogs = quiz,
                        homeworkLogs = homework,
                        onDismiss = { showGhostSynapseDialog = false }
                    )
                }
            }


            if (showSaveLayoutDialog) {
                SaveLayoutDialog(onDismiss = { showSaveLayoutDialog = false }, onSave = { name -> seatingChartViewModel.saveLayout(name); showSaveLayoutDialog = false })
            }

            if (showLoadLayoutDialog) {
                LoadLayoutDialog(layouts = layouts, onDismiss = { showLoadLayoutDialog = false }, onLoad = { layout -> seatingChartViewModel.loadLayout(layout); showLoadLayoutDialog = false }, onDelete = { layout -> seatingChartViewModel.deleteLayoutTemplate(layout) })
            }

            if (showStudentActionMenu) {
                selectedStudentUiItemForAction?.let { student ->
                    val groups by studentGroupsViewModel.allStudentGroups.collectAsState(initial = emptyList())
                    var showGroupMenu by remember { mutableStateOf(false) }
                    var showBehaviorLogViewer by remember { mutableStateOf(false) }

                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { showStudentActionMenu = false },
                        offset = DpOffset(longPressPosition.x.dp, longPressPosition.y.dp)
                    ) {
                        if (GhostConfig.GHOST_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text("Neural Insight 👻") },
                                onClick = {
                                    val behavior = allBehaviorEvents.filter { it.studentId == student.id.toLong() }
                                    val quiz = allQuizLogs.filter { it.studentId == student.id.toLong() }
                                    val homework = allHomeworkLogs.filter { it.studentId == student.id.toLong() }

                                    currentGhostInsight = GhostInsightEngine.generateInsight(
                                        student.fullName.value, behavior, quiz, homework
                                    )
                                    showGhostInsightDialog = true
                                    showStudentActionMenu = false
                                }
                            )
                        }
                        if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.SYNAPSE_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text("Neural Synapse 🧠") },
                                onClick = {
                                    showGhostSynapseDialog = true
                                    showStudentActionMenu = false
                                }
                            )
                        }
                        DropdownMenuItem(text = { Text("Edit Student") }, onClick = {
                            coroutineScope.launch {
                                editingStudent =
                                    seatingChartViewModel.getStudentForEditing(student.id.toLong())
                                showAddEditStudentDialog = true
                            }
                            showStudentActionMenu = false
                        })
                        DropdownMenuItem(
                            text = { Text("Delete Student") },
                            onClick = {
                                seatingChartViewModel.deleteSelectedItems(setOf(ChartItemId(student.id, ItemType.STUDENT)))
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Log Behavior") },
                            onClick = {
                                showBehaviorDialog = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("View Behavior Log") },
                            onClick = {
                                showBehaviorLogViewer = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Log Homework") },
                            onClick = {
                                showAdvancedHomeworkLogDialog = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Log Quiz Score") },
                            onClick = {
                                showLogQuizScoreDialog = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Assign Task") },
                            onClick = {
                                showAssignTaskDialog = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Change Student Box Style") },
                            onClick = {
                                showStudentStyleDialog = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Clear Recent Logs") },
                            onClick = {
                                seatingChartViewModel.clearRecentLogsForStudent(student.id.toLong())
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Show Recent Logs") },
                            onClick = {
                                seatingChartViewModel.showRecentLogsForStudent(student.id.toLong())
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Assign to Group") },
                            onClick = { showGroupMenu = true })
                        if (student.groupId.value != null) {
                            DropdownMenuItem(text = { Text("Remove from Group") }, onClick = {
                                seatingChartViewModel.removeStudentFromGroup(student.id.toLong())
                                showStudentActionMenu = false
                            })
                        }
                    }
                    if (showBehaviorLogViewer) {
                        BehaviorLogViewerDialog(
                            studentId = student.id.toLong(),
                            viewModel = seatingChartViewModel,
                            onDismiss = { showBehaviorLogViewer = false }
                        )
                    }
                    DropdownMenu(
                        expanded = showGroupMenu,
                        onDismissRequest = { showGroupMenu = false }) {
                        groups.forEach { group ->
                            DropdownMenuItem(text = { Text(group.name) }, onClick = {
                                seatingChartViewModel.assignStudentToGroup(
                                    student.id.toLong(),
                                    group.id
                                )
                                showGroupMenu = false
                                showStudentActionMenu = false
                            })
                        }
                    }
                }
            }

            if (showBehaviorDialog) {
                val studentIds = if (selectMode) selectedItemIds.filter { it.type == ItemType.STUDENT }.map { it.id.toLong() } else listOfNotNull(selectedStudentUiItemForAction?.id?.toLong())
                BehaviorDialog(
                    studentIds = studentIds,
                    viewModel = seatingChartViewModel,
                    behaviorTypes = behaviorTypeNames,
                    onDismiss = { showBehaviorDialog = false; selectedStudentUiItemForAction = null },
                    onBehaviorLogged = { count ->
                    if (isSparkActive) {
                        val targets = if (selectMode) selectedItemIds.filter { it.type == ItemType.STUDENT }.map { it.id.toLong() } else listOfNotNull(selectedStudentUiItemForAction?.id?.toLong())
                        targets.forEach { id ->
                            val s = students.find { it.id.toLong() == id }
                            if (s != null) {
                                ghostSparkEngine.emit(s.xPosition.value, s.yPosition.value, "Behavior")
                            }
                        }
                    }
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Logged behavior for $count student(s)",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                repeat(count) { seatingChartViewModel.undo() }
                            }
                        }
                    }
                )
            }

            if (showLogQuizScoreDialog) {
                val studentIds = if (selectMode) selectedItemIds.filter { it.type == ItemType.STUDENT }.map { it.id.toLong() } else listOfNotNull(selectedStudentUiItemForAction?.id?.toLong())
                LogQuizScoreDialog(studentIds = studentIds, viewModel = seatingChartViewModel, settingsViewModel = settingsViewModel, onDismissRequest = { showLogQuizScoreDialog = false; selectedStudentUiItemForAction = null }, onSave = { quizLogs ->
                    if (sessionType == SessionType.QUIZ) quizLogs.forEach { seatingChartViewModel.addQuizLogToSession(it) } else quizLogs.forEach { seatingChartViewModel.saveQuizLog(it) }
                })
            }

            if (showAdvancedHomeworkLogDialog) {
                val studentIds = if (selectMode) selectedItemIds.filter { it.type == ItemType.STUDENT }.map { it.id.toLong() } else listOfNotNull(selectedStudentUiItemForAction?.id?.toLong())
                AdvancedHomeworkLogDialog(studentIds = studentIds, viewModel = seatingChartViewModel, settingsViewModel = settingsViewModel, onDismissRequest = { showAdvancedHomeworkLogDialog = false; selectedStudentUiItemForAction = null }, onSave = { homeworkLogs ->
                    if (sessionType == SessionType.HOMEWORK) homeworkLogs.forEach { seatingChartViewModel.addHomeworkLogToSession(it) } else homeworkLogs.forEach { seatingChartViewModel.addHomeworkLog(it) }
                })
            }

            if (showLiveQuizMarkDialog) {
                selectedStudentUiItemForAction?.let { student ->
                    LiveQuizMarkDialog(
                        studentId = student.id.toLong(),
                        viewModel = seatingChartViewModel,
                        onDismissRequest = { showLiveQuizMarkDialog = false; selectedStudentUiItemForAction = null },
                        onSave = { quizLog -> seatingChartViewModel.addQuizLogToSession(quizLog) }
                    )
                }
            }

            if (showLiveHomeworkMarkDialog) {
                selectedStudentUiItemForAction?.let { student ->
                    LiveHomeworkMarkDialog(
                        studentId = student.id.toLong(),
                        viewModel = seatingChartViewModel,
                        settingsViewModel = settingsViewModel,
                        onDismissRequest = { showLiveHomeworkMarkDialog = false; selectedStudentUiItemForAction = null },
                        onSave = { homeworkLog -> seatingChartViewModel.addHomeworkLogToSession(homeworkLog) }
                    )
                }
            }

            if (showAddEditStudentDialog) {
                AddEditStudentDialog(
                    studentToEdit = editingStudent,
                    viewModel = seatingChartViewModel,
                    studentGroupsViewModel = studentGroupsViewModel,
                    settingsViewModel = settingsViewModel,
                    onDismiss = { showAddEditStudentDialog = false; editingStudent = null },
                    onEditStyle = {
                        showAddEditStudentDialog = false
                        showStudentStyleDialog = true
                    }
                )
            }

            if (showAddEditFurnitureDialog) {
                AddEditFurnitureDialog(furnitureToEdit = editingFurniture, viewModel = seatingChartViewModel, settingsViewModel = settingsViewModel, onDismiss = { showAddEditFurnitureDialog = false; editingFurniture = null })
            }

            if (showExportDialog) {
                ExportDialog(
                    viewModel = seatingChartViewModel,
                    settingsViewModel = settingsViewModel,
                    onDismissRequest = { showExportDialog = false },
                    onExport = { options, share ->
                        seatingChartViewModel.pendingExportOptions = options
                        if (share) {
                            onShowEmailDialogChange(true)
                        } else {
                            createDocumentLauncher.launch("seating_chart_export.xlsx")
                        }
                        showExportDialog = false
                    }
                )
            }

            if (showEmailDialog) {
                val activity = (context as? MainActivity)
                val from by settingsViewModel.defaultEmailAddress.collectAsState()
                val emailPassword by settingsViewModel.emailPassword.collectAsState()
                val smtpSettings by settingsViewModel.smtpSettings.collectAsState()
                EmailDialog(
                    fromAddress = from,
                    onDismissRequest = { onShowEmailDialogChange(false) },
                    onSend = { to, subject, body ->
                        activity?.let { mainActivity ->
                            mainActivity.lifecycleScope.launch {
                                val emailUtil = EmailUtil(mainActivity)
                                // HARDEN: Create temporary file in the restricted shared cache directory
                                val sharedDir = File(mainActivity.cacheDir, "shared")
                                if (!sharedDir.exists()) sharedDir.mkdirs()
                                val file = File.createTempFile("export_", ".xlsx", sharedDir)
                                var successfullyHandedOff = false
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        mainActivity,
                                        "com.example.myapplication.fileprovider",
                                        file
                                    )
                                    seatingChartViewModel.pendingExportOptions?.let { options ->
                                        val result = seatingChartViewModel.exportData(
                                            context = mainActivity,
                                            uri = uri,
                                            options = options
                                        )
                                        if (result.isSuccess) {
                                            try {
                                                emailUtil.sendEmailWithRetry(
                                                    from = from,
                                                    password = emailPassword,
                                                    to = to,
                                                    subject = subject,
                                                    body = body,
                                                    attachmentPath = file.absolutePath,
                                                    smtpSettings = smtpSettings
                                                )
                                                successfullyHandedOff = true
                                                Toast.makeText(mainActivity, "Email sent!", Toast.LENGTH_SHORT).show()
                                            } catch (e: EmailException) {
                                                Toast.makeText(mainActivity, "Email failed to send: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            Toast.makeText(mainActivity, "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } finally {
                                    // HARDEN: If the export failed or the hand-off to WorkManager was interrupted,
                                    // ensure the temporary file is deleted immediately to prevent PII leakage.
                                    if (!successfullyHandedOff && file.exists()) {
                                        file.delete()
                                    }
                                    onShowEmailDialogChange(false)
                                }
                            }
                        }
                    }
                )
            }

            if (showChangeBoxSizeDialog) {
                ChangeBoxSizeDialog(onDismissRequest = { showChangeBoxSizeDialog = false }, onSave = { width, height -> seatingChartViewModel.changeBoxSize(selectedItemIds, width, height); showChangeBoxSizeDialog = false })
            }

            if (showStudentStyleDialog) {
                val studentId = selectedStudentUiItemForAction?.id?.toLong() ?: editingStudent?.id
                if (studentId != null) {
                    StudentStyleScreen(studentId = studentId, seatingChartViewModel = seatingChartViewModel, onDismiss = { showStudentStyleDialog = false; if (editingStudent != null) editingStudent = null })
                }
            }

            if (showAssignTaskDialog) {
                selectedStudentUiItemForAction?.let { student ->
                    val systemBehaviors by seatingChartViewModel.allSystemBehaviors.observeAsState(initial = emptyList())
                    AssignTaskDialog(
                        studentId = student.id.toLong(),
                        viewModel = seatingChartViewModel,
                        systemBehaviors = systemBehaviors,
                        onDismissRequest = { showAssignTaskDialog = false }
                    )
                }
            }

            if (showUndoHistoryDialog) {
                UndoHistoryDialog(
                    viewModel = seatingChartViewModel,
                    onDismissRequest = { showUndoHistoryDialog = false }
                )
            }

            if (showGhostInsightDialog) {
                currentGhostInsight?.let { insight ->
                    GhostInsightDialog(
                        insight = insight,
                        onDismiss = { showGhostInsightDialog = false }
                    )
                }
            }

            if (showGhostOracleDialog) {
                GhostOracleDialog(
                    prophecies = allProphecies,
                    onDismiss = { showGhostOracleDialog = false }
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.VOICE_ASSISTANT_ENABLED) {
                GhostVoiceVisualizer(
                    amplitude = ghostAmplitude,
                    isListening = isGhostListening,
                    currentText = ghostCurrentText
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED) {
                GhostStudentHubLayer(
                    isVisible = isStudentHubVisible,
                    position = studentHubPosition,
                    onActionSelected = { action ->
                        when (action.id) {
                            "LOG_BEHAVIOR" -> showBehaviorDialog = true
                            "NEURAL_INSIGHT" -> {
                                selectedStudentUiItemForAction?.let { student ->
                                    val behavior = allBehaviorEvents.filter { it.studentId == student.id.toLong() }
                                    val quiz = allQuizLogs.filter { it.studentId == student.id.toLong() }
                                    val homework = allHomeworkLogs.filter { it.studentId == student.id.toLong() }

                                    currentGhostInsight = GhostInsightEngine.generateInsight(
                                        student.fullName.value, behavior, quiz, homework
                                    )
                                    showGhostInsightDialog = true
                                }
                            }
                            "NEURAL_SYNAPSE" -> showGhostSynapseDialog = true
                            "LOG_ACADEMIC" -> {
                                if (seatingChartViewModel.isSessionActive.value == true) showLiveQuizMarkDialog = true else showLogQuizScoreDialog = true
                            }
                            "NEURAL_DOSSIER" -> {
                                selectedStudentUiItemForAction?.let { student ->
                                    val fullName = student.fullName.value
                                    val dossier = GhostLinkEngine.generateNeuralDossier(student.id.toLong(), fullName)

                                    // PRIVACY: Mask the student name in the intent subject (e.g., "J. DOE")
                                    val nameParts = fullName.trim().split(" ")
                                    val maskedName = if (nameParts.size >= 2) {
                                        "${nameParts.first().take(1)}. ${nameParts.last()}"
                                    } else {
                                        fullName
                                    }.uppercase(java.util.Locale.US)

                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Neural Dossier: $maskedName")
                                        putExtra(Intent.EXTRA_TEXT, dossier)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Neural Dossier"))
                                }
                            }
                            "NEURAL_MORPH" -> {
                                if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.MORPH_MODE_ENABLED) {
                                    selectedStudentUiItemForAction?.let { student ->
                                        val intent = Intent(context, com.example.myapplication.labs.ghost.morph.GhostMorphActivity::class.java).apply {
                                            putExtra("STUDENT_ID", student.id.toLong())
                                            // Pass triggering position for potential transition refinement
                                            putExtra("TRIGGER_X", studentHubPosition.x)
                                            putExtra("TRIGGER_Y", studentHubPosition.y)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }
                            "EDIT_STUDENT" -> {
                                coroutineScope.launch {
                                    selectedStudentUiItemForAction?.let { student ->
                                        editingStudent = seatingChartViewModel.getStudentForEditing(student.id.toLong())
                                        showAddEditStudentDialog = true
                                    }
                                }
                            }
                        }
                    },
                    onDismiss = { isStudentHubVisible = false }
                )

                GhostHubLayer(
                    isVisible = isGhostHubVisible,
                    position = ghostHubPosition,
                    onActionSelected = { action ->
                        when (action.id) {
                            "HUD" -> isHudActive = !isHudActive
                            "VISION" -> isVisionActive = !isVisionActive
                            "PHANTASM" -> isPhantasmActive = !isPhantasmActive
                            "SPECTRA" -> isSpectraActive = !isSpectraActive
                            "AURORA" -> isAuroraActive = !isAuroraActive
                            "FUTURE" -> isFutureActive = !isFutureActive
                            "STRATEGIST" -> {
                                isStrategistActive = !isStrategistActive
                                if (isStrategistActive) seatingChartViewModel.runStrategistSynthesis()
                            }
                            "SYNC" -> isSyncActive = !isSyncActive
                            "LASSO" -> isLassoActive = !isLassoActive
                        }
                    },
                    onDismiss = { isGhostHubVisible = false }
                )
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.NAVIGATOR_MODE_ENABLED) {
                GhostNavigatorLayer(
                    students = students,
                    scale = scale,
                    offset = offset,
                    containerSize = canvasSize,
                    onTeleport = { newOffset -> offset = newOffset },
                    isActive = isNavigatorActive
                )
            }

            // Ghost Glance Overlay
            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.GLANCE_MODE_ENABLED) {
                activeGlanceStudentId?.let { id ->
                    val student = students.find { it.id.toLong() == id }
                    if (student != null) {
                        val bLogs = allBehaviorEvents.filter { it.studentId == id }
                        val qLogs = allQuizLogs.filter { it.studentId == id }
                        val hLogs = allHomeworkLogs.filter { it.studentId == id }

                        val glanceState = remember(id, bLogs, qLogs, hLogs) {
                            GhostGlanceEngine.synthesize(bLogs, qLogs, hLogs)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { activeGlanceStudentId = null })
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            GhostGlanceSurface(
                                studentName = student.fullName.value,
                                state = glanceState
                            )
                        }
                    }
                }
            }
        }
    }
}
}
}


/**
 * Renders the core interactive layer of the seating chart, including students and furniture.
 *
 * This component manages the **Pan and Zoom** transformations using the `graphicsLayer`
 * and coordinate normalization for the 4000x4000 logical canvas.
 *
 * @param scale The current zoom factor.
 * @param offset The current pan offset (in screen pixels).
 * @param onTransformChange Callback for updating pan/zoom state during gestures.
 * @param zenithScope Optional scope for applying 3D spatial elevation in Zenith mode.
 */
@Composable
fun SeatingChartContent(
    scale: Float,
    offset: Offset,
    onTransformChange: (Float, Offset) -> Unit,
    canvasSize: IntSize,
    students: List<StudentUiItem>,
    furniture: List<com.example.myapplication.ui.model.FurnitureUiItem>,
    selectedItemIds: Set<ChartItemId>,
    selectMode: Boolean,
    sessionType: SessionType,
    editModeEnabled: Boolean,
    userPreferences: com.example.myapplication.preferences.UserPreferences?,
    showRecentBehavior: Boolean,
    quizLogFontColor: androidx.compose.ui.graphics.Color,
    homeworkLogFontColor: androidx.compose.ui.graphics.Color,
    quizLogFontBold: Boolean,
    homeworkLogFontBold: Boolean,
    isIrisActive: Boolean = false,
    isHelixActive: Boolean = false,
    allBehaviorEvents: List<BehaviorEvent> = emptyList(),
    allQuizLogs: List<com.example.myapplication.data.QuizLog> = emptyList(),
    allHomeworkLogs: List<com.example.myapplication.data.HomeworkLog> = emptyList(),
    onStudentClick: (StudentUiItem) -> Unit,
    onStudentLongClick: (StudentUiItem, Offset) -> Unit,
    onFurnitureClick: (com.example.myapplication.ui.model.FurnitureUiItem) -> Unit,
    onFurnitureLongClick: (com.example.myapplication.ui.model.FurnitureUiItem) -> Unit,
    seatingChartViewModel: SeatingChartViewModel,
    isZenithActive: Boolean = false,
    zenithScope: com.example.myapplication.labs.ghost.zenith.ZenithScope? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale
                    val newScale = (scale * zoom).coerceIn(0.5f, 5f)
                    val newOffset = (offset - centroid) * (newScale / oldScale) + centroid + pan
                    onTransformChange(newScale, newOffset)
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
    ) {
        Box(modifier = Modifier.size(4000.dp)) {
            val noAnimations = userPreferences?.noAnimations ?: false
            val gridSnapEnabled = userPreferences?.gridSnapEnabled ?: false
            val gridSize = userPreferences?.gridSize ?: 20
            val autoExpandEnabled = userPreferences?.autoExpandStudentBoxes ?: true

            // BOLT: Pre-calculate selection sets to transform O(N*S) lookup into O(1).
            val selectedStudentIds = remember(selectedItemIds) {
                selectedItemIds.filter { it.type == ItemType.STUDENT }.map { it.id }.toSet()
            }
            val selectedFurnitureIds = remember(selectedItemIds) {
                selectedItemIds.filter { it.type == ItemType.FURNITURE }.map { it.id }.toSet()
            }

            // BOLT: Replace forEach with manual index loops to eliminate iterator churn in the high-frequency rendering path.
            for (i in students.indices) {
                val studentItem = students[i]
                val irisParams = if (isIrisActive) studentItem.irisParams.value else null
                val altitude = if (isZenithActive) studentItem.altitude.value else 0f

                StudentDraggableIcon(
                    studentUiItem = studentItem,
                    viewModel = seatingChartViewModel,
                    showBehavior = showRecentBehavior,
                    isSelected = studentItem.id in selectedStudentIds,
                    onClick = { onStudentClick(studentItem) },
                    onLongClick = { pos -> onStudentLongClick(studentItem, pos) },
                    onResize = { w, h -> seatingChartViewModel.changeBoxSize(setOf(ChartItemId(studentItem.id, ItemType.STUDENT)), w.toInt(), h.toInt()) },
                    noAnimations = noAnimations,
                    editModeEnabled = editModeEnabled,
                    gridSnapEnabled = gridSnapEnabled,
                    gridSize = gridSize,
                    autoExpandEnabled = autoExpandEnabled,
                    canvasSize = canvasSize,
                    canvasScale = scale,
                    canvasOffset = offset,
                    isIrisActive = isIrisActive,
                    irisParams = irisParams,
                    isHelixActive = isHelixActive,
                    onGlance = { activeGlanceStudentId = it },
                    isZenithActive = isZenithActive,
                    altitude = altitude,
                    zenithScope = zenithScope,
                    quizLogFontColor = quizLogFontColor,
                    homeworkLogFontColor = homeworkLogFontColor,
                    quizLogFontBold = quizLogFontBold,
                    homeworkLogFontBold = homeworkLogFontBold
                )
            }
            for (i in furniture.indices) {
                val furnitureItem = furniture[i]
                FurnitureDraggableIcon(
                    furnitureUiItem = furnitureItem,
                    viewModel = seatingChartViewModel,
                    scale = scale,
                    canvasOffset = offset,
                    isSelected = furnitureItem.id in selectedFurnitureIds,
                    onClick = { onFurnitureClick(furnitureItem) },
                    onLongClick = { onFurnitureLongClick(furnitureItem) },
                    onResize = { w, h -> seatingChartViewModel.changeBoxSize(setOf(ChartItemId(furnitureItem.id, ItemType.FURNITURE)), w.toInt(), h.toInt()) },
                    noAnimations = noAnimations,
                    editModeEnabled = editModeEnabled,
                    gridSnapEnabled = gridSnapEnabled,
                    gridSize = gridSize
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatingChartTopAppBar(
    sessionType: SessionType,
    isSessionActive: Boolean,
    selectMode: Boolean,
    selectedItemIds: Set<ChartItemId>,
    behaviorTypeNames: List<String>,
    onSessionTypeChange: (SessionType) -> Unit,
    onToggleSession: () -> Unit,
    onToggleSelectMode: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onShowUndoHistory: () -> Unit,
    onBehaviorLog: (String) -> Unit,
    onLogQuiz: () -> Unit,
    onDeleteSelected: () -> Unit,
    onChangeBoxSize: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDataViewer: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onHelpClick: () -> Unit,
    onTakeScreenshot: () -> Unit,
    editModeEnabled: Boolean,
    seatingChartViewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    onShowSaveLayout: () -> Unit,
    onShowLoadLayout: () -> Unit,
    onShowExport: () -> Unit,
    onImportJson: () -> Unit,
    onImportFromPythonAssets: (Context) -> Unit,
    onImportStudentsFromExcel: () -> Unit,
    onOpenLastExportFolder: (String) -> Unit,
    onOpenAppDataFolder: () -> Unit,
    onShareDatabase: () -> Unit,
    lastExportPath: String?,
    selectedStudentUiItemForAction: StudentUiItem?,
    onNeuralOracleClick: () -> Unit,
    isHudActive: Boolean,
    onToggleHud: () -> Unit,
    isStrategistActive: Boolean,
    onToggleStrategist: () -> Unit,
    strategistGoal: GhostStrategistEngine.StrategistGoal,
    onStrategistGoalChange: (GhostStrategistEngine.StrategistGoal) -> Unit,
    isChronosActive: Boolean,
    onToggleChronos: () -> Unit,
    isHologramActive: Boolean,
    onToggleHologram: () -> Unit,
    isPhantasmActive: Boolean,
    onTogglePhantasm: () -> Unit,
    isVectorActive: Boolean,
    onToggleVector: () -> Unit,
    isSpectraActive: Boolean,
    onToggleSpectra: () -> Unit,
    isAuroraActive: Boolean,
    onToggleAurora: () -> Unit,
    isNebulaActive: Boolean,
    onToggleNebula: () -> Unit,
    isLensActive: Boolean,
    onToggleLens: () -> Unit,
    isFluxActive: Boolean,
    onToggleFlux: () -> Unit,
    isSingularityActive: Boolean,
    onToggleSingularity: () -> Unit,
    isPhasingActive: Boolean,
    onTogglePhasing: () -> Unit,
    isPulseActive: Boolean,
    onTogglePulse: () -> Unit,
    isIrisActive: Boolean,
    onToggleIris: () -> Unit,
    isWarpActive: Boolean,
    onToggleWarp: () -> Unit,
    isFutureActive: Boolean,
    onToggleFuture: () -> Unit,
    isSparkActive: Boolean,
    onToggleSpark: () -> Unit,
    isOsmosisActive: Boolean,
    onToggleOsmosis: () -> Unit,
    isEntanglementActive: Boolean,
    onToggleEntanglement: () -> Unit,
    isSyncActive: Boolean,
    onToggleSync: () -> Unit,
    isIonActive: Boolean,
    onToggleIon: () -> Unit,
    isEntropyActive: Boolean,
    onToggleEntropy: () -> Unit,
    isZenithActive: Boolean,
    onToggleZenith: () -> Unit,
    isHorizonActive: Boolean,
    onToggleHorizon: () -> Unit,
    isGlitchActive: Boolean,
    onToggleGlitch: () -> Unit,
    isHelixActive: Boolean,
    onToggleHelix: () -> Unit,
    isEmergenceActive: Boolean,
    onToggleEmergence: () -> Unit,
    isCatalystActive: Boolean,
    onToggleCatalyst: () -> Unit,
    isFloraActive: Boolean,
    onToggleFlora: () -> Unit,
    isTectonicsActive: Boolean,
    onToggleTectonics: () -> Unit,
    isPulsarActive: Boolean,
    onTogglePulsar: () -> Unit,
    isCortexActive: Boolean,
    onToggleCortex: () -> Unit,
    isVortexActive: Boolean,
    onToggleVortex: () -> Unit,
    isOrbitActive: Boolean,
    onToggleOrbit: () -> Unit,
    isSupernovaActive: Boolean,
    onToggleSupernova: () -> Unit,
    isQuasarActive: Boolean,
    onToggleQuasar: () -> Unit,
    isSpotlightActive: Boolean,
    onToggleSpotlight: () -> Unit,
    isNavigatorActive: Boolean,
    onToggleNavigator: () -> Unit,
    isAdaptiveActive: Boolean,
    onToggleAdaptive: () -> Unit,
    isGlyphActive: Boolean,
    onToggleGlyph: () -> Unit,
    isArchitectActive: Boolean,
    onToggleArchitect: () -> Unit,
    onArchitectGoalChange: (GhostArchitectEngine.StrategicGoal) -> Unit,
    isVisionActive: Boolean,
    onToggleVision: () -> Unit,
    isMagnetarActive: Boolean,
    onToggleMagnetar: () -> Unit,
    onExportBlueprint: () -> Unit
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    var showLayoutSubMenu by remember { mutableStateOf(false) }
    var showGuidesSubMenu by remember { mutableStateOf(false) }
    var showAppearanceSubMenu by remember { mutableStateOf(false) }
    var showAlignSubMenu by remember { mutableStateOf(false) }

    var showSessionModeDropdown by remember { mutableStateOf(false) }
    var showDataAndExportDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    TopAppBar(
        title = {
            Column {
                Text("Seating Chart", style = MaterialTheme.typography.titleMedium)
                Text(sessionType.name.toTitleCase(), style = MaterialTheme.typography.bodySmall)
            }
        },
        actions = {
            // Undo/Redo
            IconButton(onClick = onUndo) { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo") }
            IconButton(onClick = onRedo) { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo") }

            // Behaviors Dropdown
            val behaviorTargetCount = if (selectMode) selectedItemIds.filter { it.type == ItemType.STUDENT }.size else if (selectedStudentUiItemForAction != null) 1 else 0
            if (behaviorTargetCount > 0 && behaviorTypeNames.isNotEmpty()) {
                var showQuickBehaviorMenu by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { showQuickBehaviorMenu = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Log ($behaviorTargetCount)", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = showQuickBehaviorMenu,
                        onDismissRequest = { showQuickBehaviorMenu = false }
                    ) {
                        behaviorTypeNames.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    onBehaviorLog(type)
                                    showQuickBehaviorMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Session Mode Dropdown (moved from overflow)
            Box {
                IconButton(onClick = { showSessionModeDropdown = true }) {
                    Icon(Icons.Default.RadioButtonChecked, contentDescription = "Session Mode")
                }
                DropdownMenu(expanded = showSessionModeDropdown, onDismissRequest = { showSessionModeDropdown = false }) {
                    SessionType.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name.toTitleCase()) },
                            onClick = {
                                if (isSessionActive) onToggleSession()
                                onSessionTypeChange(mode)
                                showSessionModeDropdown = false
                            },
                            trailingIcon = { if (sessionType == mode) Icon(Icons.Default.RadioButtonChecked, null) else Icon(Icons.Default.RadioButtonUnchecked, null) }
                        )
                    }
                }
            }

            // View Data (moved from overflow)
            IconButton(onClick = onNavigateToDataViewer) { Icon(Icons.Default.Analytics, contentDescription = "View Data") }

            // Reminders (moved from overflow)
            IconButton(onClick = onNavigateToReminders) { Icon(Icons.Default.Notifications, contentDescription = "Reminders") }

            // Data & Export Dropdown (moved from overflow)
            Box {
                IconButton(onClick = { showDataAndExportDropdown = true }) {
                    Icon(Icons.Default.Storage, contentDescription = "Data & Export")
                }
                DropdownMenu(expanded = showDataAndExportDropdown, onDismissRequest = { showDataAndExportDropdown = false }) {
                    DropdownMenuItem(text = { Text("Import from JSON") }, onClick = { onImportJson(); showDataAndExportDropdown = false })
                    DropdownMenuItem(text = { Text("Import from Python Assets") }, onClick = { onImportFromPythonAssets(context); showDataAndExportDropdown = false })
                    DropdownMenuItem(text = { Text("Import from Excel") }, onClick = { onImportStudentsFromExcel(); showDataAndExportDropdown = false })
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Export to Excel") },
                        onClick = { onShowExport(); showDataAndExportDropdown = false },
                        leadingIcon = { Icon(Icons.Default.CloudUpload, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Export Blueprint (SVG) 👻") },
                        onClick = { onExportBlueprint(); showDataAndExportDropdown = false },
                        leadingIcon = { Icon(Icons.Default.Layers, null) }
                    )
                    DropdownMenuItem(text = { Text("Open Last Export Folder") }, enabled = lastExportPath?.isNotBlank() == true, onClick = {
                        lastExportPath?.let { path ->
                            onOpenLastExportFolder(path)
                        }
                        showDataAndExportDropdown = false
                    })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Backup Database (Share)") }, onClick = { onShareDatabase(); showDataAndExportDropdown = false })
                    DropdownMenuItem(text = { Text("Open App Data Folder") }, onClick = { onOpenAppDataFolder(); showDataAndExportDropdown = false })
                }
            }

            // Tactical HUD Toggle
            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.ARCHITECT_MODE_ENABLED) {
                Box {
                    var showArchitectGoals by remember { mutableStateOf(false) }
                    IconButton(onClick = {
                        onToggleArchitect()
                        if (!isArchitectActive) showArchitectGoals = true
                    }) {
                        Icon(
                            Icons.Default.Chair,
                            contentDescription = "Neural Architect",
                            tint = if (isArchitectActive) androidx.compose.ui.graphics.Color.Cyan else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenu(expanded = showArchitectGoals, onDismissRequest = { showArchitectGoals = false }) {
                        GhostArchitectEngine.StrategicGoal.entries.forEach { goal ->
                            DropdownMenuItem(
                                text = { Text(goal.name.toTitleCase()) },
                                onClick = {
                                    onArchitectGoalChange(goal)
                                    showArchitectGoals = false
                                }
                            )
                        }
                    }
                }
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.HUD_MODE_ENABLED) {
                IconButton(onClick = onToggleHud) {
                    Icon(
                        Icons.Default.AutoFixHigh,
                        contentDescription = "Tactical HUD",
                        tint = if (isHudActive) androidx.compose.ui.graphics.Color.Cyan else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.STRATEGIST_MODE_ENABLED) {
                Box {
                    var showStrategistGoals by remember { mutableStateOf(false) }
                    IconButton(onClick = {
                        onToggleStrategist()
                        if (!isStrategistActive) showStrategistGoals = true
                    }) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "Neural Strategist",
                            tint = if (isStrategistActive) androidx.compose.ui.graphics.Color.Cyan else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenu(expanded = showStrategistGoals, onDismissRequest = { showStrategistGoals = false }) {
                        GhostStrategistEngine.StrategistGoal.entries.forEach { goal ->
                            DropdownMenuItem(
                                text = { Text(goal.name.toTitleCase()) },
                                onClick = {
                                    onStrategistGoalChange(goal)
                                    showStrategistGoals = false
                                }
                            )
                        }
                    }
                }
            }

            if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.VISION_MODE_ENABLED) {
                IconButton(onClick = onToggleVision) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Ghost Vision",
                        tint = if (isVisionActive) androidx.compose.ui.graphics.Color.Cyan else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Settings (moved from overflow)
            IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, contentDescription = "Settings") }

            // Main Overflow Menu (remaining items)
            Box {
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }

                DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                    if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.SILHOUETTE_MODE_ENABLED) {
                        DropdownMenuItem(
                            text = { Text(if (isSilhouetteActive) "Disable Neural Silhouette 👻" else "Ghost Silhouette 👻") },
                            onClick = {
                                isSilhouetteActive = !isSilhouetteActive
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                        )
                        if (GhostConfig.LASSO_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isLassoActive) "Disable Ghost Lasso 👻" else "Ghost Lasso 👻") },
                                onClick = {
                                    isLassoActive = !isLassoActive
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Gesture, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                    }
                    if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.COGNITIVE_ENGINE_ENABLED) {
                        if (GhostConfig.FILTER_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text("Neural Filter 👻") },
                                onClick = {
                                    context.startActivity(Intent(context, GhostFilterActivity::class.java))
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.VISION_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text("Ghost Vision AR 👻") },
                                onClick = {
                                    context.startActivity(Intent(context, GhostVisionActivity::class.java))
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.PhotoCamera, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Neural Optimize 👻") },
                            onClick = {
                                seatingChartViewModel.runCognitiveOptimization()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Psychology, null, tint = MaterialTheme.colorScheme.tertiary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Neural Oracle 👻") },
                            onClick = {
                                onNeuralOracleClick()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Psychology, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                        )
                        if (GhostConfig.STRATEGIST_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isStrategistActive) "Disable Strategist 👻" else "Neural Strategist 👻") },
                                onClick = {
                                    onToggleStrategist()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Psychology, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(if (isChronosActive) "Disable Chronos 👻" else "Enable Chronos 👻") },
                            onClick = {
                                onToggleChronos()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.PhotoCamera, null, tint = androidx.compose.ui.graphics.Color.Magenta) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isHologramActive) "Disable Hologram 👻" else "Enable Hologram 👻") },
                            onClick = {
                                onToggleHologram()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Layers, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isPhantasmActive) "Disable Phantasm 👻" else "Enable Phantasm 👻") },
                            onClick = {
                                onTogglePhantasm()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Yellow) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isSpectraActive) "Disable Ghost Spectra 👻" else "Enable Ghost Spectra 👻") },
                            onClick = {
                                onToggleSpectra()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Palette, null, tint = androidx.compose.ui.graphics.Color.Magenta) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isAuroraActive) "Disable Ghost Aurora 👻" else "Enable Ghost Aurora 👻") },
                            onClick = {
                                onToggleAurora()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Palette, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                        )
                        if (GhostConfig.NEBULA_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isNebulaActive) "Disable Ghost Nebula 👻" else "Enable Ghost Nebula 👻") },
                                onClick = {
                                    onToggleNebula()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Blue) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(if (isPulseActive) "Disable Neural Pulse 👻" else "Enable Neural Pulse 👻") },
                            onClick = {
                                onTogglePulse()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.RadioButtonChecked, null, tint = androidx.compose.ui.graphics.Color.Green) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isLensActive) "Disable Ghost Lens 👻" else "Enable Ghost Lens 👻") },
                            onClick = {
                                onToggleLens()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                        )
                        if (GhostConfig.IRIS_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isIrisActive) "Disable Neural Iris 👻" else "Enable Neural Iris 👻") },
                                onClick = {
                                    onToggleIris()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.RadioButtonChecked, null, tint = androidx.compose.ui.graphics.Color.White) }
                            )
                        }
                        if (GhostConfig.WARP_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isWarpActive) "Stabilize Spacetime 👻" else "Neural Warp 👻") },
                                onClick = {
                                    onToggleWarp()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.SYNC_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isSyncActive) "Disable Neural Sync 👻" else "Neural Sync 👻") },
                                onClick = {
                                    onToggleSync()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Link, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.NAVIGATOR_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isNavigatorActive) "Disable Ghost Navigator 👻" else "Ghost Navigator 👻") },
                                onClick = {
                                    onToggleNavigator()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Explore, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.ADAPTIVE_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isAdaptiveActive) "Disable Ghost Adaptive 👻" else "Ghost Adaptive 👻") },
                                onClick = {
                                    onToggleAdaptive()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.GridView, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.ORBIT_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isOrbitActive) "Stabilize Galaxy 👻" else "Ghost Orbit 👻") },
                                onClick = {
                                    onToggleOrbit()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.FUTURE_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isFutureActive) "Present Timeline 👻" else "Neural Future 👻") },
                                onClick = {
                                    onToggleFuture()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Yellow) }
                            )
                        }
                        if (GhostConfig.HELIX_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isHelixActive) "Sequencing Off 👻" else "Ghost Helix 👻") },
                                onClick = {
                                    onToggleHelix()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Green) }
                            )
                        }
                        if (GhostConfig.GLITCH_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isGlitchActive) "Suppress Glitch 👻" else "Neural Glitch 👻") },
                                onClick = {
                                    onToggleGlitch()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                if (GhostConfig.CORTEX_MODE_ENABLED) {
                    DropdownMenuItem(
                        text = { Text(if (isCortexActive) "De-Sensitize Cortex 👻" else "Neural Cortex 👻") },
                        onClick = {
                            onToggleCortex()
                            showMoreMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Psychology, null, tint = androidx.compose.ui.graphics.Color.Green) }
                    )
                    if (GhostConfig.VORTEX_MODE_ENABLED) {
                        DropdownMenuItem(
                            text = { Text(if (isVortexActive) "Stabilize Spindrift 👻" else "Ghost Vortex 👻") },
                            onClick = {
                                onToggleVortex()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Neural History 👻") },
                        onClick = {
                            context.startActivity(Intent(context, GhostCortexActivity::class.java))
                            showMoreMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Psychology, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                    )
                    DropdownMenuItem(
                        text = { Text("Ghost Ray Calibration 👻") },
                        onClick = {
                            context.startActivity(Intent(context, com.example.myapplication.labs.ghost.ray.GhostRayActivity::class.java))
                            showMoreMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.FlashlightOn, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                    )
                }
                if (GhostConfig.GLYPH_MODE_ENABLED) {
                    DropdownMenuItem(
                        text = { Text(if (isGlyphActive) "Disable Neural Glyph 👻" else "Ghost Glyph 👻") },
                        onClick = {
                            onToggleGlyph()
                            showMoreMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                    )
                }
                if (GhostConfig.QUASAR_MODE_ENABLED) {
                    DropdownMenuItem(
                        text = { Text(if (isQuasarActive) "Collapse Quasar 👻" else "Ghost Quasar 👻") },
                        onClick = {
                            onToggleQuasar()
                            showMoreMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Yellow) }
                    )
                }
                        if (GhostConfig.SPARK_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isSparkActive) "Static State 👻" else "Ghost Spark 👻") },
                                onClick = {
                                    onToggleSpark()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.OSMOSIS_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isOsmosisActive) "Balance Classroom 👻" else "Ghost Osmosis 👻") },
                                onClick = {
                                    onToggleOsmosis()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Blue) }
                            )
                        }
                        if (GhostConfig.ENTANGLEMENT_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isEntanglementActive) "De-Entangle 👻" else "Ghost Entanglement 👻") },
                                onClick = {
                                    onToggleEntanglement()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.ION_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isIonActive) "Discharge Ions 👻" else "Ghost Ion 👻") },
                                onClick = {
                                    onToggleIon()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Yellow) }
                            )
                        }
                        if (GhostConfig.ENTROPY_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isEntropyActive) "Stabilize Entropy 👻" else "Ghost Entropy 👻") },
                                onClick = {
                                    onToggleEntropy()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Magenta) }
                            )
                        }
                        if (GhostConfig.ZENITH_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isZenithActive) "Flatten Space 👻" else "Ghost Zenith 👻") },
                                onClick = {
                                    onToggleZenith()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Layers, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.HORIZON_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isHorizonActive) "Static Sky 👻" else "Ghost Horizon 👻") },
                                onClick = {
                                    onToggleHorizon()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Yellow) }
                            )
                        }
                        if (GhostConfig.EMERGENCE_MODE_ENABLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            DropdownMenuItem(
                                text = { Text(if (isEmergenceActive) "De-Emerge 👻" else "Ghost Emergence 👻") },
                                onClick = {
                                    onToggleEmergence()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Green) }
                            )
                        }
                        if (GhostConfig.TECTONICS_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isTectonicsActive) "Stabilize Lithosphere 👻" else "Ghost Tectonics 👻") },
                                onClick = {
                                    onToggleTectonics()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Red) }
                            )
                        }
                        if (GhostConfig.CATALYST_MODE_ENABLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            DropdownMenuItem(
                                text = { Text(if (isCatalystActive) "De-Catalyze 👻" else "Ghost Catalyst 👻") },
                                onClick = {
                                    onToggleCatalyst()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.FLORA_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isFloraActive) "De-Flora 👻" else "Ghost Flora 👻") },
                                onClick = {
                                    onToggleFlora()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Green) }
                            )
                        }
                        if (GhostConfig.PULSAR_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isPulsarActive) "Stabilize Harmonics 👻" else "Ghost Pulsar 👻") },
                                onClick = {
                                    onTogglePulsar()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.MAGNETAR_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isMagnetarActive) "De-Polarize 👻" else "Ghost Magnetar 👻") },
                                onClick = {
                                    onToggleMagnetar()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Yellow) }
                            )
                        }
                        if (GhostConfig.SPOTLIGHT_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text(if (isSpotlightActive) "Disable Ghost Spotlight 👻" else "Ghost Spotlight 👻") },
                                onClick = {
                                    onToggleSpotlight()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                        if (GhostConfig.SUPERNOVA_MODE_ENABLED) {
                             DropdownMenuItem(
                                text = { Text(if (isSupernovaActive) "Stabilize Singularity 👻" else "Ghost Supernova 👻") },
                                onClick = {
                                    onToggleSupernova()
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                            )
                        }
                    }
                    if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.FLUX_MODE_ENABLED) {
                        DropdownMenuItem(
                            text = { Text(if (isFluxActive) "Disable Ghost Flux 🌊" else "Enable Ghost Flux 🌊") },
                            onClick = {
                                onToggleFlux()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                        )
                    }
                    if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.SINGULARITY_MODE_ENABLED) {
                        DropdownMenuItem(
                            text = { Text(if (isSingularityActive) "Collapse Singularity 🕳️" else "Enable Ghost Singularity 🕳️") },
                            onClick = {
                                onToggleSingularity()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Black) }
                        )
                    }
                    if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.PHASING_MODE_ENABLED) {
                        DropdownMenuItem(
                            text = { Text(if (isPhasingActive) "De-Phase Layer 👻" else "Neural Phase 👻") },
                            onClick = {
                                onTogglePhasing()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Layers, null, tint = androidx.compose.ui.graphics.Color.Blue) }
                        )
                    }
                    if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.VECTOR_MODE_ENABLED) {
                        DropdownMenuItem(
                            text = { Text(if (isVectorActive) "Disable Social Vector 👻" else "Enable Social Vector 👻") },
                            onClick = {
                                onToggleVector()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, tint = androidx.compose.ui.graphics.Color.Green) }
                        )
                    }
                    if (GhostConfig.GHOST_MODE_ENABLED && GhostConfig.RAY_MODE_ENABLED) {
                        DropdownMenuItem(
                            text = { Text(if (isRayActive) "Disable Ghost Ray" else "Enable Ghost Ray") },
                            onClick = {
                                isRayActive = !isRayActive
                                if (isRayActive) ghostRayEngine.start() else ghostRayEngine.stop()
                                showMoreMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.FlashlightOn, null, tint = androidx.compose.ui.graphics.Color.Cyan) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Undo History") },
                        onClick = {
                            onShowUndoHistory()
                            showMoreMenu = false
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Undo, null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(if (editModeEnabled) "Disable Edit Mode" else "Enable Edit Mode") },
                        onClick = {
                            settingsViewModel.updateEditModeEnabled(!editModeEnabled)
                            showMoreMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        trailingIcon = {
                            Icon(
                                if (editModeEnabled) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                contentDescription = null
                            )
                        }
                    )
                    HorizontalDivider()
                    if (editModeEnabled) {
                        DropdownMenuItem(text = { Text("Alignment Tools") }, onClick = { showMoreMenu = false; showAlignSubMenu = true }, leadingIcon = { Icon(Icons.Default.AutoFixHigh, null) })
                    }
                    DropdownMenuItem(text = { Text("Layouts") }, onClick = { showMoreMenu = false; showLayoutSubMenu = true }, leadingIcon = { Icon(Icons.Default.Layers, null) })
                    DropdownMenuItem(text = { Text("Guides & Grid") }, onClick = { showMoreMenu = false; showGuidesSubMenu = true }, leadingIcon = { Icon(Icons.Default.GridView, null) })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Take Screenshot") }, onClick = { onTakeScreenshot(); showMoreMenu = false }, leadingIcon = { Icon(Icons.Default.PhotoCamera, null) })
                    DropdownMenuItem(text = { Text("Appearance") }, onClick = { showMoreMenu = false; showAppearanceSubMenu = true }, leadingIcon = { Icon(Icons.Default.Palette, null) })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Help") }, onClick = { onHelpClick(); showMoreMenu = false }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Help, null) })
                }

                // Submenus that remain in the overflow context
                DropdownMenu(expanded = showAlignSubMenu, onDismissRequest = { showAlignSubMenu = false }) {
                    DropdownMenuItem(text = { Text("Align Top") }, onClick = { seatingChartViewModel.alignSelectedItems("top"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Align Bottom") }, onClick = { seatingChartViewModel.alignSelectedItems("bottom"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Align Left") }, onClick = { seatingChartViewModel.alignSelectedItems("left"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Align Right") }, onClick = { seatingChartViewModel.alignSelectedItems("right"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Center Horizontal") }, onClick = { seatingChartViewModel.alignSelectedItems("center_h"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Center Vertical") }, onClick = { seatingChartViewModel.alignSelectedItems("center_v"); showAlignSubMenu = false })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Distribute Horizontal") }, onClick = { seatingChartViewModel.distributeSelectedItems("horizontal"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Distribute Vertical") }, onClick = { seatingChartViewModel.distributeSelectedItems("vertical"); showAlignSubMenu = false })
                }
                DropdownMenu(expanded = showLayoutSubMenu, onDismissRequest = { showLayoutSubMenu = false }) {
                    DropdownMenuItem(text = { Text("Save Current Layout") }, onClick = { onShowSaveLayout(); showLayoutSubMenu = false }, leadingIcon = { Icon(Icons.Default.Add, null) })
                    DropdownMenuItem(text = { Text("Load Saved Layout") }, onClick = { onShowLoadLayout(); showLayoutSubMenu = false }, leadingIcon = { Icon(Icons.Default.CloudDownload, null) })
                }
                DropdownMenu(expanded = showGuidesSubMenu, onDismissRequest = { showGuidesSubMenu = false }) {
                    DropdownMenuItem(text = { Text("Add Vertical Guide") }, onClick = { seatingChartViewModel.addGuide(GuideType.VERTICAL); showGuidesSubMenu = false })
                    DropdownMenuItem(text = { Text("Add Horizontal Guide") }, onClick = { seatingChartViewModel.addGuide(GuideType.HORIZONTAL); showGuidesSubMenu = false })
                    DropdownMenuItem(text = { Text("Clear All Guides") }, onClick = {
                        seatingChartViewModel.allGuides.value.forEach { seatingChartViewModel.deleteGuide(it) }
                        showGuidesSubMenu = false
                    })
                }
                DropdownMenu(expanded = showAppearanceSubMenu, onDismissRequest = { showAppearanceSubMenu = false }) {
                    AppTheme.entries.forEach { theme ->
                        DropdownMenuItem(text = { Text(theme.name.toTitleCase()) }, onClick = { settingsViewModel.updateAppTheme(theme); showAppearanceSubMenu = false }, trailingIcon = {
                            val currentTheme by settingsViewModel.appTheme.collectAsState()
                            if (currentTheme == theme) Icon(Icons.Default.RadioButtonChecked, null)
                        })
                    }
                }
            }
        }
    )
}
