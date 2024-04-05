def lagrange_interpolation(x, y, x_new):
    n = len(x)
    interpolated_value = 0

    for i in range(n):
        basis = 1
        for j in range(n):
            if i != j:
                basis *= (x_new - x[j]) / (x[i] - x[j])
        interpolated_value += y[i] * basis

    return interpolated_value
