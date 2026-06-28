package com.example.myapplication.labs.ghost.frost

import org.intellij.lang.annotations.Language

/**
 * GhostFrostShader: AGSL code for procedural frost crystallization.
 *
 * This shader implements a multi-stage procedural effect:
 * 1. **Voronoi Noise**: Creates the base geometric structure for ice crystals.
 * 2. **Fractional Brownian Motion (fBm)**: Layers multiple octaves of Voronoi noise
 *    to create an organic, branching "frost" pattern.
 * 3. **Spatiotemporal Distortion**: Animates crystal positions over time using [iTime].
 * 4. **Coordinate Transformation**: Maps logical coordinates to physical screen space
 *    using [iCanvasScale] and [iCanvasOffset].
 *
 * The shader utilizes `discard` to optimize overdraw, only rendering pixels within
 * the influence radius of a frost node.
 */
object GhostFrostShader {

    /**
     * The AGSL program for the frost effect.
     *
     * Uniforms:
     * - `iResolution`: The viewport dimensions.
     * - `iTime`: Absolute system time for animation.
     * - `iTarget`: The logical (4000x4000) center of the frost.
     * - `iIntensity`: Scalar (0..1) controlling the opacity/thickness of the frost.
     * - `iCanvasOffset`: The current pan position of the seating chart.
     * - `iCanvasScale`: The current zoom factor of the seating chart.
     */
    @Language("AGSL")
    const val FROST_EFFECT = """
        uniform float2 iResolution;      // Canvas size
        uniform float iTime;             // Shader time
        uniform float2 iTarget;          // Target student position (in logical pixels)
        uniform float iIntensity;        // Frost intensity (0..1)
        uniform float2 iCanvasOffset;    // Canvas pan offset
        uniform float iCanvasScale;      // Canvas zoom scale

        float hash(float2 p) {
            return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
        }

        // Voronoi for crystal structures
        float voronoi(float2 x) {
            float2 n = floor(x);
            float2 f = fract(x);
            float m = 8.0;
            for(int j=-1; j<=1; j++)
            for(int i=-1; i<=1; i++) {
                float2 g = float2(float(i), float(j));
                float2 o = float2(hash(n + g));
                // Animate the crystals slightly
                o = 0.5 + 0.5 * sin(iTime * 0.5 + 6.2831 * o);
                float2 r = g + o - f;
                float d = dot(r, r);
                if(d < m) m = d;
            }
            return sqrt(m);
        }

        // fBm for organic growth
        float fbm(float2 p) {
            float v = 0.0;
            float a = 0.5;
            float2 shift = float2(100.0);
            for (int i = 0; i < 4; ++i) {
                v += a * voronoi(p);
                p = p * 2.0 + shift;
                a *= 0.5;
            }
            return v;
        }

        half4 main(float2 fragCoord) {
            // Transform iTarget (logical 4000x4000) to screen coordinates
            float2 screenTarget = iTarget * iCanvasScale + iCanvasOffset;

            // Distance from student center
            float dist = distance(fragCoord, screenTarget);
            float radius = 250.0 * iCanvasScale;

            if (dist > radius) discard;

            // Intensity gradient
            float mask = smoothstep(radius, 0.0, dist);

            // Procedural Frost
            float2 uv = fragCoord * 0.01 / iCanvasScale;
            float frost = fbm(uv + iTime * 0.1);

            // Crystallization edge
            float crystal = smoothstep(0.4, 0.2, frost * mask);

            // Interaction with intensity
            crystal *= iIntensity;

            // Colors (Ice Blue/White)
            half3 coldBlue = half3(0.7, 0.9, 1.0);
            half3 white = half3(1.0, 1.0, 1.0);
            half3 finalColor = mix(coldBlue, white, frost);

            return half4(finalColor, crystal * 0.8);
        }
    """
}
