package com.example.myapplication.labs.ghost.morph

import org.intellij.lang.annotations.Language

/**
 * GhostMorphShader: AGSL-powered "Neural Fluid" background for student dossiers.
 *
 * This shader utilizes a multi-layered Simplex noise and plasma-inspired flow
 * to create an organic, high-fidelity background.
 *
 * BOLT ⚡ Optimization:
 * - Uses a single-pass noise approximation for 60fps performance.
 * - Uniforms are hoisted to minimize JNI overhead.
 */
object GhostMorphShader {

    @Language("AGSL")
    const val NEURAL_FLUID = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float4 iColor1; // Deep Violet
        uniform float4 iColor2; // Cyber Cyan

        float2 hash(float2 p) {
            p = float2(dot(p, float2(127.1, 311.7)), dot(p, float2(269.5, 183.3)));
            return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(mix(dot(hash(i + float2(0.0, 0.0)), f - float2(0.0, 0.0)),
                           dot(hash(i + float2(1.0, 0.0)), f - float2(1.0, 0.0)), u.x),
                       mix(dot(hash(i + float2(0.0, 1.0)), f - float2(0.0, 1.0)),
                           dot(hash(i + float2(1.0, 1.0)), f - float2(1.0, 1.0)), u.x), u.y);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = uv * 3.0;

            float t = iTime * 0.2;
            float n = noise(p + t) + 0.5 * noise(p * 2.1 + t * 1.2);

            float3 baseColor = mix(iColor1.rgb, iColor2.rgb, n);

            // Add some "neural" highlights
            float pulse = 0.5 + 0.5 * sin(iTime + uv.x * 10.0 + uv.y * 5.0);
            baseColor += iColor2.rgb * (pow(n, 4.0) * pulse * 0.5);

            return half4(baseColor, 1.0);
        }
    """
}
