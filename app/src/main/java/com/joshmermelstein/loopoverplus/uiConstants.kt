package com.joshmermelstein.loopoverplus

// This file collects a handful of magic UI constants. These values were chosen for style reasons,
// not for functional ones, so they should be able to be tweaked without breaking anything.

// Controls how small a sliver of a gamecell is drawn when it wraps around. Setting this >0 reduces
// some jank from moves snapping back due to easing.
const val eccentricityThreshold = 0.06

// Controls how far the user has to swipe before a move is registered. This value should be
// relatively small so users don't have to move too much but also large enough that it's easy to
// cancel moves by moving back to where the swipe started.
const val minSwipeDistance = 1500

// Controls how far from horizontal/vertical a swipe can be and still be registered. Making this
// smaller requires users to swipe more accurately but making it larger makes it easier to do an
// accidental/ambiguous input
// The unit is radians.
const val allowedAngleError = 0.3  // radians

// These control how much padding is between gamecells in the main grid and goal grid.
const val mainGridPadding = 5
const val goalGridPadding = 2

// This controls the layout of the level select screen.
const val mainScreenNumCols = 4