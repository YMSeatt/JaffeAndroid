package com.example.myapplication.labs.ghost.helix

import org.intellij.lang.annotations.Language

/**
 * GhostHelixShader: An AGSL shader that renders a rotating 3D Double Helix.
 *
 * This shader simulates depth through shading and uses procedural noise to
 * create a pulsing "Neural Energy" effect.
 */
object GhostHelixShader {

    @Language("AGSL")
    const val NEURAL_HELIX = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iStability; // 0.1 to 1.0
        uniform float iTwist;     // 1.0 to 3.0
        uniform float3 iColor;    // Base DNA Color
        uniform shader contents;

        // Hash function for procedural jitter
        float hash(float n) {
            return fract(sin(n) * 43758.5453123);
        }

        float noise(float x) {
            float i = floor(x);
            float f = fract(x);
            float u = f * f * (3.0 - 2.0 * f);
            return mix(hash(i), hash(i + 1.0), u);
        }

        float4 main(float2 fragCoord) {
            float2 uv = (fragCoord - 0.5 * iResolution.xy) / min(iResolution.x, iResolution.y);

            // Jitter effect based on stability
            float jitter = (1.0 - iStability) * 0.05 * (hash(iTime * 10.0) - 0.5);
            uv.x += jitter;

            // Rotation and Twist
            float twist = iTwist * 10.0;
            float speed = iTime * 2.0;

            // Distance to two strands of the helix
            float y = uv.y * 5.0;
            float x1 = sin(y * twist + speed) * 0.4;
            float x2 = sin(y * twist + speed + 3.14159) * 0.4;

            // Strand 1
            float d1 = abs(uv.x - x1);
            float glow1 = 0.02 / (d1 + 0.01);

            // Strand 2
            float d2 = abs(uv.x - x2);
            float glow2 = 0.02 / (d2 + 0.01);

            // Connecting "Base Pairs"
            float connect = 0.0;
            float rungY = floor(y * 2.0) / 2.0;
            if (abs(y - rungY) < 0.02) {
                float minX = min(x1, x2);
                float maxX = max(x1, x2);
                if (uv.x > minX && uv.x < maxX) {
                    connect = 1.0;
                }
            }

            // Combine strands and rungs
            float helix = glow1 + glow2 + connect * 0.5;

            // Color mapping
            float3 finalColor = iColor * helix;
            finalColor = clamp(finalColor, 0.0, 1.0);

            // Depth shading (darker when 'behind')
            float z1 = cos(y * twist + speed);
            float z2 = cos(y * twist + speed + 3.14159);
            finalColor *= (z1 + z2 + 2.5) * 0.4;

            // Sample original content for transparency/blending
            float4 background = contents.eval(fragCoord);

            return float4(finalColor + background.rgb * (1.0 - length(finalColor)), background.a);
        }
    """
}
