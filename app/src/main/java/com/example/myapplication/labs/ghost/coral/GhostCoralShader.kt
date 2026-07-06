package com.example.myapplication.labs.ghost.coral

import org.intellij.lang.annotations.Language

/**
 * GhostCoralShader: AGSL shader for the Social Reef.
 *
 * Implements procedural branch growth using domain-warped fractal noise.
 * The coral "calcifies" and glows based on student synergy and vitality.
 */
object GhostCoralShader {
    @Language("AGSL")
    const val CORAL_SHADER = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iDensity;  // Calcification thickness
        uniform float iVitality; // Glow intensity
        uniform float4 iColor;   // Reef color (Cyan/Gold)

        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
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
            for (int i = 0; i < 4; ++i) {
                v += a * noise(p);
                p *= 2.0;
                a *= 0.5;
            }
            return v;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Center-normalized coordinates
            float2 p = (uv - 0.5) * 2.0;
            p.x *= iResolution.x / iResolution.y;

            // Organic branch distortion
            float2 q = p + fbm(p + iTime * 0.2) * 0.5;
            float n = fbm(q * 3.0 + iTime * 0.1);

            // Distance field for the branch (simulated)
            float dist = length(p);

            // Calcification growth: Branches grow thicker with iDensity
            float growth = smoothstep(iDensity * 0.8, iDensity * 0.2, dist);

            // Vitality glow: Procedural "neural fire" inside the coral
            float glow = fbm(p * 5.0 - iTime * 0.5) * iVitality;

            // Color synthesis
            float3 baseColor = iColor.rgb;
            float3 glowColor = float3(1.0, 0.9, 0.7) * glow * iVitality;

            float3 finalColor = mix(baseColor, glowColor, glow * 0.5);

            // Alpha driven by growth and noise
            float alpha = growth * (n * 0.7 + 0.3) * iDensity;

            return half4(finalColor, alpha);
        }
    """
}
