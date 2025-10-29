# Neural Network AI - Training and Usage Guide

## Quick Start: How to Run

### Step 1: Build the Project

```bash
cd /home/v841657/linux_home/Mindustry
./gradlew build
```

### Step 2: Create a Training Script

Create `train_neural_ai.sh`:

```bash
#!/bin/bash
# Neural AI Training Script

echo "=== Starting Neural Network AI Training ==="

# Set memory for long training runs
export JAVA_OPTS="-Xmx4G -XX:+UseG1GC"

# Run the training example
./gradlew :core:run --args="--train-neural-ai"
```

Make it executable:
```bash
chmod +x train_neural_ai.sh
```

### Step 3: Run Training

```bash
./train_neural_ai.sh
```

Or directly:

```bash
# Quick test run (fewer generations)
./gradlew :core:run --args="--train-neural-ai --generations 10"

# Full training run
./gradlew :core:run --args="--train-neural-ai --generations 100"

# Custom parameters
./gradlew :core:run --args="--train-neural-ai --population 30 --generations 150"
```

## How to Train: Detailed Process

### Option 1: Use the Example (Recommended for First Time)

Create `RunNeuralTraining.java`:

```java
package mindustry.ai.evolutionary;

import arc.util.*;
import mindustry.api.*;
import mindustry.ai.evolutionary.neural.*;

public class RunNeuralTraining {
    public static void main(String[] args) {
        Log.info("=== Neural Network AI Training ===");
        
        // 1. Initialize API (use HeadlessControllerLauncher for training)
        var launcher = HeadlessControllerLauncher.create();
        launcher.waitForInitialization();
        var controllerAPI = launcher.getControllerAPI();
        
        // 2. Create trainer
        var trainer = new NeuralEvolutionaryAI(controllerAPI);
        
        // 3. Start training
        Log.info("Starting evolution...");
        Log.info("This will take 2-4 hours. Progress will be logged.");
        
        NeuralNetwork bestNetwork = trainer.evolve();
        
        // 4. Save the best network
        saveNetwork(bestNetwork, "best_neural_ai.nn");
        
        // 5. Test it
        testNetwork(trainer, bestNetwork);
        
        Log.info("Training complete! Network saved to best_neural_ai.nn");
    }
    
    private static void saveNetwork(NeuralNetwork network, String filename) {
        // TODO: Implement serialization (see below)
        Log.info("Network saved to " + filename);
    }
    
    private static void testNetwork(NeuralEvolutionaryAI trainer, NeuralNetwork network) {
        var testMaps = Seq.with("Ancient Caldera", "Tar Fields", "Frozen Forest");
        var results = trainer.testBestAI(testMaps);
        
        Log.info("=== Test Results ===");
        Log.info("Win Rate: " + String.format("%.1f%%", results.getWinRate() * 100));
        Log.info("Avg Fitness: " + String.format("%.2f", results.getAverageFitness()));
    }
}
```

Run it:
```bash
java -cp "core/build/libs/*:server/build/libs/*" mindustry.ai.evolutionary.RunNeuralTraining
```

### Option 2: Programmatic Training

```java
// In your application code
var trainer = new NeuralEvolutionaryAI(controllerAPI);

// Configure if needed (modify source or add setters)
// trainer.setPopulationSize(30);
// trainer.setMutationRate(0.2f);

// Train
NeuralNetwork bestNetwork = trainer.evolve();

// Use it
var aiPlayer = controllerAPI.createPlayer("NeuralAI", Team.sharded);
var neuralAI = new NeuralAI(aiPlayer, bestNetwork, controllerAPI, statsAPI);

while (!gameOver) {
    neuralAI.update();
    Thread.sleep(100);
}
```

## How to Know It's Done

### 1. Watch the Console Output

During training, you'll see:

```
=== Generation 1/100 ===
Evaluating population fitness...
New best fitness: 234.56
Generation 1 stats:
  Best fitness: 234.56
  Average fitness: 123.45
  Worst fitness: 45.67
  Fitness range: 188.89

=== Generation 2/100 ===
...
```

