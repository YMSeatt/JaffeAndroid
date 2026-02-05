package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostShader: Central repository for AGSL (Android Graphics Shading Language) scripts.
 *
 * These shaders are used by the Neural Map and Voice Assistant to provide high-performance,
 * GPU-accelerated visual effects. Requires Android 13 (API 33) or higher.
 */
object GhostShader {
    /**
     * A pulsating background effect for dialogs and insights.
     *
     * Uniforms:
     * - [iResolution]: The dimensions of the canvas (width, height).
     * - [iTime]: Elapsed time in seconds for animation.
     * - [iColor]: The base color for the pulse effect.
     */
    @Language("AGSL")
    const val NEURAL_PULSE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float3 iColor;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            uv = uv * 2.0 - 1.0;
            uv.x *= iResolution.x / iResolution.y;

            float d = length(uv);
            float pulse = sin(d * 10.0 - iTime * 3.0) * 0.5 + 0.5;
            pulse *= exp(-d * 2.0);

            float3 color = iColor * pulse;
            color += iColor * 0.1 / d; // Glow

            return float4(color, 0.8 * pulse);
        }
    """

    /**
     * An animated, pulsating line shader for connecting student group members.
     *
     * Uniforms:
     * - [iTime]: Elapsed time in seconds.
     * - [iColor]: Color of the line.
     * - [iResolution]: Dimensions of the canvas.
     */
    @Language("AGSL")
    const val NEURAL_LINE = """
        uniform float iTime;
        uniform float3 iColor;
        uniform float2 iResolution;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Pulsating intensity
            float pulse = sin(iTime * 4.0) * 0.3 + 0.7;

            // Add some "noise" or movement along the line
            float movement = sin(uv.x * 20.0 + iTime * 10.0) * 0.1;

            float3 finalColor = iColor * (pulse + movement);
            return float4(finalColor, 0.9);
        }
    """

    /**
     * A localized glow effect centered on a specific student. Used to highlight
     * behavioral "hotspots".
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the canvas.
     * - [iTime]: Elapsed time in seconds.
     * - [iCenter]: The center coordinate of the aura (relative to the canvas).
     * - [iColor]: The color of the aura (typically red for negative behavior).
     * - [iIntensity]: The brightness/scaling of the aura (0.0 to 1.0).
     */
    @Language("AGSL")
    const val COGNITIVE_AURA = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iCenter;
        uniform float3 iColor;
        uniform float iIntensity;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 center = iCenter / iResolution.xy;

            float d = distance(uv, center);
            float aura = 0.05 / (d + 0.01);

            // Pulsate
            aura *= (sin(iTime * 5.0 - d * 20.0) * 0.2 + 0.8);

            float3 color = iColor * aura * iIntensity;
            return float4(color, color.r * 0.5);
        }
    """

    /**
     * A multi-frequency sine wave visualizer that reacts to voice input.
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the canvas.
     * - [iTime]: Elapsed time in seconds.
     * - [iAmplitude]: The current volume/amplitude of the voice (0.0 to 1.0).
     * - [iColor]: The color of the waveform.
     */
    @Language("AGSL")
    const val VOICE_WAVEFORM = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iAmplitude;
        uniform float3 iColor;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float y = uv.y * 2.0 - 1.0;

            // Multiple sine waves for a "neural" feel
            float wave = sin(uv.x * 10.0 + iTime * 5.0) * iAmplitude;
            wave += sin(uv.x * 25.0 - iTime * 3.0) * iAmplitude * 0.5;
            wave += sin(uv.x * 5.0 + iTime * 2.0) * iAmplitude * 0.2;

            float dist = abs(y - wave);
            float intensity = 0.015 / (dist + 0.005);

            // Fade out at edges
            intensity *= sin(uv.x * 3.14159);

            float3 color = iColor * intensity;
            return float4(color, intensity * 0.8);
        }
    """
}
