# LoopingDice
Looping dice is an android game about manipulating a grid of squares.

It is inspired by the 16 puzzle but adds many new game mechanics and bespoke
levels. It is available at
https://play.google.com/store/apps/details?id=com.joshmermelstein.loopingdice

## Architecture

Let's start by talking about a few relatively abstract classes the are the core
of the game.

`GameCell`: These represent the different kinds of cells that exist across levels.
The base class mostly manages drawing the cell at the right position/wraparound
and drawing the pips. Specialized subclasses override this logic to draw cells
in special ways (i.e. different outline, overriding pip shape). GameCells are
stored in a GameBoard which is a glorified 2D array with some helper functions.

`Move`: Move represent the outcome of the player swiping; usually this means
permuting GameCells on the board. Moves are stored in a MoveQueue that ensures
that only one move is executing a time. Each subclass of Move permutes the board
in its own way.

`MoveFactory`: Move factories produce moves, like you might expect. Subclasses
return different kinds of moves and also handle validating moves.

## Data format for levels

Each level needs to know the initial state, final state, and what kind of move
factory to make. These are implement in text files in
`app/src/main/assets/levels`. The level format is:

```
num_rows
num_cols
move factory
initial state (as comma separated list)
goal state (as comma separated list)
```

The entries in those comma separated lists are strings to tell a gamecell what
type it is. The current possibilities are:

| spec | effect | 
| --- | --- |
|1-24 | normal game cells with various colors and numbers of pips|
|V \d | a vertical gamecell whose color is determined by the number|
|H \d | a horizontal  gamecell whose color is determined by the number|
|F \d | a fixed cell (for static + dynamic modes) with a number of pips determined by the number E | An enabler cell|
|B \d U D L R | a bandaged cell with color determined by the number and bonds determined by which of the {U, D, L, R} follow the number. Order does not matter.|

### Packs

Levels are organized into "packs" for tracking which levels follow each other
and for layout on the main screen. Level packs are also text files and are found
in `app/src/main/assets/packs`.

The format is a header, followed by one level per-line. Each level is a space
separated list of fields:

```
Title of the pack
display-name filename pefect-score 3*score 2*score
display-name filename pefect-score 3*score 2*score
...
```

### Creating new levels

To create a new level, add a new textfile to `app/src/main/assets/levels` and
reference it from some pack in `app/src/main/assets/packs`.
