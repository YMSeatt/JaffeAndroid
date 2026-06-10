package com.example.myapplication.labs.ghost.radar

/**
 * Repository for Ghost Radar AGSL shader programs.
 */
object GhostRadarShader {
    /**
     * AGSL Shader for the Ghost Radar visualization.
     *
     * ### Visual Features:
     * - **Rotating Sweep**: Uses `atan2` and `mod` to create a clock-wise rotating scanning line.
     * - **Concentric Rings**: Employs `sin` on distance to generate periodic circular boundaries.
     * - **Behavioral Resonance**: Intensity data drives a procedural `noise` flicker and
     *   central core glow, visually signaling high-activity zones.
     * - **Spatio-Temporal Masking**: Automatically clips rendering to a perfect circle
     *   with edge feathering.
     *
     * ### Uniforms:
     * - `iResolution`: The size of the radar in pixels.
     * - `iTime`: Animation phase for rotation and noise pulses.
     * - `iIntensity`: Behavioral resonance from [GhostRadarEngine].
     * - `iColor`: The base RGB tint for the radar.
     */
    const val RADAR_SWEEP = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity; // Local behavioral resonance
        uniform float3 iColor;     // Radar base color

        half4 main(float2 fragCoord) {
            float2 uv = (fragCoord - 0.5 * iResolution.xy) / min(iResolution.y, iResolution.x);
            float dist = length(uv);

            if (dist > 0.5) return half4(0.0);

            // Rotating sweep line
            float angle = atan2(uv.y, uv.x);
            float sweep = mod(angle - iTime * 2.0, 6.28318) / 6.28318;
            sweep = pow(sweep, 8.0);

            // Concentric rings
            float rings = sin(dist * 50.0 - iTime * 2.0) * 0.5 + 0.5;
            rings = pow(rings, 20.0) * 0.2;

            // Outer circular border
            float border = smoothstep(0.49, 0.5, dist) - smoothstep(0.5, 0.51, dist);

            // Behavioral intensity "interference"
            float noise = sin(dist * 100.0 + iTime * 10.0) * iIntensity * 0.1;

            float3 color = iColor * (sweep + rings + border + noise);

            // Fade out towards edges
            float alpha = (sweep * 0.6 + rings * 0.3 + border) * (1.0 - smoothstep(0.4, 0.5, dist));
            alpha += iIntensity * 0.2 * (1.0 - dist * 2.0); // Inner core glow from intensity

            return half4(color, alpha);
        }
    """
}
