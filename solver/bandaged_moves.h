#ifndef LOOPINGDICE_BANDAGED_MOVES
#define LOOPINGDICE_BANDAGED_MOVES

// Helpers for simulating wide moves. No validation is needed here since that is
// delegated to wide moves.

#include "board.h"

template <std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> bandagedRowMove(Board<num_rows, num_cols> board,
                                          int offset, bool forward,
                                          const Validation &validation) {
  size_t depth = 1;
  // sweep up
  while (depth < num_rows && row_contains_bond(board, offset - 1, DOWN)) {
    depth += 1;
    offset -= 1;
  }

  // sweep down
  while (depth < num_rows && row_contains_bond(board, offset + depth, UP)) {
    depth += 1;
  }

  return wideRowMove(board, offset, forward, validation, depth);
}

template <std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> bandagedColMove(Board<num_rows, num_cols> board,
                                          int offset, bool forward,
                                          const Validation &validation) {
  size_t depth = 1;
  // sweep left
  while (depth < num_cols && col_contains_bond(board, offset - 1, RIGHT)) {
    depth += 1;
    offset -= 1;
  }

  // sweep right
  while (depth < num_cols && col_contains_bond(board, offset + depth, LEFT)) {
    depth += 1;
  }

  return wideColMove(board, offset, forward, validation, depth);
}

#endif
