def lagrange_interpolation(x, y, x_new):
    n = len(x)
    interpolated_value = 0
    interpolating_polynomial = ""

    for i in range(n):
        basis = 1
        basis_string = f"( "
        for j in range(n):
            if i != j:
                # Obliczanie kolejnych czynników bazowego wielomianu Lagrange'a
                basis *= (x_new - x[j]) / (x[i] - x[j])
                if x[j] >= 0:
                    basis_string += f"(x - {x[j]}) / ({x[i]} - {x[j]}) "
                else:
                    basis_string += f"(x + {abs(x[j])}) / ({x[i]} - {x[j]}) "
        basis_string += f")"
        interpolating_polynomial += f"{y[i]} * {basis_string} + "
        interpolated_value += y[i] * basis

    # Usunięcie nadmiarowej operacji dodawania na końcu
    interpolating_polynomial = interpolating_polynomial[:-3]

    return interpolated_value, interpolating_polynomial


