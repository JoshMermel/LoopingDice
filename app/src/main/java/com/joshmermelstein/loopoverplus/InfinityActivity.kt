package com.joshmermelstein.loopoverplus

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize
import java.io.File
import kotlin.random.Random

/*
 * This activity lets the user pick params for a level and then randomly generates a level to fit
 * those params. The user picks their options using a bunch of Spinner views. The set of spinners
 * that are shown may depend on the values of other spinners, for example, it only makes sense to
 * let the user configure "row depth" when row mode is wide or static. This is implemented by giving
 * each spinner an onUpdate callback which updates the visibility/possible values of other spinners.
 * Luckily, it is possible to do so without any cyclic updates.
 */


@Parcelize
class RandomLevelParams(
    val numRows: Int,
    val numCols: Int,
    val colorScheme: String,
    val rowMode: String,
    val colMode: String?,
    val rowDepth: Int?,
    val colDepth: Int?,
    val density: String?,
    val blockedRows: Int?,
    val blockedCols: Int?,
) : Parcelable {
    override fun toString(): String {
        return "$numRows,$numCols,$colorScheme,$rowMode,$colMode,$rowDepth,$colDepth,$density,$blockedRows,$blockedCols"
    }
}

// Activity for letting the user build a level
class InfinityActivity : AppCompatActivity() {
    private val rowSizes = (2..6).map { num -> num.toString() }

    private val rowModes =
        arrayOf(
            "Wide",
            "Carousel",
            "Gear",
            "Dynamic Bandaging",
            "Bandaged",
            "Lightning",
            "Arrows",
            "Enabler",
            "Static Cells",
        )
    private val colModes = arrayOf("Wide", "Carousel", "Gear")

