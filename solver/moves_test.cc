#include "board.h"
#include "moves.h"
#include "gmock/gmock.h"
#include "gtest/gtest.h"

///////////////////////////////////////////////
// Basic Moves combined with all validations //
///////////////////////////////////////////////

TEST(Board, BasicRowMove) {
  const Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{2, 0, 1}},
      {{3, 4, 5}},
  }};

  const Board<2, 3> moved = wideRowMove(b, 0, true, Validation::NONE, 1);

  EXPECT_EQ(moved, expected);
}
TEST(Board, BasicColMove) {
  const Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{3, 1, 2}},
      {{0, 4, 5}},
  }};

  const Board<2, 3> moved = wideColMove(b, 0, true, Validation::NONE, 1);
  EXPECT_EQ(moved, expected);
}

TEST(Board, BasicArrowsMoves) {
  const Board<2, 3> b = {{
      {{0 | HORIZ, 1, 2}},
      {{3, 4, 5 | VERT}},
  }};

  // Legal row
  EXPECT_EQ(wideRowMove(b, 0, true, Validation::ARROWS, 1),
            wideRowMove(b, 0, true, Validation::NONE, 1));
  // Illegal row
  EXPECT_EQ(b, wideRowMove(b, 1, true, Validation::ARROWS, 1));
  // Legal col
  EXPECT_EQ(wideColMove(b, 2, true, Validation::ARROWS, 1),
            wideColMove(b, 2, true, Validation::NONE, 1));
  // Illegal col
  EXPECT_EQ(b, wideColMove(b, 0, true, Validation::ARROWS, 1));
}

TEST(Board, BasicEnablerMoves) {
  const Board<2, 3> b = {{
      {{ENABLER, 1, 2}},
      {{3, 4, 5}},
  }};

  // Legal row
  EXPECT_EQ(wideRowMove(b, 0, true, Validation::ENABLER, 1),
            wideRowMove(b, 0, true, Validation::NONE, 1));
  // Illegal row
  EXPECT_EQ(b, wideRowMove(b, 1, true, Validation::ENABLER, 1));
  // Legal Col
  EXPECT_EQ(wideColMove(b, 0, true, Validation::ENABLER, 1),
            wideColMove(b, 0, true, Validation::NONE, 1));
  // Illegal Col
  EXPECT_EQ(b, wideColMove(b, 1, true, Validation::ENABLER, 1));
  EXPECT_EQ(b, wideColMove(b, 2, true, Validation::ENABLER, 1));
}

TEST(Board, BasicDynamicMove) {
  const Board<2, 3> b = {{
      {{1 | FIXED, 1, 2}},
      {{3, 4, 2 | FIXED}},
  }};
  // Legal row
  EXPECT_EQ(wideRowMove(b, 0, true, Validation::DYNAMIC, 1),
            wideRowMove(b, 0, true, Validation::NONE, 1));
  // Illegal row
  EXPECT_EQ(b, wideRowMove(b, 0, false, Validation::DYNAMIC, 1));
  EXPECT_EQ(b, wideRowMove(b, 1, true, Validation::DYNAMIC, 1));
  // Legal col
  EXPECT_EQ(wideColMove(b, 0, true, Validation::DYNAMIC, 1),
            wideColMove(b, 0, true, Validation::NONE, 1));
  // Illegal col
  EXPECT_EQ(b, wideColMove(b, 0, false, Validation::DYNAMIC, 1));
  EXPECT_EQ(b, wideColMove(b, 2, true, Validation::DYNAMIC, 1));
}

TEST(Board, BasicStaticMove) {
  const Board<2, 3> b = {{{{FIXED, 1, 2}}, {{3, 4, 5}}}};
  // Legal row
  EXPECT_EQ(wideRowMove(b, 1, true, Validation::STATIC, 1),
            wideRowMove(b, 1, true, Validation::NONE, 1));
  // Illegal row
  EXPECT_EQ(b, wideRowMove(b, 0, true, Validation::STATIC, 1));
  // Legal col
  EXPECT_EQ(wideColMove(b, 1, true, Validation::STATIC, 1),
            wideColMove(b, 1, true, Validation::NONE, 1));
  EXPECT_EQ(wideColMove(b, 2, true, Validation::STATIC, 1),
            wideColMove(b, 2, true, Validation::NONE, 1));
  // Illegal col
  EXPECT_EQ(b, wideColMove(b, 0, true, Validation::STATIC, 1));
}

