import json
import os
import random

def generate_hologram_svg(students, output_path="hologram_blueprint.svg"):
    """
    Generates a futuristic SVG 'blueprint' of the classroom seating chart.
    """
    width = 1200
    height = 800

    svg_content = [
        f'<svg width="{width}" height="{height}" viewBox="0 0 {width} {height}" xmlns="http://www.w3.org/2000/svg">',
        '<defs>',
        '  <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">',
        '    <stop offset="0%" style="stop-color:#00ffff;stop-opacity:0.2" />',
        '    <stop offset="100%" style="stop-color:#0088ff;stop-opacity:0.05" />',
        '  </linearGradient>',
        '  <filter id="glow">',
        '    <feGaussianBlur stdDeviation="2.5" result="coloredBlur"/>',
        '    <feMerge>',
        '      <feMergeNode in="coloredBlur"/><feMergeNode in="SourceGraphic"/>',
        '    </feMerge>',
        '  </filter>',
        '</defs>',
        f'<rect width="{width}" height="{height}" fill="url(#grad1)" />',
        '<!-- Grid -->',
    ]

    # Draw grid lines
    for i in range(0, width, 40):
        svg_content.append(f'<line x1="{i}" y1="0" x2="{i}" y2="{height}" stroke="#00ffff" stroke-width="0.5" stroke-opacity="0.3" />')
    for i in range(0, height, 40):
        svg_content.append(f'<line x1="0" y1="{i}" x2="{width}" y2="{i}" stroke="#00ffff" stroke-width="0.5" stroke-opacity="0.3" />')

    # Draw students as 'nodes'
    for student in students:
        x = student.get('x', random.randint(100, 1100)) / 4 + 200 # Scale for SVG
        y = student.get('y', random.randint(100, 700)) / 4 + 100
        name = student.get('name', 'Unknown')
        initials = "".join([n[0] for n in name.split()]).upper()

        svg_content.append(f'  <g transform="translate({x},{y})" filter="url(#glow)">')
        svg_content.append(f'    <rect x="-30" y="-30" width="60" height="60" fill="none" stroke="#00ffff" stroke-width="2" rx="5" />')
        svg_content.append(f'    <text x="0" y="5" font-family="monospace" font-size="20" fill="#00ffff" text-anchor="middle">{initials}</text>')
        svg_content.append(f'    <text x="0" y="45" font-family="monospace" font-size="10" fill="#00ffff" text-anchor="middle" opacity="0.7">{name}</text>')
        svg_content.append(f'  </g>')

    svg_content.append('</svg>')

    with open(output_path, "w") as f:
        f.write("\n".join(svg_content))
    print(f"Hologram blueprint generated at: {output_path}")

if __name__ == "__main__":
    # Mock data if no file provided
    mock_students = [
        {"name": "Alice Wonderland", "x": 400, "y": 400},
        {"name": "Bob Builder", "x": 1200, "y": 400},
        {"name": "Charlie Brown", "x": 800, "y": 1200},
        {"name": "Ghost User", "x": 2000, "y": 2000}
    ]

    generate_hologram_svg(mock_students)
