// Lazily implemented scrambler that picks random row/col moves and doesn't
// consider validity.

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
  {{1|RIGHT,1|LEFT,2,2}},
  {{1,1,2,2}},
  {{1,1,1,1|DOWN}},
  {{1,1,1,1|UP}},
}};
Mode row_mode = Mode::BANDAGED;
Mode col_mode = Mode::BANDAGED;

template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> randomMove(Board<num_rows, num_cols> board) {
  unsigned int seed = rand() % (2 * (num_rows + num_cols));
  bool direction = seed % 2;
  seed /= 2;
  if (seed <= num_rows) {
    return rowMove(board, seed, direction, row_mode);
  } else {
    return colMove(board, seed - num_rows, direction, col_mode);
  }
}


int main () {
  if (!isCompatible(row_mode, col_mode)) {
    std::cout << "Row mode and col mode are not compatible" << std::endl;
    return 1;
  }
  srand (time(NULL));

  Board<num_rows, num_cols> my_board = initial;
  for (int i = 0; i < 100000; ++i) {
    my_board = randomMove(my_board);
  }
  std::cout << boardToString(my_board, row_mode, "\n") << std::endl;

  return 0;
}
