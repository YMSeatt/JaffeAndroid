package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostFluxShader: AGSL scripts for the "Ghost Flux" neural flow layer.
 *
 * This shader simulates fluid dynamics (engagement flow) using domain warping
 * and fractional Brownian motion. It visualizes how classroom energy moves
 * between student nodes.
 */
object GhostFluxShader {
    @Language("AGSL")
    const val NEURAL_FLOW = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPoints[40];
        uniform float iWeights[40];
        uniform int iNumPoints;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            f = f * f * (3.0 - 2.0 * f);
            float a = hash(i);
            float b = hash(i + float2(1.0, 0.0));
            float c = hash(i + float2(0.0, 1.0));
            float d = hash(i + float2(1.0, 1.0));
            return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
        }

        float fbm(float2 p) {
            float v = 0.0;
            float a = 0.5;
            for (int i = 0; i < 4; i++) {
                v += a * noise(p);
                p *= 2.0;
                a *= 0.5;
            }
            return v;
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float aspect = iResolution.x / iResolution.y;
            float2 p = uv;
            p.x *= aspect;

            // Student influence "Vortices" - calculated first for warping
            float m = 0.0;
            float2 warpOffset = float2(0.0);
            for (int i = 0; i < 40; i++) {
                if (i >= iNumPoints) break;
                float2 pos = iPoints[i] / iResolution.xy;
                pos.x *= aspect;
                float d = distance(p, pos);
                float weight = iWeights[i] * exp(-12.0 * d);
                m += weight;

                // Add directional warping based on student positions
                warpOffset += weight * (p - pos);
            }

            // Enhanced Domain Warping influenced by student activity (vortices)
            float2 q = float2(fbm(p + 0.1 * iTime + 0.1 * warpOffset), fbm(p + float2(1.0) + 0.05 * m));
            float2 r = float2(fbm(p + 4.0 * q + 0.5 * iTime), fbm(p + 4.0 * q + float2(1.7, 9.2) + 0.2 * warpOffset));
            float f = fbm(p + 4.0 * r + 0.1 * m);

            // Combine flow and student intensity
            float3 baseColor = float3(0.0, 0.05, 0.15); // Deep Neural Blue
            float3 flowColor = float3(0.0, 0.5, 0.8);  // Cyan Flow
            float3 surgeColor = float3(0.4, 0.9, 1.0); // Bright Surge

            float3 color = mix(baseColor, flowColor, f);
            color = mix(color, surgeColor, m);

            // Pulse based on time
            float pulse = 0.85 + 0.15 * sin(iTime * 1.5);

            return float4(color * pulse, (0.35 * f + 0.45 * m) * pulse);
        }
    """
}
