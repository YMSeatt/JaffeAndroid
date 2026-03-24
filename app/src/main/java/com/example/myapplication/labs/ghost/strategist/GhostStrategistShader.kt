package com.example.myapplication.labs.ghost.strategist

import org.intellij.lang.annotations.Language

/**
 * GhostStrategistShader: AGSL shaders for the AI Co-Pilot experience.
 */
object GhostStrategistShader {

    @Language("AGSL")
    const val NEURAL_STREAM_SHADER = """
        uniform float2 uResolution;
        uniform float uTime;
        uniform float uAlpha;
        uniform float uIntensity;

        float hash(float n) {
            return fract(sin(n) * 43758.5453123);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            f = f * f * (3.0 - 2.0 * f);
            float n = i.x + i.y * 57.0;
            return mix(mix(hash(n + 0.0), hash(n + 1.0), f.x),
                       mix(hash(n + 57.0), hash(n + 58.0), f.x), f.y);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / uResolution;

            // Flowing data streams
            float stream = noise(float2(uv.x * 10.0 + uTime, uv.y * 2.0 - uTime * 0.5));
            stream *= noise(float2(uv.x * 5.0 - uTime * 0.2, uv.y * 8.0 + uTime));

            // Interference pulses
            float pulse = sin(uv.x * 50.0 + uTime * 10.0) * 0.5 + 0.5;
            pulse *= sin(uv.y * 50.0 - uTime * 5.0) * 0.5 + 0.5;

            // Neural colors
            half3 baseColor = half3(0.0, 0.5, 0.8); // Cyan neural base
            half3 pulseColor = half3(1.0, 1.0, 1.0); // White data pulses

            half3 finalColor = mix(baseColor, pulseColor, stream * pulse * uIntensity);

            // Edge fading
            float edge = smoothstep(0.0, 0.2, uv.x) * smoothstep(1.0, 0.8, uv.x) *
                         smoothstep(0.0, 0.2, uv.y) * smoothstep(1.0, 0.8, uv.y);

            return half4(finalColor * edge, edge * uAlpha * (0.1 + stream * 0.4));
        }
    """
}
