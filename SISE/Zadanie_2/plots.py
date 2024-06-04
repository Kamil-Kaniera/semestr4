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
    plt.rcParams.update({'font.size': 14})
    for i, train_mse in enumerate(train_mse_list):
        plt.plot(train_epochs, train_mse, label=labels[i])

    plt.ylim(15000, 70000)
    plt.xlim(0.5, epochs)
    plt.xlabel('Liczba epok')
    plt.ylabel('MSE')
    plt.title('MSE - dane treningowe')
    plt.legend()
    plt.grid()
    plt.savefig('train_mse.png', bbox_inches='tight')
    plt.show()

    # Plotting Testing MSE
    plt.figure(figsize=(10, 5))
    plt.rcParams.update({'font.size': 14})
    for i, test_mse in enumerate(test_mse_list):
        plt.plot(test_epochs, test_mse, label=labels[i])

    plt.hlines(reference_mse, xmin=0, xmax=epochs, linestyle='--',
               color='black', label='Błąd dla danych testowych')
    plt.ylim(15000, 70000)
    plt.xlim(0.5, epochs)
    plt.xlabel('Liczba epok')
    plt.ylabel('MSE')
    plt.title('MSE - dane testowe')
    plt.legend()
    plt.grid()
    plt.savefig('test_mse.png', bbox_inches='tight')
    plt.show()


def plot_distribution(errors, labels):
    plt.figure(figsize=(10, 5))
    plt.rcParams.update({'font.size': 14})

    colors = ['tab:blue', 'tab:orange', 'tab:green', 'black']

    all_errors_combined = np.concatenate(errors)
    sorted_all_errors_combined = np.sort(all_errors_combined)

    for error, label, color in zip(errors, labels, colors):
        sorted_errors = np.sort(error)
        cdf = np.arange(len(sorted_errors)) / float(len(sorted_errors))
        if color == 'black':
            plt.plot(sorted_errors, cdf, label=label, color=color, linestyle="--")
        else:
            plt.plot(sorted_errors, cdf, label=label, color=color)

    plt.xlabel('Błąd')
    plt.ylabel('Prawdopodobieństwo skumulowane')
    plt.title('Dystrybuanty błędu')
    plt.legend(prop={'size': 10})

    # Set logarithmic scale and x-axis limits
    plt.xscale('log')
    # plt.xlim(1000, 100000)

    plt.grid(True)
    plt.savefig('distribution.png', bbox_inches='tight')
    plt.show()


def plot_corrected_measurements(actual_values, measured_values, corrected_values):
    plt.figure(figsize=(10, 5))
    plt.rcParams.update({'font.size': 14})

    plt.scatter(actual_values[:, 0], actual_values[:, 1], color='blue', label='Wartości rzeczywiste', zorder=4)
    plt.scatter(corrected_values[:, 0], corrected_values[:, 1], color='red', label='Wartości skorygowane', zorder=3)
    plt.scatter(measured_values[:, 0], measured_values[:, 1], color='green', label='Wartości zmierzone', zorder=2)

    plt.title("Skorygowane wartości wyników")
    plt.xlabel("x [mm]")
    plt.ylabel("y [mm]")
    plt.legend(prop={'size': 10})
    plt.grid(True)
    plt.savefig('corrected_measurements.png', bbox_inches='tight')
    plt.show()
