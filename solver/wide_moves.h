#ifndef LOOPINGDICE_WIDE_MOVES
#define LOOPINGDICE_WIDE_MOVES

// Helpers for simulating and validating wide moves.

#include "board.h"

template <std::size_t num_rows, std::size_t num_cols>
bool validateWideRowMove(const Board<num_rows, num_cols> &board, int offset,
                         bool forward, const Validation &validation,
                         int depth) {
  if (validation == Validation::STATIC) {
    for (int i = 0; i < depth; ++i) {
      if (row_contains_fixed_cell(board, offset + i)) {
        return false;
      }
    }
  } else if (validation == Validation::ENABLER) {
    for (int i = 0; i < depth; ++i) {
      if (row_contains_enabler(board, offset + i)) {
        return true;
      }
    }
    return false;
  } else if (validation == Validation::DYNAMIC) {
    for (int i = 0; i < depth; ++i) {
      if (row_ends_in_fixed(board, offset + i, forward)) {
        return false;
      }
    }
  } else if (validation == Validation::ARROWS) {
    for (int i = 0; i < depth; ++i) {
      if (row_contains_arrows_cell(board, offset + i)) {
        return false;
      }
    }
  }

  return true;
}

template <std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> wideRowMove(Board<num_rows, num_cols> board,
                                      int offset, bool forward,
                                      const Validation &validation, int depth) {
  if (!validateWideRowMove(board, offset, forward, validation, depth)) {
    return board;
  }

  for (int i = 0; i < depth; ++i) {
    slideRow(board, offset + i, forward);
  }
  return board;
}

template <std::size_t num_rows, std::size_t num_cols>
bool validateWideColMove(const Board<num_rows, num_cols>& board, int offset,
                         bool forward, const Validation &validation,
                         int depth) {
  if (validation == Validation::STATIC) {
    for (int i = 0; i < depth; ++i) {
      if (col_contains_fixed_cell(board, offset + i)) {
        return false;
      }
    }
  } else if (validation == Validation::ENABLER) {
    for (int i = 0; i < depth; ++i) {
      if (col_contains_enabler(board, offset + i)) {
        return true;
      }
    }
    return false;
  } else if (validation == Validation::DYNAMIC) {
    for (int i = 0; i < depth; ++i) {
      if (col_ends_in_fixed(board, offset + i, forward)) {
        return false;
      }
    }
  } else if (validation == Validation::ARROWS) {
    for (int i = 0; i < depth; ++i) {
      if (col_contains_arrows_cell(board, offset + i)) {
        return false;
      }
    }
  }

  return true;
}

template <std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> wideColMove(Board<num_rows, num_cols> board,
                                      int offset, bool forward,
                                      const Validation &validation, int depth) {
  if (!validateWideColMove(board, offset, forward, validation, depth)) {
    return board;
  }

  for (int i = 0; i < depth; ++i) {
    slideCol(board, offset + i, forward);
  }
  return board;
}

#endif
