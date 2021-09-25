#ifndef LOOPINGDICE_CAROUSEL_MOVES
#define LOOPINGDICE_CAROUSEL_MOVES

// Helpers for simulating and validating carousel moves.

#include "board.h"

template <std::size_t num_rows, std::size_t num_cols>
bool validateCarouselRowMove(const Board<num_rows, num_cols> &board, int offset,
                         bool forward, const Validation &validation) {
  if ((validation & Validation::STATIC) != Validation::NONE) {
    if (row_contains_fixed_cell(board, offset) ||
        (row_contains_fixed_cell(board, offset + 1))) {
      return false;
    }
  }
  if ((validation & Validation::DYNAMIC) != Validation::NONE) {
    if (offset == num_rows - 1) {
      if ((forward && board[0][0] & FIXED) ||
          (forward && board[num_rows - 1][num_cols - 1] & FIXED) ||
          (!forward && board[0][num_cols - 1] & FIXED) ||
          (!forward && board[num_rows - 1][0] & FIXED)) {
        return false;
      }
    }
  }
  if ((validation & Validation::ARROWS) != Validation::NONE) {
    int right_idx = offset;
    int left_idx = offset;

    if (forward) {
      left_idx += 1;
      left_idx %= num_rows;
    } else {
      right_idx += 1;
      right_idx %= num_rows;
    }

    if (board[left_idx][0] & HORIZ || board[right_idx][num_cols - 1] & HORIZ) {
      return false;
    }
    for (size_t col = 0; col < num_cols - 1; ++col) {
      if (board[right_idx][col] & VERT) {
        return false;
      }
    }
    for (size_t col = 1; col < num_cols; ++col) {
      if (board[left_idx][col] & VERT) {
        return false;
      }
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
Board<num_rows, num_cols> carouselRowMove(Board<num_rows, num_cols> board,
                                          int offset, bool forward,
                                          const Validation &validation) {
  if (!validateCarouselRowMove(board, offset, forward, validation)) {
    return board;
  }

  int right_idx = offset;
  int left_idx = offset;

  if (forward) {
    left_idx += 1;
    left_idx %= num_rows;
  } else {
    right_idx += 1;
    right_idx %= num_rows;
  }

  int right_end = board[right_idx][num_cols - 1];
  int left_end = board[left_idx][0];

  for (size_t col = num_cols - 1; col > 0; --col) {
    board[right_idx][col] = board[right_idx][col - 1];
  }
  for (size_t col = 0; col < num_cols - 1; ++col) {
    board[left_idx][col] = board[left_idx][col + 1];
  }

  board[right_idx][0] = left_end;
  board[left_idx][num_cols - 1] = right_end;

  return board;
}

template <std::size_t num_rows, std::size_t num_cols>
bool validateCarouselColMove(const Board<num_rows, num_cols> &board, int offset,
                         bool forward, const Validation &validation) {
  if ((validation & Validation::STATIC) != Validation::NONE) {
    if (col_contains_fixed_cell(board, offset) ||
        (col_contains_fixed_cell(board, offset + 1))) {
      return false;
    }
  }
  if ((validation & Validation::DYNAMIC) != Validation::NONE) {
    if (offset == num_cols - 1) {
      if ((forward && board[0][0] & FIXED) ||
          (forward && board[num_rows - 1][num_cols - 1] & FIXED) ||
          (!forward && board[0][num_cols - 1] & FIXED) ||
          (!forward && board[num_rows - 1][0] & FIXED)) {
        return false;
      }
    }
  }
  if ((validation & Validation::ARROWS) != Validation::NONE) {
    int down_idx = offset;
    int up_idx = offset;

    if (forward) {
      up_idx += 1;
      up_idx %= num_cols;
    } else {
      down_idx += 1;
      down_idx %= num_cols;
    }

    if (board[0][up_idx] & VERT || board[num_rows - 1][down_idx] & VERT) {
      return false;
    }

    for (size_t row = 0; row < num_rows - 1; ++row) {
      if (board[row][down_idx] & HORIZ) {
        return false;
      }
    }
    for (size_t row = 1; row < num_rows; ++row) {
      if (board[row][up_idx] & HORIZ) {
        return false;
      }
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
Board<num_rows, num_cols> carouselColMove(Board<num_rows, num_cols> board,
                                          int offset, bool forward,
                                          const Validation &validation) {
  
  if (!validateCarouselColMove(board, offset, forward, validation)) {
    return board;
  }

  int down_idx = offset;
  int up_idx = offset;

  if (forward) {
    up_idx += 1;
    up_idx %= num_cols;
  } else {
    down_idx += 1;
    down_idx %= num_cols;
  }

  int up_end = board[0][up_idx];
  int down_end = board[num_rows - 1][down_idx];

  for (size_t row = num_rows - 1; row > 0; --row) {
    board[row][down_idx] = board[row - 1][down_idx];
  }

  for (size_t row = 0; row < num_rows - 1; ++row) {
    board[row][up_idx] = board[row + 1][up_idx];
  }

  board[num_rows - 1][up_idx] = down_end;
  board[0][down_idx] = up_end;

  return board;
}

#endif
