from math import inf
from os import path
from Go import *

'''
2533549389:Xiuqi Zhuge
'''


class MyPlayer:

    def __init__(self, n, limit):
        self.len = n
        self.limit = limit
        piece_type, pre, now = read_input(self.len)
        move_count = read_move_number(self.len, piece_type, pre)
        self.state = Go(self.len, pre, now, piece_type, move_count)

    def estimated_value(self, value):
        return (value+0.0)*value/9*9

    def step_value(self, step):
        return (24 - step) / 24.0

    def direction(self):
        return [[0, 1], [0, -1], [1, 0], [-1, 0]]

    def score(self):
        return [[1, 2, 3, 2, 1], [2, 4, 6, 4, 2], [3, 6, 9, 6, 3], [2, 4, 6, 4, 2], [1, 2, 3, 2, 1]]

    def get_step_number(self):
        lim = 1
        step = 0
        remaining_point_count = self.state.get_remaining_point_count()
        while step + self.state.moves < self.state.max_move and lim < self.limit:
            lim *= remaining_point_count
            if lim >= self.limit or step + step >= self.state.max_move:
                break
            step = step + 1
            remaining_point_count = remaining_point_count - 1
        return step

    def get_next_state(self, state, board):
        pre = copy.deepcopy(state.board)
        cur = copy.deepcopy(board)
        next_state = Go(state.len, pre, cur, 3 - state.piece_type, state.moves + 1)
        return next_state
    
    def get_last_state(self, state, board):
        cur = copy.deepcopy(state.board)
        pre = copy.deepcopy(board)
        last_state = Go(state.len, pre, cur, 3 - state.piece_type, state.moves - 1)
        return last_state

    def get_next_next_state(self, state, board):
        pre = copy.deepcopy(state.board)
        now = copy.deepcopy(board)
        next_state = self.get_next_state(state, now)
        cur = copy.deepcopy(next_state.board)
        next_state = Go(state.len, pre, cur, 3 - state.piece_type, state.moves + 1)
        return next_state

    def function(self):
        now_state = copy.deepcopy(self.state)
        if now_state.moves == 0 and now_state.piece_type == 1:
            return "MOVE", 2, 2
        if now_state.moves == 1 and now_state.piece_type == 2 and now_state.board[2][2] == 0:
            return "MOVE", 2, 2
        if now_state.moves == 1 and now_state.piece_type == 2 and now_state.board[2][2] == 1:
            return "MOVE", 1, 2
        alpha = -inf
        beta = inf
        depth = self.get_step_number()
        pre_is_pass = now_state.the_same_board(now_state.pre_board, now_state.board)
        _, action, point = self.max(alpha, beta, now_state, pre_is_pass, depth, self.state.piece_type)
        return action, point[0], point[1]

    def max(self, alpha, beta, state, pre_is_pass, step, piece_type):
        if step == 0 or state.moves == state.max_move:
            value = self.evaluation(state, step, pre_is_pass, piece_type)
            return value, "Terminal", (-1, -1)
        
        remaining_point_list = state.get_remaining_point_list()
        next_point = (-1, -1)
        action = "Move"
        value = -inf
        next_value = -1

        #action = "Move"
        for point in remaining_point_list:
            valid, board = state.make_move("MOVE", point[0], point[1])
            if not valid:
                continue
            next_state = self.get_next_state(state, board)
            next_value, _, _ = self.min(alpha, beta, next_state, False, step - 1, piece_type)
            # loop of getting next state and value

            if next_value > value:
                value = next_value
                next_point = point
                action = "Move"
                # move to next state

            if value > beta:
                return value, action, next_point

            if value > alpha:
                alpha = next_value

        #action = "Pass"
        next_state = self.get_next_state(state, state.board)
        if state.the_same_board(state.board, state.pre_board) and state.the_same_board(next_state.board, next_state.pre_board):
            next_value = self.evaluation(next_state, step, True, piece_type)
            #two players both choose Pass
        else:
            next_value, _, _ = self.min(alpha, beta, next_state, True, step - 1, piece_type)

        if next_value > value:
            value = next_value
            action = "PASS"
            # move to next state

        if next_value > beta:
            return value, action, next_point

        if value > alpha:
            alpha = next_value

        return value, action, next_point

    def min(self, alpha, beta, state, pre_is_pass, step, piece_type):
        if step == 0 or state.moves == state.max_move:
            value = self.evaluation(state, step, pre_is_pass, piece_type)
            return value, "Terminal", (-1, -1)

        remaining_point_list = state.get_remaining_point_list()
        next_point = (-1, -1)
        action = "MOVE"
        value = inf
        next_value = -1
        # action = "Move"

        for point in remaining_point_list:
            valid, board = state.make_move("MOVE", point[0], point[1])
            if not valid:
                continue
            next_state = self.get_next_state(state, board)
            next_value, _, _ = self.max(alpha, beta, next_state, False, step - 1, piece_type)

            if next_value < value:
                value = next_value
                next_point = point
                action = "MOVE"
                #move to next state

            if value <= alpha:
                return value, action, next_point

            beta = min(beta, value)

        #action = "Pass"
        next_state = self.get_next_state(state, state.board)
        if state.the_same_board(state.board, state.pre_board) and state.the_same_board(next_state.board, next_state.pre_board):
            next_value = self.evaluation(next_state, step, True, piece_type)  #two players both choose Pass
        else:
            next_value, _, _ = self.max(alpha, beta, next_state, True, step - 1, piece_type)

        if next_value < value:
            value = next_value
            action = "PASS" # move to next state

        if next_value < alpha:
            return value, action, next_point

        beta = min(beta, value)

        return value, action, next_point

    def evaluation(self, state, step, pre_is_pass, piece_type):
        value = 0
        direction = self.direction()
        values = self.score()

        for i in range(state.len):
            for j in range(state.len):
                if state.board[i][j] != 0:
                    if piece_type == 1:
                        if state.board[i][j] == piece_type:
                            value = self.score1(value, values, i, j, step)
                        else:
                            value = self.score3(value, values, i, j, step)
                    else:
                        if state.board[i][j] == piece_type:
                            value = self.score4(value, values, i, j, step)
                        else:
                            value = self.score2(value, values, i, j, step)
                    continue
                for d in direction:
                    lib_me = False
                    lin_op = False
                    neighbor = (i + d[0], j + d[1])
                    if state.board_valid(neighbor[0], neighbor[1]):
                        if not lin_op:
                            if state.board[neighbor[0]][neighbor[1]] == (3 - piece_type):
                                value = self.score5(value, values, i, j, step)
                                lin_op = True
                        if not lib_me:
                            if state.board[neighbor[0]][neighbor[1]] == piece_type:
                                value = self.score6(value, values, i, j, step)
                                lib_me = True
                        if lib_me and lin_op:
                            break

        return value + (2 * piece_type - 3) * 2.5

    def score1(self, value, values, i, j, step):
        return value + self.estimated_value(values[i][j]) / float(0.5 + self.step_value(step))

    def score2(self, value, values, i, j, step):
        return value - self.estimated_value(values[i][j]) / float(0.5 + self.step_value(step))

    def score3(self, value, values, i, j, step):
        return value - self.estimated_value(values[i][j]) / float(0.2 + self.step_value(step))

    def score4(self, value, values, i, j, step):
        return value + self.estimated_value(values[i][j]) / float(0.2 + self.step_value(step))

    def score5(self, value, values, i, j, step):
        return value - self.estimated_value(values[i][j]) * float(self.step_value(step))

    def score6(self, value, values, i, j, step):
        return value + self.estimated_value(values[i][j]) * float(self.step_value(step))


