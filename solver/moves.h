#ifndef LOOPINGDICE_MOVES
#define LOOPINGDICE_MOVES


#include "board.h"

template<std::size_t num_rows, std::size_t num_cols>
void slideRow(Board<num_rows, num_cols>& board, int offset, bool forward) {
  // num_rows is unsigned so we convert to signed int to avoid errors when
  // offset is negative.
  int num_rows_i = num_rows;
  offset = ((offset % num_rows_i) + num_rows_i) % num_rows_i;
  if (forward) {
    int tmp = board[offset][num_cols-1];
    for (size_t col = num_cols-1; col > 0; --col) {
      board[offset][col] = board[offset][col-1];
    }
    board[offset][0] = tmp;
  } else {
    int tmp = board[offset][0];
    for (size_t col = 0; col < num_cols-1; ++col) {
      board[offset][col] = board[offset][col+1];
    }
    board[offset][num_cols-1] = tmp;
  }
}

template<std::size_t num_rows, std::size_t num_cols>
void slideCol(Board<num_rows, num_cols>& board, int offset, bool forward) {
  // num_cols is unsigned so we convert to signed int to avoid errors when
  // offset is negative.
  int num_cols_i = num_cols;
  offset = ((offset % num_cols_i) + num_cols_i) % num_cols_i;
  if (forward) {
    int tmp = board[num_rows-1][offset];
    for (size_t row = num_rows-1; row > 0; --row) {
      board[row][offset] = board[row-1][offset];
    }
    board[0][offset] = tmp;
  } else {
      int tmp = board[0][offset];
      for (size_t row = 0; row < num_rows-1; ++row) {
        board[row][offset] = board[row+1][offset];
      }
      board[num_rows-1][offset] = tmp;
  }
}

// wide
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> wideRowMove(Board<num_rows, num_cols> board, int offset, bool forward, int depth) {
  for (int i = 0; i < depth; ++i) {
    slideRow(board, offset + i, forward);
  }
  return board;
}
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> wideColMove(Board<num_rows, num_cols> board, int offset, bool forward, int depth) {
  for (int i = 0; i < depth; ++i) {
    slideCol(board, offset + i, forward);
  }
  return board;
}

// static
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> staticRowMove(Board<num_rows, num_cols> board, int offset, bool forward, int depth) {
  for (int i = 0; i < depth; ++i) {
    if (row_contains_fixed_cell(board, offset+i)) {
      return board;
    }
  }
  for (int i = 0; i < depth; ++i) {
    slideRow(board, offset + i, forward);
  }
  return board;
}
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> staticColMove(Board<num_rows, num_cols> board, int offset, bool forward, int depth) {
  for (int i = 0; i < depth; ++i) {
    if (col_contains_fixed_cell(board, offset+i)) {
      return board;
    }
  }
  for (int i = 0; i < depth; ++i) {
    slideCol(board, offset + i, forward);
  }
  return board;
}

// enabler
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> enablerRowMove(Board<num_rows, num_cols> board, int offset, bool forward) {
  if (row_contains_enabler(board, offset)) {
    slideRow(board, offset, forward);
  }
  return board;
}
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> enablerColMove(Board<num_rows, num_cols> board, int offset, bool forward) {
  if (col_contains_enabler(board, offset)) {
    slideCol(board, offset, forward);
  }
  return board;
}

// dynamic
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> dynamicRowMove(Board<num_rows, num_cols> board, int offset, bool forward) {
  size_t end = forward ? num_cols - 1 : 0;
  if (board[offset][end] > 0) {
    slideRow(board, offset, forward);
  }
  return board;
}
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> dynamicColMove(Board<num_rows, num_cols> board, int offset, bool forward) {
  size_t end = forward ? num_rows - 1 : 0;
  if (board[end][offset] > 0) {
    slideCol(board, offset, forward);
  }
  return board;
}

// gear
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> gearRowMove(Board<num_rows, num_cols> board, int offset, bool forward) {
  slideRow(board, offset, forward);
  slideRow(board, offset+1, !forward);
  return board;
}
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> gearColMove(Board<num_rows, num_cols> board, int offset, bool forward) {
  slideCol(board, offset, forward);
  slideCol(board, offset+1, !forward);
  return board;
}

