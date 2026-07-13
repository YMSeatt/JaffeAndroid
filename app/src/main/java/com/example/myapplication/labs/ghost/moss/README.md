# 👻 Ghost Moss: Social Stagnation Visualization

"Quiet students aren't just quiet; they are the forgotten roots of the classroom. Ghost Moss visualizes the natural dormancy that occurs when students go long periods without behavioral or academic interaction."

## 🛠️ The Metaphor
Ghost Moss is a biological visualization of social stagnation. When a student has zero logged interactions (Behavior, Quiz, or Homework) for an extended period, digital moss begins to grow around their icon on the seating chart.

- **Growth Threshold**: Moss starts appearing after 7 days of inactivity.
- **Max Density**: Reaches full saturation after 21 days of inactivity.
- **Data Clearing**: Any new log entry "clears" the moss, resetting the growth cycle.

## 🧬 BOLT Optimization
- **O(Recent) Logic**: The engine only checks the *latest* log timestamp per student.
- **Zero-Allocation Rendering**: The AGSL shader is hoisted and reused across all student nodes.
- **Single-Pass Synthesis**: Dormancy scores are calculated in the background update pipeline to maintain 60fps.

---
*Ghost - Rapid Prototyping for the Classroom of 2027*
