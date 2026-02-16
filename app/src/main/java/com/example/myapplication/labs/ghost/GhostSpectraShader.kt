package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostSpectraShader: AGSL scripts for the "Ghost Spectra" data refraction layer.
 *
 * This shader simulates a dispersive glass prism that "breaks" the UI into its
 * constituent data components. It uses chromatic aberration and procedural
 * spectroscopy to visualize hidden classroom metrics.
 */
object GhostSpectraShader {
    @Language("AGSL")
    const val SPECTRA_PRISM = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iPrismX;      // Normalized horizontal position (0.0 to 1.0)
        uniform float iDensity;     // Spectral density/dispersion (0.0 to 1.0)
        uniform float iAgitation;   // Global agitation level (0.0 to 1.0)

        // Helper: Generate spectral colors based on wavelength-like input
        float3 spectrum(float x) {
            float3 c = float3(0.0);
            c.r = smoothstep(0.4, 0.2, x) + smoothstep(0.6, 0.8, x);
            c.g = smoothstep(0.2, 0.4, x) * smoothstep(0.8, 0.6, x);
            c.b = smoothstep(0.4, 0.7, x) * smoothstep(1.0, 0.8, x);
            return c;
        }

        // Pseudo-random noise for digital grain
        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float pxX = iPrismX * iResolution.x;
            float dist = abs(fragCoord.x - pxX);

            // The "Glass" width expands with density
            float prismWidth = iResolution.x * (0.05 + 0.1 * iDensity);
            float edge = smoothstep(prismWidth, prismWidth * 0.5, dist);

            if (edge <= 0.0) {
                return float4(0.0);
            }

            // Calculate dispersion offset based on distance from prism center
            float dispersion = (fragCoord.x - pxX) / prismWidth;

            // Generate spectroscopic bands
            float wave = sin(uv.y * 50.0 + iTime * 2.0) * 0.5 + 0.5;
            float3 specColor = spectrum(uv.y + dispersion * 0.2 * iDensity);

            // Add "Digital Refraction" - horizontal shifts in the spectrum
            float shift = sin(uv.y * 100.0 + iTime * 5.0) * iAgitation * 0.02;
            specColor = spectrum(uv.y + dispersion * 0.2 * iDensity + shift);

            // Composite with edge mask and transparency
            float alpha = edge * (0.4 + 0.2 * wave);

            // Add some "Ghost Grain"
            float grain = hash(fragCoord + iTime) * 0.1;
            float3 finalColor = specColor + grain;

            return float4(finalColor, alpha * 0.7);
        }
    """
}
