package com.example.myapplication.labs.ghost

/**
 * GhostConfig: Central feature-flag registry for the Ghost Lab experimental suite.
 *
 * This object manages the activation of future-facing, research-oriented features.
 * Features are tiered into "Master Toggles" and "Specific Mode Toggles".
 *
 * Most features here require high-performance hardware (API 33+) due to extensive
 * use of AGSL Shaders and advanced sensor APIs.
 */
object GhostConfig {
    /** Master toggle for all experimental Ghost features. If false, all other flags are ignored. */
    const val GHOST_MODE_ENABLED = true

    /** Enables the Force-Directed Graph layout optimizer in [GhostCognitiveEngine]. */
    const val COGNITIVE_ENGINE_ENABLED = true

    /** Enables the hands-free speech-to-command parser and real-time voice visualizer. */
    const val VOICE_ASSISTANT_ENABLED = true

    /** Enables the Tactical Radar and Cognitive Aura overlays on the seating chart canvas. */
    const val HUD_MODE_ENABLED = true

    /** Enables real-time auditory atmosphere monitoring and turbulence visualization. */
    const val ECHO_MODE_ENABLED = true

    /** Enables the spatiotemporal behavioral heatmap engine. */
    const val CHRONOS_MODE_ENABLED = true

    /** Enables 3D parallax effects and the holographic scanning line shader. */
    const val HOLOGRAM_MODE_ENABLED = true

    /** Enables the on-device AI narrative synthesis (simulated AICore integration). */
    const val SYNAPSE_MODE_ENABLED = true

    /** Enables presence-based meta-ball visualizations driven by student logs. */
    const val PHANTASM_MODE_ENABLED = true

    /** Enables the AGSL wormhole transition shader for Inter-app Drag & Drop operations. */
    const val PORTAL_MODE_ENABLED = true

    /** Enables the Social Lattice graph visualization showing student relationship networks. */
    const val LATTICE_MODE_ENABLED = true

    /** Enables Social Gravity force-vector visualization and Python-bridged analysis. */
    const val VECTOR_MODE_ENABLED = true

    /** Enables the spectroscopic data refraction layer using dispersive prism shaders. */
    const val SPECTRA_MODE_ENABLED = true

    /** Enables "Neural Flow" intensity visualization driven by student density and log tempo. */
    const val FLUX_MODE_ENABLED = true

    /** Enables high-intensity data sink visualizations and gravitational lensing effects. */
    const val SINGULARITY_MODE_ENABLED = true

    /** Enables the procedural background aurora reflecting the classroom's behavioral climate. */
    const val AURORA_MODE_ENABLED = true

    /** Enables neural resonance activity ripples driven by behavior log clusters. */
    const val PULSE_MODE_ENABLED = true

    /** Enables the "Neural Backstage" glitch transition and data-void background layer. */
    const val PHASING_MODE_ENABLED = true

    /** Enables the "Ghost Lens" predictive spatiotemporal refraction layer. */
    const val LENS_MODE_ENABLED = true

    /** Enables personalized Neural Iris signatures for each student. */
    const val IRIS_MODE_ENABLED = true

    /** Enables the "Ghost Warp" neural spacetime dilation effect. */
    const val WARP_MODE_ENABLED = true
}
