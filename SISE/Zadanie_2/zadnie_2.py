import numpy as np
import pandas as pd
import tensorflow as tf

import plots
import save_results


class NN_Parameters:
    def __init__(self, num_hidden_layers, hidden_neurons, weights_initialization, learning_rate, num_epochs):
        self.num_hidden_layers = num_hidden_layers
        self.hidden_neurons = hidden_neurons
        self.weights_initialization = weights_initialization
        self.learning_rate = learning_rate
        self.num_epochs = num_epochs


class NN_Result:
    def __init__(self, train_mse, test_mse, predictions):
        self.train_mse = train_mse
        self.test_mse = test_mse
        self.predictions = predictions


class NeuralNetwork:
    def __init__(self, network, parameters, results):
        self.network = network
        self.parameters = parameters
        self.results = results


# ----------------------------------------------------------------------------------------------------------------------

# Read setup file
def read_model_parameters(file_path):
    # Mapping initializers
    init_mapping = {
        'ru': tf.keras.initializers.RandomUniform(minval=-1, maxval=1),
        'rn': tf.keras.initializers.RandomNormal(mean=0.0, stddev=0.05),
        'hn': tf.keras.initializers.HeNormal()
    }

    with open(file_path, 'r') as file:
        lines = file.readlines()

    # Remove comments and empty lines
    lines = [line.strip() for line in lines if line.strip() and not line.startswith('#')]

    # Extract the number of networks
    num_networks = int(lines[0])

    # Create a list to hold the parameters for each network
    networks_params = []

    # Read parameters for each network
    current_line = 1
    for _ in range(num_networks):
        num_hidden_layers = int(lines[current_line])
        hidden_neurons = list(map(int, lines[current_line + 1].split()))
        weights_initialization = init_mapping[lines[current_line + 2]]
        learning_rate = float(lines[current_line + 3])
        num_epochs = int(lines[current_line + 4])

        # Create an instance of NN_Parameters and add it to the list
        networks_params.append(
            NN_Parameters(num_hidden_layers, hidden_neurons, weights_initialization, learning_rate, num_epochs))

        # Move to the next set of parameters
        current_line += 5

    return networks_params


# Create the model
def create_model(input_neurons, hidden_layers, hidden_neurons, activation, output_neurons):
    # Initialize the model
    model = tf.keras.Sequential()

    # Input layer
    model.add(tf.keras.layers.InputLayer(shape=(input_neurons,), kernel_initializer=WEIGHTS_INITIALIZATION))

    # Hidden layers
    for i in range(hidden_layers):
        model.add(
            tf.keras.layers.Dense(hidden_neurons[i], activation=activation, kernel_initializer=WEIGHTS_INITIALIZATION))

    # Output layer
    model.add(tf.keras.layers.Dense(output_neurons))

    return model


# Train and test the model
def execute_model(neural_network, training_in, training_out, testing_in, testing_out):
    # Convert data to tensors
    training_in = tf.convert_to_tensor(training_in.astype(np.float32))
    training_out = tf.convert_to_tensor(training_out.astype(np.float32))
    testing_in = tf.convert_to_tensor(testing_in.astype(np.float32))
    testing_out = tf.convert_to_tensor(testing_out.astype(np.float32))

    # Compile
    neural_network.compile(loss=LOSS, optimizer=OPTIMIZER, metrics=METRICS)

    train_mse_per_epoch = []
    test_mse_per_epoch = []

    for epoch in range(EPOCHS):
        print('Epoch {}/{}'.format(epoch + 1, EPOCHS))

        # Shuffle the training data
        indices = tf.random.shuffle(tf.range(training_in.shape[0]))
        training_in = tf.gather(training_in, indices)
        training_out = tf.gather(training_out, indices)

        # Train
        neural_network.fit(training_in, training_out, epochs=1, verbose=1)

        # Evaluate on training data
        train_loss, training_mse = neural_network.evaluate(training_in, training_out, verbose=1)
        train_mse_per_epoch.append(training_mse)

        # Evaluate on testing data
        test_loss, testing_mse = neural_network.evaluate(testing_in, testing_out, verbose=1)
        test_mse_per_epoch.append(testing_mse)

    # Predict after all epochs
    predictions = neural_network.predict(testing_in, verbose=1)

    return train_mse_per_epoch, test_mse_per_epoch, predictions


