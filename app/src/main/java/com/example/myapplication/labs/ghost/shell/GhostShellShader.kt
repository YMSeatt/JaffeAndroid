package com.example.myapplication.labs.ghost.shell

import org.intellij.lang.annotations.Language

/**
 * GhostShellShader: AGSL shaders for the neural dock visualization.
 */
object GhostShellShader {
    @Language("AGSL")
    const val NEURAL_PULSE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iHealth;
        uniform float iFrequency;
        uniform half4 iColor;

        float wave(float2 uv, float speed, float freq, float amp, float offset) {
            return sin(uv.x * freq + iTime * speed + offset) * amp;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Center and scale
            float2 p = (uv - 0.5) * 2.0;
            p.x *= iResolution.x / iResolution.y;

            // Generate three interference waves
            float w1 = wave(uv, iFrequency * 2.0, 10.0, 0.05, 0.0);
            float w2 = wave(uv, iFrequency * 1.5, 15.0, 0.03, 1.0);
            float w3 = wave(uv, iFrequency * 3.0, 8.0, 0.02, 2.5);

            float combinedWave = w1 + w2 + w3;

            // Distance to the wave line
            float dist = abs(p.y - combinedWave);

            // Glow effect
            float glow = 0.02 / (dist + 0.01);
            glow = pow(glow, 1.2);

            // Color mapping: Shift color based on health (Healthier = more Cyan)
            half3 finalColor = iColor.rgb * glow;
            if (iHealth < 0.4) {
                finalColor.r += glow * (0.4 - iHealth) * 2.0;
            }

            float alpha = glow * iColor.a;

            return half4(finalColor, alpha);
        }
    """
}
