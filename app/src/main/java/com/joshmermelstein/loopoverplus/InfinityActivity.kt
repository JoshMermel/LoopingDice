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

// TODO(jmerm): move board generating logic to own file
// TODO(jmerm): add spinner for num enablers, num_bandaged, etc

@Parcelize
class RandomLevelParams(
    val numRows: Int,
    val numCols: Int,
    val rowFactory: String,
    val colFactory: String?,
    val rowDepth: Int?,
    val colDepth: Int?
) : Parcelable

// Activity for letting the user build a level
class InfinityActivity : AppCompatActivity(),  AdapterView.OnItemSelectedListener {
    private val rowSizes = (2..6).map { num -> num.toString() }
    private val colSizes = (2..4).map { num -> num.toString() }
    private val rowModes = arrayOf("Wide", "Carousel", "Gear", "Dynamic Bandaging", "Static Cells", "Enabler")
    private val colModes = arrayOf("Wide", "Carousel", "Gear")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinity)
        configureSizePickers()
        configureRowModePicker()
        configureButton()
    }

    private fun configureSizePickers() {
        val rowSizeSpinner = findViewById<Spinner>(R.id.rowSizeSpinner)
        val rowAdapter = ArrayAdapter(this, R.layout.spinner_item, rowSizes)
        rowAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rowSizeSpinner.adapter = rowAdapter
        rowSizeSpinner.onItemSelectedListener = this
        rowSizeSpinner.setSelection(Random.nextInt(1, 3))

        val colSizeSpinner = findViewById<Spinner>(R.id.colSizeSpinner)
        val colAdapter = ArrayAdapter(this, R.layout.spinner_item, colSizes)
        colAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        colSizeSpinner.adapter = colAdapter
        colSizeSpinner.onItemSelectedListener = this
        colSizeSpinner.setSelection(Random.nextInt(1, 3))

    }

    private fun configureRowModePicker() {
        val rowModeSpinner = findViewById<Spinner>(R.id.rowModeSpinner)
        val rowAdapter = ArrayAdapter(this, R.layout.spinner_item, rowModes)
        rowAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rowModeSpinner.adapter = rowAdapter
        rowModeSpinner.onItemSelectedListener = this
        rowModeSpinner.setSelection(Random.nextInt(0, rowModes.size + 1) % rowModes.size)
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

    private fun getColMode(): String? {
        return findViewById<Spinner>(R.id.colModeSpinner).selectedItem?.toString()
    }

    private fun getRowDepth(): Int? {
        return findViewById<Spinner>(R.id.rowDepthSpinner).selectedItem?.toString()?.toInt()
    }

    private fun getColDepth(): Int? {
        return findViewById<Spinner>(R.id.colDepthSpinner).selectedItem?.toString()?.toInt()
    }

    private fun updateColModePicker() {
        val container = findViewById<View>(R.id.col_mode_container)
        if (colModes.contains(getRowMode())) {
            if (container?.visibility == View.VISIBLE) {
                return
            }
            container.visibility = View.VISIBLE
            val colModeSpinner = findViewById<Spinner>(R.id.colModeSpinner)
            val colAdapter = ArrayAdapter(this, R.layout.spinner_item, colModes)
            colAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            colModeSpinner.adapter = colAdapter
            colModeSpinner.onItemSelectedListener = this
            colModeSpinner.setSelection(Random.nextInt(0, colModes.size + 1) % colModes.size)
        } else if (container?.visibility == View.VISIBLE) {
            container?.visibility = View.GONE
            findViewById<Spinner>(R.id.colModeSpinner).adapter = null
        }
    }

    private fun updateRowDepthPicker() {
        val container = findViewById<View>(R.id.row_depth_container)
        if (getRowMode() == "Wide" || getRowMode() == "Static Cells") {
            val oldDepth = getRowDepth()
            container?.visibility = View.VISIBLE
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
            container?.visibility = View.GONE
        }
    }

    private fun updateColDepthPicker() {
        val container = findViewById<View>(R.id.col_depth_container)
        if (getColMode() == "Wide" || getRowMode() == "Static Cells") {
            val oldDepth = getColDepth()
            container?.visibility = View.VISIBLE
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
            container?.visibility = View.GONE
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        updateColModePicker()
        updateRowDepthPicker()
        updateColDepthPicker()
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

fun fromRandomFactory(name: String, rowDepth: Int?, colDepth: Int?): MoveFactory {
    return when (name) {
        "Gear" -> GearMoveFactory()
        "Carousel" -> CarouselMoveFactory()
        "Wide" -> WideMoveFactory(rowDepth!!, colDepth!!)
        "Enabler" -> EnablerMoveFactory()
        "Dynamic Bandaging" -> DynamicBandagingMoveFactory()
        "Static Cells" -> StaticCellsMoveFactory(rowDepth!!, colDepth!!)
        else -> BasicMoveFactory()
    }
}

fun randomMove(
    board: GameBoard,
    factory: MoveFactory,
    num_rows: Int,
    num_cols: Int
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

// TODO(jmerm): make scramble logic work in S, D, E modes
fun scramble(
    solved: Array<String>, factory: MoveFactory, num_rows: Int, num_cols: Int, context: Context
): Array<String> {
    val gameBoard = GameBoard(num_rows, num_cols, solved, context)
    for (i in (0..1000)) {
        val move = randomMove(gameBoard, factory, num_rows, num_cols)
        move.finalize(gameBoard)
    }
    return gameBoard.toString().split(",").toTypedArray()
}

// TODO(jmerm): needing to take the context here is silly, fix that.
fun generateRandomLevel(options: RandomLevelParams, context: Context): GameplayParams {
    // make factory
    val factory: MoveFactory =
        if (options.rowFactory == options.colFactory || options.colFactory == null) {
            fromRandomFactory(options.rowFactory, options.rowDepth, options.colDepth)
        } else {
            CombinedMoveFactory(
                fromRandomFactory(options.rowFactory, options.rowDepth, options.colDepth),
                fromRandomFactory(options.colFactory!!, options.rowDepth, options.colDepth)
            )
        }

    // TODO(jmerm): add in E, and F cells as necessary
    val goal = (0..23).filter { (it < options.numRows * 4) && (it % 4 < options.numCols) }
        .map { i -> (i + 1).toString() }.toTypedArray()
    // TODO(jmerm): handle case where start == goal?
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