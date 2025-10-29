#!/bin/bash
# Test script for neural network save/load functionality

echo "=== Testing Neural Network Save/Load ==="
echo ""

# Create test Java file
cat > TestNeuralSaveLoad.java << 'EOF'
import mindustry.ai.evolutionary.neural.*;
import arc.files.*;
import arc.util.*;

public class TestNeuralSaveLoad {
    public static void main(String[] args) {
        try {
            Log.logger = (level, text) -> System.out.println("[" + level + "] " + text);
            
            System.out.println("=== Testing Neural Network Serialization ===");
            System.out.println();
            
            // Create a test network
            int[] architecture = {64, 128, 64, 32};
            var network = new NeuralNetwork(architecture, 0.01f, NeuralNetwork.ActivationFunction.LEAKY_RELU);
            
            System.out.println("1. Created network: " + network.toString());
            System.out.println("   Parameters: " + network.getParameterCount());
            System.out.println();
            
            // Test forward pass with random inputs
            float[] testInputs = new float[64];
            for (int i = 0; i < testInputs.length; i++) {
                testInputs[i] = (float) Math.random();
            }
            
            float[] originalOutputs = network.forward(testInputs);
            System.out.println("2. Computed forward pass");
            System.out.println("   Sample outputs: " + 
                String.format("%.4f, %.4f, %.4f", 
                    originalOutputs[0], originalOutputs[1], originalOutputs[2]));
            System.out.println();
            
            // Save the network
            var testFile = new Fi("test_network_save.nnet");
            network.save(testFile);
            System.out.println("3. Saved network to: " + testFile.absolutePath());
            System.out.println("   File size: " + testFile.length() + " bytes");
            System.out.println();
            
            // Verify file is valid
            boolean valid = NeuralNetwork.isValidNetworkFile(testFile);
            System.out.println("4. File validation: " + (valid ? "PASSED" : "FAILED"));
            System.out.println();
            
            // Load the network
            var loadedNetwork = NeuralNetwork.load(testFile);
            System.out.println("5. Loaded network: " + loadedNetwork.toString());
            System.out.println();
            
            // Test that loaded network produces same outputs
            float[] loadedOutputs = loadedNetwork.forward(testInputs);
            System.out.println("6. Computed forward pass on loaded network");
            System.out.println("   Sample outputs: " + 
                String.format("%.4f, %.4f, %.4f", 
                    loadedOutputs[0], loadedOutputs[1], loadedOutputs[2]));
            System.out.println();
            
            // Compare outputs
            boolean outputsMatch = true;
            float maxDiff = 0f;
            for (int i = 0; i < originalOutputs.length; i++) {
                float diff = Math.abs(originalOutputs[i] - loadedOutputs[i]);
                maxDiff = Math.max(maxDiff, diff);
                if (diff > 1e-6) {
                    outputsMatch = false;
                }
            }
            
            System.out.println("7. Output comparison:");
            System.out.println("   Match: " + (outputsMatch ? "YES" : "NO"));
            System.out.println("   Max difference: " + maxDiff);
            System.out.println();
            
            // Clean up
            testFile.delete();
            System.out.println("8. Cleaned up test file");
            System.out.println();
            
            // Final result
            if (outputsMatch) {
                System.out.println("✓ ALL TESTS PASSED");
                System.out.println("  - Network creation: OK");
                System.out.println("  - Forward pass: OK");
                System.out.println("  - Save: OK");
                System.out.println("  - Load: OK");
                System.out.println("  - Output consistency: OK");
                System.exit(0);
            } else {
                System.out.println("✗ TEST FAILED: Outputs don't match!");
                System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("✗ TEST FAILED WITH EXCEPTION:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
EOF

# Compile and run
echo "Compiling test..."
javac -cp "core/build/libs/*:desktop/build/libs/*" TestNeuralSaveLoad.java

if [ $? -eq 0 ]; then
    echo "Running test..."
    echo ""
    java -cp ".:core/build/libs/*:desktop/build/libs/*" TestNeuralSaveLoad
    exit_code=$?
    
    echo ""
    if [ $exit_code -eq 0 ]; then
        echo "=== Test completed successfully ==="
    else
        echo "=== Test failed ==="
    fi
    
    # Clean up
    rm -f TestNeuralSaveLoad.java TestNeuralSaveLoad.class
    exit $exit_code
else
    echo "Compilation failed!"
    rm -f TestNeuralSaveLoad.java
    exit 1
fi
