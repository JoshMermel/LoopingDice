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

// Helpers for converting a row to a string.
template<std::size_t len>
struct BasicRowFormatter {
  void operator()(std::string* out, const std::array<int, len>& arr) const {
    out->append(absl::StrJoin(arr, ",", [](std::string* out, int i) {
          out->append(std::to_string(i));
     }));
  }
};

template<std::size_t len>
struct EnablerRowFormatter {
  void operator()(std::string* out, const std::array<int, len>& arr) const {
    out->append(absl::StrJoin(arr, ",", [](std::string* out, int i) {
          if (i == 0) {
            out->append("E");
          } else {
            out->append(std::to_string(i));
          }
     }));
  }
};

template<std::size_t len>
struct StaticRowFormatter {
  void operator()(std::string* out, const std::array<int, len>& arr) const {
    out->append(absl::StrJoin(arr, ",", [](std::string* out, int i) {
          if (i == 0) {
            out->append("F 0");
          } else {
            out->append(std::to_string(i));
          }
     }));
  }
};

template<std::size_t len>
struct DynamicRowFormatter {
  void operator()(std::string* out, const std::array<int, len>& arr) const {
    out->append(absl::StrJoin(arr, ",", [](std::string* out, int i) {
          if (i < 0) {
            out->append("F " + std::to_string(-1 * i));
          } else {
            out->append(std::to_string(i));
          }
     }));
  }
};

template<std::size_t len>
struct AxisRowFormatter {
  void operator()(std::string* out, const std::array<int, len>& arr) const {
    out->append(absl::StrJoin(arr, ",", [](std::string* out, int i) {
          int mask = RIGHT | DOWN;
          if (i & RIGHT) {
            out->append("H " + std::to_string(i & ~mask));
          } else if (i & DOWN) {
            out->append("V " + std::to_string(i & ~mask));
          } else {
            out->append(std::to_string(i));
          }
     }));
  }
};

std::string bandagedCellToString(int i) {
  int mask = UP | DOWN | LEFT | RIGHT;
  if (!(i & mask)) {
    return std::to_string(i);
  }
  std::string ret = "B " + std::to_string(i & ~mask);
  if (i & UP) {
    ret += " U";
  }
  if (i & DOWN) {
    ret += " D";
  }
  if (i & LEFT) {
    ret += " L";
  }
  if (i & RIGHT) {
    ret += " R";
  }
  return ret;
}

template<std::size_t len>
struct BandagedRowFormatter {
  void operator()(std::string* out, const std::array<int, len>& arr) const {
    out->append(absl::StrJoin(arr, ",", [](std::string* out, int i) {
          out->append(bandagedCellToString(i));
     }));
  }
};


template<std::size_t num_cols>
std::function<void(std::string*, const std::array<int, num_cols>& arr)> getFormatter(const Mode& mode) {
  switch(mode) {
    case Mode::ENABLER:
      return EnablerRowFormatter<num_cols>();
    case Mode::DYNAMIC:
      return DynamicRowFormatter<num_cols>();
    case Mode::BANDAGED:
      return BandagedRowFormatter<num_cols>();
    case Mode::STATIC_1:
    case Mode::STATIC_2:
    case Mode::STATIC_3:
      return StaticRowFormatter<num_cols>();
    case Mode::AXIS:
      return AxisRowFormatter<num_cols>();
    default:
      return BasicRowFormatter<num_cols>();
  }
}

// Prints the board
template<std::size_t num_rows, std::size_t num_cols>
std::string boardToString(const Board<num_rows, num_cols>& board, const Mode& mode, const std::string& separator) {
  return absl::StrJoin(board, separator, getFormatter<num_cols>(mode));
}

// Checks for enablers in a row/col
template<std::size_t num_rows, std::size_t num_cols>
bool row_contains_enabler(const Board<num_rows, num_cols>& board, int offset) {
  for (size_t col = 0; col < num_cols; ++col) {
    if(board[offset][col] == 0) {
      return true;
    }
  }
  return false;
}
template<std::size_t num_rows, std::size_t num_cols>
bool col_contains_enabler(const Board<num_rows, num_cols>& board, int offset) {
  for (size_t row = 0; row < num_rows; ++row) {
    if(board[row][offset] == 0) {
      return true;
    }
  }
  return false;
}

// checks if a row/col contains a bond in a given direction. Allows out of range
// offsets.
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


// checks if a row/col contains a bond in a given direction. Allows out of range
// offsets.
template<std::size_t num_rows, std::size_t num_cols>
bool row_contains_fixed_cell(const Board<num_rows, num_cols>& board, int offset) {
  offset = (offset + num_rows) % num_rows;
  for (size_t col = 0; col < num_cols; ++col) {
    if(board[offset][col] == 0) {
      return true;
    }
  }
  return false;
}
template<std::size_t num_rows, std::size_t num_cols>
bool col_contains_fixed_cell(const Board<num_rows, num_cols>& board, int offset) {
  offset = (offset + num_cols) % num_cols;
  for (size_t row = 0; row < num_rows; ++row) {
    if(board[row][offset] == 0) {
      return true;
    }
  }
  return false;
}

// checks if a row/col contains an axis locked cell
template<std::size_t num_rows, std::size_t num_cols>
bool row_contains_axis_locked_cell(const Board<num_rows, num_cols>& board, int offset) {
  for (size_t col = 0; col < num_cols; ++col) {
    if(board[offset][col] & DOWN) {
      return true;
    }
  }
  return false;
}
template<std::size_t num_rows, std::size_t num_cols>
bool col_contains_axis_locked_cell(const Board<num_rows, num_cols>& board, int offset) {
  for (size_t row = 0; row < num_rows; ++row) {
    if(board[row][offset] & RIGHT) {
      return true;
    }
  }
  return false;
}

#endif
