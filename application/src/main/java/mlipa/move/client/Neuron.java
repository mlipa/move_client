package mlipa.move.client;

import java.util.ArrayList;
import java.util.Random;

public class Neuron {
    private Double bias;
    public Double delta;
    public Double output;

    public ArrayList<NeuronConnection> previous = new ArrayList<NeuronConnection>();
    public ArrayList<NeuronConnection> next = new ArrayList<NeuronConnection>();

    public Neuron() {
        Random r = new Random();

        bias = r.nextDouble();
    }

    public void calculateOutput() {
        Double sum = bias;

        for (int i = 0; i < previous.size(); i++) {
            sum += previous.get(i).source.output * previous.get(i).weight;
        }

        output = 1 / (1 + Math.exp(-1 * sum));
    }

    public void updateDelta(Double error) {
        delta = error * output * (1 - output);
    }

    public void updateBiasAndWeights() {
        bias -= NeuralNetwork.ETA * delta;

        for (int i = 0; i < previous.size(); i++) {
            previous.get(i).weight -= NeuralNetwork.ETA * delta * previous.get(i).source.output;
        }
    }
}
