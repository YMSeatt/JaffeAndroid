package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostHUDShader: AGSL shaders for the Tactical HUD.
 */
object GhostHUDShader {
    /**
     * TACTICAL_RADAR: A circular radar visualization shader.
     *
     * Features:
     * - **Coordinate Mapping**: Translates normalized UV coordinates (0..1) to polar coordinates (angle/distance)
     *   relative to the center of the viewport.
     * - **Heading Integration**: Incorporates the device's physical orientation ([iHeading]) to rotate the radar
     *   view, aligning virtual targets with their relative physical directions.
     * - **Target Rendering**: Renders up to 10 "blips" for students/prophecies. The color (red for high-risk,
     *   yellow for caution) and intensity of the blip depends on the [iTargetScores].
     * - **Shader Effects**: Implements a rotating sweep beam (`beam`), pulsating rings (`rings`), and a peripheral vignette.
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the drawing area.
     * - [iTime]: Elapsed time for animations.
     * - [iHeading]: Physical device heading in radians.
     * - [iTargets]: Array of angles (radians) for active blips.
     * - [iTargetScores]: Severity/confidence scores (0..1) for active blips.
     * - [iTargetCount]: Number of active targets to render.
     */
    @Language("AGSL")
    const val TACTICAL_RADAR = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iHeading;
        uniform float iTargets[10];
        uniform float iTargetScores[10];
        uniform int iTargetCount;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 center = float2(0.5, 0.5);
            float2 distVector = uv - center;
            float dist = length(distVector);
            float angle = atan(distVector.y, distVector.x);

            float rotatedAngle = angle + iHeading + 1.5708;
            rotatedAngle = mod(rotatedAngle + 3.14159, 6.28318) - 3.14159;

            float beamAngle = mod(iTime * 2.0, 6.28318) - 3.14159;
            float beamDiff = rotatedAngle - beamAngle;
            if (beamDiff < -3.14159) beamDiff += 6.28318;
            if (beamDiff > 3.14159) beamDiff -= 6.28318;

            float beam = 0.0;
            if (beamDiff < 0.0 && beamDiff > -0.5) {
                beam = smoothstep(-0.5, 0.0, beamDiff);
            }

            float rings = 0.0;
            if (abs(mod(dist * 5.0 - iTime * 0.5, 1.0) - 0.5) < 0.01) {
                rings = 0.2;
            }

            float3 color = float3(0.0, 1.0, 0.2) * (beam * 0.5 + rings);

            for (int i = 0; i < 10; i++) {
                if (i >= iTargetCount) break;

                float targetAngle = iTargets[i];
                float targetDiff = rotatedAngle - targetAngle;
                if (targetDiff < -3.14159) targetDiff += 6.28318;
                if (targetDiff > 3.14159) targetDiff -= 6.28318;

                if (abs(targetDiff) < 0.05 && dist > 0.1 && dist < 0.45) {
                    float targetPulse = sin(iTime * 10.0) * 0.5 + 0.5;
                    float3 targetColor = float3(1.0, 0.0, 0.0);
                    if (iTargetScores[i] < 0.5) targetColor = float3(1.0, 1.0, 0.0);
                    color += targetColor * targetPulse * (1.0 - abs(targetDiff) * 20.0);
                }
            }

            if (dist > 0.48) color *= 0.0;
            if (dist > 0.47 && dist < 0.48) color += float3(0.0, 1.0, 0.2) * 0.5;

            return float4(color, length(color) * 0.6);
        }
    """
}
