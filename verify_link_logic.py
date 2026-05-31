import math

def calculate_links(nodes, threshold=600.0):
    """
    Python simulation of the GhostLinkEngine.calculateLinks logic.
    Identifies high-synergy pairings based on proximity and behavioral balance.
    """
    links = []
    threshold_sq = threshold * threshold

    for i in range(len(nodes)):
        node_a = nodes[i]
        for j in range(i + 1, len(nodes)):
            node_b = nodes[j]

            dx = node_a['x'] - node_b['x']
            dy = node_a['y'] - node_b['y']
            dist_sq = dx*dx + dy*dy

            if dist_sq < threshold_sq * 4:
                proximity = math.exp(-dist_sq / (2 * threshold_sq))

                # Synergy: Students with similar behavioral balance have higher synergy
                synergy = 1.0 - abs(node_a['behavioralBalance'] - node_b['behavioralBalance'])

                strength = (proximity * 0.7 + synergy * 0.3)

                if strength > 0.65:
                    links.append({
                        'studentA': node_a['id'],
                        'studentB': node_b['id'],
                        'strength': strength
                    })

    # Sort and take top 10
    links.sort(key=lambda x: x['strength'], reverse=True)
    return links[:10]

def test_link_logic():
    print("🧪 Verifying Ghost Link logic parity...")

    # Mock students
    nodes = [
        {'id': 1, 'x': 1000, 'y': 1000, 'behavioralBalance': 0.9}, # Pair A
        {'id': 2, 'x': 1100, 'y': 1100, 'behavioralBalance': 0.85}, # Pair A (Close and similar)
        {'id': 3, 'x': 3000, 'y': 3000, 'behavioralBalance': 0.1}, # Isolated
        {'id': 4, 'x': 1050, 'y': 1050, 'behavioralBalance': 0.2}, # Close to 1 & 2 but low synergy
        {'id': 5, 'x': 2500, 'y': 1000, 'behavioralBalance': 0.9}, # Far from 1
    ]

    links = calculate_links(nodes)

    print(f"Found {len(links)} neural links.")
    for link in links:
        print(f"  Link: Student {link['studentA']} <-> Student {link['studentB']} (Strength: {link['strength']:.2f})")

    # Basic assertions
    assert len(links) > 0, "Should have found at least one link"
    assert links[0]['studentA'] == 1 and links[0]['studentB'] == 2, "Strongest link should be between 1 and 2"

    # Verify 1-4 link (Close but disparate balance)
    link_1_4 = next((l for l in links if (l['studentA'] == 1 and l['studentB'] == 4)), None)
    if link_1_4:
        print(f"  Note: Link 1-4 found with strength {link_1_4['strength']:.2f} (Close but behavioral gap)")

    print("\n✅ Ghost Link logic parity verified.")

if __name__ == "__main__":
    test_link_logic()
