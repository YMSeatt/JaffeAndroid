package com.example.myapplication.labs.ghost.ray

import org.intellij.lang.annotations.Language

/**
 * GhostRayShader: Volumetric AGSL Shaders for the Ghost Ray experiment.
 *
 * This shader simulates a "Neural Beam" of data, complete with a pulsating core,
 * chromatic aberration at the edges, and a "refractive" data-field distortion.
 */
object GhostRayShader {

    /**
     * A volumetric beam that originates from a point (the device) and extends to a target.
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the canvas.
     * - [iTime]: Elapsed time in seconds.
     * - [iSource]: Starting point of the ray (e.g., center of screen).
     * - [iTarget]: Target point of the ray (intersection on the canvas).
     * - [iIntensity]: Brightness of the beam (0.0 to 1.0).
     * - [iColor]: Base color of the beam (Cyan for stable, Magenta for agitated).
     */
    @Language("AGSL")
    const val NEURAL_BEAM = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iSource;
        uniform float2 iTarget;
        uniform float iIntensity;
        uniform float3 iColor;

        float distanceToSegment(float2 p, float2 a, float2 b) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 source = iSource / iResolution.xy;
            float2 target = iTarget / iResolution.xy;

            float d = distanceToSegment(uv, source, target);

            // The core of the beam
            float core = 0.002 / (d + 0.001);

            // Pulsating halo
            float pulse = sin(iTime * 10.0 - d * 50.0) * 0.2 + 0.8;
            float halo = 0.02 / (d + 0.02) * pulse;

            // Chromatic aberration / dispersion effect at the edges
            float3 beamColor = iColor;
            beamColor.r *= 1.0 + sin(iTime * 5.0) * 0.1;
            beamColor.b *= 1.0 + cos(iTime * 5.0) * 0.1;

            float alpha = (core + halo) * iIntensity;

            // Fade out the beam as it gets further from the target
            float targetDist = distance(uv, target);
            alpha *= exp(-targetDist * 2.0);

            return float4(beamColor * alpha, alpha * 0.7);
        }
    """
}
