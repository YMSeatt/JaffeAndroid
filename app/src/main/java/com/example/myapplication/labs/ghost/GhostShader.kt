package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

object GhostShader {
    @Language("AGSL")
    const val NEURAL_PULSE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float3 iColor;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            uv = uv * 2.0 - 1.0;
            uv.x *= iResolution.x / iResolution.y;

            float d = length(uv);
            float pulse = sin(d * 10.0 - iTime * 3.0) * 0.5 + 0.5;
            pulse *= exp(-d * 2.0);

            float3 color = iColor * pulse;
            color += iColor * 0.1 / d; // Glow

            return float4(color, 0.8 * pulse);
        }
    """

    @Language("AGSL")
    const val NEURAL_LINE = """
        uniform float iTime;
        uniform float3 iColor;
        uniform float2 iResolution;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Pulsating intensity
            float pulse = sin(iTime * 4.0) * 0.3 + 0.7;

            // Add some "noise" or movement along the line
            float movement = sin(uv.x * 20.0 + iTime * 10.0) * 0.1;

            float3 finalColor = iColor * (pulse + movement);
            return float4(finalColor, 0.9);
        }
    """

    @Language("AGSL")
    const val COGNITIVE_AURA = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iCenter;
        uniform float3 iColor;
        uniform float iIntensity;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 center = iCenter / iResolution.xy;

            float d = distance(uv, center);
            float aura = 0.05 / (d + 0.01);

            // Pulsate
            aura *= (sin(iTime * 5.0 - d * 20.0) * 0.2 + 0.8);

            float3 color = iColor * aura * iIntensity;
            return float4(color, color.r * 0.5);
        }
    """
}
