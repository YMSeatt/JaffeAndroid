package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostLensShader: AGSL shaders for the Ghost Lens experiment.
 */
object GhostLensShader {

    @Language("AGSL")
    const val GHOST_LENS = """
        uniform float2 iResolution;
        uniform float2 iLensPos;
        uniform float iLensRadius;
        uniform float iMagnification;
        uniform shader child;

        half4 main(float2 fragCoord) {
            float dist = distance(fragCoord, iLensPos);

            if (dist < iLensRadius) {
                // Spherical distortion for magnification
                float2 dir = fragCoord - iLensPos;
                float magDist = dist / iLensRadius;

                // distortion factor: smaller power = more "bulge"
                float distortion = pow(magDist, 0.8) * iMagnification;
                float2 distortedCoord = iLensPos + dir * distortion;

                // Sample with chromatic aberration at the edges
                // Increased intensity (0.05) for better visibility in PoC
                float aberration = 0.05 * smoothstep(0.0, iLensRadius, dist);
                half4 r = child.eval(distortedCoord + float2(aberration, 0.0));
                half4 g = child.eval(distortedCoord);
                half4 b = child.eval(distortedCoord - float2(aberration, 0.0));

                half4 col = half4(r.r, g.g, b.b, 1.0);

                // Add a glowing holographic border
                float border = smoothstep(iLensRadius - 3.0, iLensRadius, dist);
                half4 borderColor = half4(0.0, 0.8, 1.0, 0.6); // Cyan

                return mix(col, borderColor, border * 0.5);
            } else {
                return child.eval(fragCoord);
            }
        }
    """
}
