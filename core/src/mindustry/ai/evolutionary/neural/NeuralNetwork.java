package mindustry.ai.evolutionary.neural;

import arc.struct.*;
import arc.util.*;
import arc.files.*;
import java.util.*;
import java.io.*;

/**
 * Simple feedforward neural network for AI decision making.
 * Uses backpropagation for training and neuroevolution for optimization.
 */
public class NeuralNetwork {
    
    private final int[] layerSizes;
    private final float[][][] weights; // [layer][neuron][input]
    private final float[][] biases;    // [layer][neuron]
    private final float[][] activations; // [layer][neuron]
    
    // Training parameters
    private final float learningRate;
    private final ActivationFunction activationFunction;
    
    // Performance tracking
    private float totalError = 0f;
    private int trainingSteps = 0;
    
    public NeuralNetwork(int[] layerSizes, float learningRate, ActivationFunction activationFunction) {
        this.layerSizes = layerSizes;
        this.learningRate = learningRate;
        this.activationFunction = activationFunction;
        
        // Initialize weights and biases
        weights = new float[layerSizes.length - 1][][];
        biases = new float[layerSizes.length - 1][];
        activations = new float[layerSizes.length][];
        
        for (int layer = 0; layer < layerSizes.length - 1; layer++) {
            int inputSize = layerSizes[layer];
            int outputSize = layerSizes[layer + 1];
            
            weights[layer] = new float[outputSize][inputSize];
            biases[layer] = new float[outputSize];
            
            // Xavier/He initialization
            float std = (float) Math.sqrt(2.0 / inputSize);
            for (int i = 0; i < outputSize; i++) {
                for (int j = 0; j < inputSize; j++) {
                    weights[layer][i][j] = (float) (Math.random() * 2 - 1) * std;
                }
                biases[layer][i] = 0f;
            }
        }
        
        // Initialize activation arrays
        for (int i = 0; i < layerSizes.length; i++) {
            activations[i] = new float[layerSizes[i]];
        }
    }
    
    /**
     * Forward pass through the network.
     */
    public float[] forward(float[] inputs) {
        if (inputs.length != layerSizes[0]) {
            throw new IllegalArgumentException("Input size mismatch: expected " + layerSizes[0] + ", got " + inputs.length);
        }
        
        // Set input layer
        System.arraycopy(inputs, 0, activations[0], 0, inputs.length);
        
        // Forward propagation through hidden and output layers
        for (int layer = 0; layer < weights.length; layer++) {
            for (int neuron = 0; neuron < weights[layer].length; neuron++) {
                float sum = biases[layer][neuron];
                
                for (int input = 0; input < weights[layer][neuron].length; input++) {
                    sum += activations[layer][input] * weights[layer][neuron][input];
                }
                
                // Apply activation function
                activations[layer + 1][neuron] = activationFunction.apply(sum);
            }
        }
        
        // Return output layer
        return activations[activations.length - 1].clone();
    }
    
    /**
     * Train using backpropagation with given inputs and expected outputs.
     */
    public void train(float[] inputs, float[] expectedOutputs) {
        // Forward pass
        float[] outputs = forward(inputs);
        
        // Calculate error
        float error = 0f;
        for (int i = 0; i < outputs.length; i++) {
            float diff = expectedOutputs[i] - outputs[i];
            error += diff * diff;
        }
        totalError += error;
        trainingSteps++;
        
        // Backward pass (simplified - would need full backprop for production)
        // For now, we'll rely on neuroevolution instead
    }
    
    /**
     * Mutate this network for evolutionary training.
     */
    public NeuralNetwork mutate(float mutationRate, float mutationStrength) {
        var mutated = this.copy();
        
        // Mutate weights
        for (int layer = 0; layer < weights.length; layer++) {
            for (int i = 0; i < weights[layer].length; i++) {
                for (int j = 0; j < weights[layer][i].length; j++) {
                    if (Math.random() < mutationRate) {
                        mutated.weights[layer][i][j] += (float) (Math.random() * 2 - 1) * mutationStrength;
                    }
                }
            }
        }
        
        // Mutate biases
        for (int layer = 0; layer < biases.length; layer++) {
            for (int i = 0; i < biases[layer].length; i++) {
                if (Math.random() < mutationRate) {
                    mutated.biases[layer][i] += (float) (Math.random() * 2 - 1) * mutationStrength;
                }
            }
        }
        
        return mutated;
    }
    
    /**
     * Crossover with another network.
     */
    public static NeuralNetwork crossover(NeuralNetwork parent1, NeuralNetwork parent2) {
        if (!Arrays.equals(parent1.layerSizes, parent2.layerSizes)) {
            throw new IllegalArgumentException("Networks must have same architecture for crossover");
        }
        
        var child = parent1.copy();
        
        // Uniform crossover for weights
        for (int layer = 0; layer < child.weights.length; layer++) {
            for (int i = 0; i < child.weights[layer].length; i++) {
                for (int j = 0; j < child.weights[layer][i].length; j++) {
                    if (Math.random() < 0.5) {
                        child.weights[layer][i][j] = parent2.weights[layer][i][j];
                    }
                }
            }
        }
        
        // Uniform crossover for biases
        for (int layer = 0; layer < child.biases.length; layer++) {
            for (int i = 0; i < child.biases[layer].length; i++) {
                if (Math.random() < 0.5) {
                    child.biases[layer][i] = parent2.biases[layer][i];
                }
            }
        }
        
        return child;
    }
    
