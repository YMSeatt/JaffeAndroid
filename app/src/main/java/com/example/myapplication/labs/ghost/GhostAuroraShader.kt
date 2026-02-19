package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostAuroraShader: AGSL scripts for the "Ghost Aurora" classroom climate visualization.
 *
 * This shader uses a multi-layered procedural noise algorithm (domain warping) to
 * simulate the ethereal, flowing motion of an aurora borealis. The visual state
 * is driven by classroom "climate" metrics.
 */
object GhostAuroraShader {
    @Language("AGSL")
    const val CLASSROOM_AURORA = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity;    // Overall brightness/activity (0.0 to 1.0)
        uniform float3 iColorPrimary; // Main aurora color
        uniform float3 iColorSecondary; // Secondary accent color
        uniform float iSpeed;        // Flow velocity

        // 2D Noise function
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

        // Fractal Brownian Motion
        float fbm(float2 p) {
            float v = 0.0;
            float a = 0.5;
            float2 shift = float2(100.0);
            // Rotate to reduce axial bias
            float2x2 m = float2x2(0.8, 0.6, -0.6, 0.8);
            for (int i = 0; i < 5; ++i) {
                v += a * noise(p);
                p = m * p * 2.0 + shift;
                a *= 0.5;
            }
            return v;
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = uv * 3.0;

            // Domain Warping for fluid motion
            float2 q = float2(fbm(p + iTime * 0.2 * iSpeed), fbm(p + 1.0));
            float2 r = float2(fbm(p + 4.0 * q + iTime * 0.15 * iSpeed + 1.7), fbm(p + 4.0 * q + iTime * 0.1 * iSpeed + 9.2));
            float f = fbm(p + 4.0 * r);

            // Color synthesis
            float3 baseColor = mix(iColorPrimary, iColorSecondary, f);
            float3 finalColor = baseColor * (f * f * f + 0.6 * f * f + 0.5 * f);

            // Apply intensity and vertical gradient (stronger at the top)
            float alpha = f * iIntensity * (1.2 - uv.y);

            // Subtle digital shimmer
            float shimmer = noise(uv * 100.0 + iTime) * 0.05;
            finalColor += shimmer;

            return float4(finalColor * alpha, alpha * 0.4);
        }
    """
}
