package org.jwildfire.create.tina.variation;

// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Per-Point Tiling.
 * Maps coordinates into repeating tiles using modulo arithmetic.
 * Frequency controls tile size (higher freq = smaller tile).
 * Chance controls probability. CenterX/CenterY set the tile grid origin.
 * Follows user-specified code structure.
 */
public class GlitchTilingFunc extends VariationFunc {

    private static final long serialVersionUID = 108L; // Unique ID

    private static final String PARAM_FREQUENCY = "frequency"; // Controls tile size (size = 1/freq)
    private static final String PARAM_CHANCE = "chance";     // Probability (0-1)
    private static final String PARAM_CENTER_X = "centerX";    // Tile grid origin X
    private static final String PARAM_CENTER_Y = "centerY";    // Tile grid origin Y


    private static final String[] paramNames = { PARAM_FREQUENCY, PARAM_CHANCE, PARAM_CENTER_X, PARAM_CENTER_Y };

    private double frequency = 10.0; // Default frequency
    private double chance = 1.0;
    private double centerX = 0.0;
    private double centerY = 0.0;

    // init method removed

    // Helper function for true mathematical modulo (handles negative numbers)
    private double mod(double a, double n) {
        if (Math.abs(n) < 1e-9) return a; // Avoid division by zero
        return ((a % n) + n) % n;
    }

    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        if (pContext.random() < this.chance) {
            double tFreq = (this.frequency <= 1e-6) ? 1.0 : this.frequency; // Ensure freq > 0
            double tileSize = 1.0 / tFreq;
            if (tileSize <= 1e-9) tileSize = 1.0; // Ensure tile size > 0

            // Apply modulo relative to center point
            glitchX = mod(x - this.centerX, tileSize) + this.centerX;
            glitchY = mod(y - this.centerY, tileSize) + this.centerY;
        }

        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;
        // Color preservation logic removed
    }

    // getName method without @Override
    public String getName() {
        return "glitch_tiling";
    }

    // getParameterNames method without @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    // getParameterValues method without @Override
    public Object[] getParameterValues() {
        return new Object[] { frequency, chance, centerX, centerY };
    }

    // setParameter method without @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_FREQUENCY.equalsIgnoreCase(pName)) {
            frequency = pValue; // Allow zero/negative? Let's ensure positive in transform.
        } else if (PARAM_CHANCE.equalsIgnoreCase(pName)) {
            chance = Math.max(0.0, Math.min(1.0, pValue));
        } else if (PARAM_CENTER_X.equalsIgnoreCase(pName)) {
            centerX = pValue;
        } else if (PARAM_CENTER_Y.equalsIgnoreCase(pName)) {
            centerY = pValue;
        } else {
            throw new IllegalArgumentException("Parameter '" + pName + "' not supported by " + getName());
        }
    }
    // getParameter method removed
    // getVariationTypes method removed
}
