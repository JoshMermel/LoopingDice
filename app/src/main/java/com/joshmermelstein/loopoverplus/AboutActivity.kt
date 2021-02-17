package com.joshmermelstein.loopoverplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.text.set
import androidx.core.text.toSpannable
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

// A simple activity for displaying info about the app to interested users.
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val aboutGame = findViewById<TextView>(R.id.about_game)
        val aboutGameContents =
            "Looping Dice is a game by <a href='https://joshmermelstein.com'>Josh Mermelstein</a>"
        aboutGame.text = HtmlCompat.fromHtml(aboutGameContents, HtmlCompat.FROM_HTML_MODE_COMPACT)
        aboutGame.movementMethod = LinkMovementMethod.getInstance()

        val github = findViewById<TextView>(R.id.github)
        val githubContents =
            "The source code is available on <a href='https://github.com/JoshMermel/LoopingDice'>my Github</a> and is licensed under the GPL v3.0 license "
        github.text = HtmlCompat.fromHtml(githubContents, HtmlCompat.FROM_HTML_MODE_COMPACT)
        github.movementMethod = LinkMovementMethod.getInstance()

        val sixteenPuzzle = findViewById<TextView>(R.id.sixteen_puzzle)
        val sixteenPuzzleContents =
            "It is inspired by the <a href='https://www.chiark.greenend.org.uk/~sgtatham/puzzles/doc/sixteen.html'>Sixteen Puzzle</a> from Simon Tatham's Portable Puzzle Collection"
        sixteenPuzzle.text =
            HtmlCompat.fromHtml(sixteenPuzzleContents, HtmlCompat.FROM_HTML_MODE_COMPACT)
        sixteenPuzzle.movementMethod = LinkMovementMethod.getInstance()

        val gears = findViewById<TextView>(R.id.gears)
        val gearsContents =
            "Gear mode was inspired by geared puzzles such as <a href='https://twistypuzzles.com/cgi-bin/puzzle.cgi?pkey=1501'> The Gear Cube</a> by Oskar van Deventer"
        gears.text = HtmlCompat.fromHtml(gearsContents, HtmlCompat.FROM_HTML_MODE_COMPACT)
        gears.movementMethod = LinkMovementMethod.getInstance()

        val bandaging = findViewById<TextView>(R.id.bandaging)
        val bandagingContents =
            "Static cells and bandaging were inspired by <a href='https://joshmermelstein.com/bandaged-cube-explorer-post/'>different schemes of bandaging other twisty puzzles</a>"
        bandaging.text = HtmlCompat.fromHtml(bandagingContents, HtmlCompat.FROM_HTML_MODE_COMPACT)
        bandaging.movementMethod = LinkMovementMethod.getInstance()

        val dynamicBandaging = findViewById<TextView>(R.id.dynamic_bandaging)
        val dynamicBandagingContents =
            "Dynamic bandaging was inspired by <a href='http://twistypuzzles.com/cgi-bin/puzzle.cgi?pkey=1691'>The constrained cube</a> by TomZ and <a href='https://www.twistypuzzles.com/cgi-bin/puzzle.cgi?pkey=7227'>The joint cube</a> by Grigr and Fenz"
        dynamicBandaging.text =
            HtmlCompat.fromHtml(dynamicBandagingContents, HtmlCompat.FROM_HTML_MODE_COMPACT)
        dynamicBandaging.movementMethod = LinkMovementMethod.getInstance()

        val carousel = findViewById<TextView>(R.id.carousel)
        val carouselContents =
            "Carousel mode was inspired by the android app, <a href='https://play.google.com/store/apps/details?id=com.katas.squareminx&hl=en_US&gl=US'>squareminx</a> by Katas Studios"
        carousel.text = HtmlCompat.fromHtml(carouselContents, HtmlCompat.FROM_HTML_MODE_COMPACT)
        carousel.movementMethod = LinkMovementMethod.getInstance()

        val enabler = findViewById<TextView>(R.id.enabler)
        val enablerContents =
            "Enabler mode was inspired by <a href='https://twistypuzzles.com/cgi-bin/puzzle.cgi?pkey=3678'>The enabler cube</a> by Oskar van Deventer"
        enabler.text = HtmlCompat.fromHtml(enablerContents, HtmlCompat.FROM_HTML_MODE_COMPACT)
        enabler.movementMethod = LinkMovementMethod.getInstance()

        val libraries = findViewById<TextView>(R.id.libraries)
        val librariesText =
            "Click here for information on open source libraries used by Looping Dice.".toSpannable()
        val context: Context = this
        librariesText[0..10] = object : ClickableSpan() {
            override fun onClick(view: View) {
                startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            }
        }
        libraries.movementMethod = LinkMovementMethod.getInstance()
        libraries.text = librariesText

    }

}