import pandas as pd
import tensorflow as tf

# -------------------------------
# Neuron Network Shape
INPUT_NEURONS = 2
HIDDEN_LAYERS = [1, 2, 3]
HIDDEN_NEURONS = 10
ACTIVATION = 'relu'
OUTPUT_NEURONS = 2

# Compiler parameters
COMPILER = 'adam'
LOSS = 'mean_squared_error'
METRICS = ['mean_squared_error']

# Learning parameters
EPOCHS = 50
BATCH_SIZE = 64
# -------------------------------

# Read the data
# Training
training_data = pd.read_csv('./combined_stat.csv', sep=';', header=None, names=['read_x', 'read_y', 'real_x', 'real_y'])
training_input = training_data[['read_x', "read_y"]].values
training_output = training_data[['real_x', "real_y"]].values

# Testing
test_data = pd.read_csv('./combined_dyn.csv', sep=';', header=None, names=['read_x', 'read_y', 'real_x', 'real_y'])
test_input = test_data[['read_x', "read_y"]].values
test_output = test_data[['real_x', "real_y"]].values


# Create the model
def create_model(input_neurons, hidden_layers, hidden_neurons, activation, output_neurons):
    # Initialize the model
    model = tf.keras.Sequential()

    # Input layer
    model.add(tf.keras.layers.InputLayer(shape=(input_neurons,)))

    # Hidden layers
    for i in range(hidden_layers):
        model.add(tf.keras.layers.Dense(hidden_neurons, activation=activation))

    # Output layer
    model.add(tf.keras.layers.Dense(output_neurons))

    return model


neural_network = create_model(INPUT_NEURONS, HIDDEN_LAYERS[0], HIDDEN_NEURONS, ACTIVATION, OUTPUT_NEURONS)

# Compile the neural_network
neural_network.compile(optimizer=COMPILER, loss=LOSS, metrics=METRICS)

# Training the neural_network
history = neural_network.fit(training_input, training_output,
                             epochs=EPOCHS,
                             batch_size=BATCH_SIZE,
                             validation_data=(test_input, test_output))

print(history.history['loss'])

# Evaluate the neural_network
neural_network.evaluate(test_input, test_output)
weights = neural_network.layers[1].get_weights()[0]
print(weights)

