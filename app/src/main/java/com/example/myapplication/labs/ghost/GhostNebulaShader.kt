package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostNebulaShader: AGSL shaders for the "Ghost Nebula" experiment.
 *
 * This shader simulates a gaseous, colorful nebula that reacts to classroom activity.
 * It uses multi-layered fractional Brownian motion (fbm) and domain warping
 * to create a fluid, cosmic aesthetic.
 */
object GhostNebulaShader {

    @Language("AGSL")
    const val NEBULA_CORE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iGlobalIntensity;

        // Activity Clusters: x, y, density, color_index
        uniform float4 iClusters[10];
        uniform int iClusterCount;

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
            float2x2 m = float2x2(0.8, 0.6, -0.6, 0.8);
            for (int i = 0; i < 6; ++i) {
                v += a * noise(p);
                p = m * p * 2.0 + shift;
                a *= 0.5;
            }
            return v;
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float aspect = iResolution.x / iResolution.y;
            float2 p = uv * 4.0;
            p.x *= aspect;

            // Domain Warping for the "Gaseous" feel
            float2 q = float2(fbm(p + iTime * 0.1), fbm(p + 1.0));
            float2 r = float2(fbm(p + 4.0 * q + iTime * 0.05 + 2.0), fbm(p + 4.0 * q + iTime * 0.03 + 8.0));
            float f = fbm(p + 4.0 * r);

            // Base Cosmic Color (Dark Deep Purple/Blue)
            float3 color = mix(float3(0.02, 0.0, 0.05), float3(0.1, 0.05, 0.2), f);

            // Apply influence from activity clusters
            for (int i = 0; i < iClusterCount; i++) {
                float2 clusterPos = iClusters[i].xy / iResolution.xy;
                clusterPos.x *= aspect;
                float density = iClusters[i].z;
                float colorIdx = iClusters[i].w;

                float2 p_cluster = uv;
                p_cluster.x *= aspect;

                float dist = length(p_cluster - clusterPos);

                // Gaseous glow around activity
                float glow = exp(-dist * 4.0) * density * iGlobalIntensity;

                float3 clusterColor = float3(0.0);
                if (colorIdx < 0.5) clusterColor = float3(0.0, 0.8, 1.0); // Cyan (Positive)
                else if (colorIdx < 1.5) clusterColor = float3(1.0, 0.0, 1.0); // Magenta (Negative)
                else clusterColor = float3(1.0, 0.8, 0.0); // Gold (Neutral/High Activity)

                color += clusterColor * glow * f;
            }

            // Distant Stars
            float starNoise = hash(uv * 500.0);
            if (starNoise > 0.998) {
                color += float3(1.0) * starNoise * (0.5 + 0.5 * sin(iTime + starNoise * 10.0));
            }

            return float4(color, 0.8 * iGlobalIntensity * f + 0.2);
        }
    """
}
