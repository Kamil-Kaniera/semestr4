def f(x):
    return 1 / ((x ** 5) + 7) ** (1 / 3)


def simpson_rule(function, a, b, n):
    # Check if the number of divisions
    if n % 2 != 0:
        return None

    # Calculate the width of a division
    h = (b - a) / n

    # Calculate the first and last value
    first = function(a)
    last = function(b)

    x = a
    sum = 0

    for i in range(n - 1):
        x += h
        value = function(x)
        # For even values of i
        if i % 2 == 0:
            sum += 4 * value
        # For odd values of i
        else:
            sum += 2 * value

    # Calculate the total value
    total = (h / 3) * (first + sum + last)
    return total


def simpson_with_precision(function, start, end, precision):
    number_of_divisions = 2

    starting_value = simpson_rule(function, start, end, number_of_divisions)

    while True:
        number_of_divisions += 2
        current_value = simpson_rule(function, start, end, number_of_divisions)

        if abs(current_value - starting_value) < precision:
            return starting_value, number_of_divisions
        else:
            starting_value = current_value


print(simpson_with_precision(f, 0, 1, 0.00001))
