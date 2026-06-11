
class GhostOrigamiEngine:
    def __init__(self):
        self.fold_progress = 0.0

    def toggle_fold(self):
        if self.fold_progress > 0.5:
            self.fold_progress = 0.0
        else:
            self.fold_progress = 1.0

    def set_fold_progress(self, progress):
        self.fold_progress = max(0.0, min(1.0, progress))

    def reset(self):
        self.fold_progress = 0.0

def test_origami_logic():
    engine = GhostOrigamiEngine()

    # Test initial state
    assert engine.fold_progress == 0.0

    # Test toggle to folded
    engine.toggle_fold()
    assert engine.fold_progress == 1.0

    # Test toggle back to flat
    engine.toggle_fold()
    assert engine.fold_progress == 0.0

    # Test manual progress
    engine.set_fold_progress(0.75)
    assert engine.fold_progress == 0.75

    # Test toggle from partial fold (should go to flat if > 0.5)
    engine.toggle_fold()
    assert engine.fold_progress == 0.0

    # Test toggle from partial fold (should go to folded if <= 0.5)
    engine.set_fold_progress(0.4)
    engine.toggle_fold()
    assert engine.fold_progress == 1.0

    # Test reset
    engine.reset()
    assert engine.fold_progress == 0.0

    print("Ghost Neural Origami Logic Verification: SUCCESS")

if __name__ == "__main__":
    test_origami_logic()
