package com.example.myapplication.labs.ghost.meteor

import org.intellij.lang.annotations.Language

/**
 * GhostMeteorShader: AGSL shaders for the Ghost Meteor experiment.
 *
 * Provides shaders for high-momentum meteor projectiles and circular impact
 * shockwaves.
 */
object GhostMeteorShader {

    @Language("AGSL")
    const val METEOR_STREAK = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPoints[8]; // Tail points
        uniform half4 iColor;
        uniform float iLife;

        float distanceToSegment(float2 p, float2 a, float2 b) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        half4 main(float2 fragCoord) {
            float opacity = 0.0;

            for (int i = 0; i < 7; i++) {
                float d = distanceToSegment(fragCoord, iPoints[i], iPoints[i+1]);
                // Meteor tail tapers and glows
                float width = mix(1.0, 12.0, float(i) / 7.0);
                float glow = exp(-d / width);
                opacity += glow * pow(float(i) / 7.0, 2.0);
            }

            // Head glow
            float dHead = length(fragCoord - iPoints[7]);
            opacity += exp(-dHead / 15.0) * 1.5;

            return iColor * clamp(opacity, 0.0, 1.0) * iLife;
        }
    """

    @Language("AGSL")
    const val IMPACT_SHOCKWAVE = """
        uniform float2 iResolution;
        uniform float2 iCenter;
        uniform float iRadius;
        uniform float iLife; // 1.0 to 0.0
        uniform half4 iColor;

        half4 main(float2 fragCoord) {
            float d = length(fragCoord - iCenter);

            // Shockwave ring
            float ringWidth = 20.0 * iLife;
            float ring = smoothstep(iRadius - ringWidth, iRadius, d) *
                         smoothstep(iRadius + ringWidth, iRadius, d);

            // Expanding fade
            float fade = pow(iLife, 1.5);

            return iColor * ring * fade;
        }
    """
}
