import numpy as np


def jacobi(A, b, starting_vector, tolerance, max_iterations):

    # Sprawdzenie czy podana macierz A jest kwadratowa
    check_A = np.array(A)
    A_rows, A_cols = check_A.shape
    if A_rows != A_cols:
        print("Błędna macierz")
        exit()

    result = starting_vector

    # Sprawdzenie czy podana macierz jest diagonalnie dominująca
    diagonal = np.diag(A)
    non_diagonal_sum = np.sum(np.abs(A), axis=1) - diagonal

    if not np.all(diagonal > non_diagonal_sum):
        print("Podana macierz nie jest diagonalnie dominująca")
        exit()

    # Stworzenie macierzy z zerami na przekątnej
    zero_at_diagonal = A - np.diagflat(diagonal)

    if max_iterations is not None:
        for i in range(max_iterations):
            # Obliczanie nowych wartości tablicy result korzystając z poprzednio policzonych wartości
            result = (b - np.dot(zero_at_diagonal, result)) / diagonal

    if tolerance is not None:
        while True:
            # Obliczanie nowych wartości tablicy result korzystając z poprzednio policzonych wartości
            temp = (b - np.dot(zero_at_diagonal, result)) / diagonal

            # Sprawdzenie zadaniej dokładności
            if np.all(abs(temp - result) < tolerance):
                return temp

            result = temp

    return result
