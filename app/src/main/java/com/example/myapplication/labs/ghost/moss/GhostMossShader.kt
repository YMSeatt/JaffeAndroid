package com.example.myapplication.labs.ghost.moss

import org.intellij.lang.annotations.Language

/**
 * GhostMossShader: AGSL shader for procedural moss texture.
 */
object GhostMossShader {
    @Language("AGSL")
    const val MOSS_TEXTURE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iDormancy; // 0.0 to 1.0
        uniform float4 iColor;    // Base moss color

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
            for (int i = 0; i < 5; ++i) {
                v += a * noise(p);
                p = p * 2.0 + shift;
                a *= 0.5;
            }
            return v;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = uv * 6.0;

            // Animated texture
            float n = fbm(p + iTime * 0.1);

            // Distance from center for radial growth
            float d = distance(uv, float2(0.5));

            // Moss growth logic
            float growth = smoothstep(iDormancy * 0.6, iDormancy * 0.1, d);

            // Velvet texture factor
            float texture = n * 0.5 + 0.5;

            // Final color: Deep forest green with variation
            float3 mossGreen = iColor.rgb * (texture * 0.8 + 0.2);
            float alpha = growth * (texture * 0.9 + 0.1);

            return half4(mossGreen, alpha);
        }
    """
}
