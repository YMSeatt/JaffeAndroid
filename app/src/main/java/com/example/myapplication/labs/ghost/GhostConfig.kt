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

    /** Enables the "Ghost Future" neural classroom simulation. */
    const val FUTURE_MODE_ENABLED = true

    /** Enables the "Ghost Nebula" atmospheric density visualization. */
    const val NEBULA_MODE_ENABLED = true

    /** Enables "Ghost Osmosis" knowledge diffusion and behavioral concentration gradients. */
    const val OSMOSIS_MODE_ENABLED = true

    /** Enables "Ghost Spark" high-performance neural particle system. */
    const val SPARK_MODE_ENABLED = true

    /** Enables "Ghost Entanglement" quantum social synchronicity visualization. */
    const val ENTANGLEMENT_MODE_ENABLED = true

    /** Enables "Ghost Ion" neural ionization and electrostatic discharge visualization. */
    const val ION_MODE_ENABLED = true

    /** Enables "Ghost Entropy" neural turbulence and thermal distortion visualization. */
    const val ENTROPY_MODE_ENABLED = true

    /** Enables "Ghost Zenith" spatial depth and device-tilt parallax effects. */
    const val ZENITH_MODE_ENABLED = true

    /** Enables "Ghost Emergence" behavioral cellular automata visualization. */
    const val EMERGENCE_MODE_ENABLED = true

    /** Enables "Ghost Catalyst" behavioral chain reaction mapping. */
    const val CATALYST_MODE_ENABLED = true

    /** Enables "Ghost Flora" neural botanical visualization. */
    const val FLORA_MODE_ENABLED = true

    /** Enables "Ghost Tectonics" social stability visualization. */
    const val TECTONICS_MODE_ENABLED = true

    /** Enables "Ghost Horizon" context-aware atmospheric visualization. */
    const val HORIZON_MODE_ENABLED = true

    /** Enables "Ghost Pulsar" harmonic synchronicity visualization. */
    const val PULSAR_MODE_ENABLED = true

    /** Enables "Ghost Magnetar" social magnetism and field polarity visualization. */
    const val MAGNETAR_MODE_ENABLED = true

    /** Enables "Ghost Cortex" neural intent and somatic haptic exploration. */
    const val CORTEX_MODE_ENABLED = true

    /** Enables "Ghost Quasar" high-energy student node visualization. */
    const val QUASAR_MODE_ENABLED = true

    /** Enables "Ghost Helix" neural DNA and genomic trajectory visualization. */
    const val HELIX_MODE_ENABLED = true

    /** Enables "Ghost Supernova" classroom criticality and data-reset visualization. */
    const val SUPERNOVA_MODE_ENABLED = true

    /** Enables the "Ghost Vortex" rotational social momentum visualization. */
    const val VORTEX_MODE_ENABLED = true

    /** Enables the "Ghost Ray" neural directional pointer. */
    const val RAY_MODE_ENABLED = true

    /** Enables the "Ghost Orbit" classroom galaxy visualization. */
    const val ORBIT_MODE_ENABLED = true

    /** Enables the "Ghost Architect" strategic layout generative engine. */
    const val ARCHITECT_MODE_ENABLED = true

    /** Enables the "Ghost Vision" sensor-driven AR viewport. */
    const val VISION_MODE_ENABLED = true
}
