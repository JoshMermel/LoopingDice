#ifndef LOOPINGDICE_MOVES
#define LOOPINGDICE_MOVES

#include "bandaged_moves.h"
#include "board.h"
#include "carousel_moves.h"
#include "gear_moves.h"
#include "lightning_moves.h"
#include "wide_moves.h"

template <std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> rowMove(const Board<num_rows, num_cols> &board,
                                  int offset, bool forward, const Mode &mode,
                                  const Validation &validation) {
  switch (mode) {
  case Mode::WIDE_2:
    return wideRowMove(board, offset, forward, validation, 2);
  case Mode::WIDE_3:
    return wideRowMove(board, offset, forward, validation, 3);
  case Mode::WIDE_4:
    return wideRowMove(board, offset, forward, validation, 4);
  case Mode::GEAR:
    return gearRowMove(board, offset, forward, validation);
  case Mode::CAROUSEL:
    return carouselRowMove(board, offset, forward, validation);
  case Mode::BANDAGED:
    return bandagedRowMove(board, offset, forward, validation);
  case Mode::LIGHTNING:
    return lightningRowMove(board, offset, forward, validation);
  default:
    return wideRowMove(board, offset, forward, validation, 1);
  }
}

template <std::size_t num_rows, std::size_t num_cols>
Board<num_rows, num_cols> colMove(const Board<num_rows, num_cols> &board,
                                  int offset, bool forward, const Mode &mode,
                                  const Validation &validation) {
  switch (mode) {
  case Mode::WIDE_2:
    return wideColMove(board, offset, forward, validation, 2);
  case Mode::WIDE_3:
    return wideColMove(board, offset, forward, validation, 3);
  case Mode::WIDE_4:
    return wideColMove(board, offset, forward, validation, 4);
  case Mode::GEAR:
    return gearColMove(board, offset, forward, validation);
  case Mode::CAROUSEL:
    return carouselColMove(board, offset, forward, validation);
  case Mode::BANDAGED:
    return bandagedColMove(board, offset, forward, validation);
  case Mode::LIGHTNING:
    return lightningColMove(board, offset, forward, validation);
  default:
    return wideColMove(board, offset, forward, validation, 1);
  }
}

#endif
