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


def polynomial(x):
    return horner_scheme(x, [1, -2, 1, 1], 4)


def trigonometric(x):
    return np.sin(x)


def composition(x):
    return np.sin(x * x)


# Generowanie (n) równoodległych węzłów interpolacji
# pomiędzy dwoma określonymi wartościami (a i b)
def equidistant_nodes(a, b, n):
    return np.linspace(a, b, n)


# Wczytanie danych
beginning = float(input("Podaj początek przedziału interpolacji: "))
end = float(input("Podaj koniec przedziału interpolacji: "))
number_of_nodes = int(input("Podaj liczbę węzłów interpolacyjnych: "))
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

# Generowanie węzłów interpolacji
interpolation_nodes = equidistant_nodes(beginning, end, number_of_nodes)

# Obliczanie wartości funkcji w węzłach interpolacji
function_values = function(interpolation_nodes)

# Interpolacja Lagrange'a
x_range = np.linspace(beginning, end, 1000)
interpolated_values = [lagrange_interpolation(interpolation_nodes, function_values, x) for x in x_range]

# Obliczanie wartości oryginalnej funkcji
original_values = function(x_range)

# Rysowanie wykresu
plt.plot(x_range, original_values, label="Funkcja oryginalna")
plt.plot(x_range, interpolated_values, label="Wielomian interpolacyjny")
plt.scatter(interpolation_nodes, function_values, color='red', label="Węzły interpolacji")
plt.legend()
plt.xlabel("x")
plt.ylabel("y")
plt.title("Interpolacja funkcji")
plt.grid(True)
plt.show()
