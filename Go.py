import queue
import copy


class Go:

    def __init__(self, n, pre, now, piece_type, move_count):
        self.len = n
        self.piece_type = piece_type
        self.pre_board = pre
        self.board = now
        self.moves = move_count
        self.max_move = n*n - 1
        self.direction = [[0, 1], [0, -1], [1, 0], [-1, 0]]
        self.komi = n / 2
    
    def board_valid(self, i, j):
        return 0 <= i < self.len and 0 <= j < self.len

    def detect_neighbor_ally(self, board, i, j):
        group_allies = []
        for d in self.direction:
            neighbor = (i+d[0], j+d[1])
            if self.board_valid(neighbor[0], neighbor[1]) and board[neighbor[0]][neighbor[1]] == board[i][j]:
                group_allies.append(neighbor)
        return group_allies

    def ally_bfs(self, board, i, j):
        q = queue.Queue()
        q.put((i, j))
        ally_members = []
        while not q.empty():
            piece = q.get()
            ally_members.append(piece)
            neighbor_allies = self.detect_neighbor_ally(board, piece[0], piece[1])
            for allay in neighbor_allies:
                if allay not in ally_members:
                    q.put(allay)
        return ally_members

    def has_neigh_liberty(self, board, i, j):
        for d in self.direction:
            neighbor = (i+d[0], j+d[1])
            if self.board_valid(neighbor[0], neighbor[1]) and board[neighbor[0]][neighbor[1]] == 0:
                return True
        return False

    def has_liberty(self, board, i, j):
        allies = self.ally_bfs(board, i, j)
        for piece in allies:
            if self.has_neigh_liberty(board, piece[0], piece[1]):
                return True
        return False

    def remove_dead_stone(self, board):
        opponent = 3 - self.piece_type
        remove = False
        for i in range(self.len):
            for j in range(self.len):
                if board[i][j] == opponent and not self.has_liberty(board, i, j):
                    allies = self.ally_bfs(board, i, j)
                    for ally in allies:
                        board[ally[0]][ally[1]] = 0
                    remove = True
        return remove

    def the_same_board(self, board1, board2):
        for i in range(self.len):
            for j in range(self.len):
                if not board1[i][j] == board2[i][j]:
                    return False
        return True

    def make_move(self, action, i, j):
        board = copy.deepcopy(self.board)
        if action == "PASS":
            return True, board
        if not self.board_valid(i, j) or not self.board[i][j] == 0:
            return False, board

        board[i][j] = self.piece_type
        if self.has_liberty(board, i, j):
            self.remove_dead_stone(board)
            return True, board
        else:
            self.remove_dead_stone(board)
            if not self.has_liberty(board, i, j) or self.the_same_board(self.pre_board, board):
                return False, board
        return True, board
            
    def get_remaining_point_list(self):
        remaining_point_list = []
        for i in range(self.len):
            for j in range(self.len):
                if self.board[i][j] == 0:
                    remaining_point_list.append((i, j))
        return remaining_point_list

    def get_remaining_point_count(self):
        remaining_point_count = 0
        for i in range(self.len):
            for j in range(self.len):
                if self.board[i][j] == 0:
                    remaining_point_count += 1
        return remaining_point_count

    def remove_certain_pieces(self, positions):
        board = self.board
        for piece in positions:
            board[piece[0]][piece[1]] = 0

    def score(self, piece_type):
        board = self.board
        m = 0
        for i in range(self.len):
            for j in range(self.len):
                if board[i][j] == piece_type:
                    m += 1
        return m

    def judge_winner(self):
        c1 = self.score(1)
        c2 = self.score(2)
        if c1 > c2 + self.komi:
            return 1
        elif c1 < c2 + self.komi:
            return 2
        else:
            return 0

    def update_board(self, new_board):
        self.board = new_board
