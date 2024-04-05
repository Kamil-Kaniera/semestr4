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
    def __init__(self, board, parent=None, path=None, direction=None):
        if path is None:
            path = []
        if direction is None:
            direction = ""
        self.board = board
        self.parent = parent
        self.path = path
        self.direction = direction


class MOVE(Enum):
    LEFT = "L"
    RIGHT = "R"
    UP = "U"
    DOWN = "D"


# Set the order of neighbours
NEIGHBOURS_ORDER = [MOVE.RIGHT, MOVE.DOWN, MOVE.UP, MOVE.LEFT]


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
            return current_node

        # Convert the current board to a tuple of tuples and add it to the visited set
        visited.add(tuple(map(tuple, current_board)))

        # Generate all possible moves
        for next_board in generate_moves(current_board, NEIGHBOURS_ORDER):
            # Check if the next board configuration has not been visited
            if tuple(map(tuple, next_board.board)) not in visited:
                # Add not visited node to the queue
                queue.append(Node(next_board.board, current_node.parent, current_node.path + [next_board.board],
                                  current_node.direction + str(next_board.direction.value)))


# Depth-first search (w głąb)
def dfs(start_node):
    stack = [(start_node, 0)]
    visited = set()

    while stack:
        current_node, depth = stack.pop()
        current_board = current_node.board

        # Check if the current board is the goal state
        if is_goal(current_board):
            return current_node
        # Convert the current board to a tuple of tuples and add it to the visited set
        visited.add(tuple(map(tuple, current_board)))

        # Check current depth
        if depth < MAX_DEPTH:
            # Generate all possible moves
            moves = generate_moves(current_board, NEIGHBOURS_ORDER)
            moves.reverse()
            for next_board in moves:
                # Check if the next board configuration has not been visited
                if tuple(map(tuple, next_board.board)) not in visited:
                    # Add not visited node to the queue
                    next_node = Node(next_board.board, current_node.parent, current_node.path + [next_board.board],
                                     current_node.direction + str(next_board.direction.value))
                    stack.append((next_node, depth + 1))

    return None


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
def generate_moves(board, directions):
    moves = []
    row, column = find_zero(board)

    for direction in directions:

        # Check UP move
        if direction == MOVE.UP and row > 0:
            # Copy board to new_board
            new_board = [row[:] for row in board]
            # Switch the tiles
            new_board[row][column], new_board[row - 1][column] = new_board[row - 1][column], new_board[row][column]
            # Create new node
            move = Node(new_board, parent=board, direction=direction)
            # Add the possible move
            moves.append(move)

        # Check DOWN move
        elif direction == MOVE.DOWN and row < 3:
            new_board = [row[:] for row in board]
            new_board[row][column], new_board[row + 1][column] = new_board[row + 1][column], new_board[row][column]
            move = Node(new_board, parent=board, direction=direction)
            moves.append(move)

        # Check LEFT move
        elif direction == MOVE.LEFT and column > 0:
            new_board = [row[:] for row in board]
            new_board[row][column], new_board[row][column - 1] = new_board[row][column - 1], new_board[row][column]
            move = Node(new_board, parent=board, direction=direction)
            moves.append(move)

        # Check RIGHT move
        elif direction == MOVE.RIGHT and column < 3:
            new_board = [row[:] for row in board]
            new_board[row][column], new_board[row][column + 1] = new_board[row][column + 1], new_board[row][column]
            move = Node(new_board, parent=board, direction=direction)
            moves.append(move)

    return moves


# ----------------------------------------------------------------------
start_board = [
    [1, 2, 3, 4],
    [5, 6, 7, 8],
    [0, 14, 10, 11],
    [9, 13, 15, 12]
]

start_node = Node(start_board, path=[start_board])
solution_node = bfs(start_node)

if solution_node:
    print("Solution found in {} moves:".format(len(solution_node.direction)))
    current_node = solution_node
    while current_node:
        print(len(solution_node.direction))
        print(current_node.direction)
        print()
        current_node = current_node.parent
else:
    print("No solution found.")


