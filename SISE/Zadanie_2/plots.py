import matplotlib.pyplot as plt
import numpy as np
import tensorflow as tf


def plot_mse(train_mse_list, test_mse_list, test_input, test_output, epochs, labels):
    train_epochs = range(1, len(train_mse_list[0]) + 1)
    test_epochs = range(1, len(test_mse_list[0]) + 1)

    reference_mse = tf.keras.losses.MeanSquaredError()(test_output, test_input).numpy()

    max_mse = max(max(max(train_mse_list)), max(max(test_mse_list)), reference_mse)
    min_mse = min(min(min(train_mse_list)), min(min(test_mse_list)), reference_mse)

    padding = 0.1 * (max_mse - min_mse)  # Adjust the padding factor as needed
    y_min = min_mse - padding
    y_max = max_mse + padding

    # Plotting Training MSE
    plt.figure(figsize=(10, 5))
    for i, train_mse in enumerate(train_mse_list):
        plt.plot(train_epochs, train_mse, label=f'Training MSE ({labels[i]})')

    plt.ylim(y_min, y_max)
    plt.xlabel('Epochs')
    plt.ylabel('MSE')
    plt.title(f'Training MSE per Epoch')
    plt.legend()
    plt.grid()
    plt.show()

    # Plotting Testing MSE
    plt.figure(figsize=(10, 5))
    for i, test_mse in enumerate(test_mse_list):
        plt.plot(test_epochs, test_mse, label=f'Testing MSE ({labels[i]})')

    plt.hlines(reference_mse, xmin=0, xmax=epochs, linestyle='--',
               color='black', label='Constant Line')
    plt.ylim(y_min, y_max)
    plt.xlabel('Epochs')
    plt.ylabel('MSE')
    plt.title(f'Testing MSE per Epoch')
    plt.legend()
    plt.grid()
    plt.show()


def plot_distribution(errors, labels, colors):
    plt.figure(figsize=(10, 5))

    for error, label, color in zip(errors, labels, colors):
        sorted_errors = np.sort(error)
        cdf = np.arange(len(sorted_errors)) / float(len(sorted_errors))
        if color == 'black':
            plt.plot(sorted_errors, cdf, label=label, color=color, linestyle="--")
        else:
            plt.plot(sorted_errors, cdf, label=label, color=color)

    plt.xlabel('Error')
    plt.ylabel('CDF')
    plt.title('CDF of Errors for Different Network Variants')
    plt.legend()
    plt.show()


def plot_corrected_measurements(actual_values, measured_values, corrected_values):
    plt.figure(figsize=(10, 5))

    plt.scatter(actual_values[:, 0], actual_values[:, 1], color='blue', label='Actual value', zorder=4)
    plt.scatter(corrected_values[:, 0], corrected_values[:, 1], color='red', label='Corrected values', zorder=3)
    plt.scatter(measured_values[:, 0], measured_values[:, 1], color='green', label='Measured values', zorder=2)

    plt.title("Comparison of actual, measured and corrected values")
    plt.xlabel("x [mm]")
    plt.ylabel("y [mm]")
    plt.legend()
    plt.grid(True)
    plt.show()
