#ifndef LOOPINGDICE_ENUMS
#define LOOPINGDICE_ENUMS

#include <array>
#include <initializer_list>
#include <string>
#include <unordered_map>

#include "absl/strings/str_join.h"

// Defines the kinds of moves that are possible
enum class Mode {
  BASIC = 1,
  WIDE_1 = 1, // intentional duplicate name
  WIDE_2 = 2,
  WIDE_3 = 3,
  WIDE_4 = 4,
  GEAR = 5,
  CAROUSEL = 6,
  BANDAGED = 7,
  LIGHTNING = 8,
};

enum class Validation : int32_t {
  NONE = 0,
  ARROWS = 1,
  DYNAMIC = 2,
  ENABLER = 4,
  STATIC = 8
};

Validation operator|(Validation lhs, Validation rhs) {
  return static_cast<Validation>(static_cast<char>(lhs) |
                                 static_cast<char>(rhs));
}
Validation operator&(Validation lhs, Validation rhs) {
  return static_cast<Validation>(static_cast<char>(lhs) &
                                 static_cast<char>(rhs));
}



std::string modeToString(const Mode &mode) {
  switch (mode) {
  case Mode::BASIC:
    return "WIDE 1";
  case Mode::WIDE_2:
    return "WIDE 2";
  case Mode::WIDE_3:
    return "WIDE 3";
  case Mode::WIDE_4:
    return "WIDE 4";
  case Mode::GEAR:
    return "GEAR";
  case Mode::CAROUSEL:
    return "CAROUSEL";
  case Mode::BANDAGED:
    return "BANDAGED";
  case Mode::LIGHTNING:
    return "LIGHTNING";
  }
  return "ERROR";
}

// TODO(jmerm): update to work with combined validations
std::string validationToString(const Validation &validation) {
  std::vector<std::string> ret;

  if ((validation & Validation::ARROWS) != Validation::NONE) {
    ret.push_back("ARROWS");
  }
  if ((validation & Validation::DYNAMIC) != Validation::NONE) {
    ret.push_back("DYNAMIC");
  }
  if ((validation & Validation::ENABLER) != Validation::NONE) {
    ret.push_back("ENABLER");
  }
  if ((validation & Validation::STATIC) != Validation::NONE) {
    ret.push_back("STATIC");
  }

  if (ret.empty()) {
    return "NONE";
  } else {
    return absl::StrJoin(ret, "+");
  }
}

bool isCompatible(const Mode &horizontal, const Mode &vertical) {
  // For these modes, if either horizontal or vertical has them, the other ought
  // to as well.
  for (const Mode &m : {Mode::BANDAGED, Mode::LIGHTNING}) {
    if ((horizontal == m || vertical == m) && horizontal != vertical) {
      return false;
    }
  }

  // other than that, whatever.
  return true;
}

std::string modesToString(const Mode &horizontal, const Mode &vertical,
                          const Validation &validation) {
  return modeToString(horizontal) + "|" + modeToString(vertical) + "|" +
         validationToString(validation);
}

template <std::size_t num_rows, std::size_t num_cols>
bool sameElements(std::array<std::array<int, num_cols>, num_rows> first,
                  std::array<std::array<int, num_cols>, num_rows> second) {
  std::unordered_map<int, int> elements_first;
  std::unordered_map<int, int> elements_second;
  for (size_t row = 0; row < num_rows; ++row) {
    for (size_t col = 0; col < num_cols; ++col) {
      elements_first[first[row][col]] += 1;
      elements_second[second[row][col]] += 1;
    }
  }

  return elements_first == elements_second;
}

bool moveFits(const Mode &mode, size_t size) {
  if ((mode == Mode::WIDE_4 && size < 4) ||
      (mode == Mode::WIDE_3 && size < 3) ||
      (mode == Mode::WIDE_2 && size < 2) ||
      (mode == Mode::WIDE_1 && size < 1)) {
    return false;
  }
  return true;
}

// Bits to set to indicate extra properties of gamecells.
// Arrows
constexpr int HORIZ = 1 << 7;
constexpr int VERT = 1 << 8;

// Bonds
constexpr int UP = 1 << 9;
constexpr int DOWN = 1 << 10;
constexpr int LEFT = 1 << 11;
constexpr int RIGHT = 1 << 12;

// Lightning bolts
constexpr int LIGHTNING = 1 << 13;

// Fixed
constexpr int FIXED = 1 << 14;

// Enabler
constexpr int ENABLER = 1 << 15;

#endif
