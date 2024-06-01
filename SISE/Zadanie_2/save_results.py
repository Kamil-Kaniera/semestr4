import pickle


def save_results_to_file(train_mse, test_mse, predictions, file_path):
    results = {
        'train_mse': train_mse,
        'test_mse': test_mse,
        'predictions': predictions
    }

    with open(file_path, 'wb') as file:
        pickle.dump(results, file)


def read_results_from_file(file_path):
    with open(file_path, 'rb') as file:
        results = pickle.load(file)

    train_mse = results['train_mse']
    test_mse = results['test_mse']
    predictions = results['predictions']

    return train_mse, test_mse, predictions
