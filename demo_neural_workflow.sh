#!/bin/bash
# Demo: Complete Neural Network Training Workflow
# Shows how to train, save, and load neural networks

set -e  # Exit on error

echo "======================================================"
echo "  Neural Network AI - Complete Workflow Demo"
echo "======================================================"
echo ""
echo "This script demonstrates:"
echo "  1. Building the project"
echo "  2. Training a neural network"
echo "  3. Verifying the saved network"
echo "  4. Loading and using the trained network"
echo ""
echo "Note: This is a DEMO with reduced parameters."
echo "      For actual training, use more generations."
echo ""

# Change to project directory
cd /home/v841657/linux_home/Mindustry

# Step 1: Build
echo "======================================================"
echo "Step 1: Building Project"
echo "======================================================"
echo ""

if [ "$1" == "--skip-build" ]; then
    echo "Skipping build (--skip-build flag)"
else
    echo "Running: ./gradlew build"
    ./gradlew build -x test --quiet
    echo "✓ Build complete"
fi
echo ""

# Step 2: Train neural network (quick demo with 5 generations)
echo "======================================================"
echo "Step 2: Training Neural Network (Quick Demo)"
echo "======================================================"
echo ""
echo "Training with reduced parameters for demo:"
echo "  - Population: 5 (normally 20)"
echo "  - Generations: 3 (normally 100)"
echo "  - This will take ~5-10 minutes"
echo ""
echo "For actual training, run:"
echo "  ./gradlew :core:run --args=\"--train-neural-ai --generations 100\""
echo ""

read -p "Start training demo? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Starting training..."
    ./gradlew :core:run --args="--train-neural-ai --population 5 --generations 3"
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✓ Training complete"
    else
        echo ""
        echo "✗ Training failed"
        exit 1
    fi
else
    echo "Skipping training demo"
fi
echo ""

# Step 3: Verify saved network
echo "======================================================"
echo "Step 3: Verifying Saved Network"
echo "======================================================"
echo ""

if [ -f "trained_networks/best_neural_ai.nnet" ]; then
    echo "✓ Network file exists: trained_networks/best_neural_ai.nnet"
    
    size=$(stat -f%z "trained_networks/best_neural_ai.nnet" 2>/dev/null || stat -c%s "trained_networks/best_neural_ai.nnet" 2>/dev/null)
    echo "  File size: $size bytes (~$(($size / 1024)) KB)"
    
    echo ""
    echo "Saved networks:"
    ls -lh trained_networks/*.nnet | awk '{print "  " $9 " (" $5 ")"}'
else
    echo "⚠ No network file found (training was skipped or failed)"
fi
echo ""

# Step 4: Show usage examples
echo "======================================================"
echo "Step 4: How to Use the Trained Network"
echo "======================================================"
echo ""
echo "Java code to load and use the trained network:"
echo ""
cat << 'EOF'
// Load the trained network
NeuralNetwork network = NeuralEvolutionaryAI.loadBestNetwork();

// Or load a specific version
NeuralNetwork network = NeuralNetwork.load(
    "trained_networks/neural_ai_gen100_fitness1543.nnet"
);

// Start a game
var gameController = controllerAPI.startGame(mapName, rules);

// Create AI player with trained network
var aiPlayer = controllerAPI.createPlayer("TrainedAI", Team.sharded);
var neuralAI = new NeuralAI(aiPlayer, network, controllerAPI, statsAPI);

// Game loop
while (!gameController.isGameOver()) {
    neuralAI.update();  // AI makes decisions
    Thread.sleep(100);
}
EOF
echo ""

# Summary
echo "======================================================"
echo "Summary"
echo "======================================================"
echo ""
echo "✓ Complete workflow demonstrated"
echo ""
echo "Next steps:"
echo "  1. Full training:  ./gradlew :core:run --args=\"--train-neural-ai --generations 100\""
echo "  2. Custom params:  ./gradlew :core:run --args=\"--train-neural-ai --population 30 --generations 150\""
echo "  3. Load network:   NeuralEvolutionaryAI.loadBestNetwork()"
echo "  4. Use in game:    new NeuralAI(player, network, api, stats)"
echo ""
echo "Documentation:"
echo "  - NEURAL_TRAINING_GUIDE.md - Complete training guide"
echo "  - NEURAL_EVOLUTION_README.md - System architecture"
echo "  - NEURAL_IMPLEMENTATION_COMPLETE.md - Implementation summary"
echo ""
echo "======================================================"
echo "Demo complete!"
echo "======================================================"
