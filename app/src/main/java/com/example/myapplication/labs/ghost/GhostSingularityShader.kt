package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostSingularityShader: AGSL code for the "Data Sink" effect.
 *
 * Implements a gravitational lensing simulation. It distorts the UV coordinates
 * based on proximity to the singularity center, creating a "pull" visual effect.
 * It also renders a glowing accretion disk using procedural noise.
 */
object GhostSingularityShader {

    @Language("AGSL")
    const val GRAVITATIONAL_LENSING = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iSingularityPos;
        uniform float iRadius;
        uniform float iIntensity;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            float a = hash(i);
            float b = hash(i + float2(1.0, 0.0));
            float c = hash(i + float2(0.0, 1.0));
            float d = hash(i + float2(1.0, 1.0));
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = fragCoord - iSingularityPos;
            float d = length(p);

            // Gravitational Lensing Distortion
            // As we get closer to the event horizon, space-time curves inward
            float distortion = 0.0;
            if (d < iRadius * 4.0) {
                distortion = iIntensity * (1.0 - d / (iRadius * 4.0)) * (iRadius / (d + 1.0));
            }

            float2 distortedUV = uv - (p / iResolution.xy) * distortion * 0.5;

            // Accretion Disk (Glow)
            float disk = 0.0;
            if (d < iRadius * 2.5) {
                float angle = atan(p.y, p.x);
                float n = noise(float2(angle * 3.0 + iTime * 2.0, d * 0.05 - iTime));
                disk = pow(1.0 - d / (iRadius * 2.5), 2.0) * n * iIntensity;
            }

            // Event Horizon (The Black Hole)
            float hole = smoothstep(iRadius, iRadius - 2.0, d);

            half4 color = half4(0.0);

            // Purple/Cyan Accretion Disk Colors
            half3 diskColor = mix(half3(0.0, 1.0, 1.0), half3(0.5, 0.0, 1.0), disk);
            color.rgb = diskColor * disk;

            // Subtract the hole
            color.rgb *= (1.0 - hole);
            color.a = (disk + hole) * 0.8;

            return color;
        }
    """
}
