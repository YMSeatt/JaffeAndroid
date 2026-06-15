package com.example.myapplication.labs.ghost.phoenix

/**
 * Repository for Ghost Phoenix AGSL shader programs.
 */
object GhostPhoenixShader {
    /**
     * AGSL Shader for the Ghost Phoenix visualization.
     *
     * ### Visual Features:
     * - **Rising Embers**: Procedural particles that ascend from the base of the student icon.
     * - **Fire Core**: A central, flickering flame-like aura driven by fbm noise.
     * - **Resilience Pulse**: The intensity and color shift based on the resilience score.
     *
     * ### Uniforms:
     * - `iResolution`: The size of the effect in pixels.
     * - `iTime`: Animation phase for movement and flickering.
     * - `iResilience`: The normalized resilience score [0.0 - 1.0].
     * - `iColor`: The primary "Fire" color (typically Orange/Red).
     */
    const val PHOENIX_RISING = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iResilience;
        uniform float3 iColor;

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
            float2 centeredUv = (fragCoord - 0.5 * iResolution.xy) / min(iResolution.y, iResolution.x);
            float dist = length(centeredUv);

            // Base flame aura
            float f = 0.0;
            float2 q = uv;
            q.y -= iTime * 0.5;
            f += 0.5000 * noise(q * 4.0);
            q *= 2.01;
            f += 0.2500 * noise(q * 4.0);

            float flame = 1.0 - smoothstep(0.0, 0.4 * iResilience, dist - f * 0.1);
            float3 fireColor = iColor * flame;

            // Rising embers
            float embers = 0.0;
            for(float i=0.0; i<8.0; i++) {
                float h = hash(i * 123.456);
                float x = (h - 0.5) * 0.8;
                float speed = 0.5 + h * 0.5;
                float y = mod(-iTime * speed + h, 1.0) - 0.5;
                float size = 0.01 + h * 0.02;
                float d = length(centeredUv - float2(x, y));
                embers += smoothstep(size, 0.0, d) * (1.0 - (y + 0.5));
            }

            float3 finalColor = fireColor + iColor * embers * iResilience;
            float finalAlpha = (flame * 0.6 + embers) * iResilience * (1.0 - dist * 2.0);

            return half4(finalColor, finalAlpha);
        }
    """
}
