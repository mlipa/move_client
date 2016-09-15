package mlipa.move.client;

import java.util.ArrayList;
import java.util.Random;

public class Neuron {
    private Double bias;
    public Double output;
    public Double delta;

    public ArrayList<NeuronsConnection> previous = new ArrayList<>();
    public ArrayList<NeuronsConnection> next = new ArrayList<>();

    public Neuron() {
        Random random = new Random();

        bias = random.nextDouble();
    }

    public void calculateOutput() {
        Double sum = bias;

        for (int i = 0; i < previous.size(); i++) {
            sum += previous.get(i).head.output * previous.get(i).weight;
        }

        output = 1 / (1 + Math.exp(-1 * sum));
    }

    public void updateDelta(Double error) {
        delta = error * output * (1 - output);
    }

    public void updateBiasAndWeights() {
        bias -= ArtificialNeuralNetwork.learningConstant * delta;

        for (int i = 0; i < previous.size(); i++) {
            previous.get(i).weight -= ArtificialNeuralNetwork.learningConstant * delta * previous.get(i).head.output;
        }
    }
}
