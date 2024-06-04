import numpy as np
import pandas as pd
import tensorflow as tf

import plots


# Setup file pattern "model_setup.txt"
#
# Number of networks
# ------
# Number of hidden layers
# Number of neurons in a hidden level (each in separated by space)
# Weights initialization (ru - RandomUniform, rn - RandomNormal, hn - HeNormal)
# Learning rate
# Number of epochs
# ------
# ----------------------------------------------------------------------------------------------------------------------
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
# Prepare the data
def load_and_prepare_data(static_data_path, dynamic_data_path):
    # Load and prepare static (training) data
    static_data = pd.read_csv(static_data_path, header=None, na_values=['']).dropna()
    training_input = static_data.iloc[:, [0, 1]].reset_index(drop=True)
    training_output = static_data.iloc[:, [2, 3]].reset_index(drop=True)

    # Load and prepare dynamic (testing) data
    dynamic_data = pd.read_csv(dynamic_data_path, header=None, na_values=['']).dropna()
    testing_input = dynamic_data.iloc[:, [0, 1]].reset_index(drop=True)
    testing_output = dynamic_data.iloc[:, [2, 3]].reset_index(drop=True)

    return training_input.to_numpy(), training_output.to_numpy(), testing_input.to_numpy(), testing_output.to_numpy()


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


# ----------------------------------------------------------------------------------------------------------------------

# Create the model
def create_model(input_neurons, hidden_layers, hidden_neurons, activation, output_neurons):
    # Initialize the model
    model = tf.keras.Sequential()

    # Input layer
    model.add(tf.keras.layers.InputLayer(shape=(input_neurons,)))

    # Hidden layers
    for i in range(hidden_layers):
        model.add(
            tf.keras.layers.Dense(hidden_neurons[i], activation=activation, kernel_initializer=WEIGHTS_INITIALIZATION))

    # Output layer
    model.add(tf.keras.layers.Dense(output_neurons, activation=tf.keras.activations.linear))

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
def find_highest_num_hidden_layers(neural_networks):
    highest_hidden_layers = 0

    for nn in neural_networks:
        if nn.parameters.num_hidden_layers > highest_hidden_layers:
            highest_hidden_layers = nn.parameters.num_hidden_layers

    return highest_hidden_layers


def select_best_network(all_models, num_hidden_layers):
    # Filter networks with the specified number of hidden layers
    filtered_networks = [nn for nn in all_models if nn.parameters.num_hidden_layers == num_hidden_layers]

    # If no networks found with the specified number of hidden layers, return None
    if not filtered_networks:
        return None

    # Find the network with the lowest test MSE
    best_network = min(filtered_networks, key=lambda nn: min(nn.results.test_mse))

    return best_network


def print_networks(networks):
    for i, nn in enumerate(networks):
        if nn is None:
            continue
        print(
            "---------------------------------------------------\n"
            f"Hidden Layers: {nn.parameters.num_hidden_layers}\n"
            f"Hidden Neurons: {nn.parameters.hidden_neurons}\n"
            f"Weights Initialization: {nn.parameters.weights_initialization.__class__.__name__}\n"
            f"Learning Rate: {nn.parameters.learning_rate}\n"
            f"Epochs: {nn.parameters.num_epochs}\n"
            "---------------------------------------------------\n")


# ----------------------------------------------------------------------------------------------------------------------
# Read the data
#
# Train data
static_data = 'train_data.csv'
# Test data
dynamic_data = 'test_data.csv'

train_input, train_output, test_input, test_output = load_and_prepare_data(static_data, dynamic_data)

# -------------------------------
# Every Neuron Network Shape
INPUT_NEURONS = 2
ACTIVATION = 'relu'
OUTPUT_NEURONS = 2

# Compiler parameters
LOSS = 'mean_squared_error'
METRICS = ['mean_squared_error']

# -------------------------------

# Read remaining parameters
all_networks_params = read_model_parameters("model_setup.txt")

all_models = []

# Create, train and evaluate every network
for param in all_networks_params:
    current_index = all_networks_params.index(param)

    print(f"Network {current_index + 1}")

    HIDDEN_LAYERS = param.num_hidden_layers
    HIDDEN_NEURONS = param.hidden_neurons
    WEIGHTS_INITIALIZATION = param.weights_initialization
    LEARNING_RATE = param.learning_rate
    EPOCHS = param.num_epochs
    OPTIMIZER = tf.keras.optimizers.Adam(learning_rate=LEARNING_RATE)

    # Create
    model = create_model(INPUT_NEURONS, HIDDEN_LAYERS, HIDDEN_NEURONS, ACTIVATION, OUTPUT_NEURONS)

    # Train and predict
    train_mse, test_mse, predictions = execute_model(model, train_input, train_output, test_input, test_output)
    result = NN_Result(train_mse, test_mse, predictions)

    all_models.append(NeuralNetwork(model, param, result))

# Select 3 best models
best_models = []

for i in range(find_highest_num_hidden_layers(all_models)):
    best_models.append(select_best_network(all_models, i + 1))

print_networks(best_models)

print("Best Network: \n")
best_network = min(best_models, key=lambda x: min(x.results.test_mse))
print_networks([best_network])
