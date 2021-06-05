package com.joshmermelstein.loopoverplus

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

// TODO(jmerm): add spinner for num enablers, num_bandaged, etc

@Parcelize
class RandomLevelParams(
    val numRows: Int,
    val numCols: Int,
    val colorScheme: String,
    val rowMode: String,
    val colMode: String?,
    val rowDepth: Int?,
    val colDepth: Int?,
    val numBandaged : String?
) : Parcelable

// Activity for letting the user build a level
class InfinityActivity : AppCompatActivity() {
    private val rowSizes = (2..6).map { num -> num.toString() }
    private val colSizes = (2..5).map { num -> num.toString() }
    private val rowModes =
        arrayOf("Wide", "Carousel", "Gear", "Dynamic Bandaging", "Static Cells", "Enabler")
    private val colModes = arrayOf("Wide", "Carousel", "Gear")
    private val colorSchemes = arrayOf("Bicolor", "Columns", "Unique")
    private val numBandagedOptions = arrayOf("Rare", "Common", "Frequent")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinity)
        configureSizePickers()
        configureRowModePicker()
        configureColorSchemePicker()
        configureButton()
    }

    // TODO(jmerm): consider allowing a 6th column when the colorscheme is bicolor or if the
    //  row_mode is in {wide, carousel, gear}
    private fun configureSizePickers() {
        val rowSizeSpinner = findViewById<Spinner>(R.id.rowSizeSpinner)
        val rowAdapter = ArrayAdapter(this, R.layout.spinner_item, rowSizes)
        rowAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rowSizeSpinner.adapter = rowAdapter
        rowSizeSpinner.onItemSelectedListener = SelectionMadeListener(::onUpdateNumRows)

        rowSizeSpinner.setSelection(Random.nextInt(1, 3))

        val colSizeSpinner = findViewById<Spinner>(R.id.colSizeSpinner)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, colSizes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        colSizeSpinner.adapter = adapter
        colSizeSpinner.onItemSelectedListener = SelectionMadeListener(::onUpdateNumCols)
        colSizeSpinner.setSelection(Random.nextInt(1, 3))

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
        // TODO(jmerm): randomize color scheme
        // colorSchemeSpinner.setSelection(Random.nextInt(0, rowModes.size + 1) % rowModes.size)
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

    private fun GetNumBandaged(): String? {
        return findViewById<Spinner>(R.id.numBandagedSpinner).selectedItem?.toString()
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
            colModeSpinner.onItemSelectedListener = SelectionMadeListener(::onUpdateColMode)
            colModeSpinner.setSelection(Random.nextInt(0, colModes.size + 1) % colModes.size)
        } else if (container?.visibility == View.VISIBLE) {
            container.visibility = View.GONE
            findViewById<Spinner>(R.id.colModeSpinner).adapter = null
        }
    }

    private fun updateRowDepthPicker() {
        val container = findViewById<View>(R.id.row_depth_container)
        if (getRowMode() == "Wide" || getRowMode() == "Static Cells") {
            val oldDepth = getRowDepth()
            container.visibility = View.VISIBLE
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
            container.visibility = View.GONE
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

    private fun updateNumBandagedPicker() {
        val container = findViewById<View>(R.id.num_bandaged_container)
        if (getRowMode() == "Dynamic Bandaging") {
            container?.visibility = View.VISIBLE
            val numBandagedSpinner = findViewById<Spinner>(R.id.numBandagedSpinner)
            val adapter = ArrayAdapter(this, R.layout.spinner_item, numBandagedOptions)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            numBandagedSpinner.adapter = adapter
        } else {
            container?.visibility = View.GONE
        }
    }


    private fun onUpdateRowMode() {
        updateColModePicker()
        updateRowDepthPicker()
        updateColDepthPicker()
        updateNumBandagedPicker()
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
            GetNumBandaged()
        )
    }
}

// Helper class for calling a void callback when the user picks an option.
class SelectionMadeListener(val action: () -> Unit) : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
        action()
}