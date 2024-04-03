from enum import Enum
from collections import deque

SOLVED_BOARD = [
    [1, 2, 3, 4],
    [5, 6, 7, 8],
    [9, 10, 11, 12],
    [13, 14, 15, 0]
]
STARTING_BOARD = []
MAX_DEPTH = 20


class Node:
    def __init__(self, board, parent=None, path=None):
        if path is None:
            path = []
        self.board = board
        self.parent = parent
        self.path = path


class MOVE(Enum):
    LEFT = "L"
    RIGHT = "R"
    UP = "U"
    DOWN = "D"


# ------------------------- Algorithms -------------------------

# Breadth-first search (wszerz)
def bfs(start_node):
    queue = deque([start_node])
    visited = set()

    while queue:
        current_node = queue.popleft()
        current_board = current_node.board

        # Check if the current board is the goal state
        if is_goal(current_board):
            return current_node.path

        # Convert the current board to a tuple of tuples and add it to the visited set
        visited.add(tuple(map(tuple, current_board)))

        # Generate all possible moves
        for next_board in generate_moves(current_board):
            # Check if the next board configuration has not been visited
            if tuple(map(tuple, next_board)) not in visited:
                # Add not visited node to the queue
                queue.append(Node(next_board, current_node.parent, current_node.path + [next_board]))


# Depth-first search (w głąb)
def dfs():
    return


# A*
def a_star():
    return


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
def generate_moves(board):
    moves = []
    row, column = find_zero(board)

    # Check UP move
    if row > 0:
        # Copy board to new_board
        new_board = [row[:] for row in board]
        # Switch the tiles
        new_board[row][column], new_board[row - 1][column] = new_board[row - 1][column], new_board[row][column]
        # Add the possible move
        moves.append(new_board)

    # Check DOWN move
    if row < 3:
        new_board = [row[:] for row in board]
        new_board[row][column], new_board[row + 1][column] = new_board[row + 1][column], new_board[row][column]
        moves.append(new_board)

    # Check LEFT move
    if column > 0:
        new_board = [row[:] for row in board]
        new_board[row][column], new_board[row][column - 1] = new_board[row][column - 1], new_board[row][column]
        moves.append(new_board)

    # Check RIGHT move
    if column < 3:
        new_board = [row[:] for row in board]
        new_board[row][column], new_board[row][column + 1] = new_board[row][column + 1], new_board[row][column]
        moves.append(new_board)

    return moves


# ----------------------------------------------------------------------
start_board = [
    [1, 0, 3, 4],
    [5, 2, 6, 8],
    [9, 10, 7, 12],
    [13, 14, 11, 15]
]

start_node = Node(start_board, path=[start_board])
solution_path = bfs(start_node)
if solution_path:
    print("Solution found in {} moves:".format(len(solution_path)))
    for step, board in enumerate(solution_path):
        print("Step", step + 1)
        for row in board:
            print(row)
        print()
else:
    print("No solution found.")
