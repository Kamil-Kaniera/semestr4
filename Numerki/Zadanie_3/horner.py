def horner_scheme(x, coefficients, length):
    result = coefficients[0]

    for i in range(1, length):
        result = result * x + coefficients[i]

    return result
