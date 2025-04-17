package org.jwildfire.create.tina.variation;

import java.util.Random; // Needed for band randomness
// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.Layer; // Needed for init
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Band Horizontal Shift.
 * Applies a consistent horizontal shift to all points within a horizontal band.
 * Amount controls max shift, Chance controls probability per band.
 * Frequency controls band height.
 * Follows user-specified code structure.
 */
public class GlitchBandHShiftFunc extends VariationFunc {

    private static final long serialVersionUID = 110L; // Unique ID

    private static final String PARAM_AMOUNT = "amount";     // Max shift distance
    private static final String PARAM_CHANCE = "chance";     // Probability (0-1) per band
    private static final String PARAM_FREQUENCY = "frequency"; // Band height control

    private static final String[] paramNames = { PARAM_AMOUNT, PARAM_CHANCE, PARAM_FREQUENCY };

    private double amount = 0.2;
    private double chance = 0.5; // Chance applies per band decision
    private double frequency = 10.0;

    // Random generator for band consistency - RE-ADDED
    private transient Random bandRng = new Random();
    private transient long currentSeed = 0;

    // init method without @Override as per user code - RE-ADDED
    public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
        // Seed RNG for this pass/thread
        currentSeed = System.nanoTime() + Thread.currentThread().getId();
        bandRng = new Random(currentSeed);
    }

    // Helper for band randomness - RE-ADDED
    private double getBandRandomDouble(int bandIndex, int offset) {
       bandRng.setSeed(currentSeed + bandIndex + offset);
       return bandRng.nextDouble();
    }

    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        double freq = (this.frequency == 0) ? 1.0 : this.frequency;
        int bandIndex = (int) Math.floor(y * freq); // Determine band

        // Decide *if* this band shifts and by how much (consistent for the band)
        // Use a random value derived from the band index to check chance
        if (getBandRandomDouble(bandIndex, 0) < this.chance) {
            // Get another consistent random value for the shift magnitude/direction
            double bandRandMag = getBandRandomDouble(bandIndex, 101);
            double shiftX = (bandRandMag - 0.5) * 2.0 * this.amount;
            glitchX = x + shiftX; // Apply consistent shift
        }
        // glitchY remains y

        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;
        // Color preservation logic removed
    }

    // getName method without @Override
    public String getName() {
        return "glitch_bandHShift";
    }

    // getParameterNames method without @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    // getParameterValues method without @Override
    public Object[] getParameterValues() {
        return new Object[] { amount, chance, frequency };
    }

    // setParameter method without @Override
    public void setParameter(String pName, double pValue) {
         if (PARAM_AMOUNT.equalsIgnoreCase(pName)) {
            amount = pValue;
        } else if (PARAM_CHANCE.equalsIgnoreCase(pName)) {
            chance = Math.max(0.0, Math.min(1.0, pValue));
        } else if (PARAM_FREQUENCY.equalsIgnoreCase(pName)) {
            frequency = (pValue == 0) ? 0.001 : pValue;
        } else {
            throw new IllegalArgumentException("Parameter '" + pName + "' not supported by " + getName());
        }
    }
    // getParameter method removed
    // getVariationTypes method removed
}