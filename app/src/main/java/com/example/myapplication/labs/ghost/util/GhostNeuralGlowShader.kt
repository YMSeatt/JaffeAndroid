package com.example.myapplication.labs.ghost.util

/**
 * GhostNeuralGlowShader: AGSL source for high-performance student aura effects.
 *
 * This shader calculates a procedural glow based on coordinate distance,
 * optimized for runtime execution on API 33+.
 */
object GhostNeuralGlowShader {
    const val NEURAL_GLOW = """
        uniform float2 size;
        uniform float time;
        uniform float intensity;
        uniform half4 color;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / size;
            float2 center = float2(0.5, 0.5);
            float dist = distance(uv, center);

            // Pulse effect
            float pulse = 0.8 + 0.2 * sin(time * 3.0);
            float glow = exp(-dist * 5.0 / (intensity * pulse));

            return color * glow;
        }
    """
}
