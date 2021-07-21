#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "board.h"
#include "moves.h"

TEST(Board, SlideRowForward) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
      {{2,0,1}},
      {{3,4,5}},
  }};

  slideRow(b, 0, true);
  EXPECT_EQ(b, expected);
}
TEST(Board, SlideColForward) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{3,1,2}},
    {{0,4,5}},
  }};

  slideCol(b, 0, true);
  EXPECT_EQ(b, expected);
}

TEST(Board, WideRowMove) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{2,0,1}},
    {{5,3,4}},
  }};

  Board<2,3> moved = wideRowMove(b, 0, true, 2);

  EXPECT_EQ(moved, expected);
}

TEST(Board, WideColMove) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{3,1,5}},
    {{0,4,2}},
  }};

  Board<2,3> moved = wideColMove(b, 2, true, 2);

  EXPECT_EQ(moved, expected);
}

TEST(Board, GearRowMove) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{2,0,1}},
    {{4,5,3}},
  }};

  Board<2,3> moved = gearRowMove(b, 0, true); 
  EXPECT_EQ(moved, expected);
}

TEST(Board, GearColMove) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{3,1,5}},
    {{0,4,2}},
  }};

  Board<2,3> moved = gearColMove(b, 2, true);

  EXPECT_EQ(moved, expected);
}

TEST(Board, CarouselRowMove) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{3,0,1}},
    {{4,5,2}},
  }};

  Board<2,3> moved = carouselRowMove(b, 0, true); 
  EXPECT_EQ(moved, expected);
}

TEST(Board, CarouselColMove) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{3,1,0}},
    {{5,4,2}},
  }};

  Board<2,3> moved = carouselColMove(b, 2, true);

  EXPECT_EQ(moved, expected);
}

TEST(Board, BandagedRowMove) {
  Board<2,3> b = {{
    {{0 | DOWN,1 | UP  ,2}},
    {{3 | UP  ,4 | DOWN,5}},
  }};
  Board<2,3> expected = {{
    {{2,0 | DOWN,1 | UP}},
    {{5,3 | UP,  4 | DOWN}},
  }};

  Board<2,3> moved = bandagedRowMove(b, 0, true);

  EXPECT_EQ(moved, expected);
}

TEST(Board, BandagedColMove) {
  Board<2,3> b = {{
    {{0 | RIGHT,1 | LEFT,2}},
    {{3,        4,       5}},
  }};
  Board<2,3> expected = {{
    {{3,        4,       2}},
    {{0 | RIGHT,1 | LEFT,5}},
  }};

  Board<2,3> moved = bandagedColMove(b, 1, true);
  EXPECT_EQ(moved, expected);
}

// Tests for an old bug where bandaged moves that wrapped off the top of the
// board worked incorrectly.
TEST(Board, BandagedRowMoveUnderflow) {
  Board<3,2> b = {{
    {{1,2|UP}},
    {{3,4}},
    {{5,6|DOWN}},
  }};
  Board<3,2> expected = {{
    {{2|UP,1}},
    {{3,4}},
    {{6|DOWN,5}},
  }};

  Board<3,2> moved = bandagedRowMove(b, 0, true);
  EXPECT_EQ(moved, expected);
}

// Tests for an old bug where bandaged moves that wrapped off the left of the
// board worked incorrectly.
TEST(Board, BandagedColMoveUnderflow) {
  Board<2,3> b = {{
    {{0 | LEFT ,1 ,2 |RIGHT}},
    {{3,        4, 5}},
  }};
  Board<2,3> expected = {{
    {{3,        1, 5}},
    {{0 | LEFT ,4 ,2 |RIGHT}},
  }};

  Board<2,3> moved = bandagedColMove(b, 0, true);
  EXPECT_EQ(moved, expected);
}

TEST(Board, AxisLockedRowMove) {
  Board<2,3> b = {{
    {{0 | RIGHT,1, 2}},
    {{3,        4, 5}},
  }};
  Board<2,3> expected = {{
    {{2, 0 | RIGHT, 1}},
    {{3, 4,         5}},
  }};

  Board<2,3> moved = axisRowMove(b, 0, true);
  EXPECT_EQ(moved, expected);
}

TEST(Board, AxisLockedRowMoveIllegal) {
  Board<2,3> b = {{
    {{0 | DOWN,1, 2}},
    {{3,        4, 5}},
  }};

  Board<2,3> moved = axisRowMove(b, 0, true);
  EXPECT_EQ(moved, b);
}

TEST(Board, AxisLockedColMove) {
  Board<2,3> b = {{
    {{1, 2, 3|RIGHT}},
    {{4, 5, 6}}
  }};
  Board<2,3> expected = {{
    {{4, 2, 3|RIGHT}},
    {{1, 5, 6}}
  }};

  Board<2,3> moved = axisColMove(b, 0, true);
  EXPECT_EQ(moved, expected);
}

