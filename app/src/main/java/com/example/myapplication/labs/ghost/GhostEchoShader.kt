package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostEchoShader: AGSL shaders for the Ghost Echo classroom atmosphere monitor.
 */
object GhostEchoShader {
    /**
     * ACOUSTIC_TURBULENCE: A procedural "Acoustic Fog" shader.
     *
     * Features:
     * - **FBM Noise**: Generates multi-layered fractal Brownian motion noise for a natural fog look.
     * - **Dynamic Turbulence**: The speed and intensity of the fog movement scale with [iAmplitude].
     * - **Chromatic Shift**: Transitions from a calm cerulean (low noise) to a turbulent crimson (high noise).
     * - **Ambient Integration**: Designed to be rendered as a semi-transparent background layer.
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the drawing area.
     * - [iTime]: Elapsed time for animations.
     * - [iAmplitude]: Normalized acoustic energy (0.0 to 1.0) from [GhostEchoEngine].
     */
    @Language("AGSL")
    const val ACOUSTIC_TURBULENCE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iAmplitude;

        float hash(float n) {
            return fract(sin(n) * 43758.5453123);
        }

        float noise(float2 x) {
            float2 p = floor(x);
            float2 f = fract(x);
            f = f * f * (3.0 - 2.0 * f);
            float n = p.x + p.y * 57.0;
            return mix(mix(hash(n + 0.0), hash(n + 1.0), f.x),
                       mix(hash(n + 57.0), hash(n + 58.0), f.x), f.y);
        }

        float fbm(float2 p) {
            float f = 0.0;
            f += 0.5000 * noise(p); p = p * 2.02;
            f += 0.2500 * noise(p); p = p * 2.03;
            f += 0.1250 * noise(p); p = p * 2.01;
            f += 0.0625 * noise(p);
            return f / 0.9375;
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Distort UVs slightly based on amplitude for "vibration" effect
            float vibration = sin(iTime * 50.0) * iAmplitude * 0.005;
            uv += vibration;

            // Movement speed scales with amplitude
            float speed = 0.2 + iAmplitude * 1.5;
            float2 movement = float2(iTime * speed * 0.1, iTime * speed * 0.05);

            float n = fbm(uv * 3.0 + movement);
            float n2 = fbm(uv * 6.0 - movement * 0.7);
            float turbulence = mix(n, n2, 0.5);

            // Colors
            float3 calmColor = float3(0.05, 0.15, 0.3);   // Dark Blue
            float3 activeColor = float3(0.6, 0.1, 0.05); // Dark Red
            float3 glowColor = float3(0.0, 1.0, 0.8);    // Cyan glow for calm
            float3 alertGlow = float3(1.0, 0.5, 0.0);    // Orange glow for alert

            float3 base = mix(calmColor, activeColor, iAmplitude);
            float3 glow = mix(glowColor, alertGlow, iAmplitude);

            float3 color = base + glow * pow(turbulence, 3.0) * (0.5 + iAmplitude);

            // Alpha scales with amplitude and noise
            float alpha = (turbulence * 0.2 + 0.05) * (1.0 + iAmplitude);

            return float4(color, alpha);
        }
    """
}
