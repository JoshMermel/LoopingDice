#include <unordered_set>
#include <queue>

#include "board.h"
#include "enums.h"
#include "moves.h"

constexpr size_t num_rows = 4;
constexpr size_t num_cols = 4;
constexpr Board<num_rows, num_cols> initial = {{
  {{3,3,3,3}},
  {{3,3,3,3}},
  {{3,2,4,3}},
  {{3,3,3,0}},
}};

Mode row_mode = Mode::STATIC_3;
Mode col_mode = Mode::STATIC_3;

template<std::size_t num_rows, std::size_t num_cols>
struct Node {
  Board<num_rows, num_cols> board;
  std::string path;
  Node(const Board<num_rows, num_cols>& b, const std::string& p) : board(b), path(p) {}
};

template<std::size_t num_rows, std::size_t num_cols>
void exploreNeighbors(const Board<num_rows, num_cols>& board,
                      std::queue<Node<num_rows, num_cols>>& q,
                      std::unordered_set<Board<num_rows, num_cols>>& seen,
                      const std::string& path) {
    for (size_t row = 0; row < num_rows; ++row) {
      Board<num_rows, num_cols> forward = rowMove(board, row, true, row_mode);
      if (seen.find(forward) == seen.end()) {
        seen.insert(forward);
        q.push(Node<num_rows, num_cols>(forward, path + ",R" + std::to_string(row)));
      }

      Board<num_rows, num_cols> backward = rowMove(board, row, false, row_mode);
      if (seen.find(backward) == seen.end()) {
        seen.insert(backward);
        q.push(Node<num_rows, num_cols>(backward, path + ",R" + std::to_string(row) + "'"));
      }
    }

    for (size_t col = 0; col < num_cols; ++col) {
      Board<num_rows, num_cols> forward = colMove(board, col, true, col_mode);
      if (seen.find(forward) == seen.end()) {
        seen.insert(forward);
        q.push(Node<num_rows, num_cols>(forward, path + ",C" + std::to_string(col)));
      }

      Board<num_rows, num_cols> backward = colMove(board, col, false, col_mode);
      if (seen.find(backward) == seen.end()) {
        seen.insert(backward);
        q.push(Node<num_rows, num_cols>(backward, path + ",C" + std::to_string(col) + "'"));
      }
    }
}

template<std::size_t num_rows, std::size_t num_cols>
bool shouldPrint(const Board<num_rows, num_cols>& b) {
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

  std::queue<Node<num_rows, num_cols>> q;
  q.push(Node<num_rows, num_cols>(initial, ""));
  std::unordered_set<Board<num_rows, num_cols>> seen;
  seen.insert(initial);

  while (!q.empty()) {
    Node<num_rows, num_cols> next = q.front();
    exploreNeighbors<num_rows, num_cols>(next.board, q, seen, next.path);
    std::cout << boardToString(next.board, row_mode, "\n") 
      << std::endl << next.path << std::endl << std::endl;
    q.pop();
  }

  return 0;
}
