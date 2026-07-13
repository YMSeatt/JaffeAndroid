package com.example.myapplication.labs.ghost.ekg

import org.intellij.lang.annotations.Language

/**
 * GhostEKGShader: AGSL shaders for rendering the "Neural EKG" waveform.
 *
 * This shader provides a high-fidelity visualization of student biological signals,
 * featuring a glowing line and a scrolling "CRT" grid background.
 */
object GhostEKGShader {

    /**
     * EKG_LINE: Renders a scrolling EKG waveform.
     *
     * Uniforms:
     * - [iResolution]: Viewport dimensions.
     * - [iTime]: Elapsed time.
     * - [iWaveform]: Array of signal values (normalized 0..1).
     * - [iVitality]: Overall vitality (0..1) for color shifting.
     * - [iStress]: Overall stress (0..1) for color shifting.
     */
    @Language("AGSL")
    const val EKG_LINE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iWaveform[100];
        uniform float iVitality;
        uniform float iStress;

        float3 getSignalColor() {
            float3 healthy = float3(0.0, 1.0, 0.8); // Neural Cyan
            float3 stressed = float3(1.0, 0.2, 0.5); // Neural Magenta
            return mix(healthy, stressed, iStress);
        }

        float line(float2 p, float2 a, float2 b, float width) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return smoothstep(width, 0.0, length(pa - ba * h));
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float3 finalColor = float3(0.0);

            // 1. CRT Grid Background
            float2 grid = fract(fragCoord / 40.0);
            float gridLine = smoothstep(0.02, 0.0, grid.x) + smoothstep(0.02, 0.0, grid.y);
            finalColor += float3(0.0, 0.2, 0.1) * gridLine * 0.5;

            // 2. Waveform Rendering
            float width = 0.005;
            float3 signalColor = getSignalColor();

            // Iterate through the waveform buffer to draw segments
            // iWaveform represents 100 points across the X axis.
            for (int i = 0; i < 99; i++) {
                float x1 = float(i) / 100.0;
                float x2 = float(i + 1) / 100.0;
                float y1 = 0.5 + iWaveform[i] * 0.4;
                float y2 = 0.5 + iWaveform[i+1] * 0.4;

                float l = line(uv, float2(x1, y1), float2(x2, y2), width);

                // Add "Glow" based on signal intensity
                finalColor += signalColor * l * (1.0 + iWaveform[i] * 2.0);

                // Add extra atmospheric glow
                finalColor += signalColor * smoothstep(0.05, 0.0, length(uv - float2(x1, y1))) * 0.05 * iWaveform[i];
            }

            // 3. Scanline effect
            float scanline = sin(fragCoord.y * 2.0 + iTime * 10.0) * 0.05 + 0.95;
            finalColor *= scanline;

            return float4(finalColor, 0.9);
        }
    """
}
