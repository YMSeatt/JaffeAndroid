package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostIrisShader: AGSL scripts for the personalized Neural Iris visualization.
 */
object GhostIrisShader {

    /**
     * A procedurally generated "Neural Iris" pattern.
     * Seeded by student identity and performance data.
     *
     * Uniforms:
     * - [iResolution]: Canvas dimensions.
     * - [iTime]: Animation time.
     * - [iSeed]: Student-specific seed (e.g., hash of ID).
     * - [iColorA]: Primary iris color.
     * - [iColorB]: Secondary iris color.
     * - [iComplexity]: Pattern complexity (0.0 to 1.0).
     */
    @Language("AGSL")
    const val NEURAL_IRIS = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iSeed;
        uniform float3 iColorA;
        uniform float3 iColorB;
        uniform float iComplexity;

        float hash(float n) { return fract(sin(n) * 43758.5453123); }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            f = f * f * (3.0 - 2.0 * f);
            float n = i.x + i.y * 57.0;
            return mix(mix(hash(n), hash(n + 1.0), f.x),
                        mix(hash(n + 57.0), hash(n + 58.0), f.x), f.y);
        }

        float4 main(float2 fragCoord) {
            float2 uv = (fragCoord - 0.5 * iResolution.xy) / min(iResolution.y, iResolution.x);

            float r = length(uv);
            float angle = atan(uv.y, uv.x);

            // Core Iris structure
            float pattern = noise(float2(r * (5.0 + iComplexity * 10.0), angle * 3.0 + iTime * 0.5 + iSeed));
            pattern += 0.5 * noise(float2(r * 10.0, angle * 5.0 - iTime * 0.8));

            // Mask for iris shape
            float mask = smoothstep(0.45, 0.4, r) * smoothstep(0.1, 0.15, r);

            // Fibers / Striations
            float fibers = sin(angle * 50.0 + noise(float2(r * 20.0, angle)) * 10.0) * 0.5 + 0.5;
            fibers *= pattern;

            float3 color = mix(iColorA, iColorB, pattern);
            color += fibers * 0.2;

            // Pupil (Dark center)
            float pupil = smoothstep(0.12, 0.1, r);
            color = mix(color, float3(0.0, 0.0, 0.0), pupil);

            // Outer Ring
            float ring = smoothstep(0.44, 0.45, r) * smoothstep(0.48, 0.47, r);
            color += ring * iColorA;

            return float4(color, mask * 0.8);
        }
    """
}
