import argparse
import time
from collections import deque
from heapq import heappop, heappush

MAX_DEPTH = 20


class Node:
    def __init__(self, board, parent=None, path=None, direction=None):
        if path is None:
            path = []
        if direction is None:
            direction = ""
        self.board = board
        self.parent = parent
        self.path = path
        self.direction = direction


class HeuristicNode:
    def __init__(self, node, heuristic_score):
        self.node = node
        self.heuristic_score = heuristic_score

    def __lt__(self, other):
        # Compare HeuristicNode instances based on their heuristic scores
        return self.heuristic_score < other.heuristic_score


class StatisticsModule:
    def __init__(self):
        self.visited_states = 0
        self.processed_states = 0
        self.max_depth = 0
        self.time = 0


# ------------------------- Algorithms -------------------------

# Breadth-first search (wszerz)
def bfs(start_node):
    start_time = time.time()

    queue = deque([(start_node, 0)])
    closed_set = set()

    stats = StatisticsModule()
    max_depth = 0

    while queue:
        current_node, depth = queue.popleft()
        current_board = current_node.board

        # Check if the current board is the goal state
        if is_goal(current_board):
            end_time = time.time()
            stats.time = "{:.3f}".format(end_time - start_time)
            stats.processed_states = len(closed_set)
            stats.visited_states = stats.processed_states + len(queue)
            stats.max_depth = max_depth
            return (current_node, stats)

        # Convert the current board to a tuple of tuples and add it to the closed_set
        closed_set.add(tuple(map(tuple, current_board)))

        # Generate all possible moves
        for next_board in generate_moves(current_board, NEIGHBOURS_ORDER):
            # Check if the next board configuration has not been closed_set
            if tuple(map(tuple, next_board.board)) not in closed_set:
                # Add not closed_set node to the queue
                next_node = Node(next_board.board, current_node.parent, current_node.path + [next_board.board],
                                 current_node.direction + str(next_board.direction))
                queue.append((next_node, depth + 1))
                # Update max_depth
                max_depth = max(max_depth, depth + 1)


# Depth-first search (w głąb)
def dfs(start_node):
    start_time = time.time()

    stack = [(start_node, 0)]
    closed_set = set()

    stats = StatisticsModule()
    max_depth = 0

    while stack:
        current_node, depth = stack.pop()
        current_board = current_node.board

        # Check if the current board is the goal state
        if is_goal(current_board):
            end_time = time.time()
            stats.time = "{:.3f}".format(end_time - start_time)
            stats.processed_states = len(closed_set)
            stats.visited_states = stats.processed_states + len(stack)
            stats.max_depth = max_depth
            return (current_node, stats)

        # Convert the current board to a tuple of tuples and add it to the closed_set
        closed_set.add(tuple(map(tuple, current_board)))

        # Check current depth
        if depth < MAX_DEPTH:
            # Generate all possible moves
            moves = generate_moves(current_board, NEIGHBOURS_ORDER)
            moves.reverse()
            for next_board in moves:
                # Check if the next board configuration has not been in closed_set
                if tuple(map(tuple, next_board.board)) not in closed_set:
                    # Add not closed_set node to the stack
                    next_node = Node(next_board.board, current_node.parent, current_node.path + [next_board.board],
                                     current_node.direction + str(next_board.direction))
                    stack.append((next_node, depth + 1))
                    # Update max_depth
                    max_depth = max(max_depth, depth + 1)

    end_time = time.time()
    stats.time = "{:.3f}".format(end_time - start_time)
    stats.processed_states = len(closed_set)
    stats.visited_states = stats.processed_states + len(stack)
    stats.max_depth = max_depth
    return None, stats


# A*
def a_star(start_node, heuristic_func):
    start_time = time.time()

    open_list = [HeuristicNode(start_node, heuristic_func(start_node))]
    closed_set = set()

    stats = StatisticsModule()
    max_depth = 0

    while open_list:
        # Only pop the node with the lowest combined score
        current_node = heappop(open_list).node

        # Check if the current node is the goal state
        if is_goal(current_node.board):
            end_time = time.time()
            stats.time = "{:.3f}".format(end_time - start_time)
            stats.processed_states = len(closed_set)
            stats.visited_states = stats.processed_states + len(open_list)
            stats.max_depth = max_depth
            return (current_node, stats)

        # Convert the current board to a tuple of tuples and add it to the closed set
        closed_set.add(tuple(map(tuple, current_node.board)))

        # Generate all possible moves
        for next_board in generate_moves(current_node.board, NEIGHBOURS_ORDER):
            # Check if the next board configuration has not been in closed_set
            if tuple(map(tuple, next_board.board)) not in closed_set:
                # Calculate the path cost from the start node to the current node
                g_score = len(current_node.path)
                max_depth = max(max_depth, g_score)
                # Calculate the heuristic cost from the next node to the goal state
                h_score = heuristic_func(Node(next_board.board))
                # Calculate the combined score
                f_score = g_score + h_score

                # Create the next node
                next_node = Node(next_board.board, current_node.parent, current_node.path + [next_board.board],
                                 current_node.direction + str(next_board.direction))

                # Add the next node to the open list with its combined score
                heappush(open_list, HeuristicNode(next_node, f_score))

    end_time = time.time()
    stats.time = "{:.3f}".format(end_time - start_time)
    stats.processed_states = len(closed_set)
    stats.visited_states = stats.processed_states + len(open_list)
    stats.max_depth = max_depth
    return None, stats