def write_output(action, x, y, path ="/Users/zhuge/Desktop/output.txt"):
    res = ""
    if action == "PASS":
        res = "PASS"
    else:
        res = str(x) + ',' + str(y)
    with open(path, 'w') as f:
        f.write(res)


def read_input(n, path ="/Users/zhuge/Desktop/input.txt"):
    with open(path, 'r') as f:
        lines = f.readlines()
        piece_type = int(lines[0])
        pre_board = [[int(x) for x in line.rstrip('\n')] for line in lines[1:n + 1]]
        board = [[int(x) for x in line.rstrip('\n')] for line in lines[n + 1: 2 * n + 1]]
        return piece_type, pre_board, board


def begin(n, board):
    for i in range(n):
        for j in range(n):
            if board[i][j] != 0:
                return True
    return False


def read_move_number(n, piece_type, board, dir ="/Users/zhuge/Desktop/move.txt"):

    if not path.exists(dir) or not begin(n, board):
        with open(dir, 'w') as f:
            move_number = piece_type-1
            f.write(str(move_number+2))
    else:
        with open(dir, 'r') as f:
            n = f.readlines()
            move_number = int(n[0])
        with open(dir, 'w') as f:
            f.write(str(move_number+2))
    return move_number


if __name__ == "__main__":
    N = 5
    limit = 20000
    player = MyPlayer(N, limit)
    action, x, y = player.function()
    write_output(action, x, y, path="/Users/zhuge/Desktop/output.txt")