// carousel
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> carouselRowMove(Board<num_rows, num_cols> board, int offset, bool forward) {
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
    board[right_idx][col] = board[right_idx][col-1];
  }
  for (size_t col = 0; col < num_cols-1; ++col) {
    board[left_idx][col] = board[left_idx][col+1];
  }

  board[right_idx][0] = left_end;
  board[left_idx][num_cols - 1] = right_end;

  return board;
}
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> carouselColMove(Board<num_rows, num_cols> board, int offset, bool forward) {
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

  for (size_t row = num_rows-1; row > 0; --row) {
    board[row][down_idx] = board[row-1][down_idx];
  }

  for (size_t row = 0; row < num_rows-1; ++row) {
    board[row][up_idx] = board[row+1][up_idx];
  }

  board[num_rows - 1][up_idx] = down_end;
  board[0][down_idx] = up_end;


  return board;
}

// bandaged
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> bandagedRowMove(Board<num_rows, num_cols> board, int offset, bool forward) {
  slideRow(board, offset, forward); 
  size_t depth = 1;
  // sweep up
  while (depth < num_rows && row_contains_bond(board, offset - 1, DOWN)) {
    slideRow(board, offset - 1, forward); 
    depth += 1;
    offset -= 1;
  }

  // sweep down
  while (depth < num_rows && row_contains_bond(board, offset + depth, UP)) {
    slideRow(board, offset + depth, forward); 
    depth += 1;
  }

  return board;
}
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> bandagedColMove(Board<num_rows, num_cols> board, int offset, bool forward) {
  size_t depth = 1;
  slideCol(board, offset, forward); 
  // sweep left
  while (depth < num_cols && col_contains_bond(board, offset - 1, RIGHT)) {
    slideCol(board, offset - 1, forward);
    depth += 1;
    offset -= 1;
  }

  // sweep right
  while (depth < num_cols && col_contains_bond(board, offset + depth , LEFT)) {
    slideCol(board, offset + depth, forward);
    depth += 1;
  }

  return board;
}

// axis locked
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> axisRowMove(Board<num_rows, num_cols> board, int offset, bool forward) {
  if (!row_contains_axis_locked_cell(board, offset)) {
    slideRow(board, offset, forward);
  }
  return board;
}
template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> axisColMove(Board<num_rows, num_cols> board, int offset, bool forward) {
  if (!col_contains_axis_locked_cell(board, offset)) {
    slideCol(board, offset, forward);
  }
  return board;
}

template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> rowMove(Board<num_rows, num_cols> board, int offset, bool forward, const Mode& mode) {
  switch (mode) {
    case Mode::WIDE_2:
      return wideRowMove(board, offset, forward, 2);
    case Mode::WIDE_3:
      return wideRowMove(board, offset, forward, 3);
    case Mode::WIDE_4:
      return wideRowMove(board, offset, forward, 4);
    case Mode::GEAR:
      return gearRowMove(board, offset, forward);
    case Mode::CAROUSEL:
      return carouselRowMove(board, offset, forward);
    case Mode::BANDAGED:
      return bandagedRowMove(board, offset, forward);
    case Mode::ENABLER:
      return enablerRowMove(board, offset, forward);
    case Mode::DYNAMIC:
      return dynamicRowMove(board, offset, forward);
    case Mode::STATIC_1:
      return staticRowMove(board, offset, forward, 1);
    case Mode::STATIC_2:
      return staticRowMove(board, offset, forward, 2);
    case Mode::STATIC_3:
      return staticRowMove(board, offset, forward, 3);
    case Mode::AXIS:
      return axisRowMove(board, offset, forward);
    default:
      return wideRowMove(board, offset, forward, 1);
  }
}

template<std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> colMove(Board<num_rows, num_cols> board, int offset, bool forward, const Mode& mode) {
  switch (mode) {
    case Mode::WIDE_2:
      return wideColMove(board, offset, forward, 2);
    case Mode::WIDE_3:
      return wideColMove(board, offset, forward, 3);
    case Mode::WIDE_4:
      return wideColMove(board, offset, forward, 4);
    case Mode::GEAR:
      return gearColMove(board, offset, forward);
    case Mode::CAROUSEL:
      return carouselColMove(board, offset, forward);
    case Mode::BANDAGED:
      return bandagedColMove(board, offset, forward);
    case Mode::ENABLER:
      return enablerColMove(board, offset, forward);
    case Mode::DYNAMIC:
      return dynamicColMove(board, offset, forward);
    case Mode::STATIC_1:
      return staticColMove(board, offset, forward, 1);
    case Mode::STATIC_2:
      return staticColMove(board, offset, forward, 2);
    case Mode::STATIC_3:
      return staticColMove(board, offset, forward, 3);
    case Mode::AXIS:
      return axisColMove(board, offset, forward);
    default:
      return wideColMove(board, offset, forward, 1);
  }
}

#endif
