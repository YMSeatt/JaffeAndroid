# Ghost Carbon 👻

Ghost Carbon is an experimental behavioral analysis tool that identifies "Behavioral Twins" in the classroom.

## Concept
Behavioral twins are students who exhibit nearly identical patterns of logged behaviors over time. Identifying these patterns helps teachers understand social influences and shared behavioral archetypes.

## Technical Implementation
- **GhostCarbonEngine**: Uses cosine similarity between behavioral frequency vectors to identify pairs of students with matching signatures.
- **GhostCarbonShader**: An AGSL shader that visualizes these connections using pulsing "Resonance Bridges".
- **GhostCarbonLayer**: A Compose layer that renders the bridges on the 4000x4000 logical canvas.

## Optimization
- **Background Pipeline**: Detection logic is offloaded to the `SeatingChartViewModel` background thread.
- **Memoization**: Recalculation only occurs when behavior logs change.
- **Vector Math**: Optimized similarity calculation with manual loops to minimize object churn.
