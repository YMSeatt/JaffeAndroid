package com.example.myapplication.labs.ghost.pulsar

import org.intellij.lang.annotations.Language

/**
 * GhostPulsarShader: AGSL scripts for the "Ghost Pulsar" harmonic layer.
 *
 * This shader renders interference patterns between students. Each student acts
 * as a wave source with a specific phase and frequency.
 */
object GhostPulsarShader {
    @Language("AGSL")
    const val PULSAR_WAVES = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPoints[20]; // Student positions
        uniform float iPhases[20];  // Student harmonic phases
        uniform float iAmplitudes[20]; // Student harmonic amplitudes
        uniform int iNumPoints;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float aspect = iResolution.x / iResolution.y;
            float2 p = uv;
            p.x *= aspect;

            float wave = 0.0;
            float3 compositeColor = float3(0.0);

            for (int i = 0; i < 20; i++) {
                if (i >= iNumPoints) break;

                float2 pos = iPoints[i] / iResolution.xy;
                pos.x *= aspect;

                float d = distance(p, pos);

                // Calculate wave interference from this student.
                // Frequency is baked into the phase calculation in the engine.
                // 40.0: Wave frequency multiplier (spatial density of rings).
                // 6.28318: 2*PI, used to map normalized phase to a full sine cycle.
                float val = sin(d * 40.0 - (iPhases[i] * 6.28318)) * iAmplitudes[i];

                // Attenuation over distance. Ensures waves are localized to students.
                val *= exp(-d * 3.0);

                wave += val;

                // Dynamic color shifting based on amplitude and distance
                float3 studentColor = mix(float3(0.0, 0.8, 1.0), float3(0.8, 0.0, 1.0), iPhases[i]);
                compositeColor += studentColor * (val * 0.5 + 0.5) * exp(-d * 5.0);
            }

            // Normalization and thresholding for "Wave Fronts".
            // Values below 0.3 are discarded (dark background), creating the "ring" look.
            float intensity = abs(wave);
            float mask = smoothstep(0.3, 0.8, intensity);

            float3 finalColor = mix(float3(0.01, 0.02, 0.05), compositeColor, mask);

            // Add a subtle digital scanline effect (800 scanlines across the UI).
            finalColor *= 0.9 + 0.1 * sin(uv.y * 800.0);

            return float4(finalColor, mask * 0.4);
        }
    """
}
