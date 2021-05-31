package com.joshmermelstein.loopoverplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

// TODO(jmerm): support for additional modes

@Parcelize
class RandomLevelParams(
    val numRows: Int,
    val numCols: Int,
    val rowFactory: String,
    val colFactory: String,
    val rowDepth: Int?,
    val colDepth: Int?
) : Parcelable

// Activity for letting the user build a level
class InfinityActivity : AppCompatActivity() {
    private val rowSizes = (2..6).map { num -> num.toString() }
    private val colSizes = (2..4).map { num -> num.toString() }
    private val modes = arrayOf("Wide", "Carousel", "Gear")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinity)
        configureSizePickers()
        configureModePickers()
        configureButton()
    }

    // TODO(jmerm): randomize these?
    private fun configureSizePickers() {
        val rowSizeSpinner = findViewById<Spinner>(R.id.rowSizeSpinner)
        val rowAdapter = ArrayAdapter(this, R.layout.spinner_item, rowSizes)
        rowAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rowSizeSpinner.adapter = rowAdapter

        rowSizeSpinner.onItemSelectedListener = SelectionMadeListener(::updateRowDepthPicker)

        val colSizeSpinner = findViewById<Spinner>(R.id.colSizeSpinner)
        val colAdapter = ArrayAdapter(this, R.layout.spinner_item, colSizes)
        colAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        colSizeSpinner.adapter = colAdapter
        colSizeSpinner.onItemSelectedListener = SelectionMadeListener(::updateColDepthPicker)
    }

    // TODO(jmerm): randomize these?
    private fun configureModePickers() {
        val rowModeSpinner = findViewById<Spinner>(R.id.rowModeSpinner)
        val rowAdapter = ArrayAdapter(this, R.layout.spinner_item, modes)
        rowAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rowModeSpinner.adapter = rowAdapter
        rowModeSpinner.onItemSelectedListener = SelectionMadeListener(::updateRowDepthPicker)


        val colModeSpinner = findViewById<Spinner>(R.id.colModeSpinner)
        val colAdapter = ArrayAdapter(this, R.layout.spinner_item, modes)
        colAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        colModeSpinner.adapter = colAdapter
        colModeSpinner.onItemSelectedListener = SelectionMadeListener(::updateColDepthPicker)
    }

    private fun getNumRows(): Int {
        return findViewById<Spinner>(R.id.rowSizeSpinner).selectedItem.toString().toInt()
    }

    private fun getNumCols(): Int {
        return findViewById<Spinner>(R.id.colSizeSpinner).selectedItem.toString().toInt()
    }

    private fun getRowMode(): String {
        return findViewById<Spinner>(R.id.rowModeSpinner).selectedItem.toString()
    }

    private fun getColMode(): String {
        return findViewById<Spinner>(R.id.colModeSpinner).selectedItem.toString()
    }

    private fun getRowDepth(): Int? {
        return findViewById<Spinner>(R.id.rowDepthSpinner).selectedItem?.toString()?.toInt()
    }

    private fun getColDepth(): Int? {
        return findViewById<Spinner>(R.id.colDepthSpinner).selectedItem?.toString()?.toInt()
    }

    private fun updateRowDepthPicker() {
        if (getRowMode() == "Wide") {
            val oldDepth = getRowDepth()
            findViewById<View>(R.id.row_depth_container)?.visibility = View.VISIBLE
            val rowDepthSpinner = findViewById<Spinner>(R.id.rowDepthSpinner)
            val options = (1..getNumRows()).map { num -> num.toString() }
            val adapter = ArrayAdapter(this, R.layout.spinner_item, options)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            rowDepthSpinner.adapter = adapter
            if (oldDepth != null) {
                if (oldDepth <= getNumRows()) {
                    rowDepthSpinner.setSelection(oldDepth - 1)
                } else {
                    rowDepthSpinner.setSelection(getNumRows() - 1)
                }
            }
        } else {
            findViewById<View>(R.id.row_depth_container)?.visibility = View.GONE
        }
    }

    private fun updateColDepthPicker() {
        if (getColMode() == "Wide") {
            val oldDepth = getColDepth()
            findViewById<View>(R.id.col_depth_container)?.visibility = View.VISIBLE
            val colDepthSpinner = findViewById<Spinner>(R.id.colDepthSpinner)
            val options = (1..getNumCols()).map { num -> num.toString() }
            val adapter = ArrayAdapter(this, R.layout.spinner_item, options)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            colDepthSpinner.adapter = adapter

            if (oldDepth != null) {
                if (oldDepth <= getNumCols()) {
                    colDepthSpinner.setSelection(oldDepth - 1)
                } else {
                    colDepthSpinner.setSelection(getNumCols() - 1)
                }
            }
        } else {
            findViewById<View>(R.id.col_depth_container)?.visibility = View.GONE
        }
    }

    private fun configureButton() {
        val button = findViewById<Button>(R.id.generate_level)
        button?.setOnClickListener {
            val intent = Intent(this, GameplayActivity::class.java)
            intent.putExtra("randomLevelParams", build())
            startActivity(intent)
        }
    }

    private fun build(): RandomLevelParams {
        return RandomLevelParams(
            getNumRows(),
            getNumCols(),
            getRowMode(),
            getColMode(),
            getRowDepth(),
            getColDepth()
        )
    }
}

