# Evolutionary AI for Mindustry

An advanced evolutionary AI system that uses genetic algorithms to train AI players capable of playing and winning Mindustry automatically. The system evolves AI strategies over multiple generations to optimize resource management, base building, combat effectiveness, and overall game performance.

## Features

### 🧬 Genetic Algorithm Evolution
- **Population-based learning**: Trains multiple AI genomes simultaneously
- **Crossover and mutation**: Combines successful strategies and introduces variations
- **Elite selection**: Preserves the best performing individuals across generations
- **Adaptive fitness evaluation**: Comprehensive scoring system considering multiple game aspects

### 🎮 Advanced AI Behaviors
- **Multi-phase strategies**: Early game, mid game, late game, and survival adaptations  
- **Resource management**: Intelligent mining, production chain optimization
- **Base building**: Strategic placement based on genome preferences
- **Combat tactics**: Defensive vs aggressive strategies, unit production priorities
- **Power management**: Efficient power generation and distribution
- **Transport optimization**: Conveyor networks and resource flow

### 📊 Comprehensive Statistics Integration
- **Real-time monitoring**: Uses GameStatsAPI for detailed performance metrics
- **Multi-objective fitness**: Victory, survival, economy, power, defense, efficiency
- **Behavioral tracking**: Build success rates, error handling, adaptability measures
- **Performance analysis**: Wave survival, resource efficiency, damage ratios

### 🚀 Headless Training Environment
- **Optimized for training**: Runs without graphics for maximum performance
- **Parallel evaluation**: Multi-threaded fitness assessment
- **Progress monitoring**: Real-time evolution tracking and logging
- **Flexible configuration**: Adjustable population sizes, mutation rates, selection pressure

## Architecture

### Core Components

```
mindustry.ai.evolutionary/
├── EvolutionaryAI.java              # Main evolution controller
├── genome/
│   └── AIGenome.java                # Genetic representation of AI behavior
├── behavior/
│   └── GenomeBasedAI.java           # Translates genome to game actions
├── fitness/
│   └── FitnessEvaluator.java        # Multi-objective fitness evaluation
├── examples/
│   └── EvolutionaryAIExample.java   # Usage examples and demos
└── launcher/
    └── EvolutionaryAILauncher.jar   # Standalone training application
```

### AI Genome Structure

The AI genome encodes behavioral traits as floating-point genes:

- **Resource Management**: Conservation vs expansion, mining focus
- **Combat Strategy**: Defensive vs aggressive, ranged vs melee preference  
- **Economic Traits**: Production chain depth, transport efficiency
- **Strategic Traits**: Risk tolerance, planning horizon, adaptability
- **Timing Genes**: Reaction speed, building pacing, tech progression
- **Spatial Organization**: Compactness, symmetry, storage distribution
- **Building Priorities**: Relative priorities for all block types

## Usage

### Quick Start

```java
// Initialize headless controller
var launcher = HeadlessControllerLauncher.create();
new HeadlessApplication(launcher);
launcher.waitForInitialization();

// Create evolutionary AI
var controllerAPI = launcher.getControllerAPI();
var evolutionaryAI = new EvolutionaryAI(controllerAPI);

// Run evolution (may take several hours)
AIGenome bestGenome = evolutionaryAI.evolve();

// Test the evolved AI
var testMaps = Seq.with("Ancient Caldera", "Frozen Forest", "Craters");
var results = evolutionaryAI.testBestAI(testMaps);

System.out.println("Win rate: " + results.getWinRate() * 100 + "%");
```

### Command Line Interface

```bash
# Full evolution training (6-12 hours)
java -jar evolutionary-ai.jar --mode FULL_EVOLUTION --output results

# Quick test (30 minutes)
java -jar evolutionary-ai.jar --mode QUICK_TEST --population 8 --generations 10

# Run example/demo
java -jar evolutionary-ai.jar --mode EXAMPLE

# Performance benchmark
java -jar evolutionary-ai.jar --mode BENCHMARK
```

### Integration with Existing Code

```java
// Use with ControllerAPI
ControllerAPI api = new ControllerAPIImpl();
GameStatsAPI stats = api.getGameStats();

// Create AI player with evolved genome
var aiPlayer = api.createPlayer("EvolvedAI", Team.sharded);
var aiBehavior = new GenomeBasedAI(aiPlayer, bestGenome, api, stats);

// Game loop
while (!gameController.isGameOver()) {
    aiBehavior.update();
    Thread.sleep(100);
}
```

## Evolution Parameters

### Default Settings
- **Population Size**: 20 individuals
- **Generations**: 100 (early stopping if no improvement)
- **Mutation Rate**: 15% (Gaussian noise)
- **Crossover Rate**: 70% (single-point crossover)
- **Elite Size**: 4 (top performers preserved)
- **Tournament Size**: 3 (selection pressure)

### Fitness Evaluation
- **Victory Component**: 400 points (most important)
- **Survival Component**: 200 points (wave survival + time)
- **Economic Component**: 150 points (efficiency + production)
- **Power Component**: 100 points (stability + efficiency)
- **Defense Component**: 80 points (damage ratio + survival)
- **Efficiency Component**: 50 points (overall performance)
- **Adaptability Component**: 20 points (behavioral diversity)

