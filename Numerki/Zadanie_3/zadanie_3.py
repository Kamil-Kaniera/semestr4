import numpy as np
import matplotlib.pyplot as plt

# Schemat Hornera
from horner import horner_scheme
# Interpolacja Lagrange'a
from lagrange import lagrange_interpolation


# Funkcje do interpolacji
def linear(x):
    return x


def absolute(x):
    return abs(x)


def polynomial1(x):
    return horner_scheme(x, [1, -2, 1, 1], 4)


def polynomial2(x):
    return horner_scheme(x, [1, 0, -5, -1, 2], 5)


def trigonometric(x):
    return np.sin(x)


def composition(x):
    return np.sin(x * x)


# Generowanie (n) równoodległych węzłów interpolacji
# pomiędzy dwoma określonymi wartościami (a i b)
def equidistant_nodes(a, b, n):
    return np.linspace(a, b, n)


# Wczytanie danych
function_choice = int(input(
    "Wybierz funkcję do interpolacji:\n"
    "1. Funkcja liniowa: f(x) = x\n"
    "2. Funkcja modułu: f(x) = |x|\n"
    "3. Wielomian: f(x) = x^3 - 2x^2 + x + 1\n"
    "4. Wielomian: f(x) = x^4 - 5x^2 - x + 2\n"
    "5. Funkcja trygonometryczna: f(x) = sin(x)\n"
    "6. Złożenie: f(x) = sin(x^2)\n"))

# Wybór funkcji do interpolacji
if function_choice == 1:
    function = linear
elif function_choice == 2:
    function = absolute
elif function_choice == 3:
    function = polynomial1
elif function_choice == 4:
    function = polynomial2
elif function_choice == 5:
    function = trigonometric
elif function_choice == 6:
    function = composition
else:
    print("Niepoprawny wybór funkcji.")
    exit()

# Wybór przedziału
beginning = float(input("Podaj początek przedziału interpolacji: "))
end = float(input("Podaj koniec przedziału interpolacji: "))

x_range = np.linspace(beginning, end, 1000)

# Obliczanie wartości oryginalnej funkcji
original_values = function(x_range)

# Rysowanie wykresu oryginalnej funkcji
plt.plot(x_range, original_values)
plt.title("Funkcja oryginalna")
plt.xlabel("x")
plt.ylabel("y")
plt.grid(True)
plt.show()

# Wybór liczby węzłów
number_of_nodes = int(input("Podaj liczbę węzłów interpolacyjnych: "))

# Generowanie węzłów interpolacji
interpolation_nodes = equidistant_nodes(beginning, end, number_of_nodes)

# Obliczanie wartości funkcji w węzłach interpolacji
function_values = function(interpolation_nodes)

# Interpolacja Lagrange'a
interpolated_values = []
interpolating_polynomial = ""

for x in x_range:
    interpolated_value, interpolating_polynomial = lagrange_interpolation(interpolation_nodes, function_values, x)
    interpolated_values.append(interpolated_value)

print("\nf(x) = ", interpolating_polynomial, "\n")


# Rysowanie wykresu
plt.plot(x_range, original_values, label="Funkcja oryginalna")
plt.plot(x_range, interpolated_values, label="Wielomian interpolacyjny")
plt.scatter(interpolation_nodes, function_values, color='red', label="Węzły interpolacji", facecolors='none')
plt.legend()
plt.xlabel("x")
plt.ylabel("y")
plt.title("Interpolacja funkcji")
plt.grid(True)
plt.show()
