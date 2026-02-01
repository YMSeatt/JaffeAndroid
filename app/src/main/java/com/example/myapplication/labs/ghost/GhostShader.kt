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
}