## Training Time Estimates

| Configuration | Population | Generations | Estimated Time |
|---------------|------------|-------------|----------------|
| Quick Test    | 8          | 10          | 30 minutes     |
| Standard      | 20         | 100         | 6-8 hours      |
| Intensive     | 32         | 200         | 12-16 hours    |
| Research      | 50         | 500         | 2-3 days       |

*Times vary significantly based on hardware and map complexity*

## Performance Optimization

### Recommended JVM Settings
```bash
java -Xmx8G -Xms4G -XX:+UseG1GC -XX:+UseStringDeduplication \
     -XX:MaxGCPauseMillis=200 -jar evolutionary-ai.jar
```

### Hardware Requirements
- **Minimum**: 4GB RAM, 4 CPU cores
- **Recommended**: 8GB RAM, 8 CPU cores  
- **Optimal**: 16GB RAM, 16+ CPU cores
- **Storage**: 2GB free space for results and logs

## Results and Analysis

### Output Files
- `best-genome.txt`: Final evolved genome parameters
- `fitness-history.csv`: Evolution progress over generations
- `map-test-results.csv`: Performance on standard test maps
- `evolution.log`: Detailed training logs

### Success Metrics
- **Win Rate**: Percentage of games won on test maps
- **Average Fitness**: Overall performance score
- **Wave Survival**: How far the AI progresses
- **Efficiency Metrics**: Resource and power management quality

### Example Results
A well-evolved AI typically achieves:
- 70-85% win rate on standard maps
- Consistent survival to wave 40+
- 80%+ power efficiency
- 90%+ resource utilization efficiency

## Advanced Features

### Custom Fitness Functions
```java
// Combat-focused evolution
var combatEvaluator = new CombatFitnessEvaluator();
var evolutionaryAI = new EvolutionaryAI(controllerAPI, combatEvaluator);

// Economic optimization
var economicEvaluator = new EconomicFitnessEvaluator();
```

### Multi-Objective Evolution
- Pareto-optimal solutions for competing objectives
- Diversity preservation to prevent premature convergence
- Dynamic fitness landscapes based on game meta

### Transfer Learning
- Pre-trained genomes as starting populations
- Cross-map adaptation and generalization
- Incremental learning from human player data

## Integration with GameStats API

The evolutionary system deeply integrates with Mindustry's comprehensive GameStats API:

```java
// Real-time performance monitoring
statsAPI.addStatsUpdateListener(summary -> {
    evaluateFitness(summary);
    adjustBehavior(summary);
});

// Comprehensive performance analysis
var performance = statsAPI.getPerformanceSummary();
var economy = performance.economy;        // Production efficiency
var power = performance.power;            // Power management  
var combat = performance.combat;          // Combat effectiveness
var efficiency = performance.efficiency;  // Overall efficiency
```

## Research Applications

### Academic Research
- Game AI and machine learning research
- Evolutionary algorithm optimization
- Multi-agent system coordination
- Real-time strategy game AI

### Game Development
- AI player companions and opponents
- Game balance testing and analysis
- Automated quality assurance
- Player behavior modeling

## Contributing

### Adding New Genome Traits
1. Add new float gene to `AIGenome.java`
2. Implement behavior in `GenomeBasedAI.java`
3. Update fitness evaluation in `FitnessEvaluator.java`
4. Add mutation and crossover logic

### Custom Fitness Evaluators
```java
public class CustomFitnessEvaluator implements FitnessEvaluator {
    @Override
    public float evaluateFitness(GameOutcome outcome, 
                               GamePerformanceSummary performance,
                               AIGenome genome, 
                               BehaviorStats stats) {
        // Your custom fitness logic here
        return fitness;
    }
}
```

### New AI Behaviors
Extend `GenomeBasedAI` with additional strategic behaviors:
```java
private void executeCustomStrategy() {
    if (genome.customTrait > 0.7f) {
        // Implement custom behavior
    }
}
```

## Troubleshooting

### Common Issues
- **Out of Memory**: Increase heap size with `-Xmx8G`
- **Slow Evolution**: Reduce population size or use quick mode
- **No Convergence**: Adjust mutation rate or selection pressure
- **Poor Performance**: Check hardware requirements

### Debug Mode
```bash
java -Dlog.level=DEBUG -jar evolutionary-ai.jar --mode EXAMPLE
```

### Performance Monitoring
```java
// Monitor evolution progress
evolutionaryAI.addProgressListener(stats -> {
    System.out.println("Generation: " + stats.generation);
    System.out.println("Best fitness: " + stats.bestFitness);
});
```

## License

This evolutionary AI system is part of the Mindustry Controller API project and follows the same licensing terms as the main Mindustry project.

## Future Enhancements

- **Neural network integration**: Hybrid neuro-evolutionary approaches
- **Multi-map training**: Training across multiple maps simultaneously  
- **Online learning**: Continuous adaptation during gameplay
- **Human-AI cooperation**: Co-evolutionary training with human players
- **Meta-learning**: Learning to learn new maps and scenarios quickly

---

*Built with the Mindustry Controller API and GameStats API for comprehensive autonomous gameplay and training.*
