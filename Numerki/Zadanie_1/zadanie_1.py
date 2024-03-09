import numpy as np
import matplotlib.pyplot as plt


# Funkcje do wyboru
def polynomial(x):
    return 5 * x ** 4 - x ** 2 - 7 * x + 4


def trigonometric(x):
    return (np.sin(2 * x) + 0.5)


def exponential(x):
    return 2 ** x - 7


def composition(x):
    return np.sin(3 ** x)


# Metoda bisekcji
def bisection_method(f, left, right, epsilon, max_iterations):
    middle = 0

    # Wariant z epsilonem
    if epsilon is not None:
        while abs(middle - (left + right) / 2.0) >= epsilon:
            middle = (left + right) / 2.0
            if f(left) * f(middle) > 0:
                left = middle
            elif f(left) * f(middle) <= 0:
                right = middle
        return middle

    # Wariant z liczbą iteracji
    elif max_iterations is not None:
        current_iteration = 0
        while current_iteration <= max_iterations:
            current_iteration += 1
            middle = (left + right) / 2.0
            if f(left) * f(middle) > 0:
                left = middle
            elif f(left) * f(middle) <= 0:
                right = middle
        return middle


# Reguła falsi
def falsi_method(f, left, right, epsilon, max_iterations):
    middle = 0

    # Wariant z epsilonem
    if epsilon is not None:
        while abs(middle - (left + right) / 2.0) >= epsilon:
            middle = left - (f(left) / (f(right) - f(left))) * (right - left)
            if f(left) * f(middle) > 0:
                left = middle
            elif f(left) * f(middle) <= 0:
                right = middle
        return middle

    # Wariant z liczbą iteracji
    elif max_iterations is not None:
        current_iteration = 0
        while current_iteration <= max_iterations:
            current_iteration += 1
            middle = left - (f(left) / (f(right) - f(left))) * (right - left)
            if f(left) * f(middle) > 0:
                left = middle
            elif f(left) * f(middle) <= 0:
                right = middle
        return middle


# Funkcja do rysowania wykresu
def plot_function(f, a, b, bisection_zero, falsi_zero):
    x = np.linspace(a, b, 400)
    y = f(x)
    plt.plot(x, y, label="Funkcja")
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
print("1. Wielomian: 5x^4 - x^2 - 7x + 4")
print("2. Trygonometryczna: sin(2x) + 1/2")
print("3. Wykładnicza: 2^x - 7")
print("4. Złożenie: sin(3^x)")

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
      "\nMetoda bisekcji: ", zero_of_function_bisection,
      "\nMetioda falsi: ", zero_of_function_falsi)
plot_function(function, a, b, zero_of_function_bisection, zero_of_function_falsi)