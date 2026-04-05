package com.example.myapplication.labs.ghost.vortex

import org.intellij.lang.annotations.Language

/**
 * GhostVortexShader: AGSL code for the "Social Whirlpool" effect.
 *
 * Implements a spatial swirl distortion using a radial rotation formula.
 * The strength of the swirl decreases with distance from the vortex center.
 */
object GhostVortexShader {

    @Language("AGSL")
    const val SOCIAL_WHIRLPOOL = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iVortexPos;
        uniform float iRadius;
        uniform float iMomentum;
        uniform float iPolarity;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            float a = hash(i);
            float b = hash(i + float2(1.0, 0.0));
            float c = hash(i + float2(0.0, 1.0));
            float d = hash(i + float2(1.0, 1.0));
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = fragCoord - iVortexPos; // Pixel vector relative to vortex center
            float d = length(p);

            // 1. Swirl Distortion (Spatial Warping)
            // The rotation angle is highest at the center and decays quadratically.
            float rotation = 0.0;
            if (d < iRadius * 3.0) {
                // iPolarity determines direction (Clockwise vs Counter-clockwise)
                float strength = iMomentum * 5.0 * iPolarity;
                rotation = strength * pow(1.0 - d / (iRadius * 3.0), 2.0);
            }

            float s = sin(rotation);
            float c = cos(rotation);
            // Apply 2D rotation matrix to the pixel coordinate
            float2 rotP = float2(p.x * c - p.y * s, p.x * s + p.y * c);

            // Map rotated coordinates back to UV space for potential buffer sampling
            float2 distortedUV = (rotP + iVortexPos) / iResolution.xy;

            // 2. Spiral Glow / Accretion Lines (Procedural FX)
            float glow = 0.0;
            if (d < iRadius * 1.5) {
                float angle = atan(rotP.y, rotP.x);
                // Pulse frequency matched to momentum intensity
                float pulse = 0.5 + 0.5 * sin(iTime * 2.0);
                // Procedural noise generates the "streaks" of social energy
                float lines = smoothstep(0.4, 0.5, noise(float2(angle * 5.0 + iTime, d * 0.01 - iTime * 2.0)));
                glow = pow(1.0 - d / (iRadius * 1.5), 1.5) * lines * iMomentum * pulse;
            }

            half4 color = half4(0.0);

            // Color based on polarity: Cyan (Positive) vs Magenta (Negative)
            half3 vortexColor = (iPolarity > 0.0)
                ? half3(0.0, 1.0, 1.0) // Cyan
                : half3(1.0, 0.0, 1.0); // Magenta

            color.rgb = vortexColor * glow;
            color.a = glow * 0.6;

            return color;
        }
    """
}
