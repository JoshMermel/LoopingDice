# Solving tools

This directory contains a library for simulating looping dice as well as some
executables that use that library.

These tools were created for my personal use without any consideration for
usability by others so the interfaces are admittedly awkward.

## Overview

Libraries:

 - board.h contains helpers for representing, printing, and hashing the board
 - move.h contains helpers for executing moves on a board
   -  \*_moves.h each contain implementations of particular move types.
 - board_test.cc and move_test.cc contain tests for the corresponding .h files.
 - enums.h contains enums, constants, and some helpers related to them.

Binaries:

 - scramble.cc takes a puzzle and scrambles it by applying random moves
 - bfs.cc takes a puzzle and breadth-first explores it, printing the state every
   time a new state is discovered. I found it useful to write little helper like
   `is_symmetric()` to limit the output spam from this binary.
 - mitm.cc take an initial state and final state and does meet-in-the-middle
   breadth first search to find an optimal path from the start to the finish.
   Its output is in the format the the looping dice accepts.

## Usage

All three binaries are configured by changing global constants, `num_rows`,
`num_cols`, `row_mode`, `col_mode`, `validation`, `initial`, (and sometimes
 `win`). The binaries are intended to be run with `bazel run :<name>`

Note that C++ will warn you if you set `num_row` or `num_cols` to small but will
not warn you if you set them too large.

### Example configurations

For modes without special cells (wide, carousel, gear, hybrids of these), no
special syntax is required. Note that different depths of wide move are
implemented as different enums.

```
constexpr size_t num_rows = 4;
constexpr size_t num_cols = 3;
constexpr Board<num_rows, num_cols> initial = {{
  {{1,1,1}},
  {{1,1,2}},
  {{1,2,2}},
  {{2,2,2}},
}};
constexpr Board<num_rows, num_cols> win = {{
  {{2,2,2}},
  {{1,2,2}},
  {{1,1,2}},
  {{1,1,1}},
}};
constexpr Mode row_mode = Mode::GEAR;
constexpr Mode col_mode = Mode::WIDE_1;
constexpr Validation validation = Validation::NONE;
```

or

```
constexpr size_t num_rows = 4;
constexpr size_t num_cols = 4;
constexpr Board<num_rows, num_cols> initial = {{
  {{1,2,3,4}},
  {{1,2,3,4}},
  {{1,2,3,4}},
  {{1,2,3,4}},
}};
constexpr Board<num_rows, num_cols> win = {{
  {{1,1,1,1}},
  {{2,2,2,2}},
  {{3,3,3,3}},
  {{4,4,4,4}},
}};
constexpr Mode row_mode = Mode::WIDE_3;
constexpr Mode col_mode = Mode::CAROUSEL;
constexpr Validation validation = Validation::NONE;
```

To simulate dynamic mode, set the `FIXED` bitmask to indicate which cells can't
loop and set the `validation` to `Validation::DYNAMIC`.
Example:

```
constexpr size_t num_rows = 3;
constexpr size_t num_cols = 3;
constexpr Board<num_rows, num_cols> initial = {{
  {{2,       2, 2      }},
  {{1|FIXED, 2, 1|FIXED}},
  {{1|FIXED, 2, 1|FIXED}},
}};
constexpr Board<num_rows, num_cols> win = {{
  {{1|FIXED, 1|FIXED,2}},
  {{1|FIXED, 1|FIXED,2}},
  {{2,       2,      2}},
}};
constexpr Mode row_mode = Mode::BASIC;
constexpr Mode col_mode = Mode::BASIC;
constexpr Validation validation = Validation::DYNAMIC;
```

To simulate bandaged mode use bitmasks `UP`, `DOWN`, `LEFT`, `RIGHT` to indicate
which direction a cell has bonds. Also set the modes to `BANDAGED`.  Note that
bonds must be specified from *both* cells that share the bond. 
Example:

