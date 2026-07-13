package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostBioSyncShader: AGSL shaders for the BioSync atmospheric visualization.
 */
object GhostBioSyncShader {

    /**
     * BIOSYNC_PULSE: A procedural bioluminescent "breathing" shader.
     *
     * Features:
     * - **Neural Breathing**: A slow, organic pulse representing classroom vitality.
     * - **Vitality Hotspots**: Glowing regions around active students.
     * - **Classroom Harmony**: Shifts the global color balance based on harmony scores.
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the viewport.
     * - [iTime]: Elapsed time for fluid animations.
     * - [iHarmony]: Global classroom harmony score (0..1).
     * - [iVitalityPoints]: Array of [x, y, vitality, stress] for students.
     * - [iPointCount]: Number of active vitality points.
     */
    @Language("AGSL")
    const val BIOSYNC_PULSE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iHarmony;
        uniform float4 iVitalityPoints[40]; // [x, y, vitality, stress]
        uniform float iPointCount;

        float3 getBioColor(float vitality, float stress) {
            float3 healthy = float3(0.0, 1.0, 0.8); // Bioluminescent Cyan
            float3 stressed = float3(1.0, 0.2, 0.5); // Neural Magenta
            return mix(healthy, stressed, stress);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Global "breathing" pulse
            float breath = sin(iTime * 1.5 + uv.y * 2.0) * 0.05 + 0.95;

            float3 finalColor = float3(0.0);
            float totalAlpha = 0.0;

            for (int i = 0; i < int(iPointCount); i++) {
                float4 p = iVitalityPoints[i];
                float2 pPos = p.xy / iResolution.xy;

                float d = distance(uv, pPos);

                // Vitality Aura
                float radius = 0.1 + p.z * 0.15;
                float aura = smoothstep(radius, 0.0, d);

                // Pulsing energy
                float energy = aura * (0.8 + 0.2 * sin(iTime * 3.0 + float(i)));

                float3 bioColor = getBioColor(p.z, p.w);
                finalColor += bioColor * energy * p.z;
                totalAlpha += energy * 0.4;
            }

            // Atmospheric Background (Harmony)
            float bgGlow = (1.0 - distance(uv, float2(0.5, 0.5))) * 0.1 * iHarmony;
            float3 bgColor = float3(0.0, 0.4, 0.5) * bgGlow;

            finalColor += bgColor;
            totalAlpha += bgGlow;

            return float4(finalColor * breath, totalAlpha * 0.7);
        }
    """
}