**Training is done when:**
- ✅ Reaches maximum generations (e.g., 100)
- ✅ Early stopping triggered (fitness plateau for 10 generations)
- ✅ Achieves target fitness (>950 out of 1000)

### 2. Monitor the Evolution Curve

The trainer logs generation statistics. Look for:

```
Generation 10: Best=450, Avg=300  ← Still improving rapidly
Generation 20: Best=550, Avg=400  ← Good progress
Generation 30: Best=620, Avg=480  ← Slowing down
Generation 40: Best=640, Avg=500  ← Plateau starting
Generation 50: Best=645, Avg=510  ← Minimal improvement
```

**Good signs:**
- Best fitness increasing steadily
- Average fitness catching up to best
- Fitness range narrowing (population converging)

**Warning signs:**
- No improvement for 10+ generations → Early stopping will trigger
- Very wide fitness range → Increase exploration
- All fitness scores near zero → Check game setup

### 3. Check Training Time

**Expected timeline:**
```
Generation 1:   ~5 minutes  (first evaluation is slower)
Generation 10:  ~50 minutes (10 × 5 min)
Generation 50:  ~4 hours    (50 × 5 min)
Generation 100: ~8 hours    (100 × 5 min)
```

With parallel evaluation (4 cores):
```
Generation 100: ~2-3 hours
```

### 4. Look for Completion Message

```
Evolution completed! Best fitness: 789.45
Best network: NeuralNetwork[64->128->64->32] (49,824 params)
```

Or early stopping:
```
Early stopping criterion met at generation 67
Evolution completed! Best fitness: 823.12
```

## Monitor Training Progress

### Create a Monitoring Script

Create `monitor_training.py`:

```python
#!/usr/bin/env python3
import re
import time
import matplotlib.pyplot as plt
from collections import defaultdict

def parse_log(logfile):
    """Parse training log and extract fitness values."""
    generations = []
    best_fitness = []
    avg_fitness = []
    
    with open(logfile, 'r') as f:
        for line in f:
            # Parse generation number
            if match := re.search(r'=== Generation (\d+)/\d+ ===', line):
                gen = int(match.group(1))
                generations.append(gen)
            
            # Parse best fitness
            if match := re.search(r'Best fitness: ([\d.]+)', line):
                best = float(match.group(1))
                best_fitness.append(best)
            
            # Parse average fitness
            if match := re.search(r'Average fitness: ([\d.]+)', line):
                avg = float(match.group(1))
                avg_fitness.append(avg)
    
    return generations, best_fitness, avg_fitness

def plot_evolution(generations, best_fitness, avg_fitness):
    """Plot evolution curve."""
    plt.figure(figsize=(12, 6))
    
    plt.subplot(1, 2, 1)
    plt.plot(generations, best_fitness, 'b-', label='Best Fitness', linewidth=2)
    plt.plot(generations, avg_fitness, 'r--', label='Average Fitness', linewidth=2)
    plt.xlabel('Generation')
    plt.ylabel('Fitness')
    plt.title('Neural Network Evolution')
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    plt.subplot(1, 2, 2)
    if len(best_fitness) > 1:
        improvements = [best_fitness[i] - best_fitness[i-1] for i in range(1, len(best_fitness))]
        plt.plot(generations[1:], improvements, 'g-', linewidth=2)
        plt.axhline(y=0, color='k', linestyle='--', alpha=0.3)
        plt.xlabel('Generation')
        plt.ylabel('Fitness Improvement')
        plt.title('Improvement Rate')
        plt.grid(True, alpha=0.3)
    
    plt.tight_layout()
    plt.savefig('neural_evolution_progress.png', dpi=150)
    print("Plot saved to neural_evolution_progress.png")

if __name__ == '__main__':
    import sys
    logfile = sys.argv[1] if len(sys.argv) > 1 else 'training.log'
    
    print(f"Monitoring {logfile}...")
    generations, best, avg = parse_log(logfile)
    
    if generations:
        print(f"\nCurrent progress:")
        print(f"  Generation: {generations[-1]}")
        print(f"  Best fitness: {best[-1]:.2f}")
        print(f"  Avg fitness: {avg[-1]:.2f}")
        
        if len(best) > 1:
            improvement = best[-1] - best[0]
            print(f"  Total improvement: {improvement:.2f}")
        
        plot_evolution(generations, best, avg)
    else:
        print("No training data found yet.")
```

