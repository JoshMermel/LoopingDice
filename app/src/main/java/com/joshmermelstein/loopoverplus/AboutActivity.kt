package com.joshmermelstein.loopoverplus

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat

// A simple activity for displaying info about the app to interested users.
// TODO(jmerm): better motivation for static bandaging
// TODO(jmerm): motivation for regular bandaging.
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val txtView = findViewById<TextView>(R.id.about_text)
        val contents =
            "<br/>Looping Dice is a game by <a href='https://joshmermelstein.com'>Josh Mermelstein</a><br/><br/>" +
                    "It is inspired by the <a href='https://www.chiark.greenend.org.uk/~sgtatham/puzzles/doc/sixteen.html'>Sixteen Puzzle</a> from Simon Tatham's Portable Puzzle Collection<br/><br/>" +
                    "Additionally, many modes were inspired by puzzles in the real world<br/><br>" +
                    "Gear mode was inspired by <a href='https://twistypuzzles.com/cgi-bin/puzzle.cgi?pkey=1501'> The Gear Cube</a><br/><br>" +
                    "Static bandaging was inspired by <a href='https://joshmermelstein.com/bandaged-cube-explorer-post/'>Bandaged 3x3x3 configurations</a><br/><br>" +
                    "Dynamic bandaging was inspired by <a href='http://twistypuzzles.com/cgi-bin/puzzle.cgi?pkey=1691'>The constrained cube</a> and <a href='https://www.twistypuzzles.com/cgi-bin/puzzle.cgi?pkey=7227'>The joint cube</a><br/><br>" +
                    "Carousel mode was inspired by the android app, <a href='https://play.google.com/store/apps/details?id=com.katas.squareminx&hl=en_US&gl=US'>squareminx</a><br/><br>" +
                    "Enabler mode was inspired by <a href='https://twistypuzzles.com/cgi-bin/puzzle.cgi?pkey=3678'>The enabler cube</a><br/><br>" +
                    "The source code is available on <a href='https://github.com/joshmermel'>My Github</a> and is licensed under some license I need to pick and link here "
        txtView.text = HtmlCompat.fromHtml(contents, HtmlCompat.FROM_HTML_MODE_COMPACT)
        txtView.movementMethod = LinkMovementMethod.getInstance()

    }

}