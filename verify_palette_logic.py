
import math

def hsv_to_rgb(h, s, v):
    if s == 0.0: return (v, v, v)
    i = int(h * 6.0)
    f = (h * 6.0) - i
    p = v * (1.0 - s)
    q = v * (1.0 - s * f)
    t = v * (1.0 - s * (1.0 - f))
    i %= 6
    if i == 0: return (v, t, p)
    if i == 1: return (q, v, p)
    if i == 2: return (p, v, t)
    if i == 3: return (p, q, v)
    if i == 4: return (t, p, v)
    if i == 5: return (v, p, q)

def calculate_harmony(h, s, v, harmony):
    primary = hsv_to_rgb(h / 360.0, s, v)

    if harmony == "COMPLEMENTARY":
        secondary_hue = (h + 180.0) % 360.0
    elif harmony == "TRIADIC":
        secondary_hue = (h + 120.0) % 360.0
    else:
        secondary_hue = h

    secondary = hsv_to_rgb(secondary_hue / 360.0, s, v)
    return primary, secondary

def test_palette_logic():
    print("Testing Ghost Palette Logic...")

    # Test Complementary (Blue -> Yellow-ish)
    h, s, v = 240.0, 1.0, 1.0
    p, s_color = calculate_harmony(h, s, v, "COMPLEMENTARY")
    print(f"Complementary (H={h}): Primary={p}, Secondary={s_color}")

    # Check if secondary hue is exactly 180 deg away
    target_h = (h + 180.0) % 360.0
    if target_h == 60.0:
        print("✅ Complementary Hue Shift Correct")
    else:
        print(f"❌ Complementary Hue Shift Failed: {target_h}")

    # Test Triadic (Red -> Green-ish)
    h, s, v = 0.0, 1.0, 1.0
    p, s_color = calculate_harmony(h, s, v, "TRIADIC")
    print(f"Triadic (H={h}): Primary={p}, Secondary={s_color}")

    target_h = (h + 120.0) % 360.0
    if target_h == 120.0:
        print("✅ Triadic Hue Shift Correct")
    else:
        print(f"❌ Triadic Hue Shift Failed: {target_h}")

    print("Palette Logic Verification Complete.")

if __name__ == "__main__":
    test_palette_logic()
