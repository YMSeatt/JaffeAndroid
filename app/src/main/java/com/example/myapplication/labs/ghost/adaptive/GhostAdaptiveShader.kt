package com.example.myapplication.labs.ghost.adaptive

import org.intellij.lang.annotations.Language

/**
 * GhostAdaptiveShader: AGSL logic for the Adaptive UI density heatmap.
 *
 * This shader renders pulsating amber/cyan fields at specific grid coordinates
 * corresponding to classroom "Crowding Zones".
 */
object GhostAdaptiveShader {

    @Language("AGSL")
    val ADAPTIVE_HEATMAP = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float4 iDensityPoints[25]; // (x, y, density, padding) x 25
        uniform int iPointCount;

        float sdCircle(float2 p, float r) {
            return length(p) - r;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float3 finalColor = float3(0.01, 0.02, 0.05); // Deep space background
            float alpha = 0.0;

            for (int i = 0; i < iPointCount; i++) {
                // iDensityPoints is packed as (x, y, density, padding)
                float2 center = iDensityPoints[i].xy / 4000.0;
                float density = iDensityPoints[i].z;

                // Pulsate based on density and time
                float pulse = 0.5 + 0.5 * sin(iTime * (2.0 + density * 3.0));
                float radius = (0.05 + 0.1 * density) * pulse;

                float dist = distance(uv, center);
                if (dist < radius) {
                    float intensity = pow(1.0 - (dist / radius), 2.0) * density;

                    // High density = Amber, Low density = Cyan
                    float3 color = lerp(float3(0.0, 0.8, 0.8), float3(1.0, 0.6, 0.1), density);
                    finalColor += color * intensity * 0.6;
                    alpha += intensity * 0.4;
                }
            }

            return half4(finalColor, clamp(alpha, 0.0, 0.8));
        }
    """.trimIndent()
}
