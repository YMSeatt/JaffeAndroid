package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostNebulaShader: AGSL shaders for the upgraded "Ghost Nebula 3D" experiment.
 *
 * This shader performs full 3D Raymarching to render volumetric gaseous clouds.
 * It features responsive tilt-driven camera controls and a simulated 3D depth field
 * where student activity nodes project as glowing, colored volumetric energy wells,
 * floating in front of parallax-mapped background stars.
 */
object GhostNebulaShader {

    @Language("AGSL")
    const val NEBULA_CORE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iGlobalIntensity;
        uniform float2 iTilt;
        uniform float iDepthFactor;

        // Activity Clusters: x, y, density, color_index
        uniform float4 iClusters[10];
        uniform int iClusterCount;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
        }

        float hash3(float3 p) {
            p = fract(p * float3(443.8975, 397.2973, 491.1871));
            p += dot(p.xyz, p.yzx + 19.19);
            return fract(p.x * p.y * p.z);
        }

        float noise3(float3 p) {
            float3 i = floor(p);
            float3 f = fract(p);
            float3 u = f * f * (3.0 - 2.0 * f);

            return mix(
                mix(
                    mix(hash3(i + float3(0,0,0)), hash3(i + float3(1,0,0)), u.x),
                    mix(hash3(i + float3(0,1,0)), hash3(i + float3(1,1,0)), u.x),
                    u.y
                ),
                mix(
                    mix(hash3(i + float3(0,0,1)), hash3(i + float3(1,0,1)), u.x),
                    mix(hash3(i + float3(0,1,1)), hash3(i + float3(1,1,1)), u.x),
                    u.y
                ),
                u.z
            );
        }

        float fbm3(float3 p) {
            float v = 0.0;
            float a = 0.5;
            float3 shift = float3(100.0);
            for (int i = 0; i < 3; ++i) {
                v += a * noise3(p);
                p = p * 2.0 + shift;
                a *= 0.5;
            }
            return v;
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float aspect = iResolution.x / iResolution.y;

            // 3D Camera Setup: origin is shifted by real-time tilt sensor input
            float3 ro = float3(0.0, 0.0, -1.8) + float3(iTilt.x * 0.8, iTilt.y * 0.8, 0.0);
            float3 rd = normalize(float3((uv - 0.5) * float2(aspect, 1.0), 1.2));

            // Volumetric Raymarching
            float t = 0.0;
            float4 sum = float4(0.0);
            float stepSize = 0.15;

            for (int i = 0; i < 18; ++i) {
                if (sum.a >= 0.95) break;

                float3 p = ro + rd * t;

                // 3D procedural gaseous cloud density
                float d = fbm3(p * 1.5 + float3(0.0, 0.0, iTime * 0.12));

                // Process student clusters mapped in virtual 3D space
                float clusterGlow = 0.0;
                float3 clusterColorSum = float3(0.0);
                float totalWeight = 0.0;

                for (int j = 0; j < 10; ++j) {
                    if (j >= iClusterCount) break;

                    float2 cPos = iClusters[j].xy / iResolution.xy;
                    // Project the 2D position into a floating 3D coordinate space with dynamic depth
                    float3 cPos3D = float3(
                        (cPos.x - 0.5) * aspect * 2.0,
                        (cPos.y - 0.5) * 2.0,
                        sin(iTime * 0.2 + float(j) * 1.5) * 0.4
                    );

                    float dist = length(p - cPos3D);
                    float glow = exp(-dist * 6.0) * iClusters[j].z;
                    clusterGlow += glow;

                    float3 cCol = float3(0.0);
                    float idx = iClusters[j].w;
                    if (idx < 0.5) {
                        cCol = float3(0.0, 0.8, 1.0); // Cyan (Positive)
                    } else if (idx < 1.5) {
                        cCol = float3(1.0, 0.0, 1.0); // Magenta (Negative)
                    } else {
                        cCol = float3(1.0, 0.8, 0.0); // Gold (Neutral / Milestone)
                    }

                    clusterColorSum += cCol * glow;
                    totalWeight += glow;
                }

                // Base Deep Space Color
                float3 col = mix(float3(0.01, 0.0, 0.04), float3(0.08, 0.04, 0.2), d);
                if (totalWeight > 0.0) {
                    col = mix(col, clusterColorSum / totalWeight, clusterGlow / (clusterGlow + 0.1));
                }

                // Calculate local volumetric cloud opacity using Beer-Lambert approximation
                float opacity = (d * 0.18 + clusterGlow * 0.45) * iGlobalIntensity;
                opacity = clamp(opacity * iDepthFactor, 0.0, 1.0);

                float4 sampleCol = float4(col * opacity, opacity);
                sum += sampleCol * (1.0 - sum.a);

                t += stepSize;
            }

            float3 finalColor = sum.rgb;

            // Deep background stars showing stunning volumetric perspective shift (parallax)
            float starNoise = hash(rd.xy * 250.0);
            if (starNoise > 0.997 && sum.a < 0.8) {
                float starIntensity = starNoise * (1.0 - sum.a) * (0.3 + 0.7 * sin(iTime * 2.0 + starNoise * 10.0));
                finalColor += float3(1.0) * starIntensity;
            }

            return float4(finalColor, clamp(sum.a + 0.15, 0.0, 1.0));
        }
    """
}
