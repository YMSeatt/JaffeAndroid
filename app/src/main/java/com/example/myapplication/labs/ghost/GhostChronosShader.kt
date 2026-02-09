package com.example.myapplication.labs.ghost

object GhostChronosShader {
    const val CHRONOS_HEATMAP = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity[100];
        uniform float3 iColorPositive;
        uniform float3 iColorNegative;
        uniform float2 iOffset;
        uniform float iScale;

        half4 main(float2 fragCoord) {
            // Transform fragCoord to logical canvas space (4000x4000)
            float2 canvasCoord = (fragCoord - iOffset) / iScale;

            // Map 0..4000 to 0..1 UV for the 10x10 grid
            float2 uv = canvasCoord / 4000.0;

            if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
                return half4(0.0);
            }

            int x = int(min(uv.x * 10.0, 9.0));
            int y = int(min(uv.y * 10.0, 9.0));
            int index = y * 10 + x;

            float intensity = iIntensity[index];

            float pulse = 0.8 + 0.2 * sin(iTime * 2.0 + intensity * 10.0);

            float3 finalColor = float3(0.0);
            if (intensity > 0.0) {
                finalColor = mix(iColorPositive, float3(1.0, 1.0, 1.0), intensity * 0.5) * intensity;
            } else if (intensity < 0.0) {
                finalColor = mix(iColorNegative, float3(1.0, 1.0, 1.0), abs(intensity) * 0.5) * abs(intensity);
            }

            float alpha = abs(intensity) * 0.3 * pulse;
            return half4(finalColor, alpha);
        }
    """
}
