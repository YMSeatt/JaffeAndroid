package com.example.myapplication.labs.ghost

import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.model.FurnitureUiItem

/**
 * GhostBlueprintEngine: Ported from Python/ghost_blueprint.py
 *
 * This engine generates a futuristic SVG 'blueprint' of the classroom layout,
 * translating Android's logical coordinates to a fixed-size SVG canvas with
 * stylized nodes for students and furniture.
 */
object GhostBlueprintEngine {
    fun generateBlueprint(students: List<StudentUiItem>, furniture: List<FurnitureUiItem>): String {
        val width = 1200
        val height = 800

        val svg = StringBuilder()
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
        svg.append("<svg width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\" xmlns=\"http://www.w3.org/2000/svg\">\n")
        svg.append("  <defs>\n")
        svg.append("    <linearGradient id=\"grad1\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">\n")
        svg.append("      <stop offset=\"0%\" style=\"stop-color:#00ffff;stop-opacity:0.2\" />\n")
        svg.append("      <stop offset=\"100%\" style=\"stop-color:#0088ff;stop-opacity:0.05\" />\n")
        svg.append("    </linearGradient>\n")
        svg.append("    <filter id=\"glow\">\n")
        svg.append("      <feGaussianBlur stdDeviation=\"2.5\" result=\"coloredBlur\"/>\n")
        svg.append("      <feMerge>\n")
        svg.append("        <feMergeNode in=\"coloredBlur\"/><feMergeNode in=\"SourceGraphic\"/>\n")
        svg.append("      </feMerge>\n")
        svg.append("    </filter>\n")
        svg.append("  </defs>\n")
        svg.append("  <rect width=\"$width\" height=\"$height\" fill=\"url(#grad1)\" />\n")
        svg.append("  \n")
        svg.append("  <!-- Grid Lines (Logic Parity: Every 40 units) -->\n")
        for (i in 0..width step 40) {
            svg.append("  <line x1=\"$i\" y1=\"0\" x2=\"$i\" y2=\"$height\" stroke=\"#00ffff\" stroke-width=\"0.5\" stroke-opacity=\"0.3\" />\n")
        }
        for (i in 0..height step 40) {
            svg.append("  <line x1=\"0\" y1=\"$i\" x2=\"$width\" y2=\"$i\" stroke=\"#00ffff\" stroke-width=\"0.5\" stroke-opacity=\"0.3\" />\n")
        }

        svg.append("\n  <!-- Student Nodes -->\n")
        students.forEach { student ->
            /**
             * Scaling Logic:
             * Normalizes Android's 4000x4000 logical coordinate system into the
             * blueprint's 1200x800 SVG canvas.
             *
             * Formula: (pos / 4) + offset
             * - pos / 4: Reduces 4000 units to 1000 units.
             * - +200/+100: Offsets the layout to center it within the 1200x800 frame,
             *   providing a 100-200 unit margin.
             */
            val x = (student.xPosition.value / 4f) + 200f
            val y = (student.yPosition.value / 4f) + 100f
            val name = student.fullName.value
            val initials = student.initials.value

            svg.append("  <g transform=\"translate($x,$y)\" filter=\"url(#glow)\">\n")
            svg.append("    <rect x=\"-30\" y=\"-30\" width=\"60\" height=\"60\" fill=\"none\" stroke=\"#00ffff\" stroke-width=\"2\" rx=\"5\" />\n")
            svg.append("    <text x=\"0\" y=\"5\" font-family=\"monospace\" font-size=\"20\" fill=\"#00ffff\" text-anchor=\"middle\">$initials</text>\n")
            svg.append("    <text x=\"0\" y=\"45\" font-family=\"monospace\" font-size=\"10\" fill=\"#00ffff\" text-anchor=\"middle\" opacity=\"0.7\">$name</text>\n")
            svg.append("  </g>\n")
        }

        svg.append("\n  <!-- Furniture Nodes (Adaptation for Android Reality) -->\n")
        furniture.forEach { item ->
            val x = (item.xPosition / 4f) + 200f
            val y = (item.yPosition / 4f) + 100f
            val name = item.name

            svg.append("  <g transform=\"translate($x,$y)\" filter=\"url(#glow)\">\n")
            svg.append("    <rect x=\"-25\" y=\"-25\" width=\"50\" height=\"50\" fill=\"none\" stroke=\"#00ffff\" stroke-width=\"1.5\" stroke-dasharray=\"5,5\" rx=\"3\" />\n")
            svg.append("    <text x=\"0\" y=\"40\" font-family=\"monospace\" font-size=\"8\" fill=\"#00ffff\" text-anchor=\"middle\" opacity=\"0.6\">$name</text>\n")
            svg.append("  </g>\n")
        }

        svg.append("</svg>")
        return svg.toString()
    }
}
