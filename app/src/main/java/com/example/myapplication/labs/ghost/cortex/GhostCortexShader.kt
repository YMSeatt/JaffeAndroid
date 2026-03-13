package com.example.myapplication.labs.ghost.cortex

import org.intellij.lang.annotations.Language

/**
 * GhostCortexShader: AGSL source for the Somatic Field visualization.
 *
 * This shader renders organic, pulsing ripples that represent "Neural Intent"
 * and "Somatic Resonance". It uses multi-layered fractal noise and distance
 * warping to create a tactile visual metaphor.
 */
object GhostCortexShader {

    @Language("AGSL")
    const val SOMATIC_FIELD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iTension;
        uniform float2 iTouchPos;

        // Fractal Brownian Motion for organic flow
        float hash(float2 p) {
            return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(mix(hash(i + float2(0.0, 0.0)), hash(i + float2(1.0, 0.0)), u.x),
                       mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);
        }

        float fbm(float2 p) {
            float v = 0.0;
            float a = 0.5;
            float2 shift = float2(100.0);
            for (int i = 0; i < 4; ++i) {
                v += a * noise(p);
                p = p * 2.0 + shift;
                a *= 0.5;
            }
            return v;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.y, iResolution.x);
            float2 touch = (iTouchPos * 2.0 - iResolution.xy) / min(iResolution.y, iResolution.x);

            // Distance to touch/tension centers
            float d = length(p - touch);

            // Pulsing ripples
            float ripple = sin(d * 15.0 - iTime * 4.0) * 0.5 + 0.5;
            ripple *= exp(-d * 3.0); // Decay

            // Organic background noise
            float n = fbm(p * 2.0 + iTime * 0.2);

            // Color mapping based on tension
            // Low tension: Calm Cyan
            // High tension: Agitated Magenta/Red
            half3 colorA = half3(0.0, 0.8, 1.0); // Cyan
            half3 colorB = half3(1.0, 0.0, 0.5); // Magenta

            half3 baseColor = mix(colorA, colorB, iTension);

            // Apply ripple and noise
            half3 finalColor = baseColor * (ripple * (0.5 + iTension * 0.5) + n * 0.2);

            // Atmosphere glow
            float glow = 0.05 / (d + 0.1);
            finalColor += baseColor * glow * iTension;

            return half4(finalColor, finalColor.r * 0.3); // Semi-transparent based on intensity
        }
    """
}
