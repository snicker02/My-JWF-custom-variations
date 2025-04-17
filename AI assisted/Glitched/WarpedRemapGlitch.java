package org.jwildfire.create.tina.variation;

import java.util.Random;
// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.Layer; // Needed for init
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Warped Remap with Fragmentation.
 * Applies a smooth warp, determines a grid cell from warped coords, calculates
 * a quantized block offset for that cell, BUT only applies the offset to a
 * random fraction of points within the cell (controlled by 'fragmentation').
 * Aims for fragmented geometric effects without solid blocks/stripes.
 * Follows user-specified code structure.
 */
public class WarpedRemapGlitch extends VariationFunc {

    // Incremented serialVersionUID
    private static final long serialVersionUID = 502L;

    // --- Parameter Names ---
    // Warp Controls
    private static final String PARAM_WARP_INTENSITY = "warpIntensity";
    private static final String PARAM_WARP_FREQ_X = "warpFreqX";
    private static final String PARAM_WARP_FREQ_Y = "warpFreqY";
    // Remap Controls
    private static final String PARAM_REMAP_INTENSITY = "remapIntensity";
    private static final String PARAM_REMAP_CHANCE = "remapChance";
    private static final String PARAM_CELL_FREQ_Y = "cellFreqY";
    private static final String PARAM_CELL_FREQ_X = "cellFreqX";
    private static final String PARAM_CENTER_X = "centerX";
    private static final String PARAM_CENTER_Y = "centerY";
    // Fragmentation Control - NEW
    private static final String PARAM_FRAGMENTATION = "fragmentation";


    private static final String[] paramNames = {
        PARAM_WARP_INTENSITY, PARAM_WARP_FREQ_X, PARAM_WARP_FREQ_Y,
        PARAM_REMAP_INTENSITY, PARAM_REMAP_CHANCE,
        PARAM_CELL_FREQ_Y, PARAM_CELL_FREQ_X,
        PARAM_CENTER_X, PARAM_CENTER_Y,
        PARAM_FRAGMENTATION // Added new param
    };

    // --- Parameter Variables ---
    private double warpIntensity = 0.1;
    private double warpFreqX = 5.0;
    private double warpFreqY = 5.0;
    private double remapIntensity = 1.0;
    private double remapChance = 0.9;
    private double cellFreqY = 8.0;
    private double cellFreqX = 8.0;
    private double centerX = 0.0;
    private double centerY = 0.0;
    private double fragmentation = 0.0; // Default to solid blocks

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
       return cellRng.nextDouble();
    }

    // Helper function for true mathematical modulo
    private double mod(double a, double n) {
        if (Math.abs(n) < 1e-9) return a;
        return ((a % n) + n) % n;
    }


    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        // Overall chance to apply the remapping process for this point's region
        if (pContext.random() < this.remapChance) {

            // 1. Warp Input Coordinates
            double dx_warp = x - this.centerX;
            double dy_warp = y - this.centerY;
            double wFreqX = (this.warpFreqX == 0) ? 1.0 : this.warpFreqX;
            double wFreqY = (this.warpFreqY == 0) ? 1.0 : this.warpFreqY;
            double warpX = dx_warp + this.warpIntensity * Math.sin(dy_warp * wFreqY);
            double warpY = dy_warp + this.warpIntensity * Math.cos(dx_warp * wFreqX);
            warpX += this.centerX;
            warpY += this.centerY;

            // 2. Determine grid cell index based on *warped* coordinates
            double cFreqY = (this.cellFreqY == 0) ? 1.0 : this.cellFreqY;
            double cFreqX = (this.cellFreqX == 0) ? 1.0 : this.cellFreqX;
            int cellIndexY = (int) Math.floor((warpY - this.centerY) * cFreqY);
            int cellIndexX = (int) Math.floor((warpX - this.centerX) * cFreqX);

            // 3. Calculate Consistent Quantized Offset for this cell
            double cellSizeY = (Math.abs(cFreqY) < 1e-9) ? 1.0 : 1.0 / cFreqY;
            double cellSizeX = (Math.abs(cFreqX) < 1e-9) ? 1.0 : 1.0 / cFreqX;
            double randOffsetX = (getCellRandomDouble(cellIndexX, cellIndexY, 0) - 0.5) * 2.0;
            double randOffsetY = (getCellRandomDouble(cellIndexX, cellIndexY, 101) - 0.5) * 2.0;
            double maxCellOffset = Math.max(0.0, this.remapIntensity * 5.0);
            int numCellsOffsetX = (int)Math.round(randOffsetX * maxCellOffset);
            int numCellsOffsetY = (int)Math.round(randOffsetY * maxCellOffset);
            double finalOffsetX = numCellsOffsetX * cellSizeX;
            double finalOffsetY = numCellsOffsetY * cellSizeY;

            // 4. Apply Offset Probabilistically (Fragmentation)
            // Only apply the remap if random check > fragmentation value
            if (pContext.random() > this.fragmentation) {
                glitchX = x + finalOffsetX;
                glitchY = y + finalOffsetY;
            }
            // If check fails, glitchX/Y remain original x/y, fragmenting the block

        } // else point is not remapped at all (based on remapChance)


        // Apply the final transformation, scaled by the variation amount slider in JWF
        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;

        // Color preservation logic removed as per user code
    }

    // getName method without @Override
    public String getName() {
        // Keep name or update to reflect fragmentation? Let's keep it for now.
        return "glitch_warpedRemap";
    }

    // getParameterNames method without @Override
    public String[] getParameterNames() {
        return paramNames; // Includes fragmentation now
    }

    // getParameterValues method without @Override
    public Object[] getParameterValues() {
        return new Object[] {
            warpIntensity, warpFreqX, warpFreqY,
            remapIntensity, remapChance,
            cellFreqY, cellFreqX,
            centerX, centerY,
            fragmentation // Added fragmentation default
        };
    }

    // setParameter method without @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_WARP_INTENSITY.equalsIgnoreCase(pName)) { warpIntensity = pValue; }
        else if (PARAM_WARP_FREQ_X.equalsIgnoreCase(pName)) { warpFreqX = pValue; }
        else if (PARAM_WARP_FREQ_Y.equalsIgnoreCase(pName)) { warpFreqY = pValue; }
        else if (PARAM_REMAP_INTENSITY.equalsIgnoreCase(pName)) { remapIntensity = pValue; }
        else if (PARAM_REMAP_CHANCE.equalsIgnoreCase(pName)) { remapChance = Math.max(0.0, Math.min(1.0, pValue)); }
        else if (PARAM_CELL_FREQ_Y.equalsIgnoreCase(pName)) { cellFreqY = (pValue == 0) ? 0.001 : pValue; }
        else if (PARAM_CELL_FREQ_X.equalsIgnoreCase(pName)) { cellFreqX = (pValue == 0) ? 0.001 : pValue; }
        else if (PARAM_CENTER_X.equalsIgnoreCase(pName)) { centerX = pValue; }
        else if (PARAM_CENTER_Y.equalsIgnoreCase(pName)) { centerY = pValue; }
        else if (PARAM_FRAGMENTATION.equalsIgnoreCase(pName)) { // New parameter
            fragmentation = Math.max(0.0, Math.min(1.0, pValue)); // Clamp 0-1
        }
         else {
            throw new IllegalArgumentException("Parameter '" + pName + "' not supported by " + getName());
        }
    }

    // getParameter method removed as per user code
    // getVariationTypes method removed as per user code
}