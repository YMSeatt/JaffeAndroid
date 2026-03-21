package com.example.myapplication.labs.ghost.orbit

import org.intellij.lang.annotations.Language

/**
 * GhostOrbitShader: AGSL shaders for the "Ghost Orbit" galaxy visualization.
 */
object GhostOrbitShader {
    /**
     * NEURAL_NEBULA: A procedural background for the Classroom Galaxy.
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the drawing area.
     * - [iTime]: Animation time.
     * - [iSystemEnergy]: Aggregate energy of the classroom.
     */
    @Language("AGSL")
    const val NEURAL_NEBULA = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iSystemEnergy;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            f = f * f * (3.0 - 2.0 * f);
            float a = hash(i);
            float b = hash(i + float2(1.0, 0.0));
            float c = hash(i + float2(0.0, 1.0));
            float d = hash(i + float2(1.0, 1.0));
            return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
        }

        float fbm(float2 p) {
            float v = 0.0;
            float a = 0.5;
            float2 shift = float2(100.0);
            for (int i = 0; i < 5; ++i) {
                v += a * noise(p);
                p = p * 2.0 + shift;
                a *= 0.5;
            }
            return v;
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);

            // Rotating nebula effect
            float a = atan(p.y, p.x);
            float r = length(p);
            float2 uv2 = float2(r, a / 3.14159);

            float speed = 0.1 + iSystemEnergy * 0.5;
            float n = fbm(uv * 3.0 + iTime * speed);
            float n2 = fbm(uv2 * 2.0 - iTime * speed * 0.5);

            float3 color1 = float3(0.05, 0.0, 0.1); // Deep Space Blue/Purple
            float3 color2 = float3(0.0, 0.3, 0.5); // Nebula Cyan
            float3 color3 = float3(0.6, 0.0, 0.4); // Energy Magenta

            float3 base = mix(color1, color2, n);
            float3 finalColor = mix(base, color3, n2 * iSystemEnergy);

            // Add stars
            float star = pow(hash(fragCoord + floor(iTime * 10.0)), 100.0);
            finalColor += star * 0.5;

            return float4(finalColor, 0.8);
        }
    """

    /**
     * GRAVITY_WELL: Gravitational lensing effect around student nodes.
     *
     * Uniforms:
     * - [iPosition]: Center of the well.
     * - [iIntensity]: Intensity of the distortion.
     * - [iRadius]: Influence radius.
     */
    @Language("AGSL")
    const val GRAVITY_WELL = """
        uniform float2 iResolution;
        uniform float2 iPosition;
        uniform float iIntensity;
        uniform float iRadius;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = fragCoord;
            float2 d = p - iPosition;
            float dist = length(d);

            if (dist < iRadius) {
                float force = (1.0 - dist / iRadius) * iIntensity;
                float2 offset = normalize(d) * force * 50.0;
                // Here we would normally sample the underlying buffer,
                // but in Compose we often use this to colorize or warp
                // based on the coordinate system.
                float3 col = float3(0.0, 0.8, 1.0) * force;
                return float4(col, force * 0.5);
            }
            return float4(0.0);
        }
    """
}