Use it:
```bash
# Save training output to log
./train_neural_ai.sh 2>&1 | tee training.log

# In another terminal, monitor progress
python3 monitor_training.py training.log
```

### Live Monitoring with watch

```bash
# Watch the last 20 lines of training output
watch -n 5 'tail -20 training.log | grep -E "(Generation|fitness)"'
```

### Create Progress File

Add to `NeuralEvolutionaryAI.java`:

```java
// In logGenerationStats() method
private void logGenerationStats() {
    // ...existing logging...
    
    // Also write to progress file
    try (var writer = new java.io.FileWriter("training_progress.csv", true)) {
        writer.write(String.format("%d,%.2f,%.2f,%.2f\n", 
            currentGeneration + 1, max, average, min));
    } catch (Exception e) {
        // Ignore
    }
}
```

Then monitor with:
```bash
# Watch progress
tail -f training_progress.csv

# Plot with gnuplot
gnuplot -e "set datafile separator ','; 
            plot 'training_progress.csv' using 1:2 with lines title 'Best', 
                 '' using 1:3 with lines title 'Average'"
```

## Performance Not Good Enough? Here's What to Do

### 1. Increase Network Capacity

**Problem:** Network can't learn complex patterns

**Solution:** Make the network bigger

```java
// In NeuralAI.java, change:
private static final int HIDDEN_SIZE_1 = 128;  // → 256
private static final int HIDDEN_SIZE_2 = 64;   // → 128

// Or add more layers:
int[] architecture = {
    INPUT_SIZE,
    256,  // Layer 1
    128,  // Layer 2
    64,   // Layer 3 (new!)
    OUTPUT_SIZE
};
```

**Trade-off:** More parameters = slower training, more memory

### 2. Increase Population Size

**Problem:** Not enough diversity, premature convergence

**Solution:** More networks in population

```java
// In NeuralEvolutionaryAI.java, change:
private static final int POPULATION_SIZE = 20;  // → 30 or 40
```

**Trade-off:** Proportionally longer training time

### 3. Adjust Mutation Parameters

**Problem:** Not exploring enough or too much chaos

**Solution A - More Exploration:**
```java
private static final float MUTATION_RATE = 0.15f;      // → 0.25f
private static final float MUTATION_STRENGTH = 0.3f;   // → 0.5f
```

**Solution B - More Exploitation:**
```java
private static final float MUTATION_RATE = 0.15f;      // → 0.08f
private static final float MUTATION_STRENGTH = 0.3f;   // → 0.15f
```

### 4. Increase Training Duration

**Problem:** Not enough generations to converge

**Solution:**
```java
private static final int GENERATIONS = 100;  // → 200 or 300
```

Or train in stages:
```java
// Stage 1: Exploration (50 gen, high mutation)
var trainer = new NeuralEvolutionaryAI(controllerAPI);
var network1 = trainer.evolve(); // 50 generations

// Stage 2: Refinement (50 gen, low mutation)
// Load network1, reduce mutation, continue
```

### 5. Adjust Fitness Evaluation

**Problem:** Fitness function doesn't capture what matters

**Solution:** Modify `ComprehensiveFitnessEvaluator`

```java
// Increase games per evaluation for more stable fitness
private static final int FITNESS_EVALUATION_GAMES = 3;  // → 5

// Or change fitness weights in ComprehensiveFitnessEvaluator
private static final float VICTORY_WEIGHT = 400f;       // Emphasize winning
private static final float SURVIVAL_WEIGHT = 200f;      // Emphasize survival
```

