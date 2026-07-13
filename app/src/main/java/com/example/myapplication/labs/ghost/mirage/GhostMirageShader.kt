package com.example.myapplication.labs.ghost.mirage

/**
 * GhostMirageShader: AGSL Neural Focus Heatmap.
 *
 * This shader renders a shimmering, ethereal heatmap representing teacher focus areas.
 * It uses domain-warped FBM (Fractal Brownian Motion) noise to create a "Mirage"
 * effect that feels alive and futuristic.
 *
 * Shaders are optimized for API 33+ (Android 13+).
 */
object GhostMirageShader {
    /**
     * Renders the focus mirage heatmap.
     *
     * Uniforms:
     * - iResolution: Canvas dimensions.
     * - iTime: Global time for animation.
     * - iFocusGrid: Flattened 20x20 intensity grid (passed as a float array).
     * - iGridSize: The size of one dimension of the grid (20.0).
     */
    const val MIRAGE_HEATMAP = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iFocusGrid[400];
        uniform float iGridSize;

        float hash(float n) { return fract(sin(n) * 43758.5453123); }

        float noise(float2 x) {
            float2 p = floor(x);
            float2 f = fract(x);
            f = f * f * (3.0 - 2.0 * f);
            float n = p.x + p.y * 57.0;
            return mix(mix(hash(n + 0.0), hash(n + 1.0), f.x),
                       mix(hash(n + 57.0), hash(n + 58.0), f.x), f.y);
        }

        float fbm(float2 p) {
            float f = 0.0;
            f += 0.5000 * noise(p); p = p * 2.02;
            f += 0.2500 * noise(p); p = p * 2.03;
            f += 0.1250 * noise(p);
            return f;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution;

            // Map UV to grid coordinates
            float2 gridPos = uv * iGridSize;
            int col = int(gridPos.x);
            int row = int(gridPos.y);
            int index = row * int(iGridSize) + col;

            // Safety check for array bounds
            float intensity = 0.0;
            if (index >= 0 && index < 400) {
                intensity = iFocusGrid[index];
            }

            // Ethereal Mirage effect using Domain Warping
            float2 q = float2(fbm(uv + 0.1 * iTime), fbm(uv + 0.5 * iTime));
            float2 r = float2(fbm(uv + 1.0 * q + float2(1.7, 9.2) + 0.15 * iTime),
                              fbm(uv + 1.0 * q + float2(8.3, 2.8) + 0.126 * iTime));
            float f = fbm(uv + r);

            // Color Palette: Amber/Ghost Cyan blend
            half3 colorLow = half3(0.0, 0.2, 0.3); // Deep Cyan
            half3 colorHigh = half3(1.0, 0.8, 0.2); // Amber Ghost

            half3 finalColor = mix(colorLow, colorHigh, intensity * f * 1.5);

            // Pulse and shimmer
            float shimmer = 0.8 + 0.2 * sin(iTime * 2.0 + f * 10.0);
            float alpha = intensity * f * 0.7 * shimmer;

            return half4(finalColor * alpha, alpha);
        }
    """
}
