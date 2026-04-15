package com.example.myapplication.labs.ghost.hub

import org.intellij.lang.annotations.Language

/**
 * GhostStudentHubShader: AGSL shaders for the per-student radial quick-action menu.
 */
object GhostStudentHubShader {

    /**
     * NEURAL_STUDENT_HUB: A focused radial background shader for student-specific actions.
     *
     * Features:
     * - **Synaptic Pulse**: A core pulse that mimics neural firing.
     * - **Focus Ring**: A sharp inner ring that anchors the hub to the student icon.
     * - **Selection Sweep**: High-fidelity selection highlight with chromatic aberration.
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the hub container.
     * - [iTime]: Elapsed time for pulse animations.
     * - [iSelectedAngle]: The angle of the currently hovered segment (-PI to PI).
     * - [iHighlightAlpha]: Intensity of the selection highlight (0..1).
     * - [iSegmentCount]: Number of segments in the radial menu.
     */
    @Language("AGSL")
    const val NEURAL_STUDENT_HUB = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iSelectedAngle;
        uniform float iHighlightAlpha;
        uniform float iSegmentCount;

        float hash(float n) {
            return fract(sin(n) * 43758.5453123);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 center = float2(0.5, 0.5);
            float2 distVector = uv - center;
            float dist = length(distVector);
            float angle = atan(distVector.y, distVector.x);

            // Neural Pulse
            float pulse = sin(iTime * 4.0 - dist * 10.0) * 0.5 + 0.5;
            float coreGlow = smoothstep(0.4, 0.1, dist) * 0.4 * pulse;

            // Segment Logic
            float segmentAngle = 6.28318 / iSegmentCount;
            float normalizedAngle = mod(angle + 3.14159, 6.28318);

            // Selection Highlight
            float angleDiff = angle - iSelectedAngle;
            if (angleDiff < -3.14159) angleDiff += 6.28318;
            if (angleDiff > 3.14159) angleDiff -= 6.28318;

            float highlight = smoothstep(segmentAngle * 0.5, 0.0, abs(angleDiff));
            highlight *= (1.0 - smoothstep(0.48, 0.5, dist)) * smoothstep(0.15, 0.18, dist);
            highlight *= iHighlightAlpha;

            // Focus Rings
            float ring1 = smoothstep(0.01, 0.0, abs(dist - 0.15));
            float ring2 = smoothstep(0.01, 0.0, abs(dist - 0.48));

            float3 baseColor = float3(0.4, 0.2, 0.8); // Violet Neural
            float3 accentColor = float3(0.0, 1.0, 0.9); // Cyan Ghost

            float3 color = mix(baseColor, accentColor, highlight);
            color *= (coreGlow + highlight + (ring1 + ring2) * 0.5);

            float alpha = smoothstep(0.5, 0.45, dist) * (coreGlow * 1.5 + highlight + 0.2);
            return float4(color, alpha * 0.9);
        }
    """
}
