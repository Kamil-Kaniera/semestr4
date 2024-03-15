import numpy as np
import matplotlib.pyplot as plt

# Schemat Hornera
from horner import horner_scheme

# Metoda bisekcji
from bisection import bisection_method

# Reguła falsi
from falsi import falsi_method


# Funkcje do wyboru
def polynomial(x):
    return horner_scheme(x, [1, -2, -5, 6], 4)


def trigonometric(x):
    return np.sin((2 * x) - 0.5)


def exponential(x):
    return 2 ** (x - 2) - 4


def composition(x):
    return np.sin((2 * np.pi) ** x)


# Funkcja do rysowania wykresu
def plot_function(f, a, b, bisection_zero, falsi_zero):
    x = np.linspace(a, b, 400)
    y = f(x)
    plt.plot(x, y, label="Funkcja")
    if bisection_zero is not None and falsi_zero is not None:
        plt.scatter(bisection_zero, f(bisection_zero), color='red', label="Miejsce zerowe metodą bisekcji")
        plt.scatter(falsi_zero, f(falsi_zero), color='green', label="Miejsce zerowe metodą falsi")
    plt.xlabel('x')
    plt.ylabel('f(x)')
    plt.title('Wykres funkcji')
    plt.legend()
    plt.grid(True)
    plt.show()


# Wybór funkcji
print("Wybierz rodzaj funkcji: ")
print("1. Wielomian: x^3 - 2x^2 - 5x + 6")
print("2. Trygonometryczna: sin(2x - 1/2)")
print("3. Wykładnicza: 2^(x-2) - 4")
print("4. Złożenie: sin(2π^x)")

function_choice = int(input("Twój wybór: "))

# Wybór metody zatrzymania
print("Wybierz kryterium zatrzymania: ")
print("a. Spełnienie warunku nałożonego na dokładność")
print("b. Osiągnięcie zadanej liczby iteracji")

stopping_criteria = input("Twój wybór: ")

function = None

if function_choice == 1:
    function = polynomial
elif function_choice == 2:
    function = trigonometric
elif function_choice == 3:
    function = exponential
elif function_choice == 4:
    function = composition

plot_function(function, -10, 10, None, None)


# Ustalenie parametrów
a = float(input("Podaj początek przedziału: "))
b = float(input("Podaj koniec przedziału: "))

if function(a) * function(b) > 0:
    print("Złe założenie o znaku funkcji na krańcach przedziału.")
    exit()

# Kryterium zatrzymania algorytmu
epsilon = None
max_iterations = None

if stopping_criteria == 'a':
    epsilon = float(input("Podaj epsilon: "))
elif stopping_criteria == 'b':
    max_iterations = int(input("Podaj maksymalną liczbę iteracji: "))

# Wyniki
zero_of_function_bisection = bisection_method(function, a, b, epsilon, max_iterations)
zero_of_function_falsi = falsi_method(function, a, b, epsilon, max_iterations)

print("Miejsca zerowe wybranej funkcji:"
      "\nMetoda bisekcji: ", zero_of_function_bisection[0], ", liczba iteracji: ", zero_of_function_bisection[1],
      "\nMetioda falsi: ", zero_of_function_falsi[0], ", liczba iteracji: ", zero_of_function_falsi[1])
plot_function(function, a, b, zero_of_function_bisection[0], zero_of_function_falsi[0])