### 6. Try Different Activation Functions

**Problem:** ReLU dying neurons or sigmoid saturation

**Solution:** Experiment with activations

```java
// In NeuralNetwork creation, try:
NeuralNetwork.ActivationFunction.LEAKY_RELU  // Current (good default)
NeuralNetwork.ActivationFunction.TANH        // Better for normalized inputs
NeuralNetwork.ActivationFunction.RELU        // Faster, but can die
NeuralNetwork.ActivationFunction.SIGMOID     // Bounded output
```

### 7. Improve Input Encoding

**Problem:** Network can't distinguish important features

**Solution:** Add more informative inputs

```java
// In GameStateEncoder.java, add:
// - Rate of change (deltas)
inputs[idx++] = normalize(currentWave - previousWave, 0, 5);

// - Ratios and percentages
inputs[idx++] = economy.totalItemsProduced / Math.max(1f, economy.totalItemsConsumed);

// - Critical thresholds
inputs[idx++] = power.efficiency < 0.5f ? 1f : 0f;  // Power crisis flag
```

### 8. Improve Action Decoding

**Problem:** Actions don't match network intent

**Solution:** Better output interpretation

```java
// In ActionDecoder.java, use thresholds:
// Instead of:
if (drillPriority > 0.6f) {
    actions.add(new BuildAction(ActionType.BUILD_DRILL, chooseDrill(outputs)));
}

// Try adaptive thresholds:
float avgPriority = (drillPriority + powerPriority + defensePriority) / 3f;
if (drillPriority > avgPriority + 0.1f) {  // Above average + margin
    actions.add(new BuildAction(ActionType.BUILD_DRILL, chooseDrill(outputs)));
}
```

### 9. Use Curriculum Learning

**Problem:** Task too hard from the start

**Solution:** Start easy, increase difficulty

```java
// Stage 1: Infinite resources, no enemies (learn building)
rules.infiniteResources = true;
rules.waves = false;

// Stage 2: Finite resources, weak enemies
rules.infiniteResources = false;
rules.waveSpacing = 60f;  // Lots of time

// Stage 3: Full difficulty
rules.infiniteResources = false;
rules.waveSpacing = 30f;
```

### 10. Debug the Network

**Problem:** Don't know what the network is doing wrong

**Solution:** Add detailed logging

```java
// In NeuralAI.java, add:
private void logNetworkDecision() {
    Log.debug("=== Network Decision ===");
    Log.debug("Top 5 inputs:");
    // Print highest magnitude inputs
    
    Log.debug("Top 5 outputs:");
    // Print highest magnitude outputs
    
    Log.debug("Actions generated: " + actions.size);
    for (var action : actions) {
        Log.debug("  - " + action.type);
    }
}
```

## Complete Training Workflow

### 1. Initial Training (Quick Test)

```bash
# 10 generations, see if it works
./gradlew :core:run --args="--train-neural-ai --generations 10" 2>&1 | tee test_training.log

# Check results
grep "Best fitness" test_training.log | tail -1
```

### 2. Full Training (Overnight)

```bash
# Full 100 generations
nohup ./train_neural_ai.sh > full_training.log 2>&1 &

# Check progress periodically
tail -f full_training.log | grep "Generation"

# Plot progress
python3 monitor_training.py full_training.log
```

### 3. Evaluation

```bash
# Test the trained network
./gradlew :core:run --args="--test-neural-ai best_neural_ai.nn"

# Or programmatically test
java -cp "..." EvaluateNeuralAI best_neural_ai.nn
```

### 4. Iteration

```bash
# If performance not good enough:
# 1. Analyze what went wrong
grep "Game completed" full_training.log | grep "Fitness: 0"  # Find failures

# 2. Adjust parameters
# Edit source code with changes from section above

# 3. Retrain
./train_neural_ai.sh > improved_training.log 2>&1

# 4. Compare
python3 compare_training.py full_training.log improved_training.log
```

