package com.example.myapplication.labs.ghost.weather

import org.intellij.lang.annotations.Language

/**
 * GhostWeatherShader: AGSL shaders for the atmospheric weather effects.
 */
object GhostWeatherShader {

    @Language("AGSL")
    const val WEATHER_ATMOSPHERE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity;
        uniform float iLightningAlpha;
        uniform float iLightningX;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Base background (dark stormy sky)
            float3 stormColor = float3(0.05, 0.05, 0.1);
            float3 skyColor = mix(float3(0.02, 0.02, 0.05), stormColor, iIntensity);

            // Lightning Flash
            float distToLightning = abs(fragCoord.x - iLightningX);
            float flash = iLightningAlpha * exp(-distToLightning * 0.005);
            skyColor += float3(0.8, 0.9, 1.0) * flash;

            return half4(skyColor, 0.3 * iIntensity);
        }
    """

    @Language("AGSL")
    const val WEATHER_LIGHTNING = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iAlpha;
        uniform float iX;

        float hash(float n) { return fract(sin(n) * 43758.5453123); }

        float noise(float x) {
            float i = floor(x);
            float f = fract(x);
            return mix(hash(i), hash(i + 1.0), smoothstep(0.0, 1.0, f));
        }

        half4 main(float2 fragCoord) {
            if (iAlpha <= 0.0) return half4(0.0);

            float2 uv = fragCoord / iResolution.xy;
            float xOffset = iX + (noise(uv.y * 10.0 + iTime * 20.0) - 0.5) * 100.0;

            float dist = abs(fragCoord.x - xOffset);
            float glow = exp(-dist * 0.1) * iAlpha;
            float core = exp(-dist * 0.5) * iAlpha;

            float3 lightningColor = float3(0.7, 0.8, 1.0) * (glow + core);
            return half4(lightningColor, iAlpha);
        }
    """
}
