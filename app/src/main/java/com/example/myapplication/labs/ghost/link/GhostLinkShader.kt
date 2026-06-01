package com.example.myapplication.labs.ghost.link

import org.intellij.lang.annotations.Language

/**
 * GhostLinkShader: AGSL shaders for the "Neural Strand" effect.
 *
 * This shader visualizes the connections between student nodes as animated,
 * data-pulsing strands. It uses domain warping and temporal oscillations to
 * simulate the flow of information.
 */
object GhostLinkShader {

    @Language("AGSL")
    const val NEURAL_STRAND = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPointA;
        uniform float2 iPointB;
        uniform float iStrength;

        // Helper for procedural noise
        float hash(float n) { return fract(sin(n) * 43758.5453123); }

        float noise(float2 x) {
            float2 p = floor(x);
            float2 f = fract(x);
            f = f * f * (3.0 - 2.0 * f);
            float n = p.x + p.y * 57.0;
            return mix(mix(hash(n + 0.0), hash(n + 1.0), f.x),
                        mix(hash(n + 57.0), hash(n + 58.0), f.x), f.y);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Vector from A to B
            float2 dir = iPointB - iPointA;
            float len = length(dir);
            float2 unitDir = dir / len;

            // Vector from A to fragCoord
            float2 p = fragCoord - iPointA;

            // Projection of p onto the line AB
            float projection = dot(p, unitDir);
            float distToLine = length(p - unitDir * clamp(projection, 0.0, len));

            if (projection < 0.0 || projection > len) {
                return half4(0.0);
            }

            // Neural pulse effect
            float pulse = sin(projection * 0.05 - iTime * 10.0) * 0.5 + 0.5;
            float thickness = (2.0 + pulse * 4.0) * iStrength;

            // Domain warping for "Organic" feel
            float warp = noise(fragCoord * 0.01 + iTime) * 10.0;
            float intensity = smoothstep(thickness + warp, 0.0, distToLine);

            // Coloring
            half3 baseColor = half3(0.0, 0.8, 1.0); // Cyan neural glow
            half3 pulseColor = half3(1.0, 1.0, 1.0); // White data pulse

            half3 finalColor = mix(baseColor, pulseColor, pulse * intensity);

            return half4(finalColor * intensity * iStrength, intensity * iStrength);
        }
    """
}
