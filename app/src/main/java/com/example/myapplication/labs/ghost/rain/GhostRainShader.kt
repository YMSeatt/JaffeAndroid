package com.example.myapplication.labs.ghost.rain

import org.intellij.lang.annotations.Language

/**
 * GhostRainShader: AGSL shaders for the Neural Rain experiment.
 */
object GhostRainShader {

    @Language("AGSL")
    const val NEURAL_DROPLETS = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iDropPos[20]; // Falling droplets in screen space
        uniform float iSplashTime[20]; // 0..1 for splash animation
        uniform float iIntensities[20];
        uniform int iDropCount;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution;
            half4 color = half4(0.0);

            for (int i = 0; i < iDropCount; i++) {
                float2 dropPos = iDropPos[i];
                float splash = iSplashTime[i];

                if (splash > 0.0) {
                    // Splash Ripple Effect
                    float d = distance(fragCoord, dropPos);
                    float ripple = sin(d * 0.2 - splash * 10.0) * (1.0 - splash);
                    float mask = smoothstep(20.0 * splash, 0.0, abs(d - 40.0 * splash));
                    color += half4(0.0, 0.8, 1.0, mask * ripple * 0.5);
                } else {
                    // Falling Droplet with Motion Blur (Vertical streak)
                    float d = distance(fragCoord.x, dropPos.x);
                    float verticalDist = fragCoord.y - dropPos.y;

                    if (d < 2.0 && verticalDist > -30.0 && verticalDist < 0.0) {
                        float alpha = smoothstep(-30.0, 0.0, verticalDist) * smoothstep(2.0, 0.0, d);
                        color += half4(0.5, 0.9, 1.0, alpha * 0.6);
                    }
                }
            }

            return color;
        }
    """
}
