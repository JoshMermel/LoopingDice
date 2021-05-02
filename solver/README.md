# Solving tools

This directory contains a library for simulating looping dice as well as some
executables that use that library.

These tools were created for my personal use without any consideration for
usability by others so the interfaces are admittedly awkward.

## Overview

Libraries:

 - board.h contains helpers for representing, printing, and hashing the board
 - move.h contains helpers for executing moves on a board
 - board_test.cc and move_test.cc contain tests for the corresponding .h files.
 - enums.h contains enums, constants, and some helpers related to them.

Binaries:

 - scramble.cc takes a puzzle and scrambles it by applying random moves
 - bfs.cc takes a puzzle and breadth-first explores it, printing the state every
   time a new state is discovered. I found it useful to write little helper like
   `is_symmetric()` to limit the output spam from this binary.
 - mitm.cc take an intial state and final state and does meet-in-the-middle
   breadth first search to find an optimal path from the start to the finish.
   Its output is in the format the the looping dice accepts.

## Usage

All three binaries are configured by changing global constants, `num_rows`,
`num_cols`, `row_mode`, `col_mode`, `initial`, (and sometimes `win`). The
binaries are intended to be run with `bazel run :<name>`

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
Mode row_mode = Mode::GEAR;
Mode col_mode = Mode::WIDE_1;
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
Mode row_mode = Mode::WIDE_3;
Mode col_mode = Mode::CAROUSEL;
```

In Dynamic mode, 0's and negative numbers are treated as dynamic cells. Example

```
constexpr size_t num_rows = 3;
constexpr size_t num_cols = 3;
constexpr Board<num_rows, num_cols> initial = {{
  {{1,1,1}},
  {{0,1,0}},
  {{0,1,0}},
}};
constexpr Board<num_rows, num_cols> win = {{
  {{0,0,1}},
  {{0,0,1}},
  {{1,1,1}},
}};
Mode row_mode = Mode::DYNAMIC;
Mode col_mode = Mode::DYNAMIC;
```

In Bandaged modes, we use bitmasks to store which bonds each cell has. Note that
bonds must be specified from *both* cells that share the bond. The constants,
`LEFT`, `RIGHT`, `UP`, and `DOWN` are defined in utils.h for convenience.
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
Mode row_mode = Mode::BANDAGED;
Mode col_mode = Mode::BANDAGED;

```

Arrow mode reused the masks from bandaged mode. `DOWN` indicates a vertical cell
and `RIGHT` indicates a horizontal cell. Example:

```
constexpr size_t num_rows = 3;
constexpr size_t num_cols = 3;
constexpr Board<num_rows, num_cols> initial = {{
  {{1     ,2,1|RIGHT}},
  {{2     ,2,2}},
  {{1|DOWN,2,1}},
}};
constexpr Board<num_rows, num_cols> win = {{
  {{1     ,1|RIGHT,2}},
  {{1|DOWN,1      ,2}},
  {{2     ,2      ,2}},
}};
Mode row_mode = Mode::AXIS;
Mode col_mode = Mode::AXIS;
```

Enabler mode uses 0's to indicate enablers. Example:

```
constexpr size_t num_rows = 3;
constexpr size_t num_cols = 3;
constexpr Board<num_rows, num_cols> initial = {{
  {{0,1,2}},
  {{1,1,1}},
  {{2,1,0}},
}};
constexpr Board<num_rows, num_cols> win = {{
  {{2,1,0}},
  {{1,1,1}},
  {{0,1,2}},
}};
Mode row_mode = Mode::ENABLER;
Mode col_mode = Mode::ENABLER;
```

Static cells mode uses 0 to indicate a static cell. Like Wide mode, different
enums exist for different depths of move. Example:

```
constexpr size_t num_rows = 3;
constexpr size_t num_cols = 3;
constexpr Board<num_rows, num_cols> initial = {{
  {{1,1,1}},
  {{2,2,2}},
  {{3,3,0}},
}};
constexpr Board<num_rows, num_cols> win = {{
  {{1,2,3}},
  {{1,2,3}},
  {{1,2,0}},
}};
Mode row_mode = Mode::STATIC_1;
Mode col_mode = Mode::STATIC_2;
```
