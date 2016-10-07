package mlipa.move.client;

public class Constants {
    public static final Double ACCELERATION_ALPHA = 0.8;

    public static final Integer ACTIVITY_LIE_ID = 1;
    public static final Integer ACTIVITY_SIT_ID = 2;
    public static final Integer ACTIVITY_STAND_ID = 3;
    public static final Integer ACTIVITY_WALK_ID = 4;
    public static final Integer ACTIVITY_RUN_ID = 5;
    public static final Integer ACTIVITY_CYCLING_ID = 6;
    public static final Integer ACTIVITY_NOT_DETECTED_ID = 7;

    public static final Integer USER_NOT_DETECTED_ID = -1;

    public static final Double DEFAULT_CHRONOMETER_TIME = 120.0;
    public static final Double CHRONOMETER_TIME_MIN = 30.0;
    public static final Double CHRONOMETER_TIME_MAX = 120.0;

    public static final Double DEFAULT_DELAY_TIME = 5.0;
    public static final Double DELAY_TIME_MIN = 3.0;
    public static final Double DELAY_TIME_MAX = 10.0;

    public static final Double DEFAULT_WINDOW_LENGTH = 2.5;
    public static final Double WINDOW_LENGTH_MIN = 1.0;
    public static final Double WINDOW_LENGTH_MAX = 3.0;

    public static final String DEFAULT_CLASSIFIER_ID = "1";

    public static final Integer DEFAULT_INPUT_NEURONS = 8;
    public static final Integer INPUT_NEURONS_MIN = 8;
    public static final Integer INPUT_NEURONS_MAX = 24;

    public static final Integer DEFAULT_HIDDEN_LAYERS = 1;
    public static final Integer HIDDEN_LAYERS_MIN = 1;
    public static final Integer HIDDEN_LAYER_MAX = 2;

    public static final Integer DEFAULT_HIDDEN_NEURONS = 8;
    public static final Integer HIDDEN_NEURONS_MIN = 8;
    public static final Integer HIDDEN_NEURONS_MAX = 24;

    public static final Integer OUTPUT_NEURONS = 6;

    public static final Integer DEFAULT_INPUT_DIVISOR = 10;
    public static final Integer INPUT_DIVISOR_MIN = 10;
    public static final Integer INPUT_DIVISOR_MAX = 100;

    public static final Double DEFAULT_LEARNING_CONSTANT = 0.7;
    public static final Double LEARNING_CONSTANT_MIN = 0.1;
    public static final Double LEARNING_CONSTANT_MAX = 1.0;

    public static final Integer DEFAULT_LEARNING_ITERATIONS = 100000;
    public static final Integer LEARNING_ITERATIONS_MIN = 10000;
    public static final Integer LEARNING_ITERATIONS_MAX = 1000000;

    public static final Integer TEST_ITERATIONS = 1000;

    public static final Double DEFAULT_DESIRED_ACTIVITY_WEIGHT = 1.0;
    public static final Double DESIRED_ACTIVITY_WEIGHT_MIN = 0.0;
    public static final Double DESIRED_ACTIVITY_WEIGHT_MAX = 1.0;

    public static final Double DEFAULT_UNDESIRED_ACTIVITY_WEIGHT = 0.0;
    public static final Double UNDESIRED_ACTIVITY_WEIGHT_MIN = 0.0;
    public static final Double UNDESIRED_ACTIVITY_WEIGHT_MAX = 1.0;
}
