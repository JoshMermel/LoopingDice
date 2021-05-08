#ifndef LOOPINGDICE_ENUMS
#define LOOPINGDICE_ENUMS

#include <array>
#include <initializer_list>
#include <string>
#include <unordered_map>

// Defines the kinds of moves that are possible
// Note that basic and wide are bandage-aware
enum class Mode {
  BASIC = 1,
  WIDE_1 = 1, //intentional duplicate name
  WIDE_2 = 2,
  WIDE_3 = 3,
  WIDE_4 = 4,
  GEAR = 5,
  CAROUSEL = 6,
  ENABLER = 7,
  DYNAMIC = 8,
  BANDAGED = 9,
  STATIC_1 = 10,
  STATIC_2 = 11,
  STATIC_3 = 12,
  AXIS = 13,
};

std::string modeToString(const Mode& mode) {
  switch(mode) {
    case Mode::BASIC:
      return "1";
    case Mode::WIDE_2:
      return "2";
    case Mode::WIDE_3:
      return "3";
    case Mode::WIDE_4:
      return "4";
    case Mode::GEAR:
      return "GEAR";
    case Mode::CAROUSEL:
      return "CAROUSEL";
    case Mode::ENABLER:
      return "ENABLER";
    case Mode::DYNAMIC:
      return "DYNAMIC";
    case Mode::BANDAGED:
      return "BANDAGED";
    case Mode::STATIC_1:
      return "1";
    case Mode::STATIC_2:
      return "2";
    case Mode::STATIC_3:
      return "3";
    case Mode::AXIS:
      return "AXISLOCKED";
  }
  return "ERROR";
}

bool isWide(const Mode& mode) {
  return (mode == Mode::BASIC || mode == Mode::WIDE_2 || mode == Mode::WIDE_3 || mode == Mode::WIDE_4);
}
bool isStatic(const Mode& mode) {
  return (mode == Mode::STATIC_1|| mode == Mode::STATIC_2 || mode == Mode::STATIC_3);
}

bool isCompatible(const Mode& horizontal, const Mode& vertical) {
  // for these modes, if either horizontal or vertical has them, the other must
  // as well.
  for (const Mode& m : {Mode::BANDAGED, Mode::ENABLER, Mode::DYNAMIC, Mode::AXIS}) {
    if ((horizontal == m || vertical == m) && horizontal != vertical) {
      return false;
    }
  }

  // similarly, if either is static, the other must be as well
  if (isStatic(horizontal) != isStatic(vertical)) {
    return false;
  }

  // other than that, whatever.
  return true;
}

std::string modesToString(const Mode& horizontal, const Mode& vertical) {
  if (horizontal == Mode::BASIC && vertical == Mode::BASIC) {
    return "BASIC";
  } else if (isWide(horizontal) && isWide(vertical)) {
    return "WIDE " + modeToString(horizontal) + " " + modeToString(vertical);
  } else if (isStatic(horizontal) && isStatic(vertical)) {
    return "STATIC " + modeToString(horizontal) + " " + modeToString(vertical);
  } else if (horizontal == vertical) {
    return modeToString(horizontal);
  }
  const std::string H = isWide(horizontal)
    ? modesToString(horizontal,horizontal)
    : modeToString(horizontal);
  const std::string V = isWide(vertical)
    ? modesToString(vertical,vertical)
    : modeToString(vertical);
  return H + "|" +  V;
}

template<std::size_t num_rows, std::size_t num_cols>
bool sameElements(
    std::array<std::array<int, num_cols>, num_rows> first,
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

// defines the directions bonds can be
constexpr int UP = 1 << 8;
constexpr int DOWN = 1 << 9;
constexpr int LEFT = 1 << 10;
constexpr int RIGHT = 1 << 11;
#endif
