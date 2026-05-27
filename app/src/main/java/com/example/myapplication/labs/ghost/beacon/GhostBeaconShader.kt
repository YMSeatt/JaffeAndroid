package com.example.myapplication.labs.ghost.beacon

import org.intellij.lang.annotations.Language

/**
 * GhostBeaconShader: AGSL code for the volumetric attention beacon.
 */
object GhostBeaconShader {

    @Language("AGSL")
    const val BEACON_EFFECT = """
        uniform float2 iResolution;      // Canvas size
        uniform float iTime;             // Shader time
        uniform float2 iTarget;          // Target student position (in logical pixels)
        uniform float iIntensity;        // Beacon intensity (0..1)
        uniform float2 iCanvasOffset;    // Canvas pan offset
        uniform float iCanvasScale;      // Canvas zoom scale

        float noise(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        half4 main(float2 fragCoord) {
            // Transform iTarget (logical 4000x4000) to screen coordinates
            float2 screenTarget = iTarget * iCanvasScale + iCanvasOffset;

            // Normalize coordinates
            float2 uv = fragCoord / iResolution.xy;
            float2 targetUv = screenTarget / iResolution.xy;

            // Distance from the vertical beam center
            float distToBeam = abs(fragCoord.x - screenTarget.x);

            // Volumetric beam logic
            float beamWidth = 40.0 * iCanvasScale;
            float beam = smoothstep(beamWidth, 0.0, distToBeam);

            // Taper the beam from top to bottom
            beam *= (1.0 - uv.y);

            // Add procedural flicker/noise
            float flicker = noise(float2(iTime * 0.1, uv.y * 10.0)) * 0.2 + 0.8;
            beam *= flicker;

            // Pulse intensity
            float pulse = sin(iTime * 4.0) * 0.1 + 0.9;
            beam *= pulse * iIntensity;

            // Core color (Cyan/White)
            half3 color = half3(0.0, 0.8, 1.0) * beam;
            color += half3(0.5, 0.9, 1.0) * pow(beam, 3.0); // White core

            // Add a "Splash" at the target position
            float distToTarget = distance(fragCoord, screenTarget);
            float splashRadius = 60.0 * iCanvasScale;
            float splash = smoothstep(splashRadius, 0.0, distToTarget);
            splash *= pulse * iIntensity;
            color += half3(0.0, 1.0, 0.8) * splash;

            return half4(color, beam * 0.6 + splash * 0.4);
        }
    """
}
