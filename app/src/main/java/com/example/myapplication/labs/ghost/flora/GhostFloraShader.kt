package com.example.myapplication.labs.ghost.flora

import org.intellij.lang.annotations.Language

/**
 * GhostFloraShader: AGSL procedural generation for "Neural Botanical" effects.
 *
 * This shader uses polar coordinate warping and procedural noise to generate
 * blossoming flowers that react to student growth and vitality.
 */
object GhostFloraShader {

    @Language("AGSL")
    const val NEURAL_BLOOM = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iGrowth;      // 0.0 to 1.0
        uniform float iVitality;    // -1.0 to 1.0
        uniform int iPetalCount;    // Number of petals
        uniform float iComplexity;  // Fractal depth
        uniform float iColorShift;  // 0.0 (Magenta) to 1.0 (Cyan)

        float plot(float r, float theta, float growth, float complexity, int petals) {
            // Polar warping for petals
            float pCount = float(petals);
            float petal = abs(cos(theta * pCount * 0.5));

            // Shape modulation based on growth
            float shape = 0.5 + 0.5 * petal;

            // Fractal noise for complexity
            float noise = sin(theta * 20.0 + iTime) * 0.05 * complexity;

            float targetRadius = growth * shape + noise;
            return smoothstep(targetRadius, targetRadius - 0.02, r);
        }

        half4 main(float2 fragCoord) {
            float2 uv = (fragCoord - 0.5 * iResolution) / min(iResolution.y, iResolution.x);

            // Convert to polar coordinates
            float r = length(uv) * 2.0;
            float theta = atan(uv.y, uv.x);

            // Time-based rotation
            theta += iTime * 0.2 * iVitality;

            // Blossom logic
            float blossom = plot(r, theta, iGrowth, iComplexity, iPetalCount);

            // Core logic (center of the flower)
            float core = smoothstep(0.15 * iGrowth, 0.1, r);

            // Color mapping: Negative Vitality = Wilting (Brownish/Desaturated)
            // Positive Vitality = Thriving (Vibrant Cyan/Green)
            float3 cyan = float3(0.0, 0.8, 1.0);
            float3 magenta = float3(1.0, 0.0, 0.8);
            float3 baseColor = mix(magenta, cyan, iColorShift);

            // Desaturate if vitality is negative
            if (iVitality < 0.0) {
                float desat = abs(iVitality);
                baseColor = mix(baseColor, float3(0.4, 0.3, 0.2), desat); // Brownish wilt
            }

            float3 finalColor = baseColor * blossom;
            finalColor += float3(1.0, 0.9, 0.5) * core; // Yellow core

            float alpha = blossom * 0.8 + core;

            return half4(finalColor, alpha);
        }
    """
}
