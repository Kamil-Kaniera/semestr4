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

