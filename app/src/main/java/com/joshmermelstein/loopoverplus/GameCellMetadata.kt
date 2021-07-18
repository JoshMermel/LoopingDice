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
// This class holds all the things a Game
class GameCellMetadata(
    val colors: Array<Int>,
    val pipColor: Int,
    val bondColor: Int,
    val lock: Drawable,
    val key: Drawable,
    val hArrow: Drawable,
    val vArrow: Drawable,
) {
    constructor(context: Context) : this(
        arrayOf(
            Color.parseColor("#FF4242"), // red
            Color.parseColor("#A691AE"), // gray
            Color.parseColor("#235FA4"), // blue
            Color.parseColor("#6FDE6E"), // green
            ContextCompat.getColor(context, R.color.bandaged_cell), // bandaged (black/white)
            Color.parseColor("#E8F086"), // enabler
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
        )!!
    )
}

// Returns a well formed but useless GameCellMetadata for use in tests.
fun fakeGameCellMetadata() : GameCellMetadata {
    return GameCellMetadata(
        Array(6) { Color.BLACK },
        Color.BLACK,
        Color.BLACK,
        ShapeDrawable(RectShape()),
        ShapeDrawable(RectShape()),
        ShapeDrawable(RectShape()),
        ShapeDrawable(RectShape())
    )
}