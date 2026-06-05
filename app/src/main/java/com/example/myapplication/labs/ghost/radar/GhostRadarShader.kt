package com.example.myapplication.labs.ghost.radar

object GhostRadarShader {
    /**
     * AGSL Shader for the Ghost Radar visualization.
     * Features:
     * - Rotating circular sweep line.
     * - Concentric distance rings.
     * - Localized behavioral "glow" based on intensity.
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
