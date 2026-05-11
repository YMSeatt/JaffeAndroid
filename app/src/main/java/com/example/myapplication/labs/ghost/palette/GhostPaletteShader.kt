package com.example.myapplication.labs.ghost.palette

import org.intellij.lang.annotations.Language

/**
 * GhostPaletteShader: AGSL interactive color field for the Ghost Palette experiment.
 */
object GhostPaletteShader {
    @Language("AGSL")
    const val NEURAL_PALETTE_FIELD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float3 iBaseHSV;
        uniform float2 iTouchPos;
        uniform float iHarmonyMode; // 0: COMPLEMENTARY, 1: TRIADIC

        // Helper to convert HSV to RGB
        float3 hsv2rgb(float3 c) {
            float4 K = float4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
            float3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
            return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 touchUV = iTouchPos / iResolution.xy;

            // Base field driven by hue (x) and saturation (y)
            float h = uv.x;
            float s = uv.y;
            float v = iBaseHSV.z;

            float3 rgb = hsv2rgb(float3(h, s, v));

            // Neural ripples around touch point
            float d = distance(uv, touchUV);
            float ripple = sin(d * 20.0 - iTime * 4.0) * 0.05 * exp(-d * 5.0);
            rgb += ripple;

            // Highlight harmonic points
            float h2 = fract(touchUV.x + (iHarmonyMode == 0.0 ? 0.5 : 0.333));
            float d2 = distance(uv, float2(h2, touchUV.y));
            float highlight = smoothstep(0.05, 0.0, d2) * 0.3;
            rgb += highlight;

            if (iHarmonyMode == 1.0) {
                float h3 = fract(touchUV.x + 0.666);
                float d3 = distance(uv, float2(h3, touchUV.y));
                float highlight2 = smoothstep(0.05, 0.0, d3) * 0.3;
                rgb += highlight2;
            }

            return half4(rgb, 1.0);
        }
    """
}
