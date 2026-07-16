package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostFocusShader: AGSL shaders for the "Ghost Focus" neural field.
 *
 * This shader visualizes the classroom's concentration state as a flowing field.
 * Calm, synchronized waves represent high focus. Turbulent, fragmented,
 * and red-shifted interference patterns appear near low-concentration nodes.
 */
object GhostFocusShader {

    @Language("AGSL")
    const val FOCUS_FIELD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iGlobalFocus;

        // Student Focus: x, y, concentration, _unused
        uniform float4 iStudentFocus[20];
        uniform int iStudentCount;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(mix(hash(i + float2(0.0, 0.0)), hash(i + float2(1.0, 0.0)), u.x),
                       mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float aspect = iResolution.x / iResolution.y;

            // Base "Calm" Field (Neural Blue/Teal)
            float wave = sin(uv.x * 10.0 + iTime) * cos(uv.y * 8.0 - iTime * 0.5);
            float3 baseColor = mix(float3(0.0, 0.05, 0.1), float3(0.0, 0.2, 0.3), wave * 0.5 + 0.5);

            float interference = 0.0;
            float3 distortionColor = float3(0.0);

            for (int i = 0; i < iStudentCount; i++) {
                float2 pos = iStudentFocus[i].xy / iResolution.xy;
                float focus = iStudentFocus[i].z;

                float d = distance(uv * float2(aspect, 1.0), pos * float2(aspect, 1.0));

                // If focus is low, create a "Distraction Void"
                if (focus < 0.7) {
                    float influence = exp(-d * 10.0) * (1.0 - focus);
                    interference += influence;

                    // Red-shifted chaotic noise
                    float n = noise(uv * 50.0 + iTime * 5.0);
                    distortionColor += float3(0.8, 0.1, 0.2) * influence * n;
                } else {
                    // High focus creates a "Synchronization Glow"
                    float influence = exp(-d * 15.0) * (focus - 0.7);
                    distortionColor += float3(0.0, 1.0, 0.8) * influence;
                }
            }

            float3 finalColor = mix(baseColor, distortionColor, clamp(interference + 0.2, 0.0, 1.0));

            // Global focus affects brightness and stability
            finalColor *= (0.5 + 0.5 * iGlobalFocus);

            return float4(finalColor, 0.4);
        }
    """
}