    // TODO(jmerm): could either of these be made into Enums for safety.
    private val colorSchemes = arrayOf("Bicolor", "Speckled", "Columns", "Unique")
    private val densities = arrayOf("Rare", "Common", "Frequent")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinity)
        configureRowSizePicker()
        configureRowModePicker()
        configureColorSchemePicker()
        configureButton()
    }

    // Configures the row size spinner. This is always shown.
    private fun configureRowSizePicker() {
        val rowSizeSpinner = findViewById<Spinner>(R.id.rowSizeSpinner)
        val rowAdapter = ArrayAdapter(this, R.layout.spinner_item, rowSizes)
        rowAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rowSizeSpinner.adapter = rowAdapter
        rowSizeSpinner.onItemSelectedListener = SelectionMadeListener(::onUpdateNumRows)

        rowSizeSpinner.setSelection(Random.nextInt(1, 3))
    }

    // Configures the row mode spinner. This is always shown.
    private fun configureRowModePicker() {
        val rowModeSpinner = findViewById<Spinner>(R.id.rowModeSpinner)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, rowModes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rowModeSpinner.adapter = adapter
        rowModeSpinner.onItemSelectedListener = SelectionMadeListener(::onUpdateRowMode)
        rowModeSpinner.setSelection(Random.nextInt(0, rowModes.size + 1) % rowModes.size)
    }

    // Configures the color scheme spinner. This is always shown.
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

    // It's important to null these two out when they aren't used since they are part of the ID used for saving/restoring.
    private fun getRowDepth(): Int? {
        if (findViewById<View>(R.id.row_depth_container).visibility == View.VISIBLE) {
            return findViewById<Spinner>(R.id.rowDepthSpinner).selectedItem?.toString()?.toInt()
        }
        return null
    }

    private fun getColDepth(): Int? {
        if (findViewById<View>(R.id.col_depth_container).visibility == View.VISIBLE) {
            return findViewById<Spinner>(R.id.colDepthSpinner).selectedItem?.toString()?.toInt()
        }
        return null
    }

    private fun getDensity(): String? {
        return findViewById<Spinner>(R.id.densitySpinner).selectedItem?.toString()
    }

    private fun getNumBlockedRows(): Int? {
        if (findViewById<View>(R.id.blocked_rows_container).visibility == View.VISIBLE) {
            return findViewById<Spinner>(R.id.blockedRowsSpinner).selectedItem?.toString()?.toInt()
        }
        return null
    }

    private fun getNumBlockedCols(): Int? {
        if (findViewById<View>(R.id.blocked_cols_container).visibility == View.VISIBLE) {
            return findViewById<Spinner>(R.id.blockedColsSpinner).selectedItem?.toString()?.toInt()
        }
        return null
    }

    // The available col sizes depends on the user's mode. In modes where a color has a special
    // meaning, the number of columns is limited to 5 so no normal game cells of that special color
    // will be used. Arguably this could be increased in the "bicolor" color scheme but I think that
    // would be too confusing a UI.
    private fun updateColSizePicker() {
        val colSizeSpinner = findViewById<Spinner>(R.id.colSizeSpinner)
        val oldValue: Int? = colSizeSpinner.selectedItem?.toString()?.toInt()
        val rowMode = getRowMode()
        val maxValue = if (rowMode in colModes || rowMode == "Arrows" || rowMode == "Lightning") {
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
                // Initialize to random
                colSizeSpinner.setSelection(Random.nextInt(1, 3))
            }
            oldValue <= maxValue -> {
                // Reset old value
                colSizeSpinner.setSelection(oldValue - 2)
            }
            else -> {
                // Old value was too big, replace with largest value that fits
                colSizeSpinner.setSelection(maxValue - 2)
            }
        }
    }

    private fun updateColModePicker() {
        val container = findViewById<View>(R.id.col_mode_container)
        if (colModes.contains(getRowMode())) {
            if (container?.visibility == View.VISIBLE) {
                // Avoid churn if spinner was already visible.
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
            val maxDepth = getNumRows()

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
            val maxDepth = getNumCols()

            val options = (1..maxDepth).map { it.toString() }
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

    // Shows/hides a density picker (i.e. how many bonds, how many enablers).
    private fun updateDensityPicker() {
        val container = findViewById<View>(R.id.density_container)
        val rowMode = getRowMode()
        if (rowMode in listOf("Dynamic Bandaging", "Enabler", "Arrows", "Bandaged", "Lightning")) {
            container?.visibility = View.VISIBLE
            val numBandagedSpinner = findViewById<Spinner>(R.id.densitySpinner)
            val adapter = ArrayAdapter(this, R.layout.spinner_item, densities)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            numBandagedSpinner.adapter = adapter
            findViewById<TextView>(R.id.densityLabel)?.text = when (rowMode) {
                "Dynamic Bandaging" -> getString(R.string.infinityNumBandaged)
                "Enabler" -> getString(R.string.infinityNumEnablers)
                "Arrows" -> getString(R.string.infinityNumArrows)
                "Bandaged" -> getString(R.string.infinityNumBlocks)
                "Lightning" -> getString(R.string.infinityNumBolts)
                else -> getString(R.string.infinityDensity)  // should never happen.
            }
        } else {
            container?.visibility = View.GONE
        }
    }

    private fun updateNumBlockedRowsPicker() {
        val container = findViewById<View>(R.id.blocked_rows_container)
        if (getRowMode() == "Static Cells") {
            container?.visibility = View.VISIBLE
            val spinner = findViewById<Spinner>(R.id.blockedRowsSpinner)

            val options = (1..getNumRows()).map { it.toString() }
            val adapter = ArrayAdapter(this, R.layout.spinner_item, options)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            spinner.adapter = adapter
        } else {
            container?.visibility = View.GONE
        }
    }

    private fun updateNumBlockedColsPicker() {
        val container = findViewById<View>(R.id.blocked_cols_container)
        if (getRowMode() == "Static Cells") {
            container?.visibility = View.VISIBLE
            val spinner = findViewById<Spinner>(R.id.blockedColsSpinner)

            val options = (1..getNumCols()).map { it.toString() }
            val adapter = ArrayAdapter(this, R.layout.spinner_item, options)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            spinner.adapter = adapter
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
        updateDensityPicker()
        updateNumBlockedRowsPicker()
        updateNumBlockedColsPicker()
    }

    // Callback for when col mode is changed.
    private fun onUpdateColMode() {
        updateColDepthPicker()
    }

    // Callback for when num cols is changed.
    private fun onUpdateNumCols() {
        updateColDepthPicker()
    }

    // Callback for when num rows is changed.
    private fun onUpdateNumRows() {
        updateRowDepthPicker()
    }

    // Configured the "generate level" button to launch a gameplay activity based on the values of spinners.
    private fun configureButton() {
        val button = findViewById<Button>(R.id.generate_level)
        button?.setOnClickListener {
            if (saveFileExists()) {
                // If there is a save file for these params, ask the user if they want to load it.
                loadSaveDialog()
            } else {
                startGame(false)
            }
        }
    }

    private fun saveFileExists(): Boolean {
        return File(filesDir, "∞${build()}.txt").exists()
    }

    private fun loadSaveDialog() {
        AlertDialog.Builder(this)
            .setTitle("Load Save?")
            .setMessage("You've previously played an ∞ level with these params. Would you like to resume it?")
            .setPositiveButton("Yes (Resume old level)") { _, _ -> startGame(true) }
            .setNegativeButton("No (Create new level)") { _, _ -> startGame(false) }
            .show()
    }

    private fun startGame(loadSave: Boolean) {
        val params = build()
        if (!loadSave) {
            getSharedPreferences("highscores", Context.MODE_PRIVATE).edit().remove("∞$params")
                .apply()
        }
        val intent = Intent(this, GameplayActivity::class.java)
        intent.putExtra("randomLevelParams", params)
        intent.putExtra("loadSave", loadSave)
        startActivity(intent)
    }

    // Collects the values of all spinners into a struct for easy serialization.
    private fun build(): RandomLevelParams {
        return RandomLevelParams(
            getNumRows(),
            getNumCols(),
            getColorScheme(),
            getRowMode(),
            getColMode(),
            getRowDepth(),
            getColDepth(),
            getDensity(),
            getNumBlockedRows(),
            getNumBlockedCols()
        )
    }
}

// Helper class for calling a void callback when the user picks an option.
class SelectionMadeListener(val action: () -> Unit) : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
        action()
}