//////////////////////////////////////////////
// Wide Moves combined with all validations //
//////////////////////////////////////////////

// Wide moves, no validation
TEST(Board, WideRowMove) {
  const Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{2, 0, 1}},
      {{5, 3, 4}},
  }};

  const Board<2, 3> moved = wideRowMove(b, 0, true, Validation::NONE, 2);

  EXPECT_EQ(moved, expected);
}
TEST(Board, WideColMove) {
  const Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{3, 1, 5}},
      {{0, 4, 2}},
  }};

  const Board<2, 3> moved = wideColMove(b, 2, true, Validation::NONE, 2);

  EXPECT_EQ(moved, expected);
}

TEST(Board, WideArrowsMove) {
  const Board<3, 4> b = {{
      {{0 | HORIZ, 1, 2, 3}},
      {{4, 5, 6, 7}},
      {{8, 9, 10 | VERT, 11}},
  }};

  // Legal Row
  EXPECT_EQ(wideRowMove(b, 0, true, Validation::ARROWS, 2),
            wideRowMove(b, 0, true, Validation::NONE, 2));
  // Illegal Row
  EXPECT_EQ(b, wideRowMove(b, 1, true, Validation::ARROWS, 2));
  EXPECT_EQ(b, wideRowMove(b, 2, true, Validation::ARROWS, 2));
  // Legal Col
  EXPECT_EQ(wideColMove(b, 1, true, Validation::ARROWS, 2),
            wideColMove(b, 1, true, Validation::NONE, 2));
  EXPECT_EQ(wideColMove(b, 2, true, Validation::ARROWS, 2),
            wideColMove(b, 2, true, Validation::NONE, 2));
  // Illegal Col
  EXPECT_EQ(b, wideColMove(b, 0, true, Validation::ARROWS, 2));
  EXPECT_EQ(b, wideColMove(b, 3, true, Validation::ARROWS, 2));
}

TEST(Board, WideDynamicMove) {
  const Board<3, 4> b = {{
      {{0 | FIXED, 1, 2, 3}},
      {{4, 5, 6, 7}},
      {{8, 9, 10 | FIXED, 11}},
  }};

  // Legal row
  EXPECT_EQ(wideRowMove(b, 0, true, Validation::DYNAMIC, 2),
            wideRowMove(b, 0, true, Validation::NONE, 2));
  // Illegal row
  EXPECT_EQ(b, wideRowMove(b, 0, false, Validation::DYNAMIC, 2));
  // Legal col
  EXPECT_EQ(wideRowMove(b, 1, true, Validation::DYNAMIC, 2),
            wideRowMove(b, 1, true, Validation::NONE, 2));

  // Illegal col
  EXPECT_EQ(b, wideColMove(b, 0, false, Validation::DYNAMIC, 2));
}

TEST(Board, WideEnablerMove) {
  const Board<3, 4> b = {{
      {{ENABLER, 0, 1, 2}},
      {{3, 4, 5, 6}},
      {{7, 8, 9, 10}},
  }};

  // Legal row
  EXPECT_EQ(wideRowMove(b, 0, true, Validation::ENABLER, 2),
            wideRowMove(b, 0, true, Validation::NONE, 2));
  EXPECT_EQ(wideRowMove(b, 2, true, Validation::ENABLER, 2),
            wideRowMove(b, 2, true, Validation::NONE, 2));
  // Illegal row
  EXPECT_EQ(b, wideRowMove(b, 1, true, Validation::ENABLER, 2));
  // Legal col
  EXPECT_EQ(wideColMove(b, 0, true, Validation::ENABLER, 2),
            wideColMove(b, 0, true, Validation::NONE, 2));
  EXPECT_EQ(wideColMove(b, 3, true, Validation::ENABLER, 2),
            wideColMove(b, 3, true, Validation::NONE, 2));
  // Illegal col
  EXPECT_EQ(b, wideColMove(b, 1, true, Validation::ENABLER, 2));
  EXPECT_EQ(b, wideColMove(b, 2, true, Validation::ENABLER, 2));
}

