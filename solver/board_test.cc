#include <unordered_set>

#include "board.h"
#include "gmock/gmock.h"
#include "gtest/gtest.h"

TEST(Board, EnablerToString) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};

  EXPECT_EQ(boardToString(b, Mode::ENABLER, ","), "E,1,2,3,4,5");
}

TEST(Board, StaticToString) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};

  EXPECT_EQ(boardToString(b, Mode::STATIC_1, ","), "F 0,1,2,3,4,5");
}

TEST(Board, DynamicToString) {
  Board<2,3> b = {{
    {{-3,-2,-1}},
    {{1,2,3}},
  }};

  EXPECT_EQ(boardToString(b, Mode::DYNAMIC, ","), "F 3,F 2,F 1,1,2,3");
}

TEST(Board, BandagedToString) {
  Board<2,3> b = {{
    {{0 | UP   ,1 | DOWN | RIGHT ,2 | DOWN | LEFT}},
    {{3 | DOWN ,4 | UP | RIGHT   ,5 | UP | LEFT}},
  }};

  EXPECT_EQ(boardToString(b, Mode::BANDAGED, ","), "B 0 U,B 1 D R,B 2 D L,B 3 D,B 4 U R,B 5 U L");
}

TEST(Board, AxisToString) {
  Board<2,3> b = {{
    {{0 | DOWN, 1 | RIGHT, 2}},
    {{3, 4 | DOWN, 5 | RIGHT}},
  }};

  EXPECT_EQ(boardToString(b, Mode::AXIS, ","), "V 0,H 1,2,3,V 4,H 5");
}

TEST(Board, LightningToString) {
  Board<2,3> b = {{
    {{0, 1|UP, 2}},
    {{3, 4, 5|UP}},
  }};

  EXPECT_EQ(boardToString(b, Mode::LIGHTNING, ","), "0,L 1,2,3,4,L 5");
}

TEST(Board, hash) {
  std::unordered_set<Board<4,3>> s;

  Board<4,3> b1 = {{
    {{0,1,2}},
    {{3,4,5}},
    {{6,7,8}},
    {{9,10,11}},
  }};
  Board<4,3> b2 = {{
    {{0,1,2}},
    {{3,4,5}},
    {{6,7,8}},
    {{9,10,12}},
  }};

  // insert two different boards and verify they don't hash to the same thing.
  s.insert(b1);
  s.insert(b2);
  EXPECT_EQ(s.size(), 2);

  // Try to insert one again and verify its hash is consistent
  s.insert(b2);
  EXPECT_EQ(s.size(), 2);
}

