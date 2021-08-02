#include <unordered_set>

#include "board.h"
#include "gmock/gmock.h"
#include "gtest/gtest.h"

TEST(Board, SlideRowForward) {
  Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
  }};
  Board<2, 3> expected = {{
      {{2, 0, 1}},
      {{3, 4, 5}},
  }};

  slideRow(b, 0, true);
  EXPECT_EQ(b, expected);
}
TEST(Board, SlideColForward) {
  Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
  }};
  Board<2, 3> expected = {{
      {{3, 1, 2}},
      {{0, 4, 5}},
  }};

  slideCol(b, 0, true);
  EXPECT_EQ(b, expected);
}

TEST(Board, BandagedToString) {
  Board<2, 3> b = {{
      {{0 | UP, 1 | DOWN | RIGHT, 2 | DOWN | LEFT}},
      {{3 | DOWN, 4 | UP | RIGHT, 5 | UP | LEFT}},
  }};

  EXPECT_EQ(boardToString(b, Mode::BANDAGED, ","),
            "B 0 U,B 1 D R,B 2 D L,B 3 D,B 4 U R,B 5 U L");
}

TEST(Board, OthersToString) {
  Board<2, 3> b = {{
      {{0 | HORIZ, 1 | VERT, 2 | FIXED}},
      {{3 | LIGHTNING, 4 | ENABLER, 5}},
  }};

  EXPECT_EQ(boardToString(b, Mode::LIGHTNING, ","), "H 0,V 1,F 2,L 3,E,5");
}

TEST(Board, RowContainsEnabler) {
  Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, ENABLER}},
  }};

  EXPECT_FALSE(row_contains_enabler(b, 0));
  EXPECT_TRUE(row_contains_enabler(b, 1));
}

TEST(Board, ColContainsEnabler) {
  Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, ENABLER}},
  }};

  EXPECT_FALSE(col_contains_enabler(b, 0));
  EXPECT_TRUE(col_contains_enabler(b, 2));
}

TEST(Board, RowEndInFixed) {
  Board<2, 3> b = {{
      {{0 | FIXED, 1, 2}},
      {{3, 4, 5 | FIXED}},
  }};
  EXPECT_FALSE(row_ends_in_fixed(b, 0, true));
  EXPECT_TRUE(row_ends_in_fixed(b, 0, false));

  EXPECT_FALSE(row_ends_in_fixed(b, 1, false));
  EXPECT_TRUE(row_ends_in_fixed(b, 1, true));
}

TEST(Board, ColEndsInFixed) {
  Board<2, 3> b = {{
      {{0 | FIXED, 1, 2}},
      {{3, 4, 5 | FIXED}},
  }};
  EXPECT_FALSE(col_ends_in_fixed(b, 0, true));
  EXPECT_TRUE(col_ends_in_fixed(b, 0, false));

  EXPECT_FALSE(col_ends_in_fixed(b, 2, false));
  EXPECT_TRUE(col_ends_in_fixed(b, 2, true));
}

TEST(Board, RowContainsLightning) {
  Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5 | LIGHTNING}},
  }};

  EXPECT_FALSE(row_contains_lightning(b, 0));
  EXPECT_TRUE(row_contains_lightning(b, 1));
}

TEST(Board, ColContainsLightning) {
  Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5 | LIGHTNING}},
  }};

  EXPECT_FALSE(col_contains_lightning(b, 0));
  EXPECT_TRUE(col_contains_lightning(b, 2));
}

TEST(Board, RowContainsBond) {
  Board<2, 3> b = {{
      {{0 | DOWN, 1 | RIGHT, 2 | LEFT}},
      {{3 | UP, 4, 5}},
  }};

  EXPECT_TRUE(row_contains_bond(b, 0, DOWN));
  EXPECT_TRUE(row_contains_bond(b, 0, RIGHT));
  EXPECT_FALSE(row_contains_bond(b, 0, UP));

  EXPECT_FALSE(row_contains_bond(b, 1, DOWN));
  EXPECT_FALSE(row_contains_bond(b, 1, RIGHT));
  EXPECT_TRUE(row_contains_bond(b, 1, UP));
}

TEST(Board, ColContainsBond) {
  Board<2, 3> b = {{
      {{0 | DOWN, 1 | RIGHT, 2 | LEFT}},
      {{3 | UP, 4, 5}},
  }};

  EXPECT_TRUE(col_contains_bond(b, 0, DOWN));
  EXPECT_TRUE(col_contains_bond(b, 0, UP));
  EXPECT_FALSE(col_contains_bond(b, 0, LEFT));

  EXPECT_FALSE(col_contains_bond(b, 2, DOWN));
  EXPECT_FALSE(col_contains_bond(b, 2, RIGHT));
  EXPECT_TRUE(col_contains_bond(b, 2, LEFT));
}

TEST(Board, RowContainsFixed) {
  Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4 | FIXED, 5}},
  }};

  EXPECT_FALSE(row_contains_fixed_cell(b, 0));
  EXPECT_TRUE(row_contains_fixed_cell(b, 1));
}

TEST(Board, ColContainsFixed) {
  Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4 | FIXED, 5}},
  }};

  EXPECT_FALSE(col_contains_fixed_cell(b, 0));
  EXPECT_TRUE(col_contains_fixed_cell(b, 1));
}

TEST(Board, hash) {
  std::unordered_set<Board<4, 3>> s;

  Board<4, 3> b1 = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
      {{6, 7, 8}},
      {{9, 10, 11}},
  }};
  Board<4, 3> b2 = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
      {{6, 7, 8}},
      {{9, 10, 12}},
  }};

  // insert two different boards and verify they don't hash to the same thing.
  s.insert(b1);
  s.insert(b2);
  EXPECT_EQ(s.size(), 2);

  // Try to insert one again and verify its hash is consistent
  s.insert(b2);
  EXPECT_EQ(s.size(), 2);
}
