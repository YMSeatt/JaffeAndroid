package com.example.myapplication.labs.ghost.stellar

/**
 * GhostStellarShader: AGSL shaders for the "Ghost Stellar" experiment.
 *
 * This experiment visualizes the classroom as a neural constellation, where
 * students are stars and group connections are glowing cosmic threads.
 */
object GhostStellarShader {

    /**
     * Procedural Star Field background.
     * Generates a deep-space environment with twinkling stars.
     */
    const val STAR_FIELD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity;

        float hash(float2 p) {
            p = fract(p * float2(123.34, 456.21));
            p += dot(p, p + 45.32);
            return fract(p.x * p.y);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = uv * 10.0;
            float2 ip = floor(p);
            float2 fp = fract(p);

            float star = 0.0;
            float h = hash(ip);
            if (h > 0.95) {
                float twinkle = sin(iTime * 2.0 + h * 10.0) * 0.5 + 0.5;
                float dist = length(fp - 0.5);
                star = smoothstep(0.1 * twinkle, 0.0, dist) * iIntensity;
            }

            half3 color = half3(0.02, 0.05, 0.1) * (1.0 - uv.y); // Deep space gradient
            color += half3(star);

            return half4(color, 1.0);
        }
    """

    /**
     * Glowing Constellation Line shader.
     * Renders an animated, glowing thread between student stars.
     */
    const val STELLAR_THREAD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iStart;
        uniform float2 iEnd;
        uniform half4 iColor;
        uniform float iStrength;

        float distanceToSegment(float2 p, float2 a, float2 b) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        half4 main(float2 fragCoord) {
            float d = distanceToSegment(fragCoord, iStart, iEnd);

            // Pulsating glow
            float pulse = sin(iTime * 3.0 + length(iStart - fragCoord) * 0.01) * 0.2 + 0.8;
            float glow = exp(-d * 0.15) * pulse * iStrength;

            half4 color = iColor * glow;
            return color;
        }
    """
}
