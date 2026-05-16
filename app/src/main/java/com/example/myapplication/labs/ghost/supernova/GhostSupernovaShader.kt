package com.example.myapplication.labs.ghost.supernova

import org.intellij.lang.annotations.Language

/**
 * GhostSupernovaShader: AGSL scripts for the "Classroom Supernova" effect.
 *
 * These shaders implement the procedural visual lifecycle of a classroom criticality event.
 *
 * ### Shader Architecture:
 * 1. **[CORE_PRESSURE]**: A persistent background distortion shader that visualizes
 *    cumulative behavioral stress through heat distortion and flickering.
 * 2. **[SUPERNOVA_EXPLOSION]**: A multi-stage sequence shader that handles the
 *    physics of the contraction (implosion), shockwave (explosion), and nebula cooling.
 */
object GhostSupernovaShader {

    /**
     * Renders a procedural heat-map and core glow representing the current pressure.
     *
     * ### Uniforms:
     * - `iPressure`: Drives the intensity of the heat distortion and flickering speed.
     * - `iColor`: The primary base color (typically Cyan).
     */
    @Language("AGSL")
    const val CORE_PRESSURE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iPressure; // 0.0 (Idle) to 1.0 (Critical)
        uniform float3 iColor;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 center = float2(0.5, 0.5);
            float d = distance(uv, center);

            // 🌡️ Heat Distortion:
            // Uses a sine wave combined with the pressure factor to warp UV coordinates vertically.
            float distortion = sin(uv.y * 100.0 + iTime * 5.0) * iPressure * 0.01;
            float2 p = uv + distortion;

            // 🕯️ Core Glow:
            // An inverse attenuation glow around the center of the screen.
            // Pulsates faster as iPressure increases (iTime * 10.0 * iPressure).
            float glow = 0.05 / (d + 0.05);
            glow *= (sin(iTime * 10.0 * iPressure) * 0.2 + 0.8);

            float3 color = iColor * glow * iPressure;

            // ⚠️ Neural Flicker:
            // At high pressure (>0.8), introduces random chromatic noise to signal instability.
            if (iPressure > 0.8) {
                color.r += hash(uv + iTime) * 0.1;
            }

            return float4(color, color.r * 0.4);
        }
    """

    /**
     * Orchestrates the high-energy stages of the supernova lifecycle.
     *
     * ### Uniforms:
     * - `iProgress`: Linear 0.0 to 1.0 progress of the current stage.
     * - `iStage`: 1 (Contraction), 2 (Explosion), 3 (Nebula).
     */
    @Language("AGSL")
    const val SUPERNOVA_EXPLOSION = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iProgress; // Animation progress (0.0 -> 1.0)
        uniform int iStage;     // 1: Contraction, 2: Explosion, 3: Nebula
        uniform float3 iColor;

        float3 hash33(float3 p3) {
            p3 = fract(p3 * float3(.1031, .1030, .0973));
            p3 += dot(p3, p3.yxz + 33.33);
            return fract((p3.xxy + p3.yxx) * p3.zyx);
        }

        float4 main(float2 fragCoord) {
            // Normalize UVs to center of screen with aspect ratio correction.
            float2 uv = (fragCoord - 0.5 * iResolution.xy) / iResolution.y;
            float3 finalColor = float3(0.0);
            float alpha = 0.0;

            if (iStage == 1) {
                // 🌀 STAGE 1: CONTRACTION (Implosion)
                // Radius shrinks as progress increases (1.0 - iProgress).
                float radius = (1.0 - iProgress) * 1.5;
                float d = length(uv);
                // Renders a thin, bright ring that moves toward the center.
                float ring = smoothstep(radius, radius - 0.1, d) * smoothstep(radius - 0.2, radius - 0.1, d);
                finalColor = float3(0.0, 0.8, 1.0) * ring * 2.0; // Blue-shifted
                alpha = ring * 0.8;
            } else if (iStage == 2) {
                // 💥 STAGE 2: EXPLOSION (Shockwave)
                // Radius expands outward (iProgress * 3.0).
                float radius = iProgress * 3.0;
                float d = length(uv);
                // An exponential wave centered at the expanding radius.
                float wave = exp(-abs(d - radius) * 10.0);
                finalColor = float3(1.0, 0.2, 0.8) * wave * 5.0; // Magenta-shifted

                // ✨ Neural Debris:
                // Procedural "stars" or debris particles that appear during the shockwave.
                float3 p = hash33(float3(floor(fragCoord * 0.05), iProgress));
                if (p.x > 0.99) {
                    finalColor += 1.0;
                }

                alpha = wave * (1.0 - iProgress);
            } else if (iStage == 3) {
                // 🌌 STAGE 3: NEBULA (Cooling Gas)
                // A soft, central cloud that fades out as progress increases.
                float d = length(uv);
                float cloud = exp(-d * 2.0) * (1.0 - iProgress);
                finalColor = iColor * cloud;
                alpha = cloud * 0.3;
            }

            return float4(finalColor, alpha);
        }
    """
}
