package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostPhantasmShader: AGSL scripts for the "Ghost Phantasm" presence layer.
 *
 * This shader renders "Neural Meta-balls" that merge organically based on student
 * behavioral data. It also includes a futuristic "Privacy Glitch" overlay.
 */
object GhostPhantasmShader {
    @Language("AGSL")
    const val PHANTASM_BLOBS = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPoints[20]; // Student positions
        uniform float iWeights[20]; // Student agitation/intensity weights
        uniform float3 iColors[20];  // Student-specific colors
        uniform int iNumPoints;
        uniform float iAgitation;    // Global classroom agitation level
        uniform float iIsRecording;  // 1.0 if screen recording is detected, 0.0 otherwise

        // Pseudo-random noise for glitch
        float hash(float n) { return fract(sin(n) * 43758.5453123); }

        float3 glitch(float2 uv, float time) {
            float strength = iIsRecording * 0.5;
            float g = hash(floor(uv.y * 10.0) + time) * strength;
            float r = hash(floor(uv.y * 15.0) + time * 1.1) * strength;
            return float3(r, g, 0.0);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float aspect = iResolution.x / iResolution.y;
            float2 p = uv;
            p.x *= aspect;

            float m = 0.0;
            float3 compositeColor = float3(0.0);
            float totalWeight = 0.0;

            // Meta-balls logic
            for (int i = 0; i < 20; i++) {
                if (i >= iNumPoints) break;

                float2 pos = iPoints[i] / iResolution.xy;
                pos.x *= aspect;

                float d = distance(p, pos);
                float weight = iWeights[i] * (0.05 + 0.02 * sin(iTime + float(i)));

                // Meta-ball field contribution
                float val = weight / (d * d + 0.001);
                m += val;

                compositeColor += iColors[i] * val;
                totalWeight += val;
            }

            // Normalize color
            if (totalWeight > 0.0) {
                compositeColor /= totalWeight;
            }

            // Thresholding for blobs
            float threshold = 5.0 - (iAgitation * 2.0);
            float alpha = smoothstep(threshold, threshold + 2.0, m);

            float3 finalColor = compositeColor * alpha;

            // Add background "Neural Fog"
            float fog = sin(uv.x * 10.0 + iTime) * cos(uv.y * 10.0 - iTime) * 0.05;
            finalColor += float3(0.0, 0.05, 0.1) * (1.0 - alpha) + fog;

            // Apply Privacy Glitch if recording
            if (iIsRecording > 0.5) {
                float2 glitchUv = uv;
                if (hash(iTime) > 0.9) {
                    glitchUv.x += (hash(iTime * 2.0) - 0.5) * 0.1;
                }
                float3 g = glitch(glitchUv, iTime);
                finalColor = mix(finalColor, g, 0.4);
                alpha = mix(alpha, 0.8, 0.3);
            }

            return float4(finalColor, alpha * 0.4);
        }
    """
}
