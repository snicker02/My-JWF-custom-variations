package org.jwildfire.create.tina.variation;

// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Per-Point Quantization.
 * Snaps coordinates to a grid defined by frequency.
 * Frequency controls grid size (higher freq = smaller steps).
 * Chance controls probability.
 * Follows user-specified code structure.
 */
public class GlitchQuantizeFunc extends VariationFunc {

    private static final long serialVersionUID = 109L; // Unique ID

    private static final String PARAM_FREQUENCY = "frequency"; // Grid density (steps = 1/freq)
    private static final String PARAM_CHANCE = "chance";     // Probability (0-1)

    private static final String[] paramNames = { PARAM_FREQUENCY, PARAM_CHANCE };

    private double frequency = 20.0; // Default grid frequency
    private double chance = 1.0;

    // init method removed

    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        if (pContext.random() < this.chance) {
            double qFreq = (this.frequency <= 1e-6) ? 1.0 : this.frequency; // Ensure freq > 0
            double invQFreq = 1.0 / qFreq;
            // Snap coordinates
            glitchX = Math.round(x * qFreq) * invQFreq;
            glitchY = Math.round(y * qFreq) * invQFreq;
        }

        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;
        // Color preservation logic removed
    }

    // getName method without @Override
    public String getName() {
        return "glitch_quantize";
    }

    // getParameterNames method without @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    // getParameterValues method without @Override
    public Object[] getParameterValues() {
        return new Object[] { frequency, chance };
    }

    // setParameter method without @Override
    public void setParameter(String pName, double pValue) {
       if (PARAM_FREQUENCY.equalsIgnoreCase(pName)) {
            frequency = pValue; // Ensure positive in transform
        } else if (PARAM_CHANCE.equalsIgnoreCase(pName)) {
            chance = Math.max(0.0, Math.min(1.0, pValue));
        } else {
            throw new IllegalArgumentException("Parameter '" + pName + "' not supported by " + getName());
        }
    }
    // getParameter method removed
    // getVariationTypes method removed
}