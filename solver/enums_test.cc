#include "enums.h"
#include "gmock/gmock.h"
#include "gtest/gtest.h"

TEST(Enums, IsCompatible) {
  // Compatible doctrinaire modes.
  EXPECT_TRUE(isCompatible(Mode::WIDE_1, Mode::WIDE_2));
  EXPECT_TRUE(isCompatible(Mode::WIDE_2, Mode::WIDE_3));
  EXPECT_TRUE(isCompatible(Mode::WIDE_1, Mode::CAROUSEL));
  EXPECT_TRUE(isCompatible(Mode::WIDE_1, Mode::GEAR));
  EXPECT_TRUE(isCompatible(Mode::CAROUSEL, Mode::GEAR));

  // Static modes.
  EXPECT_TRUE(isCompatible(Mode::STATIC_1, Mode::STATIC_2));
  EXPECT_TRUE(isCompatible(Mode::STATIC_2, Mode::STATIC_3));

  // Modes with themselves.
  EXPECT_TRUE(isCompatible(Mode::DYNAMIC, Mode::DYNAMIC));
  EXPECT_TRUE(isCompatible(Mode::AXIS, Mode::AXIS));
  EXPECT_TRUE(isCompatible(Mode::BANDAGED, Mode::BANDAGED));
  EXPECT_TRUE(isCompatible(Mode::ENABLER, Mode::ENABLER));

  // Modes incompatible with WIDE.
  EXPECT_FALSE(isCompatible(Mode::WIDE_1, Mode::STATIC_1));
  EXPECT_FALSE(isCompatible(Mode::WIDE_1, Mode::DYNAMIC));
  EXPECT_FALSE(isCompatible(Mode::WIDE_1, Mode::AXIS));
  EXPECT_FALSE(isCompatible(Mode::WIDE_1, Mode::BANDAGED));
  EXPECT_FALSE(isCompatible(Mode::WIDE_1, Mode::ENABLER));

  // Various illegal combos.
  EXPECT_FALSE(isCompatible(Mode::DYNAMIC, Mode::AXIS));
  EXPECT_FALSE(isCompatible(Mode::AXIS, Mode::BANDAGED));
  EXPECT_FALSE(isCompatible(Mode::BANDAGED, Mode::ENABLER));
  EXPECT_FALSE(isCompatible(Mode::ENABLER, Mode::DYNAMIC));
}

TEST(Enums, ModesToString) {
  EXPECT_EQ(modesToString(Mode::BASIC, Mode::BASIC), "BASIC");

  // both wide
  EXPECT_EQ(modesToString(Mode::WIDE_2, Mode::WIDE_3), "WIDE 2 3");
  EXPECT_EQ(modesToString(Mode::WIDE_3, Mode::WIDE_1), "WIDE 3 1");

  // both static
  EXPECT_EQ(modesToString(Mode::STATIC_2, Mode::STATIC_3), "STATIC 2 3");
  EXPECT_EQ(modesToString(Mode::STATIC_3, Mode::STATIC_1), "STATIC 3 1");

  // some combos
  EXPECT_EQ(modesToString(Mode::WIDE_1, Mode::GEAR), "BASIC|GEAR");
  EXPECT_EQ(modesToString(Mode::WIDE_2, Mode::GEAR), "WIDE 2 2|GEAR");
  EXPECT_EQ(modesToString(Mode::WIDE_3, Mode::CAROUSEL), "WIDE 3 3|CAROUSEL");
  EXPECT_EQ(modesToString(Mode::GEAR, Mode::CAROUSEL), "GEAR|CAROUSEL");
}

TEST(Enums, SameElements) {
  std::array<std::array<int, 3>, 2> b = {{
    {{1,2,3}},
    {{4,5,6}},
  }};
  std::array<std::array<int, 3>, 2> shuffled = {{
    {{2,4,6}},
    {{3,1,5}},
  }};

  EXPECT_TRUE(sameElements(b, shuffled));

  std::array<std::array<int, 3>, 2> unequal = {{
    {{2,2,2}},
    {{3,1,5}},
  }};

  EXPECT_FALSE(sameElements(b, unequal));
}
