package com.example.myapplication.labs.ghost.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GhostPreferencesStoreTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())
    private lateinit var context: Context
    private lateinit var store: GhostPreferencesStore
    private lateinit var testDataStore: DataStore<Preferences>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // We need a way to inject a test dataStore or use the real one in a test-controlled way.
        // For simplicity in this PoC test, we'll use the real store but it might be tricky
        // due to the delegate. Instead, let's just test the ViewModel if possible,
        // or mock the store.
        store = GhostPreferencesStore(context)
    }

    @Test
    fun `test default values`() = runTest {
        assertEquals(0.5f, store.ghostGlowIntensity.first())
        assertEquals(true, store.neuralHapticEnabled.first())
        assertEquals(false, store.glassmorphismEnabled.first())
        assertEquals(false, store.scanlineEffectEnabled.first())
    }

    @Test
    fun `test updating glow intensity`() = runTest {
        store.updateGlowIntensity(0.8f)
        assertEquals(0.8f, store.ghostGlowIntensity.first())
    }
}
