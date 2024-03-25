import numpy as np

from jacobi import jacobi


# Konwersja string na tablice
def read_equations(text):
    lines = text.strip().split('\n')

    coefficients = []
    constants = []

    for line in lines:
        parts = line.split('|')

        coefficients_row = [float(x) for x in parts[0].strip().split()]
        coefficients.append(coefficients_row)

        constant = float(parts[1].strip())
        constants.append(constant)

    return coefficients, constants


# Czytanie pliku z macierzą
data = open("data.txt", encoding="utf8")

matrix, vector = read_equations(data.read())

# Ustawienie początku pliku
data.seek(0)
print("Wczytana macierz:\n", data.read(), "\n")

# Wybór metody zatrzymania
print("Wybierz kryterium zatrzymania: ")
print("a. Spełnienie warunku nałożonego na dokładność")
print("b. Osiągnięcie zadanej liczby iteracji")

stopping_criteria = input("Twój wybór: ")

epsilon = None
max_iterations = None

if stopping_criteria == 'a':
    epsilon = float(input("Podaj epsilon: "))
elif stopping_criteria == 'b':
    max_iterations = int(input("Podaj maksymalną liczbę iteracji: "))

print("\nRozwiązanie macierzy:")
print(jacobi(matrix, vector, np.zeros_like(vector), epsilon, max_iterations))
