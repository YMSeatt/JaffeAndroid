package com.example.myapplication.labs.ghost.spotlight

import org.intellij.lang.annotations.Language

/**
 * GhostSpotlightShader: AGSL shader for the pedagogical focus tool.
 *
 * This shader renders a dimming scrim with a procedural circular cutout (the "spotlight").
 * It supports softness (feathering) and dynamic intensity.
 */
object GhostSpotlightShader {
    @Language("AGSL")
    const val SPOTLIGHT = """
        uniform float2 iResolution;
        uniform float2 iCenter;
        uniform float iRadius;
        uniform float iSoftness;
        uniform float iIntensity;
        uniform float4 iColor;

        half4 main(float2 fragCoord) {
            float d = distance(fragCoord, iCenter);

            // Calculate the spotlight mask (0.0 inside, 1.0 outside)
            float mask = smoothstep(iRadius, iRadius + iSoftness, d);

            // Apply dimming intensity to the area outside the spotlight
            float alpha = mask * iIntensity;

            return half4(iColor.rgb, alpha);
        }
    """
}
