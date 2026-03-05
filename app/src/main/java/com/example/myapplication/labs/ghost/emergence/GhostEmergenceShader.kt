package com.example.myapplication.labs.ghost.emergence

/**
 * GhostEmergenceShader: AGSL shader for visualizing behavioral emergence.
 * Renders a glowing, organic field based on a 10x10 grid of vitality.
 */
object GhostEmergenceShader {
    val SHADER = """
        uniform shader content;
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iGrid[100]; // 10x10 vitality grid (optimized)

        float getVitality(float2 uv) {
            float2 gridCoord = uv * 9.99;
            int x = int(gridCoord.x);
            int y = int(gridCoord.y);
            return iGrid[y * 10 + x];
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Sample grid with slight blur
            float v = 0.0;
            v += getVitality(uv);
            v += getVitality(uv + float2(0.01, 0.0));
            v += getVitality(uv - float2(0.01, 0.0));
            v += getVitality(uv + float2(0.0, 0.01));
            v += getVitality(uv - float2(0.0, 0.01));
            v /= 5.0;

            // Base color based on vitality (Positive = Cyan, Negative = Red)
            float3 posColor = float3(0.0, 0.8, 1.0);
            float3 negColor = float3(1.0, 0.2, 0.2);
            float3 color = v > 0.0 ? posColor : negColor;

            // Dynamic organic texture
            float noise = sin(uv.x * 20.0 + iTime) * cos(uv.y * 20.0 + iTime * 0.5);
            float alpha = abs(v) * (0.4 + 0.2 * noise);

            // Glow effect
            alpha += pow(abs(v), 2.0) * 0.3;

            return half4(color * alpha, alpha * 0.7);
        }
    """.trimIndent()
}
