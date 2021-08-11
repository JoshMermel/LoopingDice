// Defines a board and a hash function for it.
#ifndef LOOPINGDICE_BOARD
#define LOOPINGDICE_BOARD

#include <array>
#include "absl/strings/str_join.h"
#include "enums.h"
#include <iostream>

// A board is a 2D array of ints that can be hashed and printed.
template<std::size_t num_rows, std::size_t num_cols>
using Board = std::array<std::array<int, num_cols>, num_rows>;

// Taken from stack overflow to make these hashable
namespace std
{
    template<typename T, size_t N>
    struct hash<array<T, N> >
    {
        typedef array<T, N> argument_type;
        typedef size_t result_type;

        result_type operator()(const argument_type& a) const
        {
            hash<T> hasher;
            result_type h = 0;
            for (result_type i = 0; i < N; ++i)
            {
                h = h * 31 + hasher(a[i]);
            }
            return h;
        }
    };
}

template <std::size_t num_rows, std::size_t num_cols>
void slideRow(Board<num_rows, num_cols> &board, int offset, bool forward) {
  // num_rows is unsigned so we convert to signed int to avoid errors when
  // offset is negative.
  int num_rows_i = num_rows;
  offset = ((offset % num_rows_i) + num_rows_i) % num_rows_i;
  if (forward) {
    int tmp = board[offset][num_cols - 1];
    for (size_t col = num_cols - 1; col > 0; --col) {
      board[offset][col] = board[offset][col - 1];
    }
    board[offset][0] = tmp;
  } else {
    int tmp = board[offset][0];
    for (size_t col = 0; col < num_cols - 1; ++col) {
      board[offset][col] = board[offset][col + 1];
    }
    board[offset][num_cols - 1] = tmp;
  }
}

template <std::size_t num_rows, std::size_t num_cols>
void slideCol(Board<num_rows, num_cols> &board, int offset, bool forward) {
  // num_cols is unsigned so we convert to signed int to avoid errors when
  // offset is negative.
  int num_cols_i = num_cols;
  offset = ((offset % num_cols_i) + num_cols_i) % num_cols_i;
  if (forward) {
    int tmp = board[num_rows - 1][offset];
    for (size_t row = num_rows - 1; row > 0; --row) {
      board[row][offset] = board[row - 1][offset];
    }
    board[0][offset] = tmp;
  } else {
    int tmp = board[0][offset];
    for (size_t row = 0; row < num_rows - 1; ++row) {
      board[row][offset] = board[row + 1][offset];
    }
    board[num_rows - 1][offset] = tmp;
  }
}


// Helper for converting a cell to a string
std::string cellToString(int i) {
  std::vector<std::string> ret;
  if (i & UP) {
    ret.push_back("U");
  }
  if (i & DOWN) {
    ret.push_back("D");
  }
  if (i & LEFT) {
    ret.push_back("L");
  }
  if (i & RIGHT) {
    ret.push_back("R");
  }
  if (i & ENABLER) {
    ret.push_back("E");
  }
  if (i & FIXED) {
    ret.push_back("F");
  }
  if (i & LIGHTNING) {
    ret.push_back("B");
  }
  if (i & HORIZ) {
    ret.push_back("H");
  }
  if (i & VERT) {
    ret.push_back("V");
  }

  int mask = UP | DOWN | LEFT | RIGHT | ENABLER | FIXED | LIGHTNING | HORIZ | VERT;
  ret.push_back(std::to_string(i & ~mask));

  return absl::StrJoin(ret, " ");
}

// Helpers for converting a row to a string.
template<std::size_t len>
struct rowFormatter {
  void operator()(std::string* out, const std::array<int, len>& arr) const {
    out->append(absl::StrJoin(arr, ",", [](std::string* out, int i) {
          out->append(cellToString(i));
     }));
  }
};

// Prints the board
template<std::size_t num_rows, std::size_t num_cols>
std::string boardToString(const Board<num_rows, num_cols>& board, const Mode& mode, const std::string& separator) {
  return absl::StrJoin(board, separator, rowFormatter<num_cols>());
}

