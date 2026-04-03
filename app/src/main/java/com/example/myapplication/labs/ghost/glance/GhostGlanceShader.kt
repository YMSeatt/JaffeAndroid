package com.example.myapplication.labs.ghost.glance

/**
 * GhostGlanceShader: AGSL source for the pulsating "Neural Wave" background of the Glance overlay.
 *
 * This shader visualizes "Neural Momentum" as a wave-like interference pattern,
 * with its intensity driven by recent student engagement metrics.
 */
object GhostGlanceShader {
    const val NEURAL_WAVE = """
        uniform float2 size;
        uniform float time;
        uniform float momentum;
        uniform float stability;
        uniform half4 colorA;
        uniform half4 colorB;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / size;
            float pulse = 0.5 + 0.5 * sin(time * 2.0);

            // Interference pattern
            float d1 = distance(uv, float2(0.5, 0.5));
            float d2 = distance(uv, float2(0.3, 0.7));
            float wave = sin(d1 * 15.0 - time * 4.0) * sin(d2 * 10.0 + time * 3.0);

            // Blend colors based on stability and momentum
            float mixFactor = (wave * 0.5 + 0.5) * momentum;
            half4 finalColor = mix(colorA, colorB, mixFactor);

            // Apply stability-based intensity
            float alpha = stability * (0.1 + 0.2 * pulse);

            return finalColor * alpha;
        }
    """
}
