package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostPortalShader: AGSL shaders for the "Ghost Portal" experiment.
 *
 * It provides a futuristic "Wormhole" or "Portal" effect used during
 * inter-app data transfer (Drag & Drop).
 */
object GhostPortalShader {

    @Language("AGSL")
    const val PORTAL_WORMHOLE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPortalPos;
        uniform float iRadius;
        uniform float3 iColor;
        uniform float iIntensity;

        float2 rotate(float2 v, float a) {
            float s = sin(a);
            float c = cos(a);
            return float2(v.x * c - v.y * s, v.x * s + v.y * c);
        }

        half4 main(float2 fragCoord) {
            float2 uv = (fragCoord - iPortalPos) / iResolution.y;
            float dist = length(uv);

            if (dist > iRadius * 1.5) {
                return half4(0.0);
            }

            // Swirl effect
            float angle = atan(uv.y, uv.x);
            float swirl = iIntensity * 5.0 / (dist + 0.1);
            float2 rotatedUv = rotate(uv, angle + swirl + iTime * 2.0);

            // Pulse
            float pulse = 0.8 + 0.2 * sin(iTime * 5.0);
            float edge = smoothstep(iRadius * pulse, iRadius * 0.5 * pulse, dist);

            // Core
            float core = smoothstep(iRadius * 0.2, 0.0, dist);

            float3 finalColor = iColor * edge;
            finalColor += float3(1.0, 1.0, 1.0) * core * iIntensity;

            // Adding some "digital noise" or sparks
            float sparks = fract(sin(dot(rotatedUv, float2(12.9898, 78.233))) * 43758.5453);
            if (sparks > 0.98) {
                finalColor += iColor * 0.5;
            }

            return half4(finalColor, edge * iIntensity);
        }
    """
}
