package com.example.myapplication.labs.ghost.sonar

import org.intellij.lang.annotations.Language

/**
 * GhostSonarShader: AGSL shader for the Spatial Engagement Discovery wave.
 *
 * This shader renders a circular expanding "ping" wave. It highlights areas
 * within a specific distance from the expanding wavefront.
 */
object GhostSonarShader {
    @Language("AGSL")
    const val SONAR_WAVE = """
        uniform float2 iResolution;
        uniform float2 iCenter;
        uniform float iRadius;
        uniform float iWidth;
        uniform float4 iColor;
        uniform float iIntensity;

        half4 main(float2 fragCoord) {
            float d = distance(fragCoord, iCenter);

            // The expanding ring effect
            float ring = smoothstep(iRadius - iWidth, iRadius, d) * (1.0 - smoothstep(iRadius, iRadius + iWidth, d));

            // Fade out as the ring expands
            float fade = 1.0 - (iRadius / max(iResolution.x, iResolution.y));

            return half4(iColor.rgb, ring * iIntensity * fade);
        }
    """
}
