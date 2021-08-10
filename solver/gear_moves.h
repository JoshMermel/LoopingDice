#ifndef LOOPINGDICE_GEAR_MOVES
#define LOOPINGDICE_GEAR_MOVES

// Helpers for simulating and validating gear moves.

#include "board.h"

template <std::size_t num_rows, std::size_t num_cols>
bool validateGearRowMove(const Board<num_rows, num_cols> &board, int offset,
                         bool forward, const Validation &validation) {
  if ((validation & Validation::STATIC) != Validation::NONE) {
    if (row_contains_fixed_cell(board, offset) ||
        (row_contains_fixed_cell(board, offset + 1))) {
      return false;
    }
  }
  if ((validation & Validation::DYNAMIC) != Validation::NONE) {
    if (row_ends_in_fixed(board, offset, forward) ||
        row_ends_in_fixed(board, offset + 1, !forward)) {
      return false;
    }
  }
  if ((validation & Validation::ARROWS) != Validation::NONE) {
    if (row_contains_arrows_cell(board, offset) ||
        row_contains_arrows_cell(board, offset + 1)) {
      return false;
    }
  }
  if ((validation & Validation::ENABLER) != Validation::NONE) {
    if (!row_contains_enabler(board, offset) &&
        !row_contains_enabler(board, offset + 1)) {
      return false;
    }
  }

  return true;
}

template <std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> gearRowMove(Board<num_rows, num_cols> board,
                                      int offset, bool forward,
                                      const Validation &validation) {
  if (!validateGearRowMove(board, offset, forward, validation)) {
    return board;
  }

  slideRow(board, offset, forward);
  slideRow(board, offset + 1, !forward);
  return board;
}

template <std::size_t num_rows, std::size_t num_cols>
bool validateGearColMove(const Board<num_rows, num_cols> &board, int offset,
                         bool forward, const Validation &validation) {
  if ((validation & Validation::STATIC) != Validation::NONE) {
    if (col_contains_fixed_cell(board, offset) ||
        (col_contains_fixed_cell(board, offset + 1))) {
      return false;
    }
  }
  if ((validation & Validation::DYNAMIC) != Validation::NONE) {
    if (col_ends_in_fixed(board, offset, forward) ||
        col_ends_in_fixed(board, offset + 1, !forward)) {
      return false;
    }
  }
  if ((validation & Validation::ARROWS) != Validation::NONE) {
    if (col_contains_arrows_cell(board, offset) ||
        col_contains_arrows_cell(board, offset + 1)) {
      return false;
    }
  }
  if ((validation & Validation::ENABLER) != Validation::NONE) {
    if (!col_contains_enabler(board, offset) &&
        !col_contains_enabler(board, offset + 1)) {
      return false;
    }
  }

  return true;
}

template <std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> gearColMove(Board<num_rows, num_cols> board,
                                      int offset, bool forward,
                                      const Validation &validation) {
  if (!validateGearColMove(board, offset, forward, validation)) {
    return board;
  }

  slideCol(board, offset, forward);
  slideCol(board, offset + 1, !forward);
  return board;
}


#endif
