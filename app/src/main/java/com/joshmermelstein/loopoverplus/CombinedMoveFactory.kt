package com.joshmermelstein.loopoverplus

// Combines two moves factories into one. The first one is used to generate horizontal moves and the
// second one is used to generate vertical moves.
// Many combinations would result in levels that don't really make sense - i.e.
// a level where enabler cells are required for horizontal moves but not
// vertical ones. For best results, I recommend only combining {wide, carousel,
// gear} with one another.
// TODO(jmerm): replace all users of this with MoveFactory's new interface. This is a sorta big
//  goal since I think it'll require changing the file format for levels
class CombinedMoveFactory(horizontal: MoveFactory, vertical: MoveFactory) :
    MoveFactory(horizontal.rowEffect, vertical.colEffect, MoveValidator())