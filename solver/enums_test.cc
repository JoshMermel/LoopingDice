#include "enums.h"
#include "gmock/gmock.h"
#include "gtest/gtest.h"

TEST(Enums, ModesToString) {
  EXPECT_EQ(modesToString(Mode::BASIC, Mode::BASIC, Validation::NONE),
            "WIDE 1|WIDE 1|NONE");
  EXPECT_EQ(modesToString(Mode::WIDE_2, Mode::WIDE_3, Validation::STATIC),
            "WIDE 2|WIDE 3|STATIC");
  EXPECT_EQ(modesToString(Mode::CAROUSEL, Mode::GEAR, Validation::NONE),
            "CAROUSEL|GEAR|NONE");
  EXPECT_EQ(modesToString(Mode::BASIC, Mode::BASIC, Validation::DYNAMIC),
            "WIDE 1|WIDE 1|DYNAMIC");
  EXPECT_EQ(modesToString(Mode::BASIC, Mode::BASIC, Validation::ENABLER),
            "WIDE 1|WIDE 1|ENABLER");
  EXPECT_EQ(modesToString(Mode::LIGHTNING, Mode::LIGHTNING, Validation::NONE),
            "LIGHTNING|LIGHTNING|NONE");
  EXPECT_EQ(modesToString(Mode::BANDAGED, Mode::BANDAGED, Validation::ARROWS),
            "BANDAGED|BANDAGED|ARROWS");
}

TEST(Enums, SameElements) {
  std::array<std::array<int, 3>, 2> b = {{
      {{1, 2, 3}},
      {{4, 5, 6}},
  }};
  std::array<std::array<int, 3>, 2> shuffled = {{
      {{2, 4, 6}},
      {{3, 1, 5}},
  }};

  EXPECT_TRUE(sameElements(b, shuffled));

  std::array<std::array<int, 3>, 2> unequal = {{
      {{2, 2, 2}},
      {{3, 1, 5}},
  }};

  EXPECT_FALSE(sameElements(b, unequal));
}

TEST(Enums, MoveFits) {
  EXPECT_TRUE(moveFits(Mode::WIDE_2, 3));
  EXPECT_TRUE(moveFits(Mode::WIDE_2, 4));
  EXPECT_TRUE(moveFits(Mode::WIDE_3, 3));

  EXPECT_FALSE(moveFits(Mode::WIDE_2, 1));
  EXPECT_FALSE(moveFits(Mode::WIDE_1, 0));
  EXPECT_FALSE(moveFits(Mode::WIDE_4, 3));
}
