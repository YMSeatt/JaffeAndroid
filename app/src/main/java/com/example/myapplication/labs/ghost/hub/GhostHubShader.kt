package com.example.myapplication.labs.ghost.hub

import org.intellij.lang.annotations.Language

/**
 * GhostHubShader: AGSL shaders for the Ghost Hub radial menu.
 */
object GhostHubShader {

    /**
     * RADIAL_HUB: A futuristic radial background shader.
     *
     * Features:
     * - **Orbital Glow**: A procedural core glow that pulses with time.
     * - **Segment Borders**: Sharp radial lines separating menu actions.
     * - **Selection Highlight**: A rotating highlight sweep driven by [iSelectedAngle].
     * - **Neural Noise**: Subtle background jitter to maintain the "Ghost" aesthetic.
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the hub container.
     * - [iTime]: Elapsed time for pulse animations.
     * - [iSelectedAngle]: The angle of the currently hovered segment (-PI to PI).
     * - [iHighlightAlpha]: Intensity of the selection highlight (0..1).
     * - [iSegmentCount]: Number of segments in the radial menu.
     */
    @Language("AGSL")
    const val RADIAL_HUB = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iSelectedAngle;
        uniform float iHighlightAlpha;
        uniform float iSegmentCount;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 center = float2(0.5, 0.5);
            float2 distVector = uv - center;
            float dist = length(distVector);
            float angle = atan(distVector.y, distVector.x);

            // Base glow
            float pulse = sin(iTime * 2.0) * 0.1 + 0.9;
            float glow = smoothstep(0.5, 0.2, dist) * 0.3 * pulse;

            // Segment lines
            float segmentAngle = 6.28318 / iSegmentCount;
            float normalizedAngle = mod(angle + 3.14159, 6.28318);
            float lineIntensity = smoothstep(0.02, 0.0, abs(mod(normalizedAngle, segmentAngle)));
            lineIntensity *= smoothstep(0.1, 0.15, dist) * (1.0 - smoothstep(0.45, 0.5, dist));

            // Selection Highlight
            float angleDiff = angle - iSelectedAngle;
            if (angleDiff < -3.14159) angleDiff += 6.28318;
            if (angleDiff > 3.14159) angleDiff -= 6.28318;

            float highlight = smoothstep(segmentAngle * 0.5, 0.0, abs(angleDiff));
            highlight *= (1.0 - smoothstep(0.48, 0.5, dist)) * smoothstep(0.1, 0.12, dist);
            highlight *= iHighlightAlpha;

            float3 baseColor = float3(0.0, 0.8, 1.0); // Cyan Ghost
            float3 color = baseColor * (glow + lineIntensity * 0.5 + highlight);

            // Core accent
            if (dist < 0.08) {
                color = baseColor * (0.5 + 0.5 * sin(iTime * 4.0));
            }

            float alpha = smoothstep(0.5, 0.48, dist) * (glow * 2.0 + lineIntensity + highlight + 0.1);
            return float4(color, alpha * 0.8);
        }
    """
}
