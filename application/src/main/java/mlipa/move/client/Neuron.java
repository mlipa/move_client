package mlipa.move.client;

import java.util.ArrayList;
import java.util.Random;

public class Neuron {
    private double bias;
    public double delta;
    public double output;

    public ArrayList<NeuronConnection> previous = new ArrayList<NeuronConnection>();
    public ArrayList<NeuronConnection> next = new ArrayList<NeuronConnection>();

    public Neuron() {
        Random r = new Random();

        bias = r.nextDouble();
    }

    public void calculateOutput() {
        double sum = bias;

        for (int i = 0; i < previous.size(); i++) {
            sum += previous.get(i).source.output * previous.get(i).weight;
        }

        output = 1 / (1 + Math.exp(-1 * sum));
    }

    public void updateDelta(double error) {
        delta = error * output * (1 - output);
    }

    public void updateBiasAndWeights() {
        bias -= NeuralNetwork.eta * delta;

        for (int i = 0; i < previous.size(); i++) {
            previous.get(i).weight -= NeuralNetwork.eta * delta * previous.get(i).source.output;
        }
    }
}
