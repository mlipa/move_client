package mlipa.move.client;

import java.util.Random;

public class NeuronConnection {
    public Neuron source;
    public Neuron destination;

    public double weight;

    public NeuronConnection(Neuron newSource, Neuron newDestination) {
        Random r = new Random();

        source = newSource;
        destination = newDestination;

        weight = r.nextDouble();
    }
}