# -----------------------  Heuristics --------------------------

def manhattan_distance(node):
    total_distance = 0

    for i in range(ROWS):
        for j in range(COLS):
            current_tile = node.board[i][j]
            # Exclude the empty current_tile
            if current_tile != 0:
                # Find the goal position of the current_tile
                goal_row, goal_col = divmod(current_tile - 1, COLS)
                # Calculate the Manhattan distance and add it to the total distance
                total_distance += abs(i - goal_row) + abs(j - goal_col)

    return total_distance


def hamming_distance(node):
    h_score = 0

    for i in range(len(SOLVED_BOARD)):
        for j in range(len(SOLVED_BOARD[i])):
            # Skip the empty tile
            if node.board[i][j] == 0:
                continue
            # Check if the tile is in the wrong position
            if node.board[i][j] != SOLVED_BOARD[i][j]:
                h_score += 1
    return h_score


# ---------------------  Other Functions -----------------------

# Check if the board is solved
def is_goal(board):
    return board == SOLVED_BOARD


# Find empty tile "0" in the board
def find_zero(board):
    for row in range(len(board)):
        for column in range(len(board[row])):
            if board[row][column] == 0:
                return row, column


# Generate all possible moves from the current board state
def generate_moves(board, directions):
    moves = []
    row, column = find_zero(board)

    for direction in directions:

        # Check UP move
        if direction == "U" and row > 0:
            # Copy board to new_board
            new_board = [row[:] for row in board]
            # Switch the tiles
            new_board[row][column], new_board[row - 1][column] = new_board[row - 1][column], new_board[row][column]
            # Create new node
            move = Node(new_board, parent=board, direction=direction)
            # Add the possible move
            moves.append(move)

        # Check DOWN move
        elif direction == "D" and row < len(board) - 1:
            new_board = [row[:] for row in board]
            new_board[row][column], new_board[row + 1][column] = new_board[row + 1][column], new_board[row][column]
            move = Node(new_board, parent=board, direction=direction)
            moves.append(move)

        # Check LEFT move
        elif direction == "L" and column > 0:
            new_board = [row[:] for row in board]
            new_board[row][column], new_board[row][column - 1] = new_board[row][column - 1], new_board[row][column]
            move = Node(new_board, parent=board, direction=direction)
            moves.append(move)

        # Check RIGHT move
        elif direction == "R" and column < len(board[row]) - 1:
            new_board = [row[:] for row in board]
            new_board[row][column], new_board[row][column + 1] = new_board[row][column + 1], new_board[row][column]
            move = Node(new_board, parent=board, direction=direction)
            moves.append(move)

    return moves


def generate_solved_board(rows, cols):
    board = []
    num = 1

    for i in range(rows):
        row = []
        for j in range(cols):
            if i == rows - 1 and j == cols - 1:
                # Last cell should be empty (0)
                row.append(0)
            else:
                row.append(num)
                num += 1
        board.append(row)

    return board


def read_start_board(file_name):
    start_board = []

    with open(file_name) as file:
        # Read the dimensions of the board (first line)
        dimensions = file.readline().split()
        rows = int(dimensions[0])

        # Read each subsequent line and parse the numbers
        for _ in range(rows):
            row = list(map(int, file.readline().split()))
            start_board.append(row)

    return start_board


def save_solution(solution_node, file_name):
    if solution_node:
        with open(file_name, "w") as file:
            file.write(str(len(solution_node.direction)) + "\n")
            file.write(solution_node.direction)
    else:
        with open(file_name, "w") as file:
            file.write("-1")


def save_statistics(solution_node, statistics, file_name):
    if solution_node:
        solution_length = str(len(solution_node.direction))
    else:
        solution_length = "-1"

    with open(file_name, "w") as file:
        file.write(solution_length + "\n")
        file.write(str(statistics.visited_states) + "\n")
        file.write(str(statistics.processed_states) + "\n")
        file.write(str(statistics.max_depth) + "\n")
        file.write(str(statistics.time) + "\n")


# ----------------------------------------------------------------------
# Parsing
parser = argparse.ArgumentParser(description="Algorithm, order, source file, solution file, statistics file.")
parser.add_argument('algorithm')
parser.add_argument('order')
parser.add_argument('source_file')
parser.add_argument('solution_file')
parser.add_argument('statistic_file')
arguments = parser.parse_args()

start_board = read_start_board(arguments.source_file)

# Set ROWS, COLS and generate SOLVED_BOARD based on intput
ROWS = len(start_board)
COLS = len(start_board[0])
SOLVED_BOARD = generate_solved_board(ROWS, COLS)

start_node = Node(start_board, path=[start_board])

# Call correct algorithm with given parameters
if arguments.order == "hamm":
    NEIGHBOURS_ORDER = ["R", "D", "U", "L"]
    result = a_star(start_node, hamming_distance)

elif arguments.order == "manh":
    NEIGHBOURS_ORDER = ["R", "D", "U", "L"]
    result = a_star(start_node, manhattan_distance)

else:
    NEIGHBOURS_ORDER = [arguments.order[0], arguments.order[1], arguments.order[2], arguments.order[3]]

    if arguments.algorithm == "bfs":
        result = bfs(start_node)

    elif arguments.algorithm == "dfs":
        result = dfs(start_node)

# Save solution and statistics
solution_node = result[0]
statistics = result[1]
save_solution(solution_node, file_name=arguments.solution_file)
save_statistics(solution_node, statistics, file_name=arguments.statistic_file)
