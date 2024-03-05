import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import bisect


# Funkcje do wyboru
def polynomial(x):
    return 5 * x ** 4 - x ** 2 - 7 * x + 4


def trigonometric(x):
    return np.sin(2 * x) + 0.5


def exponential(x):
    return 2 ** x - 7


def composition(x):
    return polynomial(x) + trigonometric(x) + exponential(x)


# Metoda bisekcji
def bisection_method(f, a, b, epsilon, max_iterations):
    if f(a) * f(b) > 0:
        print("Złe założenie o znaku funkcji na krańcach przedziału.")
        return None

    iterations = 0
    while (b - a) / 2 > epsilon and iterations < max_iterations:
        midpoint = (a + b) / 2
        if f(midpoint) == 0:
            return midpoint
        elif f(a) * f(midpoint) < 0:
            b = midpoint
        else:
            a = midpoint
        iterations += 1

    return (a + b) / 2


# Reguła falsi
def false_position_method(f, a, b, epsilon, max_iterations):
    if f(a) * f(b) > 0:
        print("Złe założenie o znaku funkcji na krańcach przedziału.")
        return None

    iterations = 0
    while (b - a) / 2 > epsilon and iterations < max_iterations:
        x = (a * f(b) - b * f(a)) / (f(b) - f(a))
        if f(x) == 0:
            return x
        elif f(a) * f(x) < 0:
            b = x
        else:
            a = x
        iterations += 1

    return (a + b) / 2


# Funkcja do rysowania wykresu
def plot_function(f, a, b, root):
    x = np.linspace(a, b, 400)
    y = f(x)
    plt.plot(x, y, label="Funkcja")
    plt.scatter(root, f(root), color='red', label="Miejsce zerowe")
    plt.xlabel('x')
    plt.ylabel('f(x)')
    plt.title('Wykres funkcji')
    plt.legend()
    plt.grid(True)
    plt.show()


# Wybór funkcji
print("Wybierz rodzaj funkcji: ")
print("1. Wielomian: 5x^4 - x^2 - 7x + 4")
print("2. Trygonometryczna: sin(2x) + 1/2")
print("3. Wykładnicza: 2^x - 7")
print("4. Złożenie")

function_choice = int(input("Twój wybór: "))

# Wybór metody zatrzymania
print("Wybierz kryterium zatrzymania: ")
print("a. Spełnienie warunku nałożonego na dokładność")
print("b. Osiągnięcie zadanej liczby iteracji")

stopping_criteria = input("Twój wybór: ")

# Ustalenie parametrów
a = float(input("Podaj początek przedziału: "))
b = float(input("Podaj koniec przedziału: "))

if stopping_criteria == 'a':
    epsilon = float(input("Podaj epsilon: "))
    max_iterations = None
elif stopping_criteria == 'b':
    max_iterations = int(input("Podaj maksymalną liczbę iteracji: "))
    epsilon = None

if function_choice == 1:
    function = polynomial
elif function_choice == 2:
    function = trigonometric
elif function_choice == 3:
    function = exponential
elif function_choice == 4:
    function = composition

# Obliczenie miejsca zerowego
# if max_iterations is None:
#     root = bisect(function, a, b, xtol=epsilon)
#     method_name = "Metoda bisekcji"
# else:
    root = bisection_method(function, a, b, epsilon, max_iterations)
    method_name = "Reguła falsi"

if root is not None:
    print(f"Miejsce zerowe znalezione za pomocą {method_name}: {root}")
    plot_function(function, a, b, root)
else:
    print("Nie udało się znaleźć miejsca zerowego.")
