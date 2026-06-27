package com.example.myapplication.labs.ghost.kaleidoscope

import org.intellij.lang.annotations.Language

/**
 * GhostKaleidoscopeShader: AGSL shaders for radial symmetry visualization.
 */
object GhostKaleidoscopeShader {

    @Language("AGSL")
    const val KALEIDOSCOPE_FIELD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iHarmony; // 0.1 to 1.0 (Drives symmetry count)

        // Fragments (x, y, polarity, intensity)
        uniform float4 iFragments[12];
        uniform int iFragmentCount;

        // Radial symmetry helper
        float2 getSymmetricUv(float2 uv, float symmetryCount) {
            float angle = atan2(uv.y, uv.x);
            float radius = length(uv);

            // Slice the space
            float slice = 2.0 * 3.14159 / symmetryCount;
            angle = mod(angle, slice);

            // Mirror within the slice
            if (angle > slice * 0.5) {
                angle = slice - angle;
            }

            return float2(cos(angle), sin(angle)) * radius;
        }

        float4 main(float2 fragCoord) {
            float2 uv = (fragCoord - 0.5 * iResolution.xy) / min(iResolution.y, iResolution.x);

            // Determine symmetry based on harmony (3 to 12 axes)
            float symmetryCount = floor(3.0 + iHarmony * 9.0);

            // Apply radial symmetry
            float2 symUv = getSymmetricUv(uv, symmetryCount);

            float3 finalColor = float3(0.0);

            for (int i = 0; i < iFragmentCount; i++) {
                float2 p = (iFragments[i].xy - 0.5 * iResolution.xy) / min(iResolution.y, iResolution.x);
                float polarity = iFragments[i].z;
                float intensity = iFragments[i].w;

                // Rotational movement based on time
                float rot = iTime * 0.2;
                float2 rotP = float2(
                    p.x * cos(rot) - p.y * sin(rot),
                    p.x * sin(rot) + p.y * cos(rot)
                );

                float d = length(symUv - rotP);

                // Glowing shard effect
                float shard = smoothstep(0.15 * intensity, 0.0, d);
                float3 shardColor = mix(float3(1.0, 0.0, 1.0), float3(0.0, 1.0, 1.0), polarity * 0.5 + 0.5);

                finalColor += shardColor * shard * (0.5 + 0.5 * sin(iTime + float(i)));
            }

            // Substrate glow
            finalColor += float3(0.05, 0.05, 0.1) * (1.0 - length(uv));

            return float4(finalColor, 0.7);
        }
    """
}
