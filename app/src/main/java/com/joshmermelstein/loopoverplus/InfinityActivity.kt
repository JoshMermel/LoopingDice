package com.joshmermelstein.loopoverplus

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

// TODO(jmerm): spinner for how many locked cells in Static mode?
// TODO(jmerm): should there be a way to resume your last infinity level?

@Parcelize
class RandomLevelParams(
    val numRows: Int,
    val numCols: Int,
    val colorScheme: String,
    val rowMode: String,
    val colMode: String?,
    val rowDepth: Int?,
    val colDepth: Int?,
    val numBandaged: String?,
    val numEnablers: String?,
    val numArrows: String?,
    val numBlocks: String?
) : Parcelable

// Activity for letting the user build a level
class InfinityActivity : AppCompatActivity() {
    private val rowSizes = (2..6).map { num -> num.toString() }

    private val rowModes =
        arrayOf(
            "Wide",
            "Carousel",
            "Gear",
            "Dynamic Bandaging",
            "Static Cells",
            "Enabler",
            "Arrows",
            "Bandaged"
        )
    private val colModes = arrayOf("Wide", "Carousel", "Gear")
    private val colorSchemes = arrayOf("Bicolor", "Columns", "Unique")
    private val densities = arrayOf("Rare", "Common", "Frequent")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinity)
        configureRowSizePicker()
        configureRowModePicker()
        configureColorSchemePicker()
        configureButton()
    }

    private fun configureRowSizePicker() {
        val rowSizeSpinner = findViewById<Spinner>(R.id.rowSizeSpinner)
        val rowAdapter = ArrayAdapter(this, R.layout.spinner_item, rowSizes)
        rowAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rowSizeSpinner.adapter = rowAdapter
        rowSizeSpinner.onItemSelectedListener = SelectionMadeListener(::onUpdateNumRows)

        rowSizeSpinner.setSelection(Random.nextInt(1, 3))
    }

    private fun configureRowModePicker() {
        val rowModeSpinner = findViewById<Spinner>(R.id.rowModeSpinner)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, rowModes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rowModeSpinner.adapter = adapter
        rowModeSpinner.onItemSelectedListener = SelectionMadeListener(::onUpdateRowMode)
        rowModeSpinner.setSelection(Random.nextInt(0, rowModes.size + 1) % rowModes.size)
    }

    private fun configureColorSchemePicker() {
        val colorSchemeSpinner = findViewById<Spinner>(R.id.colorSchemeSpinner)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, colorSchemes)

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        colorSchemeSpinner.adapter = adapter
        colorSchemeSpinner.setSelection(Random.nextInt(0, colorSchemes.size))
    }

    private fun getNumRows(): Int {
        return findViewById<Spinner>(R.id.rowSizeSpinner).selectedItem.toString().toInt()
    }

    private fun getNumCols(): Int {
        return findViewById<Spinner>(R.id.colSizeSpinner).selectedItem.toString().toInt()
    }

    private fun getColorScheme(): String {
        return findViewById<Spinner>(R.id.colorSchemeSpinner).selectedItem.toString()
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

    private fun getNumBandaged(): String? {
        return findViewById<Spinner>(R.id.numBandagedSpinner).selectedItem?.toString()
    }

    private fun getNumEnablers(): String? {
        return findViewById<Spinner>(R.id.numEnablersSpinner).selectedItem?.toString()
    }

    private fun getNumArrows(): String? {
        return findViewById<Spinner>(R.id.numArrowsSpinner).selectedItem?.toString()
    }

    private fun getNumBlocks(): String? {
        return findViewById<Spinner>(R.id.numBlocksSpinner).selectedItem?.toString()
    }

    private fun updateColSizePicker() {
        val colSizeSpinner = findViewById<Spinner>(R.id.colSizeSpinner)
        val oldValue: Int? = colSizeSpinner.selectedItem?.toString()?.toInt()
        val rowMode = getRowMode()
        val maxValue = if (rowMode in colModes || rowMode == "Arrows") {
            6
        } else {
            5
        }
        val colSizes = (2..maxValue).map { num -> num.toString() }


        val adapter = ArrayAdapter(this, R.layout.spinner_item, colSizes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        colSizeSpinner.adapter = adapter
        colSizeSpinner.onItemSelectedListener = SelectionMadeListener(::onUpdateNumCols)

        when {
            oldValue == null -> {
                // initialize to random
                colSizeSpinner.setSelection(Random.nextInt(1, 3))
            }
            oldValue <= maxValue -> {
                // reset old value
                colSizeSpinner.setSelection(oldValue - 2)
            }
            else -> {
                // old value was too big, replace with largest value that fits
                colSizeSpinner.setSelection(maxValue - 2)
            }
        }
    }


    private fun updateColModePicker() {
        val container = findViewById<View>(R.id.col_mode_container)
        if (colModes.contains(getRowMode())) {
            if (container?.visibility == View.VISIBLE) {
                // avoid churn if spinner was already visible
                return
            }
            container.visibility = View.VISIBLE
            val colModeSpinner = findViewById<Spinner>(R.id.colModeSpinner)
            val colAdapter = ArrayAdapter(this, R.layout.spinner_item, colModes)
            colAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            colModeSpinner.adapter = colAdapter
            colModeSpinner.onItemSelectedListener = SelectionMadeListener(::onUpdateColMode)
            colModeSpinner.setSelection(findViewById<Spinner>(R.id.rowModeSpinner).selectedItemPosition)
        } else if (container?.visibility == View.VISIBLE) {
            container.visibility = View.GONE
            findViewById<Spinner>(R.id.colModeSpinner).adapter = null
        }
    }

    private fun updateRowDepthPicker() {
        val container = findViewById<View>(R.id.row_depth_container)
        val mode = getRowMode()
        if (mode == "Wide" || mode == "Static Cells") {
            container.visibility = View.VISIBLE
            val rowDepthSpinner = findViewById<Spinner>(R.id.rowDepthSpinner)

            val oldDepth = getRowDepth()
            val maxDepth = if (mode == "Wide") {
                getNumRows()
            } else {
                getNumRows() - 1
            }

            val options = (1..maxDepth).map { num -> num.toString() }
            val adapter = ArrayAdapter(this, R.layout.spinner_item, options)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            rowDepthSpinner.adapter = adapter

            // set spinner to old value if it still fits

            if (oldDepth != null) {
                if (oldDepth <= maxDepth) {
                    rowDepthSpinner.setSelection(oldDepth - 1)
                } else {
                    rowDepthSpinner.setSelection(maxDepth - 1)
                }
            }
        } else {
            container.visibility = View.GONE
        }
    }

    private fun updateColDepthPicker() {
        val container = findViewById<View>(R.id.col_depth_container)
        val colMode = getColMode()
        val rowMode = getRowMode()
        if (colMode == "Wide" || rowMode == "Static Cells") {
            container.visibility = View.VISIBLE
            val colDepthSpinner = findViewById<Spinner>(R.id.colDepthSpinner)

            val oldDepth = getColDepth()
            val maxDepth = if (rowMode == "Wide") {
                getNumCols()
            } else {
                getNumCols() - 1
            }

            val options = (1..maxDepth).map { num -> num.toString() }
            val adapter = ArrayAdapter(this, R.layout.spinner_item, options)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            colDepthSpinner.adapter = adapter

            // set spinner to old value if it still fits
            if (oldDepth != null) {
                if (oldDepth <= maxDepth) {
                    colDepthSpinner.setSelection(oldDepth - 1)
                } else {
                    colDepthSpinner.setSelection(maxDepth - 1)
                }
            }
        } else {
            container?.visibility = View.GONE
        }
    }

    private fun updateNumBandagedPicker() {
        val container = findViewById<View>(R.id.num_bandaged_container)
        if (getRowMode() == "Dynamic Bandaging") {
            container?.visibility = View.VISIBLE
            val numBandagedSpinner = findViewById<Spinner>(R.id.numBandagedSpinner)
            val adapter = ArrayAdapter(this, R.layout.spinner_item, densities)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            numBandagedSpinner.adapter = adapter
        } else {
            container?.visibility = View.GONE
        }
    }

    private fun updateNumEnablersPicker() {
        val container = findViewById<View>(R.id.num_enablers_container)
        if (getRowMode() == "Enabler") {
            container?.visibility = View.VISIBLE
            val numEnablersSpinner = findViewById<Spinner>(R.id.numEnablersSpinner)
            val adapter = ArrayAdapter(this, R.layout.spinner_item, densities)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            numEnablersSpinner.adapter = adapter
        } else {
            container?.visibility = View.GONE
        }
    }

    private fun updateNumArrowsPicker() {
        val container = findViewById<View>(R.id.num_arrows_container)
        if (getRowMode() == "Arrows") {
            container?.visibility = View.VISIBLE
            val numArrowsSpinner = findViewById<Spinner>(R.id.numArrowsSpinner)
            val adapter = ArrayAdapter(this, R.layout.spinner_item, densities)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            numArrowsSpinner.adapter = adapter
        } else {
            container?.visibility = View.GONE
        }
    }

    // TODO(jmerm): this method is very similar to the one above, maybe unify?
    private fun updateBlocksPicker() {
        val container = findViewById<View>(R.id.num_blocks_container)
        if (getRowMode() == "Bandaged") {
            container?.visibility = View.VISIBLE
            val numBlocksSpinner = findViewById<Spinner>(R.id.numBlocksSpinner)
            val adapter = ArrayAdapter(this, R.layout.spinner_item, densities)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            numBlocksSpinner.adapter = adapter
        } else {
            container?.visibility = View.GONE
        }
    }

    private fun onUpdateRowMode() {
        findViewById<TextView>(R.id.row_mode).apply {
            text = if (getRowMode() in colModes) "Row Mode" else "Mode"
        }

        updateColSizePicker()
        updateColModePicker()
        updateRowDepthPicker()
        updateColDepthPicker()
        updateNumBandagedPicker()
        updateNumEnablersPicker()
        updateNumArrowsPicker()
        updateBlocksPicker()
    }

    private fun onUpdateColMode() {
        updateColDepthPicker()
    }

    private fun onUpdateNumCols() {
        updateColDepthPicker()
    }

    private fun onUpdateNumRows() {
        updateRowDepthPicker()
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
            getColorScheme(),
            getRowMode(),
            getColMode(),
            getRowDepth(),
            getColDepth(),
            getNumBandaged(),
            getNumEnablers(),
            getNumArrows(),
            getNumBlocks(),
        )
    }
}

// Helper class for calling a void callback when the user picks an option.
class SelectionMadeListener(val action: () -> Unit) : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
        action()
}