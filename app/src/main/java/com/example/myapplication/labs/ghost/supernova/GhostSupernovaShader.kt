package com.example.myapplication.labs.ghost.supernova

import org.intellij.lang.annotations.Language

/**
 * GhostSupernovaShader: AGSL scripts for the "Classroom Supernova" effect.
 *
 * This shader captures the four stages of a supernova:
 * 1. **IDLE**: No effect or subtle heat distortion.
 * 2. **CONTRACTION**: A bright, blue-shifted implosion toward the classroom center.
 * 3. **EXPLOSION**: A massive, magenta-shifted shockwave that "resets" the data layer.
 * 4. **NEBULA**: A slow, cooling gaseous cloud indicating the new stable state.
 */
object GhostSupernovaShader {

    @Language("AGSL")
    const val CORE_PRESSURE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iPressure; // 0.0 to 1.0
        uniform float3 iColor;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 center = float2(0.5, 0.5);
            float d = distance(uv, center);

            // Heat Distortion
            float distortion = sin(uv.y * 100.0 + iTime * 5.0) * iPressure * 0.01;
            float2 p = uv + distortion;

            // Core Glow
            float glow = 0.05 / (d + 0.05);
            glow *= (sin(iTime * 10.0 * iPressure) * 0.2 + 0.8);

            float3 color = iColor * glow * iPressure;

            // Chromatic shift at high pressure
            if (iPressure > 0.8) {
                color.r += hash(uv + iTime) * 0.1;
            }

            return float4(color, color.r * 0.4);
        }
    """

    @Language("AGSL")
    const val SUPERNOVA_EXPLOSION = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iProgress; // 0.0 to 1.0 (Contraction -> Explosion)
        uniform int iStage;     // 1: Contraction, 2: Explosion, 3: Nebula
        uniform float3 iColor;

        float3 hash33(float3 p3) {
            p3 = fract(p3 * float3(.1031, .1030, .0973));
            p3 += dot(p3, p3.yxz + 33.33);
            return fract((p3.xxy + p3.yxx) * p3.zyx);
        }

        float4 main(float2 fragCoord) {
            float2 uv = (fragCoord - 0.5 * iResolution.xy) / iResolution.y;
            float3 finalColor = float3(0.0);
            float alpha = 0.0;

            if (iStage == 1) {
                // CONTRACTION: Blue-shifted implosion
                float radius = (1.0 - iProgress) * 1.5;
                float d = length(uv);
                float ring = smoothstep(radius, radius - 0.1, d) * smoothstep(radius - 0.2, radius - 0.1, d);
                finalColor = float3(0.0, 0.8, 1.0) * ring * 2.0;
                alpha = ring * 0.8;
            } else if (iStage == 2) {
                // EXPLOSION: Magenta shockwave
                float radius = iProgress * 3.0;
                float d = length(uv);
                float wave = exp(-abs(d - radius) * 10.0);
                finalColor = float3(1.0, 0.2, 0.8) * wave * 5.0;

                // Add debris (stars)
                float3 p = hash33(float3(floor(fragCoord * 0.05), iProgress));
                if (p.x > 0.99) {
                    finalColor += 1.0;
                }

                alpha = wave * (1.0 - iProgress);
            } else if (iStage == 3) {
                // NEBULA: Cooling gas
                float d = length(uv);
                float cloud = exp(-d * 2.0) * (1.0 - iProgress);
                finalColor = iColor * cloud;
                alpha = cloud * 0.3;
            }

            return float4(finalColor, alpha);
        }
    """
}
