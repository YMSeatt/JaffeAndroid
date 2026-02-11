package com.example.myapplication.labs.ghost.synapse

import org.intellij.lang.annotations.Language

/**
 * GhostSynapseShader: Specialized AGSL scripts for the Synapse experience.
 */
object GhostSynapseShader {
    /**
     * A shader that visualizes "data flowing through a synapse".
     * It uses moving particles and pulsating lines to represent AI processing.
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the canvas.
     * - [iTime]: Elapsed time in seconds.
     * - [iColor]: Base theme color.
     */
    @Language("AGSL")
    const val NEURAL_FLOW = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float3 iColor;

        float hash(float n) { return fract(sin(n) * 43758.5453123); }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = uv * 2.0 - 1.0;
            p.x *= iResolution.x / iResolution.y;

            float3 color = float3(0.0);

            // Create flowing "filaments"
            for (float i = 1.0; i < 4.0; i++) {
                float wave = sin(p.x * 2.0 + iTime * 3.0 + i) * 0.5;
                float dist = abs(p.y - wave);
                float filament = 0.005 / (dist + 0.01);
                color += iColor * filament * (sin(iTime + i) * 0.5 + 0.5);
            }

            // Pulsating core
            float d = length(p);
            float pulse = sin(iTime * 10.0 - d * 5.0) * 0.1 + 0.9;
            color += iColor * (0.02 / d) * pulse;

            // Scanlines
            color *= 0.9 + 0.1 * sin(uv.y * 400.0 + iTime * 20.0);

            return float4(color, 0.6);
        }
    """
}
