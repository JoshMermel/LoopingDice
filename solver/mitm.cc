#include <iostream>
#include <memory>
#include <unordered_map>

#include <queue>
#include "board.h"
#include "enums.h"
#include "moves.h"

// Global settings
constexpr size_t num_rows = 4;
constexpr size_t num_cols = 4;
constexpr Board<num_rows, num_cols> initial = {{
	{{0|UP,3,3,3}},
	{{2,0|UP,3,3}},
	{{2,2,0|UP,3}},
	{{2,2,2,0|UP}},
}};
constexpr Board<num_rows, num_cols> win = {{
	{{0|UP,2,2,2}},
	{{3,0|UP,2,2}},
	{{3,3,0|UP,2}},
	{{3,3,3,0|UP}},
}};
Mode row_mode = Mode::LIGHTNING;
Mode col_mode = Mode::LIGHTNING;

template<std::size_t num_rows, std::size_t num_cols>
struct Node {
  Board<num_rows, num_cols> board;
  std::vector<std::string> path;
  Node(const Board<num_rows, num_cols>& b, const std::vector<std::string>& p) 
    : board(b), path(p) {}
};

std::vector<std::string> make_path(std::vector<std::string> path,
    const std::string& move) {
  path.push_back(move);
  return path;
}

// helpers for doing bfs
template<std::size_t num_rows, std::size_t num_cols>
std::vector<Node<num_rows, num_cols>> exploreNeighbors(
    const Board<num_rows, num_cols>& board,
    const std::vector<std::string>& path) {
  std::vector<Node<num_rows, num_cols>> ret;

  for (size_t row = 0; row < num_rows; ++row) {
    Board<num_rows, num_cols> forward = rowMove(board, row, true, row_mode);
    ret.push_back(Node<num_rows, num_cols>(
          forward, make_path(path,"R" + std::to_string(row))));

    Board<num_rows, num_cols> backward = rowMove(board, row, false, row_mode);
    ret.push_back(
        Node<num_rows, num_cols>(backward, make_path(path, "R" + std::to_string(row) + "'")));
  }

  for (size_t col = 0; col < num_cols; ++col) {
    Board<num_rows, num_cols> forward = colMove(board, col, true, col_mode);
    ret.push_back(
        Node<num_rows, num_cols>(forward, make_path(path, "C" + std::to_string(col))));

    Board<num_rows, num_cols> backward = colMove(board, col, false, col_mode);
    ret.push_back(
        Node<num_rows, num_cols>(backward, make_path(path, "C" + std::to_string(col) + "'")));
  }

  return ret;
}

template<std::size_t num_rows, std::size_t num_cols>
std::vector<Node<num_rows, num_cols>> exploreNeighborsBackward(
    const Board<num_rows, num_cols>& board,
    const std::vector<std::string>& path) {
  std::vector<Node<num_rows, num_cols>> ret;

  for (size_t row = 0; row < num_rows; ++row) {
    Board<num_rows, num_cols> forward = rowMove(board, row, true, row_mode);
    ret.push_back(
        Node<num_rows, num_cols>(forward, make_path(path, "R" + std::to_string(row) + "'")));

    Board<num_rows, num_cols> backward = rowMove(board, row, false, row_mode);
    ret.push_back(
        Node<num_rows, num_cols>(backward, make_path(path,"R" + std::to_string(row))));
  }

  for (size_t col = 0; col < num_cols; ++col) {
    Board<num_rows, num_cols> forward = colMove(board, col, true, col_mode);
    ret.push_back(
        Node<num_rows, num_cols>(forward, make_path(path, "C" + std::to_string(col) + "'")));

    Board<num_rows, num_cols> backward = colMove(board, col, false, col_mode);
    ret.push_back(
        Node<num_rows, num_cols>(backward, make_path(path, "C" + std::to_string(col))));
  }

  return ret;
}

void printSolution(const std::vector<std::string>& fwd,
    const std::vector<std::string>& bwd) {
  std::cout << "# "
    << absl::StrJoin(fwd, ",")
    << "," 
    << absl::StrJoin(bwd.rbegin(), bwd.rend(), ",")
    << " (" << fwd.size() + bwd.size() << ")"
    << std::endl;
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
  if (!sameElements(initial, win)) {
    std::cout << "Initial and win must contain the same elements" << std::endl;
    return 4;
  }

  std::cout << num_rows << std::endl
    << num_cols << std::endl
    << modesToString(row_mode, col_mode) << std::endl
    << boardToString(initial, row_mode, ",") << std::endl
    << boardToString(win, row_mode, ",") << std::endl;

  std::queue<Node<num_rows, num_cols>> fwd_q;
  fwd_q.push(Node<num_rows, num_cols>(initial, {}));
  std::unordered_map<Board<num_rows, num_cols>, std::vector<std::string>> fwd_seen;
  fwd_seen[initial] = {};

  std::queue<Node<num_rows, num_cols>> bwd_q;
  bwd_q.push(Node<num_rows, num_cols>(win, {}));
  std::unordered_map<Board<num_rows, num_cols>, std::vector<std::string>> bwd_seen;
  bwd_seen[win] = {};

  unsigned int target_depth = 1;
  
  while (!fwd_q.empty() || !bwd_q.empty()) {
    // Explore forward.
    while (!fwd_q.empty() && fwd_q.front().path.size() < target_depth) {
      Node<num_rows, num_cols> next = fwd_q.front();
      auto neighbors = exploreNeighbors(next.board, next.path);
      for (const auto& neighbor : neighbors) {
        // New cell, note we've seen it and add it to the queue to explore more
        if (fwd_seen.find(neighbor.board) == fwd_seen.end()) {
          fwd_seen[neighbor.board] = neighbor.path;
          fwd_q.push(Node<num_rows, num_cols>(neighbor.board, neighbor.path));
          if (bwd_seen.find(neighbor.board) != bwd_seen.end()) {
            printSolution(neighbor.path, bwd_seen[neighbor.board]);
            return 0;
          }
        }
      }
      fwd_q.pop();
    }

    // Explore backward.
    while (!bwd_q.empty() && bwd_q.front().path.size() < target_depth) {
      Node<num_rows, num_cols> next = bwd_q.front();
      auto neighbors = exploreNeighborsBackward(next.board, next.path);
      for (const auto& neighbor : neighbors) {
        if (bwd_seen.find(neighbor.board) == bwd_seen.end()) {
          // New cell, note we've seen it and add it to the queue to explore more
          bwd_seen[neighbor.board] = neighbor.path;
          bwd_q.push(Node<num_rows, num_cols>(neighbor.board, neighbor.path));
          if (fwd_seen.find(neighbor.board) != bwd_seen.end()) {
            printSolution(fwd_seen[neighbor.board], neighbor.path);
            return 0;
          }
        }
      }
      bwd_q.pop();
    }

    ++target_depth;
  }


  return 0;
}
