package com.example.myapplication.labs.ghost.phasing

import org.intellij.lang.annotations.Language

/**
 * GhostPhasingShader: AGSL shaders for the "Ghost Phasing" experiment.
 *
 * Provides a "Backstage" neural void visualization and a glitchy transition
 * effect to switch between the physical classroom and the data layer.
 */
object GhostPhasingShader {

    @Language("AGSL")
    const val PHASE_TRANSITION = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iPhase; // 0.0 to 1.0
        uniform shader iContent;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            if (iPhase <= 0.0) return iContent.eval(fragCoord);
            if (iPhase >= 1.0) return float4(0.0);

            // Chromatic Aberration
            float amount = 0.05 * iPhase;
            float r = iContent.eval(fragCoord + float2(amount * iResolution.x, 0.0)).r;
            float g = iContent.eval(fragCoord).g;
            float b = iContent.eval(fragCoord - float2(amount * iResolution.x, 0.0)).b;

            // Scanline Glitch
            float scanline = sin(uv.y * 800.0 + iTime * 10.0) * 0.1 * iPhase;
            float glitch = hash(float2(floor(uv.y * 50.0), iTime)) * iPhase;

            float3 color = float3(r, g, b);
            if (glitch > 0.98 - 0.1 * iPhase) {
                color += 0.2 * iPhase;
            }

            return float4(color, 1.0 - iPhase);
        }
    """

    @Language("AGSL")
    const val NEURAL_VOID = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        // Simplex-like noise
        float3 hash33(float3 p3) {
            p3 = fract(p3 * float3(.1031, .1030, .0973));
            p3 += dot(p3, p3.yxz + 33.33);
            return fract((p3.xxy + p3.yxx) * p3.zyx);
        }

        float4 main(float2 fragCoord) {
            float2 uv = (fragCoord - 0.5 * iResolution.xy) / iResolution.y;
            float3 color = float3(0.01, 0.02, 0.05); // Deep space blue

            // Floating "Neural Seeds"
            for (int i = 0; i < 8; i++) {
                float3 p = hash33(float3(float(i) * 123.456, 0.0, 0.0));
                float2 pos = (p.xy - 0.5) * 1.5;
                pos.x += sin(iTime * 0.5 + p.z * 6.28) * 0.2;
                pos.y += cos(iTime * 0.4 + p.z * 6.28) * 0.2;

                float dist = length(uv - pos);
                float glow = 0.002 / (dist * dist + 0.001);

                float3 seedColor = mix(float3(0.0, 0.8, 1.0), float3(1.0, 0.2, 0.8), p.y);
                color += seedColor * glow * iIntensity;
            }

            // Data streams (particles)
            float2 grid = floor(fragCoord * 0.05);
            float h = hash(grid);
            if (h > 0.95) {
                float speed = 200.0 * (0.5 + h);
                float y = mod(fragCoord.y + iTime * speed, iResolution.y);
                if (abs(fragCoord.x - grid.x / 0.05) < 1.0) {
                    color += float3(0.0, 0.5, 0.4) * iIntensity * smoothstep(10.0, 0.0, abs(y - mod(iTime * speed, iResolution.y)));
                }
            }

            return float4(color, 1.0);
        }
    """
}
