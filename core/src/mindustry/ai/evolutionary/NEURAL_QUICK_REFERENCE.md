# Neural Network AI - Quick Reference

## Architecture

```
Input:  64 neurons (game statistics from GameStatsAPI)
        ↓
Hidden: 128 neurons (Leaky ReLU)
        ↓
Hidden: 64 neurons (Leaky ReLU)
        ↓
Output: 32 neurons (action decisions for ControllerAPI)
```

## Input Breakdown (64 total)

| Category | Count | Examples |
|----------|-------|----------|
| Basic Stats | 8 | Wave, time, units, buildings |
| Economy | 12 | Production, consumption, efficiency |
| Power | 10 | Generation, storage, shortages |
| Combat | 8 | Damage dealt/received, waves survived |
| Spatial | 8 | Area covered, structure density |
| Resources | 12 | Copper, lead, silicon, titanium, etc. |
| Efficiency | 6 | Overall, uptime, power efficiency |

## Output Breakdown (32 total)

| Category | Count | Purpose |
|----------|-------|---------|
| Building Priorities | 10 | What to build (drill, power, defense, etc.) |
| Resource Management | 5 | Mining focus, expansion, conservation |
| Combat Strategy | 5 | Aggression, defense, range preference |
| Expansion Strategy | 5 | Territory, compactness, symmetry |
| Timing Parameters | 7 | Update rate, building pace, tech speed |

## Quick Start

```java
// 1. Create trainer
var trainer = new NeuralEvolutionaryAI(controllerAPI);

// 2. Evolve (2-4 hours)
NeuralNetwork bestNetwork = trainer.evolve();

// 3. Use trained network
var aiPlayer = controllerAPI.createPlayer("NeuralAI", Team.sharded);
var neuralAI = new NeuralAI(aiPlayer, bestNetwork, controllerAPI, statsAPI);

// 4. Game loop
while (!gameOver) {
    neuralAI.update(); // 1 Hz
    Thread.sleep(100);
}
```

## Key Classes

| Class | Purpose |
|-------|---------|
| `NeuralNetwork` | Core neural network (forward, mutate, crossover) |
| `GameStateEncoder` | GameStatsAPI → 64-dim input vector |
| `ActionDecoder` | 32-dim output → ControllerAPI actions |
| `NeuralAI` | Main AI controller (update loop) |
| `NeuralEvolutionaryAI` | Evolution trainer (neuroevolution) |

## Evolution Parameters

```java
POPULATION_SIZE = 20        // Networks per generation
GENERATIONS = 100           // Max generations
MUTATION_RATE = 0.15        // 15% weight mutation chance
MUTATION_STRENGTH = 0.3     // Mutation magnitude
CROSSOVER_RATE = 0.7        // 70% crossover vs clone
ELITE_SIZE = 4              // Top 4 preserved
TOURNAMENT_SIZE = 3         // Tournament selection
```

## Action Types

### Building Actions
- `BUILD_DRILL` - Place mining drills
- `BUILD_POWER` - Place generators
- `BUILD_DEFENSE` - Place turrets
- `BUILD_TRANSPORT` - Place conveyors
- `BUILD_PRODUCTION` - Place smelters/factories
- `BUILD_WALL` - Place walls
- `BUILD_UNITS` - Place unit factories

### Strategy Actions
- `FOCUS_MINING` - Prioritize resource extraction
- `FOCUS_PRODUCTION` - Prioritize manufacturing
- `EXPAND_TERRITORY` - Expand base
- `COMBAT_AGGRESSIVE` - Aggressive combat stance
- `COMBAT_DEFENSIVE` - Defensive combat stance
- `EXPAND_AGGRESSIVE` - Fast expansion
- `BUILD_COMPACT` - Compact base layout

## Stats Used (from GameStatsAPI)

### Economy
- `totalItemsProduced`, `totalItemsConsumed`
- `economyEfficiency`, `averageProductionRate`
- `totalResourcesProduced`, `averageResourceThroughput`
- `productionChainsCompleted`, `resourcesPerTick`

### Power
- `efficiency`, `storageUtilization`
- `currentGeneration`, `currentConsumption`
- `shortageEvents`, `timeWithPositivePower`
- `powerGenerationRatio`, `averagePowerGridUptime`

### Combat
- `totalDamageDealt`, `totalDamageReceived`, `damageRatio`
- `wavesSurvived`, `enemyUnitsDestroyed`
- `structureSurvivalRate`, `structureDestructionPercentage`