TEST(Board, WideStaticMove) {
  const Board<3, 4> b = {{
      {{FIXED, 0, 1, 2}},
      {{3, 4, 5, 6}},
      {{7, 8, 9, 10}},
  }};

  // Legal row
  EXPECT_EQ(wideRowMove(b, 1, true, Validation::STATIC, 2),
            wideRowMove(b, 1, true, Validation::NONE, 2));
  // Illegal row
  EXPECT_EQ(b, wideRowMove(b, 0, true, Validation::STATIC, 2));
  EXPECT_EQ(b, wideRowMove(b, 2, true, Validation::STATIC, 2));
  // Legal col
  EXPECT_EQ(wideColMove(b, 1, true, Validation::STATIC, 2),
            wideColMove(b, 1, true, Validation::NONE, 2));
  // Illegal col
  EXPECT_EQ(b, wideColMove(b, 0, true, Validation::STATIC, 2));
  EXPECT_EQ(b, wideColMove(b, 3, true, Validation::STATIC, 2));
}

/////////////////////////////////////
// Gear moves plus all validations //
/////////////////////////////////////

TEST(Board, GearRowMove) {
  const Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{2, 0, 1}},
      {{4, 5, 3}},
  }};

  const Board<2, 3> moved = gearRowMove(b, 0, true, Validation::NONE);
  EXPECT_EQ(moved, expected);
}

TEST(Board, GearColMove) {
  const Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{3, 1, 5}},
      {{0, 4, 2}},
  }};

  const Board<2, 3> moved = gearColMove(b, 2, true, Validation::NONE);

  EXPECT_EQ(moved, expected);
}

