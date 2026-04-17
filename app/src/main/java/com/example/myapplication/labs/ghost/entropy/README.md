# 👻 Ghost Entropy: Neural Turbulence & Behavioral Uncertainty

## Overview
The **Ghost Entropy** experiment models the classroom as a thermodynamic system where student predictability is represented as "Neural Entropy." It analyzes both behavioral diversity and academic variance to identify students in a "turbulent" or "chaotic" state, visualizing this state through real-time AGSL thermal distortion effects.

## The Thermodynamic Metaphor
In this model, a "high-entropy" student is one whose actions are difficult to predict based on historical patterns.
- **Stable State (Low Entropy):** Predictable behavior (e.g., consistently positive or consistently quiet) and steady academic performance.
- **Turbulent State (High Entropy):** Unpredictable behavior (e.g., rapid swings between positive and negative participation) and high variance in quiz scores.

## Mathematical Core

### 1. Behavioral Shannon Entropy
The engine calculates the Shannon Entropy ($H$) of a student's behavioral types:
$$H(X) = -\sum_{i=1}^{n} P(x_i) \log P(x_i)$$
Where $P(x_i)$ is the probability of behavior type $i$ occurring in the student's log history.
- **Normalization:** The result is normalized against $\ln(5)$ (assuming 5 primary behavior categories) to produce a value between `0.0` (predictable) and `1.0` (chaotic).

### 2. Academic Variance
Normalized statistical variance of quiz score ratios ($score / max\_score$):
$$\sigma^2 = \frac{\sum (x_i - \mu)^2}{N}$$
- **Normalization:** Since the maximum variance for values in the range $[0, 1]$ is $0.25$, the raw variance is multiplied by 4 to map it to the `0.0` to `1.0` range.

### 3. Total Entropy Score
The final "Neural Entropy" score is a weighted combination:
- **60% Behavioral Entropy**
- **40% Academic Variance**

## Visual Logic (AGSL)
Students with high entropy scores trigger the **GhostEntropyShader**, which applies a "Thermal Distortion" effect to their seating chart icon:
- **Procedural Turbulence:** Uses a simplex-noise-like hash function to generate non-periodic fluctuations.
- **UV Distortion:** The underlying icon pixels are sampled with an offset driven by the noise and the student's entropy level, creating a "shimmering" or "heat haze" effect.
- **Chromatic Shift:** High-entropy nodes are tinted with a pulsing Magenta glow to signify neural instability.

## Performance Optimization (BOLT ⚡)
- **Pre-calculated Moments:** The engine accepts pre-calculated statistical moments (sums and sum of squares) to ensure $O(1)$ calculation during the background update pipeline.
- **First-20 Optimization:** The `GhostEntropyLayer` only samples the entropy of the first 20 students to determine the global distortion intensity, ensuring 60fps rendering even in large classrooms.
- **RenderEffect Memoization:** The `RenderEffect` is cached and only re-allocated when the maximum entropy threshold crosses significant boundaries, minimizing object churn.