// Helper class for calling a void callback when the user picks an option.
class SelectionMadeListener(val action: () -> Unit) : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
        action()
}

fun fromRandomFactory(name: String, rowDepth: Int?, colDepth: Int?): MoveFactory {
    return if (name == "Gear") {
        GearMoveFactory()
    } else if (name == "Carousel") {
        CarouselMoveFactory()
    } else if (name == "Wide") {
        WideMoveFactory(rowDepth!!, colDepth!!)
    } else {
        BasicMoveFactory()
    }
}

fun randomMove(
    board : GameBoard,
    factory: MoveFactory,
    num_rows: Int,
    num_cols: Int,
    context: Context
): Move {
    val direction = if (Random.nextBoolean()) {
        Direction.FORWARD
    } else {
        Direction.BACKWARD
    }
    val seed = Random.nextInt(num_rows + num_cols)
    return if (seed < num_rows) {
        factory.makeMove(Axis.HORIZONTAL, direction, seed, board)
    } else {
        factory.makeMove(Axis.VERTICAL, direction, seed - num_rows, board)
    }
}

fun scramble(
    solved: Array<String>, factory: MoveFactory, num_rows: Int, num_cols: Int, context: Context
): Array<String> {
    val gameBoard = GameBoard(num_rows, num_cols, solved, context)
    for (i in (0..1000)) {
        val move = randomMove(gameBoard, factory, num_rows, num_cols, context)
        move.finalize(gameBoard)
    }
    return gameBoard.toString().split(",").toTypedArray()
}

// TODO(jmerm): needing to take the context here is silly, fix that.
fun generateRandomLevel(options: RandomLevelParams, context: Context): GameplayParams {
    // make factory
    val factory: MoveFactory =
        if (options.rowFactory == options.colFactory) {
            fromRandomFactory(options.rowFactory, options.rowDepth, options.colDepth)
        } else {
            CombinedMoveFactory(
                fromRandomFactory(options.rowFactory, options.rowDepth, options.colDepth),
                fromRandomFactory(options.colFactory, options.rowDepth, options.colDepth)
            )
        }

    // TODO(jmerm): make the board in a nicer way.
    // TODO(jmerm): replace this with a proper scrambler
    val goal = Array(options.numRows * options.numCols) { i ->
        i + 1
    }.map { i -> i.toString() }.toTypedArray()
    val start = scramble(goal, factory, options.numRows, options.numCols, context)

    return GameplayParams(
        "∞",
        options.numRows,
        options.numCols,
        factory,
        start,
        goal,
        ""
    )
}