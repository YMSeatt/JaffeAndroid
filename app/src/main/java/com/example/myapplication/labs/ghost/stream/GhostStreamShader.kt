package com.example.myapplication.labs.ghost.stream

import org.intellij.lang.annotations.Language

/**
 * GhostStreamShader: AGSL shader for the futuristic data stream background.
 */
object GhostStreamShader {

    @Language("AGSL")
    val DATA_STREAM_SHADER = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity;

        float hash(float n) { return fract(sin(n) * 43758.5453123); }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            f = f * f * (3.0 - 2.0 * f);
            float n = i.x + i.y * 57.0;
            return mix(mix(hash(n + 0.0), hash(n + 1.0), f.x),
                       mix(hash(n + 57.0), hash(n + 58.0), f.x), f.y);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Scrolling data lines
            float line = step(0.98, fract(uv.y * 20.0 + iTime * 0.5));
            float data = noise(float2(uv.x * 50.0, uv.y * 2.0 + iTime * 2.0));

            float alpha = (line * 0.1 + data * 0.05) * iIntensity;

            // Base "Ghost Cyan" tinted with "Neural Blue"
            half3 color = mix(half3(0.0, 1.0, 0.8), half3(0.1, 0.2, 1.0), uv.x);

            return half4(color * alpha, alpha);
        }
    """.trimIndent()
}
