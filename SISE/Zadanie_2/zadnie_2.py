import numpy as np
import pandas as pd
import tensorflow as tf
import matplotlib.pyplot as plt

# Setup file pattern
#
# Number of hidden layers
# Number of neurons in a hidden level (each in separated by space)
# Number of epochs

# -------------------------------
# Neuron Network Shape
INPUT_NEURONS = 2
ACTIVATION = 'relu'
OUTPUT_NEURONS = 2

# Compiler parameters
OPTIMIZER = 'adam'
LOSS = 'mean_squared_error'
METRICS = ['mean_squared_error']


# -------------------------------
# Read setup file
def read_model_parameters(file_path):
    with open(file_path, 'r') as file:
        lines = file.readlines()

    # Remove comments and empty lines
    lines = [line.strip() for line in lines if line.strip() and not line.startswith('#')]

    # Extract parameters
    num_hidden_layers = int(lines[0])
    hidden_neurons = list(map(int, lines[1].split()))
    num_epochs = int(lines[2])

    return num_hidden_layers, hidden_neurons, num_epochs


# Create the model
def create_model(input_neurons, hidden_layers, hidden_neurons, activation, output_neurons):
    # Initialize the model
    model = tf.keras.Sequential()

    # Input layer
    model.add(tf.keras.layers.InputLayer(shape=(input_neurons,)))

    # Hidden layers
    for i in range(hidden_layers):
        model.add(tf.keras.layers.Dense(hidden_neurons[i], activation=activation))

    # Output layer
    model.add(tf.keras.layers.Dense(output_neurons))

    return model


# Train and test the model
def execute_model(neural_network, training_in, training_out, test_in, test_out):
    # Compile
    neural_network.compile(loss=LOSS, optimizer=OPTIMIZER, metrics=METRICS)

    train_mse_per_epoch = []
    test_mse_per_epoch = []

    for epoch in range(EPOCHS):
        print('Epoch {}/{}'.format(epoch + 1, EPOCHS))
        # Shuffle the training data
        indices = np.arange(training_in.shape[0])
        np.random.shuffle(indices)
        training_in = training_in[indices]
        training_out = training_out[indices]

        # Train
        neural_network.fit(training_in, training_out, epochs=1, verbose=1)

        # Evaluate on training data
        train_loss, train_mse = neural_network.evaluate(training_in, training_out, verbose=1)
        train_mse_per_epoch.append(train_mse)

        # Evaluate on testing data
        test_loss, test_mse = neural_network.evaluate(test_in, test_out, verbose=1)
        test_mse_per_epoch.append(test_mse)

    # Predict after all epochs
    predictions = neural_network.predict(test_in, verbose=1)

    return train_mse_per_epoch, test_mse_per_epoch, predictions


def plot_mse(data, variant, color):
    epochs = range(1, len(data) + 1)

    plt.figure(figsize=(10, 5))

    # Plotting MSE
    plt.plot(epochs, data, label=f'{variant} MSE', color=color)

    # Adding labels and title
    plt.xlabel('Epochs')
    plt.ylabel('MSE')
    plt.title(f'{variant} MSE per Epoch')
    plt.legend()

    # Display the plot
    plt.show()


# ----------------------------------------------------------------------------------------------------------------------

# Read the data
# Training
training_data = pd.read_csv('./combined_stat.csv', sep=';', header=None, names=['read_x', 'read_y', 'real_x', 'real_y'])
training_input = training_data[['read_x', "read_y"]].values
training_output = training_data[['real_x', "real_y"]].values

# Testing
test_data = pd.read_csv('./combined_dyn.csv', sep=';', header=None, names=['read_x', 'read_y', 'real_x', 'real_y'])
test_input = test_data[['read_x', "read_y"]].values
test_output = test_data[['real_x', "real_y"]].values

# Network shape
HIDDEN_LAYERS, HIDDEN_NEURONS, EPOCHS = read_model_parameters("model_setup.txt")

# Create
model = create_model(INPUT_NEURONS, HIDDEN_LAYERS, HIDDEN_NEURONS, ACTIVATION, OUTPUT_NEURONS)

train_mse, test_mse, predictions = execute_model(model, training_input, training_output, test_input, test_output)
plot_mse(train_mse, "Training", "blue")
plot_mse(test_mse, "Testing", "red")