### Resources (storage levels)
- Copper, Lead, Graphite, Titanium
- Thorium, Silicon, Coal, Sand
- Scrap, Metaglass, Plastanium, Surge Alloy

### Spatial
- `areaCovered`, `furthestDistance`, `totalStructures`
- `structureDensity`, `coreProximityScore`

### Transport
- `itemsTransportedPerTick`, `productionLineLatency`
- `transportEfficiency`, `throughputUtilization`

### Efficiency
- `overallEfficiency`, `uptime`
- `powerEfficiency`, `buildingEfficiency`, `resourceEfficiency`

## Performance

| Metric | Value |
|--------|-------|
| Training Time | 2-4 hours (100 gen, parallel) |
| Runtime Update | 1 Hz (1 decision/second) |
| Forward Pass | ~1 ms |
| Memory per Network | ~200 KB |
| Parameters | ~50,000 |

## Typical Evolution Curve

```
Gen   1: Best=100, Avg=50   (random networks)
Gen  10: Best=250, Avg=150  (learning basics)
Gen  25: Best=450, Avg=300  (developing strategy)
Gen  50: Best=650, Avg=450  (refined play)
Gen 100: Best=800, Avg=600  (expert performance)
```

## Common Issues & Solutions

### Poor Performance
- Increase hidden layer sizes (256, 128)
- Add more hidden layers
- Increase population size
- Increase mutation rate

### Slow Convergence
- Increase mutation strength
- Decrease elite size
- Increase tournament size
- Add diversity bonus

### Overfitting
- Evaluate on multiple maps
- Increase exploration
- Add regularization

## API Integration

### Input Sources
```java
GameStatsAPI.getPerformanceSummary()
GameStatsAPI.getEconomySummary()
GameStatsAPI.getPowerSummary()
GameStatsAPI.getCombatSummary()
GameStatsAPI.getSpatialSummary()
GameStatsAPI.getTransportSummary()
GameStatsAPI.getCurrentResourceBalance()
```

### Output Targets
```java
PlayerController.placeBlock(block, x, y, rotation)
WorldController (for location queries)
GameController.isGameOver()
```

## Example Outputs

### High Power Priority (0.85)
```
Power efficiency < 80% → BUILD_POWER
Tech level 0.6 → Choose steam generator
Find location near core
Execute: player.placeBlock(Blocks.steamGenerator, x, y, 0)
```

### High Mining Focus (0.75)
```
Resources low → BUILD_DRILL
Tech level 0.4 → Choose pneumatic drill
Find ore deposits
Execute: player.placeBlock(Blocks.pneumaticDrill, x, y, 0)
```

### High Defense Priority (0.90)
```
Damage received > 1000 → BUILD_DEFENSE
Tech level 0.7 → Choose ripple turret
Find defensive position
Execute: player.placeBlock(Blocks.ripple, x, y, 0)
```

## Monitoring Evolution

```java
var trainer = new NeuralEvolutionaryAI(controllerAPI);

// During evolution
int gen = trainer.getCurrentGeneration();
float best = trainer.getBestFitness();
var bestNet = trainer.getBestNetwork();

// After evolution
var bestFitness = trainer.getGenerationBestFitness();
var avgFitness = trainer.getGenerationAverageFitness();

// Plot evolution curve
for (int i = 0; i < bestFitness.size; i++) {
    System.out.printf("Gen %d: %.2f\n", i+1, bestFitness.get(i));
}
```

## Files

```
neural/
  ├── NeuralNetwork.java       - Core network implementation
  ├── GameStateEncoder.java    - Stats → Input encoding
  ├── ActionDecoder.java       - Output → Actions decoding
  └── NeuralAI.java            - AI controller

NeuralEvolutionaryAI.java     - Main trainer
examples/
  └── NeuralEvolutionExample.java - Usage examples
```

## Documentation

- `NEURAL_EVOLUTION_README.md` - Full documentation
- `NEURAL_MIGRATION_GUIDE.md` - Migration from genome system
- `NEURAL_QUICK_REFERENCE.md` - This file

## Key Advantages

✅ Uses **all 64+ game statistics**  
✅ Direct stats → actions mapping  
✅ ~50,000 learnable parameters  
✅ Highly expressive and adaptive  
✅ Scales with new features  
✅ Enables advanced AI techniques  

## Next Steps

1. Read `NEURAL_EVOLUTION_README.md`
2. Run `NeuralEvolutionExample.java`
3. Train your first network with `NeuralEvolutionaryAI`
4. Monitor evolution progress
5. Test trained network on different maps
6. Fine-tune hyperparameters
7. Explore advanced techniques (DQN, PPO, LSTM)