TEST(Board, AxisLockedColMoveIllegal) {
  Board<2,3> b = {{
    {{1, 2, 3|RIGHT}},
    {{4, 5, 6}}
  }};
  Board<2,3> moved = axisColMove(b, 2, true);
  EXPECT_EQ(moved, b);
}

TEST(Board, EnablerRowMove) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{2,0,1}},
    {{3,4,5}},
  }};

  Board<2,3> moved = enablerRowMove(b, 0, true);

  EXPECT_EQ(moved, expected);
}

TEST(Board, EnablerRowMoveIllegal) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};

  Board<2,3> moved = enablerRowMove(b, 1, true);

  EXPECT_EQ(moved, b);
}

TEST(Board, EnablerColMove) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{3,1,2}},
    {{0,4,5}},
  }};

  Board<2,3> moved = enablerColMove(b, 0, true);

  EXPECT_EQ(moved, expected);
}

TEST(Board, EnablerColMoveIllegal) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};

  Board<2,3> moved = enablerColMove(b, 1, true);

  EXPECT_EQ(moved, b);
}

TEST(Board, StaticRowMove) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{0,1,2}},
    {{5,3,4}},
  }};

  Board<2,3> moved = staticRowMove(b, 1, true, 1);

  EXPECT_EQ(moved, expected);
}

TEST(Board, StaticRowMoveIllegal) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};

  Board<2,3> moved = staticRowMove(b, 0, true, 2);

  EXPECT_EQ(moved, b);
}

TEST(Board, StaticColMove) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{0,4,5}},
    {{3,1,2}},
  }};

  Board<2,3> moved = staticColMove(b, 1, true, 2);

  EXPECT_EQ(moved, expected);
}

TEST(Board, StaticColMoveIllegal) {
  Board<2,3> b = {{
    {{0,1,2}},
    {{3,4,5}},
  }};

  Board<2,3> moved = staticColMove(b, 2, true, 2);

  EXPECT_EQ(moved, b);
}

TEST(Board, DynamicRowMove) {
  Board<2,3> b = {{
    {{-1,1,2}},
    {{3,4,-2}},
  }};
  Board<2,3> expected = {{
    {{2,-1,1}},
    {{3,4,-2}},
  }};

  Board<2,3> moved = dynamicRowMove(b, 0, true);
  EXPECT_EQ(moved, expected);
}

TEST(Board, DynamicRowMoveIllegal) {
  Board<2,3> b = {{
    {{-1,1,2}},
    {{3,4,-2}},
  }};
  Board<2,3> left = dynamicRowMove(b, 0, false);
  Board<2,3> right = dynamicRowMove(b, 1, true);

  EXPECT_EQ(b, left);
  EXPECT_EQ(b, right);
}

TEST(Board, DynamicColMove) {
  Board<2,3> b = {{
    {{-1,1,2}},
    {{3,4,-2}},
  }};
  Board<2,3> expected = {{
    {{3,1,2}},
    {{-1,4,-2}},
  }};

  Board<2,3> moved = dynamicColMove(b, 0, true);
  EXPECT_EQ(moved, expected);
}

TEST(Board, DynamicColMoveIllegal) {
  Board<2,3> b = {{
    {{-1,1,2}},
    {{3,4,-2}},
  }};

  Board<2,3> up = dynamicColMove(b, 0, false);
  Board<2,3> down = dynamicColMove(b, 2, true);

  EXPECT_EQ(b, up);
  EXPECT_EQ(b, down);
}


TEST(Board, LightiningRowMoveFast) {
  Board<2,3> b = {{
    {{0,1|UP,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{1|UP,2,0}},
    {{3,4,5}},
  }};

  Board<2,3> moved = lightningRowMove(b, 0, true);

  EXPECT_EQ(moved, expected);
}

TEST(Board, LightiningRowMoveSlow) {
  Board<2,3> b = {{
    {{0,1|UP,2}},
    {{3,4,5}},
  }};
  Board<2,3> expected = {{
    {{0,1|UP,2}},
    {{5,3,4}},
  }};

  Board<2,3> moved = lightningRowMove(b, 1, true);

  EXPECT_EQ(moved, expected);
}

TEST(Board, LightningColMoveFast) {
  Board<3,2> b = {{
    {{1,2}},
    {{3|UP,4}},
    {{5,6}},
  }};
  Board<3,2> expected = {{
    {{3|UP,2}},
    {{5,4}},
    {{1,6}},
  }};

  Board<3,2> moved = lightningColMove(b, 0, true);
  EXPECT_EQ(moved, expected);
}

TEST(Board, LightningColMoveSlow) {
  Board<3,2> b = {{
    {{1,2}},
    {{3|UP,4}},
    {{5,6}},
  }};
  Board<3,2> expected = {{
    {{1,6}},
    {{3|UP,2}},
    {{5,4}},
  }};

  Board<3,2> moved = lightningColMove(b, 1, true);
  EXPECT_EQ(moved, expected);
}
