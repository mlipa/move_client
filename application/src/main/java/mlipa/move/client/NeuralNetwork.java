package mlipa.move.client;

import java.util.ArrayList;

public class NeuralNetwork {
    private int layersNumber;

    private ArrayList<ArrayList<Neuron>> layers = new ArrayList<ArrayList<Neuron>>();

    public static ArrayList<NeuronConnection> connections = new ArrayList<NeuronConnection>();

    public static final double eta = 0.5;

    public NeuralNetwork(int inputNeurons, int hiddenLayers, int hiddenNeurons, int outputNeurons) {
        layersNumber = hiddenLayers + 2;

        for (int i = 0; i < layersNumber; i++) {
            ArrayList<Neuron> layer = new ArrayList<Neuron>();

            if (i == 0) {
                for (int j = 0; j < inputNeurons; j++) {
                    layer.add(new Neuron());
                }
            } else if (i == layersNumber - 1) {
                for (int j = 0; j < outputNeurons; j++) {
                    layer.add(new Neuron());
                }
            } else {
                for (int j = 0; j < hiddenNeurons; j++) {
                    layer.add(new Neuron());
                }
            }

            layers.add(layer);
        }

        createConnections();
    }

    private void createConnections() {
        for (int i = 0; i < layersNumber - 1; i++) {
            for (int j = 0; j < layers.get(i).size(); j++) {
                for (int k = 0; k < layers.get(i + 1).size(); k++) {
                    NeuronConnection connection = new NeuronConnection(layers.get(i).get(j), layers.get(i + 1).get(k));

                    connections.add(connection);

                    layers.get(i).get(j).next.add(connection);
                    layers.get(i + 1).get(k).previous.add(connection);
                }
            }
        }
    }

    public void trainNetwork(double[] input, double[] target) {
        int inputLayer = 0;

        for (int i = 0; i < layers.get(inputLayer).size(); i++) {
            layers.get(inputLayer).get(i).output = input[i] / 1000;
        }

        for (int i = 1; i < layers.size(); i++) {
            for (int j = 0; j < layers.get(i).size(); j++) {
                layers.get(i).get(j).calculateOutput();
            }
        }

        int outputLayer = layers.size() - 1;

        for (int i = 0; i < layers.get(outputLayer).size(); i++) {
            layers.get(outputLayer).get(i).updateDelta(layers.get(outputLayer).get(i).output - target[i]);
        }

        for (int i = layers.size() - 2; i > inputLayer; i--) {
            for (int j = 0; j < layers.get(i).size(); j++) {
                double error = 0.0;

                for (int k = 0; k < layers.get(i).get(j).next.size(); k++) {
                    error += (layers.get(i).get(j).next.get(k).destination.delta * layers.get(i).get(j).next.get(k).weight);
                }

                layers.get(i).get(j).updateDelta(error);
            }
        }

        for (int i = 1; i < layers.size(); i++) {
            for (int j = 0; j < layers.get(i).size(); j++) {
                layers.get(i).get(j).updateBiasAndWeights();
            }
        }
    }

    public double[] runNetwork(double[] input) {
        int inputLayer = 0;
        int outputLayer = layers.size() - 1;

        double[] output = new double[layers.get(outputLayer).size()];

        for (int i = 0; i < layers.get(inputLayer).size(); i++) {
            layers.get(inputLayer).get(i).output = input[i] / 1000;
        }

        for (int i = 1; i < layers.size(); i++) {
            for (int j = 0; j < layers.get(i).size(); j++) {
                layers.get(i).get(j).calculateOutput();
            }
        }

        for (int i = 0; i < layers.get(outputLayer).size(); i++) {
            output[i] = layers.get(outputLayer).get(i).output;
        }

        return output;
    }
}
