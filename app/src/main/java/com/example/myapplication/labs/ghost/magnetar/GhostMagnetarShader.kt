package com.example.myapplication.labs.ghost.magnetar

import org.intellij.lang.annotations.Language

/**
 * GhostMagnetarShader: AGSL code for visualizing "Social Magnetic Field Lines".
 *
 * This shader simulates the flow of magnetic flux between student "poles."
 * It uses a multi-dipole vector field summation to draw streamlines.
 */
object GhostMagnetarShader {

    @Language("AGSL")
    const val MAGNETIC_FIELD_LINES = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iHeading;
        uniform float2 iDipolePos[15];
        uniform float iDipoleStrength[15];
        uniform int iDipoleCount;

        float2 getField(float2 p) {
            float2 field = float2(0.0);
            // Sum contributions from up to 15 student dipoles.
            // Uses Inverse-Square Law approximation for magnetic flux.
            for (int i = 0; i < 15; i++) {
                if (i >= iDipoleCount) break;
                float2 d = p - iDipolePos[i];
                float r2 = dot(d, d) + 100.0;
                // Magnetic Monopole simulation for visual streamlines.
                // Parity with Python/ghost_magnetar_analysis.py field calculation.
                field += (d / pow(r2, 1.5)) * iDipoleStrength[i] * 5000.0;
            }
            // Apply external magnetic field from device magnetometer to skew the global field.
            float2 externalField = float2(cos(iHeading), sin(iHeading)) * 0.05;
            return field + externalField;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float aspect = iResolution.x / iResolution.y;
            float2 p = fragCoord;

            // Streamline simulation using Noise-based LIC (Line Integral Convolution) simplified
            float2 f = getField(p);
            float strength = length(f);

            // Procedural "Iron Filings" texture
            float pattern = sin(dot(p, f * 0.1) - iTime * 5.0 * strength);
            pattern *= sin(dot(p, float2(-f.y, f.x) * 0.05));

            float3 northColor = float3(0.0, 0.8, 1.0); // Cyan North
            float3 southColor = float3(1.0, 0.0, 0.5); // Magenta South

            float3 baseColor = mix(southColor, northColor, step(0.0, dot(f, float2(1.0, 0.0))));

            float alpha = smoothstep(0.0, 0.2, strength) * (0.3 + 0.7 * pattern);

            return half4(baseColor * alpha, alpha * 0.4);
        }
    """
}
