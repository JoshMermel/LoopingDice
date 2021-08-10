#ifndef LOOPINGDICE_LIGHTNING_MOVES
#define LOOPINGDICE_LIGHTNING_MOVES

// Helpers for simulating and validating lightning moves.

#include "board.h"

template <std::size_t num_rows, std::size_t num_cols>
bool validateLightningRowMove(const Board<num_rows, num_cols> &board,
                              int offset, bool forward,
                              const Validation &validation) {
  if ((validation & Validation::STATIC) != Validation::NONE) {
    if (row_contains_fixed_cell(board, offset)) {
      return false;
    }
  }
  if ((validation & Validation::DYNAMIC) != Validation::NONE) {
    size_t end = forward ? num_cols - 1 : 0;
    size_t pre_end = forward ? num_cols - 2 : 1;
    if ((board[offset][end] & FIXED) ||
        (num_cols > 1 && row_contains_lightning(board, offset) &&
         board[offset][pre_end] & FIXED)) {
      return false;
    }
  }
  if ((validation & Validation::ARROWS) != Validation::NONE) {
    if (row_contains_arrows_cell(board, offset)) {
      return false;
    }
  }
  if ((validation & Validation::ENABLER) != Validation::NONE) {
    if (!row_contains_enabler(board, offset)) {
      return false;
    }
  }

  return true;
}

template <std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> lightningRowMove(Board<num_rows, num_cols> board,
                                           int offset, bool forward,
                                           const Validation &validation) {
  if (!validateLightningRowMove(board, offset, forward, validation)) {
    return board;
  }

  slideRow(board, offset, forward);
  if (row_contains_lightning(board, offset)) {
    slideRow(board, offset, forward);
  }
  return board;
}

template <std::size_t num_rows, std::size_t num_cols>
bool validateLightningColMove(const Board<num_rows, num_cols> &board,
                              int offset, bool forward,
                              const Validation &validation) {
  if ((validation & Validation::STATIC) != Validation::NONE) {
    if (col_contains_fixed_cell(board, offset)) {
      return false;
    }
  }
  if ((validation & Validation::DYNAMIC) != Validation::NONE) {
    size_t end = forward ? num_rows - 1 : 0;
    size_t pre_end = forward ? num_rows - 2 : 1;
    if ((board[end][offset] & FIXED) ||
        (num_cols > 1 && col_contains_lightning(board, offset) &&
         board[pre_end][offset] & FIXED)) {
      return false;
    }
  }
  if ((validation & Validation::ARROWS) != Validation::NONE) {
    if (col_contains_arrows_cell(board, offset)) {
      return false;
    }
  }
  if ((validation & Validation::ENABLER) != Validation::NONE) {
    if (!col_contains_enabler(board, offset)) {
      return false;
    }
  }

  return true;
}

template <std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> lightningColMove(Board<num_rows, num_cols> board,
                                           int offset, bool forward,
                                           const Validation &validation) {
  if (!validateLightningColMove(board, offset, forward, validation)) {
    return board;
  }

  slideCol(board, offset, forward);
  if (col_contains_lightning(board, offset)) {
    slideCol(board, offset, forward);
  }
  return board;
}

#endif