TEST(Board, GearArrowsMove) {
  const Board<3, 4> b = {{
      {{0 | HORIZ, 1, 2, 3}},
      {{4, 5, 6, 7}},
      {{8, 9, 10 | VERT, 11}},
  }};

  // Legal row
  EXPECT_EQ(gearRowMove(b, 0, true, Validation::ARROWS),
            gearRowMove(b, 0, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, gearRowMove(b, 1, true, Validation::ARROWS));
  EXPECT_EQ(b, gearRowMove(b, 2, true, Validation::ARROWS));
  // Legal col
  EXPECT_EQ(gearColMove(b, 1, true, Validation::ARROWS),
            gearColMove(b, 1, true, Validation::ARROWS));
  EXPECT_EQ(gearColMove(b, 2, true, Validation::ARROWS),
            gearColMove(b, 2, true, Validation::ARROWS));
  // Illegal col
  EXPECT_EQ(b, gearColMove(b, 0, true, Validation::ARROWS));
  EXPECT_EQ(b, gearColMove(b, 3, true, Validation::ARROWS));
}

TEST(Board, GearDynamicMove) {
  const Board<3, 4> b = {{
      {{0 | FIXED, 1, 2, 3}},
      {{4, 5, 6, 7}},
      {{8, 9, 10 | FIXED, 11}},
  }};

  // Legal row
  EXPECT_EQ(gearRowMove(b, 0, true, Validation::DYNAMIC),
            gearRowMove(b, 0, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, gearRowMove(b, 0, false, Validation::DYNAMIC));
  EXPECT_EQ(b, gearRowMove(b, 2, true, Validation::DYNAMIC));
  // Legal col
  EXPECT_EQ(gearColMove(b, 1, true, Validation::DYNAMIC),
            gearColMove(b, 1, true, Validation::NONE));
  EXPECT_EQ(gearColMove(b, 3, false, Validation::DYNAMIC),
            gearColMove(b, 3, false, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, gearColMove(b, 0, false, Validation::DYNAMIC));
  EXPECT_EQ(b, gearColMove(b, 2, true, Validation::DYNAMIC));
}

TEST(Board, GearEnablerRowMove) {
  const Board<3, 4> b = {{
      {{ENABLER, 0, 1, 2}},
      {{3, 4, 5, 6}},
      {{7, 8, 9, 10}},
  }};

  // Legal row
  EXPECT_EQ(gearRowMove(b, 0, true, Validation::ENABLER),
            gearRowMove(b, 0, true, Validation::NONE));
  EXPECT_EQ(gearRowMove(b, 2, true, Validation::ENABLER),
            gearRowMove(b, 2, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, gearRowMove(b, 1, true, Validation::ENABLER));
  // Legal col
  EXPECT_EQ(gearColMove(b, 0, true, Validation::ENABLER),
            gearColMove(b, 0, true, Validation::NONE));
  EXPECT_EQ(gearColMove(b, 3, true, Validation::ENABLER),
            gearColMove(b, 3, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, gearColMove(b, 1, true, Validation::ENABLER));
  EXPECT_EQ(b, gearColMove(b, 2, true, Validation::ENABLER));
}

TEST(Board, GearStaticMove) {
  const Board<3, 4> b = {{
      {{FIXED, 0, 1, 2}},
      {{3, 4, 5, 6}},
      {{7, 8, 9, 10}},
  }};

  // Legal row
  EXPECT_EQ(gearRowMove(b, 1, true, Validation::STATIC),
            gearRowMove(b, 1, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, gearRowMove(b, 0, true, Validation::STATIC));
  EXPECT_EQ(b, gearRowMove(b, 2, true, Validation::STATIC));
  // Legal col
  EXPECT_EQ(gearColMove(b, 1, true, Validation::STATIC),
            gearColMove(b, 1, true, Validation::NONE));
  EXPECT_EQ(gearColMove(b, 2, true, Validation::STATIC),
            gearColMove(b, 2, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, gearColMove(b, 0, true, Validation::STATIC));
  EXPECT_EQ(b, gearColMove(b, 3, true, Validation::STATIC));
}

/////////////////////////////////////////
// Carousel moves plus all validations //
/////////////////////////////////////////

TEST(Board, CarouselRowMove) {
  const Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{3, 0, 1}},
      {{4, 5, 2}},
  }};

  const Board<2, 3> moved = carouselRowMove(b, 0, true, Validation::NONE);
  EXPECT_EQ(moved, expected);
}

TEST(Board, CarouselColMove) {
  const Board<2, 3> b = {{
      {{0, 1, 2}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{3, 1, 0}},
      {{5, 4, 2}},
  }};

  const Board<2, 3> moved = carouselColMove(b, 2, true, Validation::NONE);

  EXPECT_EQ(moved, expected);
}

TEST(Board, CarouselArrowsMove) {
  const Board<3, 4> b = {{
      {{0, 1, 2, 3|HORIZ}},
      {{4|VERT, 5, 6, 7}},
      {{8, 9, 10, 11|VERT}},
  }};

  // Legal row
  EXPECT_EQ(carouselRowMove(b, 1, false, Validation::ARROWS),
            carouselRowMove(b, 1, false, Validation::NONE));
  EXPECT_EQ(carouselRowMove(b, 2, true, Validation::ARROWS),
            carouselRowMove(b, 2, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, carouselRowMove(b, 0, true, Validation::ARROWS));
  EXPECT_EQ(b, carouselRowMove(b, 0, false, Validation::ARROWS));
  // Legal col
  EXPECT_EQ(carouselColMove(b, 2, true, Validation::ARROWS),
            carouselColMove(b, 2, true, Validation::NONE));
  EXPECT_EQ(carouselColMove(b, 3, false, Validation::ARROWS),
            carouselColMove(b, 3, false, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, carouselColMove(b, 2, false, Validation::ARROWS));
  EXPECT_EQ(b, carouselColMove(b, 3, true, Validation::ARROWS));
}

// Carousel dynamic has a lot of edge cases. This one covers fixed cells in
// top-left or bottom-right..
TEST(Board, CarouselDynamicMove1) {
  const Board<3, 4> b1 = {{
      {{FIXED, 0, 1, 2}},
      {{3, 4, 5, 6}},
      {{7, 8, 9, 10}},
  }};
  const Board<3, 4> b2 = {{
      {{0, 1, 2, 3}},
      {{4, 5, 6, 7}},
      {{8, 9, 10, FIXED}},
  }};

  for (const Board<3, 4> &b : {b1, b2}) {
    // Legal row
    EXPECT_EQ(carouselRowMove(b, 0, true, Validation::DYNAMIC),
              carouselRowMove(b, 0, true, Validation::NONE));
    EXPECT_EQ(carouselRowMove(b, 2, false, Validation::DYNAMIC),
              carouselRowMove(b, 2, false, Validation::NONE));
    // Illegal row
    EXPECT_EQ(b, carouselRowMove(b, 2, true, Validation::DYNAMIC));
    // Legal col
    EXPECT_EQ(carouselColMove(b, 0, true, Validation::DYNAMIC),
              carouselColMove(b, 0, true, Validation::NONE));
    EXPECT_EQ(carouselColMove(b, 3, false, Validation::DYNAMIC),
              carouselColMove(b, 3, false, Validation::NONE));
    // Illegal col
    EXPECT_EQ(b, carouselColMove(b, 3, true, Validation::DYNAMIC));
  }
}

// Carousel dynamic has a lot of edge cases. This one covers fixed cells in
// top-right or bottom-left.
TEST(Board, CarouselDynamicMove2) {
  const Board<3, 4> b1 = {{
      {{0, 1, 2, FIXED}},
      {{3, 4, 5, 6}},
      {{7, 8, 9, 10}},
  }};
  const Board<3, 4> b2 = {{
      {{0, 1, 2, 3}},
      {{4, 5, 6, 7}},
      {{FIXED, 8, 9, 10}},
  }};

  for (const Board<3, 4> &b : {b1, b2}) {
    // Legal row
    EXPECT_EQ(carouselRowMove(b, 2, true, Validation::DYNAMIC),
              carouselRowMove(b, 2, true, Validation::NONE));
    // Illegal row
    EXPECT_EQ(b, carouselRowMove(b, 2, false, Validation::DYNAMIC));
    // Legal col
    EXPECT_EQ(carouselColMove(b, 3, true, Validation::DYNAMIC),
              carouselColMove(b, 3, true, Validation::NONE));
    // Illegal col
    EXPECT_EQ(b, carouselColMove(b, 3, false, Validation::DYNAMIC));
  }
}

TEST(Board, CarouselEnablerMove) {
  const Board<3, 4> b = {{
      {{ENABLER, 0, 1, 2}},
      {{3, 4, 5, 6}},
      {{7, 8, 9, 10}},
  }};

  // Legal row
  EXPECT_EQ(carouselRowMove(b, 0, true, Validation::ENABLER),
            carouselRowMove(b, 0, true, Validation::NONE));
  EXPECT_EQ(carouselRowMove(b, 2, true, Validation::ENABLER),
            carouselRowMove(b, 2, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, carouselRowMove(b, 1, true, Validation::ENABLER));
  // Legal col
  EXPECT_EQ(carouselColMove(b, 0, true, Validation::ENABLER),
            carouselColMove(b, 0, true, Validation::NONE));
  EXPECT_EQ(carouselColMove(b, 3, true, Validation::ENABLER),
            carouselColMove(b, 3, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, carouselColMove(b, 1, true, Validation::ENABLER));
  EXPECT_EQ(b, carouselColMove(b, 2, true, Validation::ENABLER));
}

TEST(Board, CarouselStaticMove) {
  const Board<3, 4> b = {{
      {{FIXED, 0, 1, 2}},
      {{3, 4, 5, 6}},
      {{7, 8, 9, 10}},
  }};

  // Legal row
  EXPECT_EQ(carouselRowMove(b, 1, true, Validation::STATIC),
            carouselRowMove(b, 1, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, carouselRowMove(b, 0, true, Validation::STATIC));
  EXPECT_EQ(b, carouselRowMove(b, 2, true, Validation::STATIC));
  // Legal col
  EXPECT_EQ(carouselColMove(b, 1, true, Validation::STATIC),
            carouselColMove(b, 1, true, Validation::NONE));
  EXPECT_EQ(carouselColMove(b, 2, true, Validation::STATIC),
            carouselColMove(b, 2, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, carouselColMove(b, 0, true, Validation::STATIC));
  EXPECT_EQ(b, carouselColMove(b, 3, true, Validation::STATIC));
}

/////////////////////////////////////////
// Bandaged moves plus all validations //
/////////////////////////////////////////

TEST(Board, BandagedRowMove) {
  const Board<2, 3> b = {{
      {{0 | DOWN, 1 | UP, 2}},
      {{3 | UP, 4 | DOWN, 5}},
  }};
  const Board<2, 3> expected = {{
      {{2, 0 | DOWN, 1 | UP}},
      {{5, 3 | UP, 4 | DOWN}},
  }};

  const Board<2, 3> moved = bandagedRowMove(b, 0, true, Validation::NONE);

  EXPECT_EQ(moved, expected);
}

TEST(Board, BandagedColMove) {
  const Board<2, 3> b = {{
      {{0 | RIGHT, 1 | LEFT, 2}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{3, 4, 2}},
      {{0 | RIGHT, 1 | LEFT, 5}},
  }};

  const Board<2, 3> moved = bandagedColMove(b, 1, true, Validation::NONE);
  EXPECT_EQ(moved, expected);
}

TEST(Board, BandagedArrowsMove) {
  const Board<3, 4> b = {{
      {{0 | RIGHT, 1 | LEFT, 2 | HORIZ, 3}},
      {{4|VERT, 5, 6 | DOWN | RIGHT, 7 | DOWN | LEFT}},
      {{8, 9, 10 | UP | RIGHT, 11 | UP | LEFT}},
  }};

  // Legal row
  EXPECT_EQ(bandagedRowMove(b, 0, true, Validation::ARROWS),
            bandagedRowMove(b, 0, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, bandagedRowMove(b, 1, false, Validation::ARROWS));
  // Legal col
  EXPECT_EQ(bandagedColMove(b, 0, true, Validation::ARROWS),
            bandagedColMove(b, 0, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, bandagedColMove(b, 3, false, Validation::ARROWS));
}

TEST(Board, BandagedDynamicMove) {
  const Board<3, 4> b = {{
      {{0 | RIGHT, 1 | LEFT, 2, 3 | FIXED}},
      {{4, 5, 6 | DOWN | RIGHT, 7 | DOWN | LEFT}},
      {{8|FIXED, 9, 10 | UP | RIGHT, 11 | UP | LEFT}},
  }};

  // Legal row
  EXPECT_EQ(bandagedRowMove(b, 1, true, Validation::DYNAMIC),
            bandagedRowMove(b, 1, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, bandagedRowMove(b, 1, false, Validation::DYNAMIC));
  // Legal col
  EXPECT_EQ(bandagedColMove(b, 1, false, Validation::DYNAMIC),
            bandagedColMove(b, 1, false, Validation::NONE));
  EXPECT_EQ(bandagedColMove(b, 2, true, Validation::DYNAMIC),
            bandagedColMove(b, 2, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, bandagedColMove(b, 2, false, Validation::DYNAMIC));
}

TEST(Board, BandagedEnablerMove) {
  const Board<3, 4> b = {{
      {{0 | RIGHT, 1 | LEFT, 2, 3 | ENABLER}},
      {{4, 5, 6 | DOWN | RIGHT, 7 | DOWN | LEFT}},
      {{8, 9, 10 | UP | RIGHT, 11 | UP | LEFT}},
  }};

  // Legal row
  EXPECT_EQ(bandagedRowMove(b, 0, true, Validation::ENABLER),
            bandagedRowMove(b, 0, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, bandagedRowMove(b, 1, true, Validation::ENABLER));
  // Legal col
  EXPECT_EQ(bandagedColMove(b, 2, true, Validation::ENABLER),
            bandagedColMove(b, 2, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, bandagedColMove(b, 1, true, Validation::ENABLER));
}

TEST(Board, BandagedStaticMove) {
  const Board<3, 4> b = {{
      {{0 | RIGHT, 1 | LEFT, 2, 3 | FIXED}},
      {{4, 5, 6 | DOWN | RIGHT, 7 | DOWN | LEFT}},
      {{8, 9, 10 | UP | RIGHT, 11 | UP | LEFT}},
  }};

  // Legal row
  EXPECT_EQ(bandagedRowMove(b, 1, true, Validation::STATIC),
            bandagedRowMove(b, 1, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, bandagedRowMove(b, 0, true, Validation::STATIC));
  // Legal col
  EXPECT_EQ(bandagedColMove(b, 1, true, Validation::STATIC),
            bandagedColMove(b, 1, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, bandagedColMove(b, 2, true, Validation::STATIC));
}

// Tests for an old bug where bandaged moves that wrapped off the top of the
// board worked incorrectly.
TEST(Board, BandagedRowMoveUnderflow) {
  const Board<3, 2> b = {{
      {{1, 2 | UP}},
      {{3, 4}},
      {{5, 6 | DOWN}},
  }};
  const Board<3, 2> expected = {{
      {{2 | UP, 1}},
      {{3, 4}},
      {{6 | DOWN, 5}},
  }};

  const Board<3, 2> moved = bandagedRowMove(b, 0, true, Validation::NONE);
  EXPECT_EQ(moved, expected);
}

// Tests for an old bug where bandaged moves that wrapped off the left of the
// board worked incorrectly.
TEST(Board, BandagedColMoveUnderflow) {
  const Board<2, 3> b = {{
      {{0 | LEFT, 1, 2 | RIGHT}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{3, 1, 5}},
      {{0 | LEFT, 4, 2 | RIGHT}},
  }};

  const Board<2, 3> moved = bandagedColMove(b, 0, true, Validation::NONE);
  EXPECT_EQ(moved, expected);
}

//////////////////////////////////////////
// Lightning moves plus all validations //
//////////////////////////////////////////

TEST(Board, LightningRowMoveFast) {
  const Board<2, 3> b = {{
      {{0, 1 | LIGHTNING, 2}},
      {{3, 4, 5}},
  }};
  const Board<2, 3> expected = {{
      {{1 | LIGHTNING, 2, 0}},
      {{3, 4, 5}},
  }};

  const Board<2, 3> moved = lightningRowMove(b, 0, true, Validation::NONE);

  EXPECT_EQ(moved, expected);
}

TEST(Board, LightningRowMoveSlow) {
  const Board<2,3> b = {{
    {{0,1|LIGHTNING,2}},
    {{3,4,5}},
  }};
  const Board<2, 3> expected = {{
      {{0, 1 | LIGHTNING, 2}},
      {{5, 3, 4}},
  }};

  const Board<2, 3> moved = lightningRowMove(b, 1, true, Validation::NONE);

  EXPECT_EQ(moved, expected);
}

TEST(Board, LightningColMoveFast) {
  const Board<3, 2> b = {{
      {{1, 2}},
      {{3 | LIGHTNING, 4}},
      {{5, 6}},
  }};
  const Board<3, 2> expected = {{
      {{3 | LIGHTNING, 2}},
      {{5, 4}},
      {{1, 6}},
  }};

  const Board<3, 2> moved = lightningColMove(b, 0, true, Validation::NONE);
  EXPECT_EQ(moved, expected);
}

TEST(Board, LightningColMoveSlow) {
  const Board<3, 2> b = {{
      {{1, 2}},
      {{3 | LIGHTNING, 4}},
      {{5, 6}},
  }};
  const Board<3, 2> expected = {{
      {{1, 6}},
      {{3 | LIGHTNING, 2}},
      {{5, 4}},
  }};

  const Board<3, 2> moved = lightningColMove(b, 1, true, Validation::NONE);
  EXPECT_EQ(moved, expected);
}

TEST(Board, LightningArrowsMove) {
  const Board<2, 3> b = {{
      {{0 | LIGHTNING, 1 | HORIZ, 2}},
      {{3 | VERT, 4, 5}},
  }};

  // Legal row
  EXPECT_EQ(lightningRowMove(b, 0, true, Validation::ARROWS),
            lightningRowMove(b, 0, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, lightningRowMove(b, 1, true, Validation::ARROWS));
  // Legal col
  EXPECT_EQ(lightningColMove(b, 0, true, Validation::ARROWS),
            lightningColMove(b, 0, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, lightningColMove(b, 1, true, Validation::ARROWS));
}

TEST(Board, LightningDynamicMove) {
  const Board<4, 5> b = {{
      {{0, 1 | FIXED, 2, 3, 4}},
      {{5 | FIXED, 6 | LIGHTNING, 7, 8, 9 | FIXED}},
      {{10, 11, 12, 13 | LIGHTNING, 14}},
      {{15, 16 | FIXED, 17, 18, 19}},
  }};

  // Legal row
  EXPECT_EQ(lightningRowMove(b, 0, true, Validation::DYNAMIC),
            lightningRowMove(b, 0, true, Validation::NONE));
  EXPECT_EQ(lightningRowMove(b, 2, true, Validation::DYNAMIC),
            lightningRowMove(b, 2, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, lightningRowMove(b, 1, true, Validation::DYNAMIC));
  EXPECT_EQ(b, lightningRowMove(b, 1, false, Validation::DYNAMIC));
  // Legal col
  EXPECT_EQ(lightningColMove(b, 0, true, Validation::DYNAMIC),
            lightningColMove(b, 0, true, Validation::NONE));
  EXPECT_EQ(lightningColMove(b, 3, true, Validation::DYNAMIC),
            lightningColMove(b, 3, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, lightningColMove(b, 1, true, Validation::DYNAMIC));
  EXPECT_EQ(b, lightningColMove(b, 1, false, Validation::DYNAMIC));
}

TEST(Board, LightningEnablerMove) {
  const Board<2, 3> b = {{
      {{0 | LIGHTNING, 1, 2 | ENABLER}},
      {{3, 4, 5 | LIGHTNING}},
  }};

  // Legal row
  EXPECT_EQ(lightningRowMove(b, 0, true, Validation::ENABLER),
            lightningRowMove(b, 0, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, lightningRowMove(b, 1, true, Validation::ENABLER));
  // Legal col
  EXPECT_EQ(lightningColMove(b, 2, true, Validation::ENABLER),
            lightningColMove(b, 2, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, lightningColMove(b, 0, true, Validation::ENABLER));
  EXPECT_EQ(b, lightningColMove(b, 1, true, Validation::ENABLER));
}

TEST(Board, LightningStaticMove) {
  const Board<2, 3> b = {{
      {{0 | LIGHTNING, 1, 2 | FIXED}},
      {{3, 4, 5 | LIGHTNING}},
  }};

  // Legal row
  EXPECT_EQ(lightningRowMove(b, 1, true, Validation::STATIC),
            lightningRowMove(b, 1, true, Validation::NONE));
  // Illegal row
  EXPECT_EQ(b, lightningRowMove(b, 0, true, Validation::STATIC));
  // Legal col
  EXPECT_EQ(lightningColMove(b, 0, true, Validation::STATIC),
            lightningColMove(b, 0, true, Validation::NONE));
  EXPECT_EQ(lightningColMove(b, 1, true, Validation::STATIC),
            lightningColMove(b, 1, true, Validation::NONE));
  // Illegal col
  EXPECT_EQ(b, lightningColMove(b, 2, true, Validation::STATIC));
}
