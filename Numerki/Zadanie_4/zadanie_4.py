import math

from horner import horner_scheme


def f_1(x):
    return horner_scheme(x, [1, 0, 18], 3)


def f_2(x):
    return math.cos(2 * x)


def f_3(x):
    return math.log(x + 4)


def weight_function(x):
    return math.exp(-x ** 2)


class Points_With_Weight:
    def __init__(self, n, weight, coordinate):
        self.n = n
        self.weight = weight
        self.coordinate = coordinate


# ----------------------------------------------------------------
def read_data_from_file(filename):
    points = []
    with open(filename, 'r') as file:
        for line in file:
            line = line.strip().split()

            # Skip empty line
            if len(line) == 0:
                continue

            # Get the number of points
            if line[0] == "n":
                number_of_points = int(line[2])

            # Save the weight and coordinate
            elif number_of_points is not None:
                weight = float(line[0])
                coordinate = float(line[1])
                points.append(Points_With_Weight(number_of_points, weight, coordinate))
    return points


# ---------------------------------------------------------------- Kwadratura złożona Newtona-Cotesa
def simpson_rule(function, a, b, n, weight_function):
    # Check if the number of divisions
    if n % 2 != 0:
        return None

    # Calculate the width of a division
    h = (b - a) / n

    # Calculate the first and last value
    first = function(a) * weight_function(a)
    last = function(b) * weight_function(b)

    x = a
    sum = 0

    for i in range(n - 1):
        x += h
        value = function(x) * (weight_function(x) if weight_function else 1)
        # For even values of i
        if i % 2 == 0:
            sum += 4 * value
        # For odd values of i
        else:
            sum += 2 * value

    # Calculate the total value
    total = (h / 3) * (first + sum + last)
    return total


def simpson_with_precision(function, begin, end, precision):
    number_of_divisions = 2

    starting_value = simpson_rule(function, begin, end, number_of_divisions, weight_function)

    while True:
        number_of_divisions += 2
        current_value = simpson_rule(function, begin, end, number_of_divisions, weight_function)

        if abs(current_value - starting_value) < precision:
            return starting_value, number_of_divisions
        else:
            starting_value = current_value


# ---------------------------------------------------------------- Kwadratura Gaussa-Hermite’a
def get_n_points(points, n):
    n_points = []

    for point in points:
        if point.n == n:
            n_points.append(point)

    return n_points


def calculate_integral_with_gauss_hermite(f, points):
    # Obliczenie przybliżonej wartości całki
    result = 0
    for point in points:
        result += point.weight * f(point.coordinate)
    return result


# ----------------------------------------------------------------
def choose_function():
    choice = int(input(
        "Wybierz funkcję:\n"
        "1. f(x) = x^2 + 18\n"
        "2. f(x) = cos(2x)\n"
        "3. f(x) = log(x+4)\n"))

    if choice == 1:
        return f_1
    elif choice == 2:
        return f_2
    elif choice == 3:
        return f_3


function = choose_function()

begin = int(input(
    "Podaj początek przedziału: "
))
end = int(input(
    "Podaj koniec przedziału: "
))
precision = float(input(
    "Podaj dokładność: "
))

filename = "hermite.txt"
points = read_data_from_file(filename)

# Kwadratura złożona Newtona-Cotesa
simpson_result = simpson_with_precision(function, begin, end, precision)

print("\nKwadratura złożona Newtona-Cotesa\n\n"
      "Wynik: " + str(simpson_result[0]) + "\nLiczba przedziałów: " + str(simpson_result[1]))

# Kwadratura Gaussa-Hermite’a
print("\nKwadratura Gaussa-Hermite’a")

# 2 points
print("\n2 węzły: ")
print(calculate_integral_with_gauss_hermite(function, get_n_points(points, 2)))
# 3 points
print("\n3 węzły: ")
print(calculate_integral_with_gauss_hermite(function, get_n_points(points, 3)))
# 4 points
print("\n4 węzły: ")
print(calculate_integral_with_gauss_hermite(function, get_n_points(points, 4)))
# 5 points
print("\n5 węzłów: ")
print(calculate_integral_with_gauss_hermite(function, get_n_points(points, 5)))