# ----------------------------------------------------------------------------------------------------------------------
# Setup file pattern
#
# Number of networks
# ------
# Number of hidden layers
# Number of neurons in a hidden level (each in separated by space)
# Weights initialization (ru - RandomUniform, rn - RandomNormal, hn - HeNormal)
# Learning rate
# Number of epochs
# ------

# Read the data
# Training
training_data = pd.read_csv('./combined_stat.csv', sep=';', header=None, names=['read_x', 'read_y', 'real_x', 'real_y'])
train_input = training_data[['read_x', "read_y"]].values
train_output = training_data[['real_x', "real_y"]].values

# Testing
test_data = pd.read_csv('./combined_dyn.csv', sep=';', header=None, names=['read_x', 'read_y', 'real_x', 'real_y'])
test_input = test_data[['read_x', "read_y"]].values
test_output = test_data[['real_x', "real_y"]].values

# -------------------------------
# Every Neuron Network Shape
INPUT_NEURONS = 2
ACTIVATION = 'relu'
OUTPUT_NEURONS = 2

# Compiler parameters
LOSS = 'mean_squared_error'
METRICS = ['mean_squared_error']

# -------------------------------

all_networks_params = read_model_parameters("model_setup.txt")
all_models = []

for param in all_networks_params:
    current_index = all_networks_params.index(param)

    HIDDEN_LAYERS = param.num_hidden_layers
    HIDDEN_NEURONS = param.hidden_neurons
    WEIGHTS_INITIALIZATION = param.weights_initialization
    LEARNING_RATE = param.learning_rate
    EPOCHS = param.num_epochs
    OPTIMIZER = tf.keras.optimizers.Adam(learning_rate=LEARNING_RATE)

    # Create
    model = create_model(INPUT_NEURONS, HIDDEN_LAYERS, HIDDEN_NEURONS, ACTIVATION, OUTPUT_NEURONS)

    # train_mse, test_mse, predictions = execute_model(model, train_input, train_output, test_input, test_output)
    # save_results.save_results_to_file(train_mse, test_mse, predictions, f"data_{current_index + 1}.pkl")
    train_mse, test_mse, predictions = save_results.read_results_from_file(f"data_{current_index + 1}.pkl")
    result = NN_Result(train_mse, test_mse, predictions)

    all_models.append(NeuralNetwork(model, param, result))


# Plots
#
# 1 & 2
train_mse_list = [nn.results.train_mse for nn in all_models]
test_mse_list = [nn.results.test_mse for nn in all_models]
labels = [f'Network {i+1}' for i in range(len(all_models))]
plots.plot_mse(train_mse_list, test_mse_list, test_input, test_output, EPOCHS, labels)
#
# 3
# Calculate errors for each network
errors = []
labels = []
colors = ['blue', 'red', 'green']  # Add more colors if needed

for nn in all_models:
    error = np.mean((nn.results.predictions - test_output) ** 2, axis=1)
    errors.append(error)
    labels.append(f'Network {all_models.index(nn) + 1}')

# Calculate baseline errors
baseline_errors = np.mean((test_input - test_output) ** 2, axis=1)
errors.append(baseline_errors)
labels.append('Baseline')
colors.append('black')

# Plot the CDF of errors
plots.plot_distribution(errors, labels, colors)

# 4
# Plot corrected measurements for the best performing network
best_network = max(all_models, key=lambda x: min(x.results.test_mse))
best_predictions = best_network.results.predictions.astype("float64")
plots.plot_corrected_measurements(test_output, test_input, best_predictions)
