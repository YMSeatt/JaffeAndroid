package com.example.myapplication.labs.ghost.lasso

import org.intellij.lang.annotations.Language

/**
 * GhostLassoShader: AGSL shader for the "Neural Lasso" selection effect.
 *
 * Renders a shimmering neon path with a semi-transparent neural glow
 * filling the enclosed area.
 */
object GhostLassoShader {

    @Language("AGSL")
    const val LASSO_SHADER = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 points[64];
        uniform int pointCount;
        uniform float iAlpha; // 0 to 1

        /**
         * Signed distance to a line segment.
         */
        float sdSegment(float2 p, float2 a, float2 b) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float minEdgeDist = 1e10;

            // Calculate distance to the path edges
            for (int i = 0; i < pointCount - 1; i++) {
                minEdgeDist = min(minEdgeDist, sdSegment(fragCoord, points[i], points[i+1]));
            }

            // Shimmering neon effect
            float shimmer = sin(iTime * 10.0 - minEdgeDist * 0.1) * 0.5 + 0.5;
            float edgeWidth = 3.0;
            float edgeMask = smoothstep(edgeWidth, 0.0, minEdgeDist);

            float3 neonColor = float3(0.0, 1.0, 1.0); // Cyan
            float3 glowColor = float3(0.0, 0.5, 0.8); // Deep Blue

            // Background fill (heuristic based on proximity to points for PoC)
            // Note: True polygon fill in AGSL requires winding number or ray casting,
            // but for a quick PoC, we'll use a soft glow around the path.
            float internalGlow = smoothstep(100.0, 0.0, minEdgeDist) * 0.2;

            float3 finalColor = mix(glowColor * internalGlow, neonColor, edgeMask * (0.8 + 0.2 * shimmer));
            float finalAlpha = (edgeMask + internalGlow) * iAlpha;

            return float4(finalColor * finalAlpha, finalAlpha);
        }
    """
}
