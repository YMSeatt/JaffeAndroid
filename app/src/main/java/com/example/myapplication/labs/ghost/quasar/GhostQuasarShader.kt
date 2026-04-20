package com.example.myapplication.labs.ghost.quasar

import org.intellij.lang.annotations.Language

/**
 * GhostQuasarShader: AGSL scripts for rendering Quasar effects.
 */
object GhostQuasarShader {

    /**
     * Renders a pulsing accretion disk around high-energy students using procedural noise and swirls.
     *
     * The shader uses a signed distance field (SDF) approach to render a ring (disk)
     * whose radius and thickness scale with the provided `iEnergy`. It incorporates
     * a temporal sine wave for pulsing and an angular swirl component to simulate
     * rotational momentum.
     *
     * Uniforms:
     * - [iResolution]: Canvas dimensions used for coordinate normalization.
     * - [iTime]: Elapsed time used to drive the pulse and swirl animations.
     * - [iCenter]: Center of the Quasar in screen coordinates.
     * - [iEnergy]: Energy level (0.0 - 1.0) scaling the disk's physical presence.
     * - [iColor]: RGB color vector (Cyan for positive, Magenta for negative).
     */
    @Language("AGSL")
    const val ACCRETION_DISK = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iCenter;
        uniform float iEnergy;
        uniform float3 iColor;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 center = iCenter / iResolution.xy;
            float aspect = iResolution.x / iResolution.y;

            float2 diff = uv - center;
            diff.x *= aspect;

            float d = length(diff);

            // Accretion disk radius depends on energy
            float radius = 0.05 + iEnergy * 0.05;
            float thickness = 0.01 + iEnergy * 0.02;

            // Pulsing effect
            float pulse = sin(iTime * 5.0 - d * 50.0) * 0.1 + 0.9;

            float disk = smoothstep(radius + thickness, radius, d) * smoothstep(radius - thickness, radius, d);

            // Swirl effect
            float angle = atan(diff.y, diff.x);
            float swirl = sin(angle * 5.0 + iTime * 10.0 + d * 20.0);

            float3 color = iColor * disk * pulse * (0.8 + 0.2 * swirl);

            // Central core glow
            float core = 0.005 / (d + 0.001);
            color += iColor * core * iEnergy;

            return float4(color, length(color));
        }
    """
}
