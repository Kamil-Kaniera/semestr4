import numpy as np
import matplotlib.pyplot as plt
from scipy.special import hermite

# Schemat Hornera
from horner import horner_scheme
from hermgauss import *


# Wielomiany Hermite'a
def hermite_polynomials(n, x):
    H = hermite(n)
    return H(x)


# ---------------------------------------------------- Funkcje
def linear(x):
    return x


def absolute(x):
    return np.abs(x)


def polynomial(x):
    return horner_scheme(x, [1, -2, 1, 1], 4)


def trigonometric(x):
    return np.sin(x)


def composition(x):
    return np.sin(x * x)


# ---------------------------------------------------- Aproksymacja
def calculate_dot_products(func, n_points, degree):
    dot_products = []
    for i in range(degree + 1):
        H_i = hermite(i)
        integral_value = calculate_integral_with_gauss_hermite(lambda x: func(x) * H_i(x), n_points)
        dot_products.append(integral_value)
    return dot_products


def calculate_norms(n_points, degree):
    norms = []
    for i in range(degree + 1):
        H_i = hermite(i)
        norm = calculate_integral_with_gauss_hermite(lambda x: H_i(x) * H_i(x), n_points)
        norms.append(norm)
    return norms


def calculate_coefficients(func, n_points, degree):
    dot_products = calculate_dot_products(func, n_points, degree)
    norms = calculate_norms(n_points, degree)
    coefficients = [dp / norm for dp, norm in zip(dot_products, norms)]
    return coefficients


# ---------------------------------------------------- Wyznaczenie wielomianu
def build_approximation_polynomial(coefficients, degree):
    def approximation_polynomial(x):
        result = 0
        for i, coefficient in enumerate(coefficients):
            H_i = hermite_polynomials(i, x)
            result += coefficient * H_i
        return result

    return approximation_polynomial


# ---------------------------------------------------- Wyświetlenie wielomianu
def display_approximation_polynomial(coefficients):
    terms = []
    for i, coef in enumerate(coefficients):
        term = f"{coef:.4f} * H_{i}(x)"
        terms.append(term)
    polynomial_str = " + ".join(terms)
    print(f"Wielomian aproksymujący:\nP(x) = {polynomial_str}")


# ---------------------------------------------------- Obliczanie błędu

def calculate_error(func, approximation_polynomial, a, b, num_points):
    x = np.linspace(a, b, num_points)
    y_true = func(x)
    y_approx = approximation_polynomial(x)
    error = np.mean((y_true - y_approx) ** 2)
    return error


# ---------------------------------------------------- Wczytanie danych
function_choice = int(input(
    "Wybierz funkcję do interpolacji:\n"
    "1. Funkcja liniowa: f(x) = x\n"
    "2. Funkcja modułu: f(x) = |x|\n"
    "3. Wielomian: f(x) = x^3 - 2x^2 + x + 1\n"
    "4. Funkcja trygonometryczna: f(x) = sin(x)\n"
    "5. Złożenie: f(x) = sin(x^2)\n"))

# Wybór funkcji do interpolacji
if function_choice == 1:
    function = linear
elif function_choice == 2:
    function = absolute
elif function_choice == 3:
    function = polynomial
elif function_choice == 4:
    function = trigonometric
elif function_choice == 5:
    function = composition
else:
    print("Niepoprawny wybór funkcji.")
    exit()

# Wybór przedziału
beginning = float(input("Podaj początek przedziału aproksymacji: "))
end = float(input("Podaj koniec przedziału aproksymacji: "))

# Wybór stopnia
degree = int(input("Podaj stopień wielomianu: "))

# Wybór liczby węzłów
num_nodes = int(input("Podaj liczbę węzłów: "))

# ---------------------------------------------------- Wykres oryginalnej funkcji
x = np.linspace(beginning, end, 1000)
y_true = function(x)

plt.plot(x, y_true, label='Funkcja oryginalna')
plt.legend()
plt.show()

# ---------------------------------------------------- Obliczenia
print("\n----------------------------------------------------\n")
points = read_data_from_file("hermite.txt")

n_points = get_n_points(points, num_nodes)
coefficients = calculate_coefficients(function, n_points, degree)
approximation_polynomial = build_approximation_polynomial(coefficients, degree)

error = calculate_error(function, approximation_polynomial, beginning, end, 1000)
print(f"Błąd aproksymacji: {error}\n")
display_approximation_polynomial(coefficients)

# ---------------------------------------------------- Wykres oryginalnej funkcji i aproksymacji
y_approx = approximation_polynomial(x)

plt.plot(x, y_true, label='Funkcja oryginalna')
plt.plot(x, y_approx, label='Aproksymacja', linestyle='--')
plt.legend()
plt.show()