```
constexpr size_t num_rows = 4;
constexpr size_t num_cols = 3;
constexpr Board<num_rows, num_cols> initial = {{
  {{1      ,1,      2|DOWN}},
  {{3|RIGHT,3|LEFT, 2|UP}},
  {{2|DOWN ,3|RIGHT,3|LEFT}},
  {{2|UP   ,1,      1}}
}};
constexpr Board<num_rows, num_cols> win = {{
  {{1,2|DOWN, 2|DOWN}},
  {{1,2|UP,   2|UP}},
  {{1,3|RIGHT,3|LEFT}},
  {{1,3|RIGHT,3|LEFT}},
}};
constexpr Mode row_mode = Mode::BANDAGED;
constexpr Mode col_mode = Mode::BANDAGED;
constexpr Validation validation = Validation::NONE;
```

To simulate arrows mode, use the bitmasks, `HORIZ` and `VERT` to indicate which
cells are horizontal and vertically locked, and set the validaiton to
`Validation::ARROWS`.
Example:

```
constexpr size_t num_rows = 3;
constexpr size_t num_cols = 3;
constexpr Board<num_rows, num_cols> initial = {{
  {{1     ,2,1|HORIZ}},
  {{2     ,2,2}},
  {{1|VERT,2,1}},
}};
constexpr Board<num_rows, num_cols> win = {{
  {{1     ,1|HORIZ,2}},
  {{1|VERT,1      ,2}},
  {{2     ,2      ,2}},
}};
constexpr Mode row_mode = Mode::BASIC;
constexpr Mode col_mode = Mode::BASIC;
constexpr Validation validation = Validation::ARROWS;
```

To simulate enabler mode, use the `ENABLER` bitmask to indicate which cells are
enabler and set the validation to `Validation::ENABLER`.
Example:

```
constexpr size_t num_rows = 3;
constexpr size_t num_cols = 3;
constexpr Board<num_rows, num_cols> initial = {{
  {{ENABLER,1,2      }},
  {{1      ,1,1      }},
  {{2      ,1,ENABLER}},
}};
constexpr Board<num_rows, num_cols> win = {{
  {{2      ,1,ENABLER}},
  {{1      ,1,1      }},
  {{ENABLER,1,2      }},
}};
constexpr Mode row_mode = Mode::BASIC;
constexpr Mode col_mode = Mode::BASIC;
constexpr Validation validation = Validation::ENABLER;
```

To simulate static cells mode, use the `FIXED` bitmask to indicate which cells
are fixed and set the validation to `Validation::STATIC`.
Example:

```
constexpr size_t num_rows = 3;
constexpr size_t num_cols = 3;
constexpr Board<num_rows, num_cols> initial = {{
  {{1,1,1}},
  {{2,2,2}},
  {{3,3,1|FIXED}},
}};
constexpr Board<num_rows, num_cols> win = {{
  {{1,2,3}},
  {{1,2,3}},
  {{1,2,1|FIXED}},
}};
constexpr Mode row_mode = Mode::WIDE_1;
constexpr Mode col_mode = Mode::WIDE_2;
constexpr Validation validation = Validation::STATIC;
```

To simluate lightning mode, use the bitmask `LIGHTNING` to indicate which cells
have bolts on them and set the mode to `Mode::LIGHTNING`.
Example

```
constexpr size_t num_rows = 3;
constexpr size_t num_cols = 2;
constexpr Board<num_rows, num_cols> initial = {{
  {{2|LIGHTNING,1}},
  {{0,1}},
  {{0,2|LIGHTNING}}
}};
constexpr Board<num_rows, num_cols> win = {{
  {{2|LIGHTNING,0}},
  {{1,0}},
  {{1,2|LIGHTNING}}
}};
constexpr Mode row_mode = Mode::LIGHTNING;
constexpr Mode col_mode = Mode::LIGHTNING;
constexpr Validation validation = Validation::NONE;
```

### Notes on Hybrids.

The app presents the user with the idea of "Hybrid" modes but that's a
convenient lie. Hybrids are just mixes of row+col modes and validators.
This solver can simulate those hybrid modes the same way, i.e. 

```
constexpr Mode row_mode = Mode::LIGHTNING;
constexpr Mode col_mode = Mode::LIGHTNING;
constexpr Validation validation = Validation::ARROWS;
```

This solver actually supports more kinds of hybrid gamecells than the app does.
For example `1 | ENABLER | UP` can't exist in the app (yet). Perhaps someday
I'll add app support for such levels. In the meantime, the solver will solve
correctly with them but may print them in nonsensical ways.
