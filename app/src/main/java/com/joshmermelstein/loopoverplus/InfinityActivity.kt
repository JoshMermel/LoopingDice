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
data class RandomLevelParams(
    val numRows: Int,
    val numCols: Int,
    val colorScheme: ColorScheme,
    val rowMode: Mode,
    val colMode: Mode?,
    val rowDepth: Int?,
    val colDepth: Int?,
    val density: Density?,
    val blockedRows: Int?,
    val blockedCols: Int?,
) : Parcelable {
    override fun toString(): String {
        return "$numRows,$numCols,$colorScheme,$rowMode,$colMode,$rowDepth,$colDepth,$density,$blockedRows,$blockedCols"
    }
}

enum class Density(val userString: Int) {
    RARE(R.string.infinityDensityRare),
    COMMON(R.string.infinityDensityCommon),
    FREQUENT(R.string.infinityDensityFrequent), ;
}

enum class ColorScheme(val userString: Int) {
    BICOLOR(R.string.infinityColorSchemeBicolor),
    SPECKLED(R.string.infinityColorSchemeSpeckled),
    COLUMNS(R.string.infinityColorSchemeColumns),
    UNIQUE(R.string.infinityColorSchemeUnique),
}

enum class Mode(val userString: Int) {
    WIDE(R.string.infinityModeWide),
    CAROUSEL(R.string.infinityModeCarousel),
    GEAR(R.string.infinityModeGear),
    DYNAMIC(R.string.infinityModeDynamic),
    BANDAGED(R.string.infinityModeBandaged),
    LIGHTNING(R.string.infinityModeLightning),
    ARROWS(R.string.infinityModeArrows),
    ENABLER(R.string.infinityModeEnabler),
    STATIC(R.string.infinityModeStatic),
}

// Activity for letting the user build a level
class InfinityActivity : AppCompatActivity() {
    // Arrays to go into spinner adaptors.
    private val rowSizes = (2..6).map { num -> num.toString() }
    private val rowModes = Mode.values()
    private val colModes = arrayOf(Mode.WIDE, Mode.CAROUSEL, Mode.GEAR)
    private val colorSchemes = ColorScheme.values()
    private val densities = Density.values()

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
        val adapter =
            ArrayAdapter(this, R.layout.spinner_item, rowModes.map { getString(it.userString) })
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rowModeSpinner.adapter = adapter
        rowModeSpinner.onItemSelectedListener = SelectionMadeListener(::onUpdateRowMode)
        rowModeSpinner.setSelection(Random.nextInt(0, rowModes.size + 1) % rowModes.size)
    }

    // Configures the color scheme spinner. This is always shown.
    private fun configureColorSchemePicker() {
        val colorSchemeSpinner = findViewById<Spinner>(R.id.colorSchemeSpinner)
        val adapter =
            ArrayAdapter(this, R.layout.spinner_item, colorSchemes.map { getString(it.userString) })

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

    private fun getColorScheme(): ColorScheme {
        return colorSchemes[findViewById<Spinner>(R.id.colorSchemeSpinner).selectedItemPosition]
    }

    private fun getRowMode(): Mode {
        return rowModes[findViewById<Spinner>(R.id.rowModeSpinner).selectedItemPosition]
    }

    private fun getColMode(): Mode? {
        return colModes.getOrNull(findViewById<Spinner>(R.id.colModeSpinner).selectedItemPosition)
    }

    // The following getters return null when the container isn't shown because spinner value is
    // used as part of the ID for saving/restoring
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

    private fun getDensity(): Density? {
        if (findViewById<View>(R.id.density_container).visibility == View.VISIBLE) {
            return densities[findViewById<Spinner>(R.id.densitySpinner).selectedItemPosition]
        }
        return null
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
        val maxValue =
            if (rowMode in colModes || rowMode == Mode.ARROWS || rowMode == Mode.LIGHTNING) {
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
            val colAdapter =
                ArrayAdapter(this, R.layout.spinner_item, colModes.map { getString(it.userString) })
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
        if (mode == Mode.WIDE || mode == Mode.STATIC) {
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
        if (colMode == Mode.WIDE || rowMode == Mode.STATIC) {
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
        if (rowMode in listOf(
                Mode.DYNAMIC,
                Mode.ENABLER,
                Mode.ARROWS,
                Mode.BANDAGED,
                Mode.LIGHTNING
            )
        ) {
            container?.visibility = View.VISIBLE
            val numBandagedSpinner = findViewById<Spinner>(R.id.densitySpinner)
            val adapter = ArrayAdapter(
                this,
                R.layout.spinner_item,
                densities.map { getString(it.userString) })
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            numBandagedSpinner.adapter = adapter
            findViewById<TextView>(R.id.densityLabel)?.text = when (rowMode) {
                Mode.DYNAMIC -> getString(R.string.infinityNumBandaged)
                Mode.ENABLER -> getString(R.string.infinityNumEnablers)
                Mode.ARROWS -> getString(R.string.infinityNumArrows)
                Mode.BANDAGED -> getString(R.string.infinityNumBlocks)
                Mode.LIGHTNING -> getString(R.string.infinityNumBolts)
                else -> getString(R.string.infinityDensity)  // should never happen.
            }
        } else {
            container?.visibility = View.GONE
        }
    }

    private fun updateNumBlockedRowsPicker() {
        val container = findViewById<View>(R.id.blocked_rows_container)
        if (getRowMode() == Mode.STATIC) {
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
        if (getRowMode() == Mode.STATIC) {
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