// Checks for enablers in a row/col
template<std::size_t num_rows, std::size_t num_cols>
bool row_contains_enabler(const Board<num_rows, num_cols>& board, int offset) {
  offset = (offset + num_rows) % num_rows;
  for (size_t col = 0; col < num_cols; ++col) {
    if(board[offset][col] & ENABLER) {
      return true;
    }
  }
  return false;
}
template<std::size_t num_rows, std::size_t num_cols>
bool col_contains_enabler(const Board<num_rows, num_cols>& board, int offset) {
  offset = (offset + num_cols) % num_cols;
  for (size_t row = 0; row < num_rows; ++row) {
    if(board[row][offset] & ENABLER) {
      return true;
    }
  }
  return false;
}

// Checks if a row/col ends in a fixed cell, when swiped in |forward| direction.
template<std::size_t num_rows, std::size_t num_cols>
bool row_ends_in_fixed(const Board<num_rows, num_cols>& board, int offset, bool forward) {
  size_t end = forward ? num_cols - 1 : 0;
  offset = (offset + num_rows) % num_rows;
  return board[offset][end] & FIXED;
}
template<std::size_t num_rows, std::size_t num_cols>
bool col_ends_in_fixed(const Board<num_rows, num_cols>& board, int offset, bool forward) {
  size_t end = forward ? num_rows - 1 : 0;
  offset = (offset + num_cols) % num_cols;
  return board[end][offset] & FIXED;
}

// Checks for lightning in a row/col
template<std::size_t num_rows, std::size_t num_cols>
bool row_contains_lightning(const Board<num_rows, num_cols>& board, int offset) {
  for (size_t col = 0; col < num_cols; ++col) {
    if(board[offset][col] & LIGHTNING) {
      return true;
    }
  }
  return false;
}
template<std::size_t num_rows, std::size_t num_cols>
bool col_contains_lightning(const Board<num_rows, num_cols>& board, int offset) {
  for (size_t row = 0; row < num_rows; ++row) {
    if(board[row][offset] & LIGHTNING) {
      return true;
    }
  }
  return false;
}

// checks if a row/col contains a bond in a given direction.
template<std::size_t num_rows, std::size_t num_cols>
bool row_contains_bond(const Board<num_rows, num_cols>& board, int offset, int bond) {
  offset = (offset + num_rows) % num_rows;
  for (size_t col = 0; col < num_cols; ++col) {
    if(board[offset][col] & bond) {
      return true;
    }
  }
  return false;
}
template<std::size_t num_rows, std::size_t num_cols>
bool col_contains_bond(const Board<num_rows, num_cols>& board, int offset, int bond) {
  offset = (offset + num_cols) % num_cols;
  for (size_t row = 0; row < num_rows; ++row) {
    if(board[row][offset] & bond) {
      return true;
    }
  }
  return false;
}


// checks if a row/col contains a fixed cell.
template<std::size_t num_rows, std::size_t num_cols>
bool row_contains_fixed_cell(const Board<num_rows, num_cols>& board, int offset) {
  offset = (offset + num_rows) % num_rows;
  for (size_t col = 0; col < num_cols; ++col) {
    if(board[offset][col] & FIXED) {
      return true;
    }
  }
  return false;
}
template<std::size_t num_rows, std::size_t num_cols>
bool col_contains_fixed_cell(const Board<num_rows, num_cols>& board, int offset) {
  offset = (offset + num_cols) % num_cols;
  for (size_t row = 0; row < num_rows; ++row) {
    if(board[row][offset] & FIXED) {
      return true;
    }
  }
  return false;
}

// checks if a row/col contains an arrows cell in the wrong direction
template<std::size_t num_rows, std::size_t num_cols>
bool row_contains_arrows_cell(const Board<num_rows, num_cols>& board, int offset) {
  offset = (offset + num_rows) % num_rows;
  for (size_t col = 0; col < num_cols; ++col) {
    if(board[offset][col] & VERT) {
      return true;
    }
  }
  return false;
}
template<std::size_t num_rows, std::size_t num_cols>
bool col_contains_arrows_cell(const Board<num_rows, num_cols>& board, int offset) {
  offset = (offset + num_cols) % num_cols;
  for (size_t row = 0; row < num_rows; ++row) {
    if(board[row][offset] & HORIZ) {
      return true;
    }
  }
  return false;
}

#endif
