package mlipa.move.client;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class ArtificialNeuralNetwork {
    private SharedPreferences preferences;

    private Integer layersNumber;
    private Integer inputDivisor;
    public static Double learningConstant;

    private ArrayList<ArrayList<Neuron>> layers = new ArrayList<>();

    public ArtificialNeuralNetwork(Context context, Integer inputNeurons, Integer hiddenLayers, Integer hiddenNeurons, Integer outputNeurons) {
        preferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_settings), Context.MODE_PRIVATE);

        layersNumber = hiddenLayers + 2;
        inputDivisor = preferences.getInt(context.getString(R.string.shared_preferences_settings_input_divisor), Constants.DEFAULT_INPUT_DIVISOR);
        learningConstant = Double.longBitsToDouble(preferences.getLong(context.getString(R.string.shared_preferences_settings_learning_constant), Double.doubleToLongBits(Constants.DEFAULT_LEARNING_CONSTANT)));

        for (int i = 0; i < layersNumber; i++) {
            ArrayList<Neuron> layer = new ArrayList<>();

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

        for (int i = 0; i < layersNumber - 1; i++) {
            for (int j = 0; j < layers.get(i).size(); j++) {
                for (int k = 0; k < layers.get(i + 1).size(); k++) {
                    NeuronsConnection connection = new NeuronsConnection(layers.get(i).get(j), layers.get(i + 1).get(k));

                    layers.get(i).get(j).next.add(connection);
                    layers.get(i + 1).get(k).previous.add(connection);
                }
            }
        }
    }

    public void learn(ArrayList<Double> input, ArrayList<Double> target) {
        Integer inputLayer = 0;

        for (int i = 0; i < layers.get(inputLayer).size(); i++) {
            layers.get(inputLayer).get(i).output = input.get(i) / inputDivisor;
        }

        for (int i = 1; i < layers.size(); i++) {
            for (int j = 0; j < layers.get(i).size(); j++) {
                layers.get(i).get(j).calculateOutput();
            }
        }

        Integer outputLayer = layers.size() - 1;

        for (int i = 0; i < layers.get(outputLayer).size(); i++) {
            layers.get(outputLayer).get(i).updateDelta(layers.get(outputLayer).get(i).output - target.get(i));
        }

        for (int i = layers.size() - 2; i > inputLayer; i--) {
            for (int j = 0; j < layers.get(i).size(); j++) {
                Double error = 0.0;

                for (int k = 0; k < layers.get(i).get(j).next.size(); k++) {
                    error += (layers.get(i).get(j).next.get(k).tail.delta * layers.get(i).get(j).next.get(k).weight);
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

    public ArrayList<Double> run(ArrayList<Double> input) {
        Integer inputLayer = 0;
        Integer outputLayer = layers.size() - 1;

        ArrayList<Double> output = new ArrayList<>(layers.get(outputLayer).size());

        for (int i = 0; i < layers.get(inputLayer).size(); i++) {
            layers.get(inputLayer).get(i).output = input.get(i) / inputDivisor;
        }

        for (int i = 1; i < layers.size(); i++) {
            for (int j = 0; j < layers.get(i).size(); j++) {
                layers.get(i).get(j).calculateOutput();
            }
        }

        for (int i = 0; i < layers.get(outputLayer).size(); i++) {
            output.add(layers.get(outputLayer).get(i).output);
        }

        return output;
    }
}