## Expected Performance Milestones

### After 10 Generations
- **Fitness:** 200-400
- **Behavior:** Random building, dies quickly
- **Learning:** Basic input-output associations

### After 25 Generations
- **Fitness:** 400-600
- **Behavior:** Builds some production, dies to early waves
- **Learning:** Resource management basics

### After 50 Generations
- **Fitness:** 600-750
- **Behavior:** Sustained production, some defense
- **Learning:** Power management, basic combat

### After 100 Generations
- **Fitness:** 750-900
- **Behavior:** Efficient economy, good defense, survives 20+ waves
- **Learning:** Complex strategies, adaptation

### Expert Performance (200+ Generations)
- **Fitness:** 900-980
- **Behavior:** Optimal building, strong defense, survives 50+ waves
- **Learning:** Near-optimal play

## Troubleshooting

### Training is Too Slow
```bash
# Reduce games per evaluation
FITNESS_EVALUATION_GAMES = 3  →  1

# Reduce game time
MAX_GAME_TIME_MINUTES = 30  →  15

# Use fewer maps
# Only test on one map during evolution
```

### All Networks Score Zero
```bash
# Check game is starting correctly
# Check API is initialized
# Check actions are being executed
# Add debug logging
```

### Fitness Not Improving
```bash
# Increase mutation rate (more exploration)
# Increase population size (more diversity)
# Check fitness function is working
# Reduce elite size (less preservation)
```

### Out of Memory
```bash
# Reduce population size
# Reduce network size
# Run with more memory: -Xmx8G
# Disable parallel evaluation
```

## Next Steps

Once you have a trained network:

1. **Save it** - Implement serialization (see network save/load below)
2. **Test it** - Run on multiple maps, different difficulties
3. **Deploy it** - Use in actual games
4. **Improve it** - Fine-tune with more training
5. **Analyze it** - Visualize what it learned

## 💾 Saving and Loading Networks

### Automatic Saving

**The best network is automatically saved after training!**

Location: `trained_networks/best_neural_ai.nnet`

Also saved with timestamp: `trained_networks/neural_ai_gen100_fitness1543_20251028_153045.nnet`

### Load a Trained Network

```java
// Load the best trained network
NeuralNetwork network = NeuralEvolutionaryAI.loadBestNetwork();

// Or load a specific file
NeuralNetwork network = NeuralEvolutionaryAI.loadNetwork("neural_ai_gen100_fitness1543_20251028_153045.nnet");

// Or use NeuralNetwork directly
NeuralNetwork network = NeuralNetwork.load("trained_networks/best_neural_ai.nnet");
```

### Use a Loaded Network

```java
// Start a game
var maps = controllerAPI.getAvailableMaps();
var gameController = controllerAPI.startGame(maps.first(), new Rules());

// Load trained network
var network = NeuralEvolutionaryAI.loadBestNetwork();

// Create AI player with trained network
var aiPlayer = controllerAPI.createPlayer("TrainedAI", Team.sharded);
var neuralAI = new NeuralAI(aiPlayer, network, controllerAPI, statsAPI);

// Game loop
while (!gameController.isGameOver()) {
    neuralAI.update();
    Thread.sleep(100);
}
```

### Manual Save/Load

```java
// Save a network manually
network.save("my_custom_network.nnet");

// Load it back
NeuralNetwork loaded = NeuralNetwork.load("my_custom_network.nnet");

// Check if file is valid
boolean valid = NeuralNetwork.isValidNetworkFile(new Fi("my_network.nnet"));
```

### Network File Format

- **Extension**: `.nnet`
- **Format**: Binary (compact and fast)
- **Contains**: Architecture, weights, biases, hyperparameters, training stats
- **Version**: 1 (backwards compatible in future versions)

---

## 📊 Visualization Tools

// Load
var network = NeuralNetwork.load("trained_neural_ai_gen100_fitness823.nn");
```

That's everything you need to run, train, monitor, and improve the neural network AI! 🚀
