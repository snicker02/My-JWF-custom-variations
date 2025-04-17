package org.jwildfire.create.tina.variation;

import java.util.Random;
// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.Layer; // Needed for init
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Block Remap (Quantized).
 * Divides space into grid cells. For each cell, calculates a consistent
 * random offset that is a MULTIPLE OF CELL SIZE, and remaps input
 * coordinates by that offset. Aims to simulate displaced rectangular blocks
 * with less arbitrary overlap.
 * Follows user-specified code structure.
 */
public class BlockRemapGlitch extends VariationFunc {

    // Incremented serialVersionUID
    private static final long serialVersionUID = 202L;

    // Parameter names
    private static final String PARAM_INTENSITY = "intensity"; // Controls MAX offset in # of CELLS
    private static final String PARAM_CHANCE = "chance";     // Probability (0-1) of remapping a point
    private static final String PARAM_FREQUENCY = "frequency"; // Controls grid cell height (lower=taller)
    private static final String PARAM_FREQ_X = "freqX";      // Controls grid cell width (lower=wider)
    private static final String PARAM_CENTER_X = "centerX";    // Grid center X
    private static final String PARAM_CENTER_Y = "centerY";    // Grid center Y

    private static final String[] paramNames = {
        PARAM_INTENSITY, PARAM_CHANCE, PARAM_FREQUENCY, PARAM_FREQ_X, PARAM_CENTER_X, PARAM_CENTER_Y
    };

    // Parameter variables
    // Intensity now controls max cell offset range (e.g., intensity 1.0 might allow +/- 5 cells offset)
    private double intensity = 1.0;
    private double chance = 0.9;
    private double frequency = 8.0;
    private double freqX = 8.0;
    private double centerX = 0.0;
    private double centerY = 0.0;

    // Random generator for cell consistency
    private transient Random cellRng = new Random();
    private transient long currentSeed = 0;

    // init method without @Override
    public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
        currentSeed = System.nanoTime() + Thread.currentThread().getId();
        cellRng = new Random(currentSeed);
    }

    // Helper for cell consistency
    private double getCellRandomDouble(int cellIdX, int cellIdY, int offset) {
       long seed = currentSeed + (cellIdX * 314159) + (cellIdY * 271828) + offset;
       cellRng.setSeed(seed);
       return cellRng.nextDouble(); // Return value between 0.0 and 1.0
    }

    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        // Overall chance to apply the effect
        if (pContext.random() < this.chance) {

            // Determine grid cell indices relative to center
            double cellFreqY = (this.frequency == 0) ? 1.0 : this.frequency;
            double cellFreqX = (this.freqX == 0) ? 1.0 : this.freqX;
            int cellIndexY = (int) Math.floor((y - this.centerY) * cellFreqY);
            int cellIndexX = (int) Math.floor((x - this.centerX) * cellFreqX);

            // Calculate cell size (handle potential zero frequency)
            double cellSizeY = (Math.abs(cellFreqY) < 1e-9) ? 1.0 : 1.0 / cellFreqY;
            double cellSizeX = (Math.abs(cellFreqX) < 1e-9) ? 1.0 : 1.0 / cellFreqX;


            // Get consistent random values (-1 to +1) for determining offset direction
            double randOffsetX = (getCellRandomDouble(cellIndexX, cellIndexY, 0) - 0.5) * 2.0;
            double randOffsetY = (getCellRandomDouble(cellIndexX, cellIndexY, 101) - 0.5) * 2.0;

            // Determine max offset in terms of number of cells based on intensity
            // (e.g., intensity 1.0 = max 5 cells away, intensity 2.0 = max 10 cells away)
            // This scaling factor (5.0) can be adjusted
            double maxCellOffset = Math.max(0.0, this.intensity * 5.0);

            // Calculate the offset in number of cells (quantized)
            int numCellsOffsetX = (int)Math.round(randOffsetX * maxCellOffset);
            int numCellsOffsetY = (int)Math.round(randOffsetY * maxCellOffset);

            // Calculate the final coordinate offset (multiple of cell size)
            double finalOffsetX = numCellsOffsetX * cellSizeX;
            double finalOffsetY = numCellsOffsetY * cellSizeY;

            // Apply the quantized offset to the original coordinates to find the source
            glitchX = x + finalOffsetX;
            glitchY = y + finalOffsetY;

        }

        // Apply the final transformation, scaled by the variation amount slider in JWF
        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;

        // Color preservation logic removed as per user code
    }

    // getName method without @Override
    public String getName() {
        // Updated name slightly
        return "glitch_blockRemapQ"; // Q for Quantized
    }

    // getParameterNames method without @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    // getParameterValues method without @Override
    public Object[] getParameterValues() {
        return new Object[] { intensity, chance, frequency, freqX, centerX, centerY };
    }

    // setParameter method without @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_INTENSITY.equalsIgnoreCase(pName)) { intensity = pValue; }
        else if (PARAM_CHANCE.equalsIgnoreCase(pName)) { chance = Math.max(0.0, Math.min(1.0, pValue)); }
        else if (PARAM_FREQUENCY.equalsIgnoreCase(pName)) { frequency = (pValue == 0) ? 0.001 : pValue; }
        else if (PARAM_FREQ_X.equalsIgnoreCase(pName)) { freqX = (pValue == 0) ? 0.001 : pValue; }
        else if (PARAM_CENTER_X.equalsIgnoreCase(pName)) { centerX = pValue; }
        else if (PARAM_CENTER_Y.equalsIgnoreCase(pName)) { centerY = pValue; }
        else {
            throw new IllegalArgumentException("Parameter '" + pName + "' not supported by " + getName());
        }
    }

    // getParameter method removed as per user code
    // getVariationTypes method removed as per user code
}