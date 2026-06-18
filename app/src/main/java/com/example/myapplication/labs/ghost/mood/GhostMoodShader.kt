package com.example.myapplication.labs.ghost.mood

import org.intellij.lang.annotations.Language

/**
 * GhostMoodShader: AGSL shaders for the Neural Mood Board.
 *
 * This shader visualizes the collective classroom mood as an organic, shifting color field
 * using a combination of Voronoi-like noise and smooth color blending.
 */
object GhostMoodShader {

    @Language("AGSL")
    const val NEURAL_MOOD_BOARD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity;
        uniform float iValence;
        uniform float iStability;
        uniform half4 iColorPrimary;
        uniform half4 iColorSecondary;

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

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = uv * 3.0;

            // 1. Organic Noise Synthesis:
            // Combines two layers of gradient noise at different scales and speeds.
            // iStability modulates the flow velocity.
            float n1 = noise(p + iTime * 0.2 * iStability);
            float n2 = noise(p * 2.0 - iTime * 0.1 * iStability);
            float combinedNoise = (n1 + n2 * 0.5) / 1.5;

            // 2. Valence-Based Color Mixing:
            // Interpolates between the primary and secondary colors based on the
            // synthesized classroom valence (-1.0 to 1.0).
            half4 moodColor = mix(iColorSecondary, iColorPrimary, iValence * 0.5 + 0.5);

            // 3. Neural Turbulence:
            // Injects high-frequency noise ("flicker") when stability is low.
            float turbulence = (1.0 - iStability) * noise(p * 10.0 + iTime);
            moodColor.rgb += turbulence * 0.1;

            // 4. Alpha Refraction:
            // The final transparency is a product of synthesized intensity and noise,
            // creating an ethereal, data-driven atmosphere.
            float alpha = iIntensity * combinedNoise * 0.6;

            return half4(moodColor.rgb, alpha);
        }
    """
}
