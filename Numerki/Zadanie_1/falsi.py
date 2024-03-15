def falsi_method(f, left, right, epsilon, max_iterations):
    middle = 0
    current_iterations = 0

    while ((epsilon is not None and (abs(middle - ((left + right) / 2.0)) >= epsilon)) or
           (max_iterations is not None and current_iterations < max_iterations)):
        current_iterations += 1
        middle = left - (f(left) / (f(right) - f(left))) * (right - left)
        if f(middle) == 0:
            return [middle, current_iterations]
        if f(left) * f(middle) > 0:
            left = middle
        else:
            right = middle

    return [middle, current_iterations]