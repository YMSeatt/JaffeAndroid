package com.example.myapplication.labs.ghost.prism

import org.intellij.lang.annotations.Language

/**
 * GhostPrismShader: AGSL shader definitions for the Ghost Prism aesthetic system.
 *
 * This object contains high-performance shaders that drive the visual identity
 * of each student "vibe".
 */
object GhostPrismShader {

    @Language("AGSL")
    const val PRISM_BACKGROUND = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform int iVibe; // 0: NEON_DREAM, 1: CYBER_PUNK, 2: ZEN_GARDEN, 3: VOID_RUNNER, 4: SOLAR_FLARE

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
            float3 color = float3(0.1, 0.1, 0.1);

            if (iVibe == 0) { // NEON_DREAM: Cyan/Magenta gradients
                float n = noise(uv * 3.0 + iTime * 0.2);
                color = mix(float3(0.0, 1.0, 1.0), float3(1.0, 0.0, 1.0), uv.y + n * 0.3);
                color *= 0.6 + 0.4 * sin(iTime + uv.x * 5.0);
            } else if (iVibe == 1) { // CYBER_PUNK: Glitchy yellow/black
                float n = noise(uv * 10.0 + iTime * 2.0);
                if (n > 0.8) {
                    color = float3(1.0, 0.9, 0.0);
                } else {
                    color = float3(0.05, 0.05, 0.05);
                }
                float scanline = sin(uv.y * 100.0 + iTime * 10.0);
                color += 0.1 * float3(scanline);
            } else if (iVibe == 2) { // ZEN_GARDEN: Soft green/blue
                float n = noise(uv * 2.0 + iTime * 0.1);
                color = mix(float3(0.2, 0.4, 0.3), float3(0.3, 0.4, 0.5), uv.x + n);
                color *= 0.8;
            } else if (iVibe == 3) { // VOID_RUNNER: Deep red/darkness
                float dist = distance(uv, float2(0.5, 0.5));
                float pulse = 0.5 + 0.5 * sin(iTime * 1.5);
                color = mix(float3(0.3, 0.0, 0.0), float3(0.02, 0.02, 0.02), dist * (2.0 - pulse));
            } else if (iVibe == 4) { // SOLAR_FLARE: Bright orange/white
                float n = noise(uv * 5.0 - iTime * 0.5);
                color = mix(float3(1.0, 0.4, 0.0), float3(1.0, 1.0, 0.8), n);
                float glow = 0.7 + 0.3 * sin(iTime * 4.0);
                color *= glow;
            }

            return half4(color, 1.0);
        }
    """
}
