package com.example.myapplication.labs.ghost.glyph

/**
 * GhostGlyphShader: Neural Ink AGSL Shader.
 *
 * This shader visualizes the "Neural Ink" trail as a glowing, procedurally
 * jittery line that fades over time. It represents the "Intent Trace"
 * of a pedagogical gesture.
 */
object GhostGlyphShader {
    const val NEURAL_INK_SHADER = """
        uniform float2 resolution;
        uniform float time;
        uniform float2 points[32];
        uniform int pointCount;
        uniform float intensity;

        // Procedural noise for "Neural Jitter"
        float hash(float n) { return fract(sin(n) * 43758.5453123); }
        float noise(float x) {
            float i = floor(x);
            float f = fract(x);
            return mix(hash(i), hash(i + 1.0), smoothstep(0.0, 1.0, f));
        }

        float sdSegment(float2 p, float2 a, float2 b) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        half4 main(float2 fragCoord) {
            float d = 1000.0;
            for (int i = 0; i < pointCount - 1; i++) {
                // Apply jitter to points based on time
                float2 a = points[i];
                float2 b = points[i+1];
                float jitterA = noise(time * 5.0 + float(i)) * 4.0;
                float jitterB = noise(time * 5.0 + float(i + 1)) * 4.0;

                d = min(d, sdSegment(fragCoord, a + jitterA, b + jitterB));
            }

            // Glowing line effect
            float glow = intensity / (d + 1.0);
            glow = pow(glow, 0.8);

            // Neon Cyan Color
            float3 color = float3(0.0, 0.8, 1.0) * glow;

            // Add some "data interference"
            if (noise(fragCoord.x * 0.1 + time * 10.0) > 0.95) {
                color += float3(1.0, 0.0, 1.0) * 0.2;
            }

            return half4(color, clamp(glow, 0.0, 1.0));
        }
    """
}