    /**
     * Deep copy of this network.
     */
    public NeuralNetwork copy() {
        var copy = new NeuralNetwork(layerSizes.clone(), learningRate, activationFunction);
        
        // Copy weights
        for (int layer = 0; layer < weights.length; layer++) {
            for (int i = 0; i < weights[layer].length; i++) {
                System.arraycopy(weights[layer][i], 0, copy.weights[layer][i], 0, weights[layer][i].length);
            }
        }
        
        // Copy biases
        for (int layer = 0; layer < biases.length; layer++) {
            System.arraycopy(biases[layer], 0, copy.biases[layer], 0, biases[layer].length);
        }
        
        return copy;
    }
    
    /**
     * Get average training error.
     */
    public float getAverageError() {
        return trainingSteps > 0 ? totalError / trainingSteps : 0f;
    }
    
    /**
     * Reset training statistics.
     */
    public void resetStats() {
        totalError = 0f;
        trainingSteps = 0;
    }
    
    /**
     * Get total number of parameters (weights + biases).
     */
    public int getParameterCount() {
        int count = 0;
        for (int layer = 0; layer < weights.length; layer++) {
            count += weights[layer].length * weights[layer][0].length;
            count += biases[layer].length;
        }
        return count;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NeuralNetwork[");
        for (int i = 0; i < layerSizes.length; i++) {
            sb.append(layerSizes[i]);
            if (i < layerSizes.length - 1) sb.append("->");
        }
        sb.append("] (").append(getParameterCount()).append(" params)");
        return sb.toString();
    }
    
    // === Serialization ===
    
    /**
     * Save this network to a file.
     */
    public void save(Fi file) {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(file.write(false)))) {
            // Write header
            out.writeInt(0x4E4E4554); // "NNET" magic number
            out.writeInt(1); // Version
            
            // Write network architecture
            out.writeInt(layerSizes.length);
            for (int size : layerSizes) {
                out.writeInt(size);
            }
            
            // Write hyperparameters
            out.writeFloat(learningRate);
            out.writeInt(activationFunction.ordinal());
            
            // Write weights
            for (int layer = 0; layer < weights.length; layer++) {
                for (int i = 0; i < weights[layer].length; i++) {
                    for (int j = 0; j < weights[layer][i].length; j++) {
                        out.writeFloat(weights[layer][i][j]);
                    }
                }
            }
            
            // Write biases
            for (int layer = 0; layer < biases.length; layer++) {
                for (int i = 0; i < biases[layer].length; i++) {
                    out.writeFloat(biases[layer][i]);
                }
            }
            
            // Write training stats
            out.writeFloat(totalError);
            out.writeInt(trainingSteps);
            
            Log.info("Network saved to: " + file.absolutePath());
        } catch (IOException e) {
            Log.err("Failed to save network", e);
            throw new RuntimeException("Failed to save network to " + file, e);
        }
    }
    
    /**
     * Save this network to a file path.
     */
    public void save(String filePath) {
        save(new Fi(filePath));
    }
    
    /**
     * Load a network from a file.
     */
    public static NeuralNetwork load(Fi file) {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(file.read()))) {
            // Read and verify header
            int magic = in.readInt();
            if (magic != 0x4E4E4554) {
                throw new IOException("Invalid network file: wrong magic number");
            }
            
            int version = in.readInt();
            if (version != 1) {
                throw new IOException("Unsupported network version: " + version);
            }
            
            // Read network architecture
            int numLayers = in.readInt();
            int[] layerSizes = new int[numLayers];
            for (int i = 0; i < numLayers; i++) {
                layerSizes[i] = in.readInt();
            }
            
            // Read hyperparameters
            float learningRate = in.readFloat();
            ActivationFunction activationFunc = ActivationFunction.values()[in.readInt()];
            
            // Create network
            NeuralNetwork network = new NeuralNetwork(layerSizes, learningRate, activationFunc);
            
            // Read weights
            for (int layer = 0; layer < network.weights.length; layer++) {
                for (int i = 0; i < network.weights[layer].length; i++) {
                    for (int j = 0; j < network.weights[layer][i].length; j++) {
                        network.weights[layer][i][j] = in.readFloat();
                    }
                }
            }
            
            // Read biases
            for (int layer = 0; layer < network.biases.length; layer++) {
                for (int i = 0; i < network.biases[layer].length; i++) {
                    network.biases[layer][i] = in.readFloat();
                }
            }
            
            // Read training stats
            network.totalError = in.readFloat();
            network.trainingSteps = in.readInt();
            
            Log.info("Network loaded from: " + file.absolutePath());
            Log.info("Architecture: " + network.toString());
            
            return network;
        } catch (IOException e) {
            Log.err("Failed to load network", e);
            throw new RuntimeException("Failed to load network from " + file, e);
        }
    }
    
    /**
     * Load a network from a file path.
     */
    public static NeuralNetwork load(String filePath) {
        return load(new Fi(filePath));
    }
    
    /**
     * Check if a file is a valid network file.
     */
    public static boolean isValidNetworkFile(Fi file) {
        if (!file.exists()) return false;
        
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(file.read()))) {
            int magic = in.readInt();
            int version = in.readInt();
            return magic == 0x4E4E4554 && version == 1;
        } catch (Exception e) {
            return false;
        }
    }
    
    // === Activation Functions ===
    
    public enum ActivationFunction {
        SIGMOID {
            @Override
            public float apply(float x) {
                return (float) (1.0 / (1.0 + Math.exp(-x)));
            }
        },
        TANH {
            @Override
            public float apply(float x) {
                return (float) Math.tanh(x);
            }
        },
        RELU {
            @Override
            public float apply(float x) {
                return Math.max(0, x);
            }
        },
        LEAKY_RELU {
            @Override
            public float apply(float x) {
                return x > 0 ? x : 0.01f * x;
            }
        };
        
        public abstract float apply(float x);
    }
}
