package mlipa.move.client;

import java.util.Random;

public class NeuronsConnection {
    public Neuron head;
    public Neuron tail;

    public Double weight;

    public NeuronsConnection(Neuron newHead, Neuron newTail) {
        head = newHead;
        tail = newTail;

        Random random = new Random();

        weight = random.nextDouble();
    }
}
