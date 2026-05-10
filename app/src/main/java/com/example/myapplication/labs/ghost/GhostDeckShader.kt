package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostDeckShader: AGSL-powered background effects for the Ghost Deck cards.
 *
 * It implements a "Neural Flux" domain warping shader that reacts to swipe
 * progress and student affinity.
 */
object GhostDeckShader {

    @Language("AGSL")
    const val NEURAL_FLUX_DECK = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iSwipe;      // -1.0 (Left/Negative) to 1.0 (Right/Positive)
        uniform float iAffinity;   // 0.0 to 1.0 (Student turbulence)
        uniform half4 iColorA;     // GhostCyan
        uniform half4 iColorB;     // GhostMagenta

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
            for (int i = 0; i < 3; i++) {
                v += a * noise(p);
                p *= 2.0;
                a *= 0.5;
            }
            return v;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = uv * 3.0;

            // Domain warping driven by time and affinity
            float2 q = float2(fbm(p + iTime * 0.2), fbm(p + 1.0));
            float2 r = float2(fbm(p + q + iTime * 0.15 + iSwipe * 0.5), fbm(p + q + iTime * 0.1));
            float f = fbm(p + r);

            // Blend colors based on affinity and swipe
            // iSwipe > 0 (Right) shifts towards positive, iSwipe < 0 (Left) shifts towards negative
            float blend = clamp(f + iAffinity * 0.5 + iSwipe * 0.3, 0.0, 1.0);
            half4 baseColor = mix(iColorA, iColorB, blend);

            // Add highlights/shadows based on noise
            baseColor.rgb *= (0.8 + 0.4 * f);

            // Add a subtle "Neural Pulse" based on time
            baseColor.rgb += half3(0.1 * sin(iTime * 2.0)) * iAffinity;

            return half4(baseColor.rgb, 0.9);
        }
    """
}
