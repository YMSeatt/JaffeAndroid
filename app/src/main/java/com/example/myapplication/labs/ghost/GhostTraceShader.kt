package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostTraceShader: AGSL logic for rendering glowing, spatiotemporal paths.
 */
object GhostTraceShader {

    @Language("AGSL")
    const val TRACE_PATH = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iPointCount;
        uniform float2 iPoints[100];
        uniform float iAges[100]; // 0.0 (old) to 1.0 (new)

        float sdSegment(float2 p, float2 a, float2 b) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float minDataDist = 1000.0;
            float ageAtMin = 0.0;

            // BOLT: Manual loop for distance field calculation
            for (int i = 0; i < 99; i++) {
                if (float(i) >= iPointCount - 1.0) break;

                float2 a = iPoints[i] / 4000.0;
                float2 b = iPoints[i+1] / 4000.0;

                float dist = sdSegment(uv, a, b);
                if (dist < minDataDist) {
                    minDataDist = dist;
                    ageAtMin = mix(iAges[i], iAges[i+1], 0.5);
                }
            }

            // Glow effect
            float glow = 0.0015 / (minDataDist + 0.001);
            glow = pow(glow, 1.2);

            // Pulse based on time and age
            float pulse = 0.8 + 0.2 * sin(iTime * 2.0 + ageAtMin * 5.0);

            // Color shift from Violet (old) to Cyan (new)
            float3 oldColor = float3(0.5, 0.0, 1.0); // Violet
            float3 newColor = float3(0.0, 1.0, 1.0); // Cyan
            float3 color = mix(oldColor, newColor, ageAtMin);

            return half4(color * glow * pulse * ageAtMin, glow * 0.5 * ageAtMin);
        }
    """
}
