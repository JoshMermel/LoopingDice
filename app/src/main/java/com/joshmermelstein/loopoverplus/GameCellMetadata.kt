package com.joshmermelstein.loopoverplus

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

// Creating a GameCell requires a bunch of nonsense from Context like colors and drawables but that
// level of access makes testing annoying.
// This class holds all the things a GameCell needs to be created but is just a struct so it can be
// easily replaced by a lightweight fake when needed.
class GameCellMetadata(
    val colors: Array<Int>,
    val pipColor: Int,
    val bondColor: Int,
    val lock: Drawable,
    val key: Drawable,
    val hArrow: Drawable,
    val vArrow: Drawable,
    val lightning: Drawable,
) {
    constructor(context: Context) : this(
        arrayOf(
            ContextCompat.getColor(context, R.color.red_cell),
            ContextCompat.getColor(context, R.color.gray_cell),
            ContextCompat.getColor(context, R.color.blue_cell),
            ContextCompat.getColor(context, R.color.green_cell),
            ContextCompat.getColor(context, R.color.bandaged_cell),
            ContextCompat.getColor(context, R.color.enabler_cell),
        ),
        ContextCompat.getColor(context, R.color.gameplay_background),
        ContextCompat.getColor(context, R.color.bandaged_cell),
        ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.ic_baseline_lock_24,
            null
        )!!,
        ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.ic_baseline_vpn_key_24,
            null
        )!!,
        ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.ic_baseline_swap_horiz_24,
            null
        )!!,
        ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.ic_baseline_swap_vert_24,
            null
        )!!,
        ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.ic_baseline_flash_on_24,
            null
        )!!
    )
}

// Returns a well formed but useless GameCellMetadata for use in tests and scrambling (where the
// board is manipulated but never shown to the user).
fun fakeGameCellMetadata(): GameCellMetadata {
    return GameCellMetadata(
        Array(6) { Color.BLACK },
        Color.BLACK,
        Color.BLACK,
        ShapeDrawable(RectShape()),
        ShapeDrawable(RectShape()),
        ShapeDrawable(RectShape()),
        ShapeDrawable(RectShape()),
        ShapeDrawable(RectShape()),
    )
}