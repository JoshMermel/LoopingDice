#include <unordered_set>
#include <queue>

#include "board.h"
#include "enums.h"
#include "moves.h"

constexpr size_t num_rows = 4;
constexpr size_t num_cols = 2;
constexpr Board<num_rows, num_cols> initial = {{
  {{0,0}},
  {{1,1}},
  {{2,2}},
  {{3,3}},
}};

Mode row_mode = Mode::GEAR;
Mode col_mode = Mode::GEAR;
Validation validation = Validation::NONE;

template <std::size_t num_rows, std::size_t num_cols> struct Node {
  Board<num_rows, num_cols> board;
  std::string path;
  Node(const Board<num_rows, num_cols> &b, const std::string &p)
      : board(b), path(p) {}
};

template<std::size_t num_rows, std::size_t num_cols>
void exploreNeighbors(const Board<num_rows, num_cols>& board,
                      std::queue<Node<num_rows, num_cols>>& q,
                      std::unordered_set<Board<num_rows, num_cols>>& seen,
                      const std::string& path) {
    for (size_t row = 0; row < num_rows; ++row) {
      Board<num_rows, num_cols> forward = rowMove(board, row, true, row_mode, validation);
      if (seen.find(forward) == seen.end()) {
        seen.insert(forward);
        q.push(Node<num_rows, num_cols>(forward, path + ",R" + std::to_string(row)));
      }

      Board<num_rows, num_cols> backward = rowMove(board, row, false, row_mode, validation);
      if (seen.find(backward) == seen.end()) {
        seen.insert(backward);
        q.push(Node<num_rows, num_cols>(backward, path + ",R" + std::to_string(row) + "'"));
      }
    }

    for (size_t col = 0; col < num_cols; ++col) {
      Board<num_rows, num_cols> forward = colMove(board, col, true, col_mode, validation);
      if (seen.find(forward) == seen.end()) {
        seen.insert(forward);
        q.push(Node<num_rows, num_cols>(forward, path + ",C" + std::to_string(col)));
      }

      Board<num_rows, num_cols> backward = colMove(board, col, false, col_mode, validation);
      if (seen.find(backward) == seen.end()) {
        seen.insert(backward);
        q.push(Node<num_rows, num_cols>(backward, path + ",C" + std::to_string(col) + "'"));
      }
    }
}

template<std::size_t num_rows, std::size_t num_cols>
int diff(const Board<num_rows, num_cols>& b1, const Board<num_rows, num_cols>& b2) {
  int ret = 0;
  for (size_t row = 0; row < num_rows; ++row) {
    for (size_t col = 0; col < num_cols; ++col) {
      if (b1[row][col] != b2[row][col]) {
        ++ret;
      }
    }
  }
  return ret;
}

template<std::size_t num_rows, std::size_t num_cols>
bool shouldPrint(const Board<num_rows, num_cols>& b) {
  return true;
  for (size_t row = 0; row < num_rows; ++row) {
    for (size_t col = 0; col < num_cols; ++col) {
      if (b[row][col] != b[col][row]) {
        return false;
      }
    }
  }
  return true;
}

int main () {
  if (!isCompatible(row_mode, col_mode)) {
    std::cout << "Row mode and col mode are not compatible" << std::endl;
    return 1;
  }
  if (!moveFits(row_mode, num_rows)) {
    std::cout << "Row move affects too many rows" << std::endl;
    return 2;
  }
  if (!moveFits(col_mode, num_cols)) {
    std::cout << "col move affects too many rows" << std::endl;
    return 3;
  }

  std::queue<Node<num_rows, num_cols>> q;
  q.push(Node<num_rows, num_cols>(initial, ""));
  std::unordered_set<Board<num_rows, num_cols>> seen;
  seen.insert(initial);

  while (!q.empty()) {
    Node<num_rows, num_cols> next = q.front();
    exploreNeighbors<num_rows, num_cols>(next.board, q, seen, next.path);
    if (shouldPrint(next.board))
      std::cout << boardToString(next.board, row_mode, "\n") 
        << std::endl << next.path << std::endl 
        << diff(next.board, initial) << std::endl
        << std::endl;
    q.pop();
  }

  return 0;
}
