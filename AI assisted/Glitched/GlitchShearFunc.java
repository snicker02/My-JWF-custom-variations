package org.jwildfire.create.tina.variation;

// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Per-Point Shear.
 * Applies a random shear transformation to each point.
 * Amount controls max shear factor, Chance controls probability.
 * Axis selects shear direction (0=XbyY, 1=YbyX).
 * Follows user-specified code structure.
 */
public class GlitchShearFunc extends VariationFunc {

    private static final long serialVersionUID = 103L; // Unique ID

    private static final String PARAM_AMOUNT = "amount"; // Max shear factor
    private static final String PARAM_CHANCE = "chance"; // Probability (0-1)
    private static final String PARAM_AXIS = "axis";   // 0=Shear X by Y, 1=Shear Y by X

    private static final String[] paramNames = { PARAM_AMOUNT, PARAM_CHANCE, PARAM_AXIS };

    private double amount = 0.1; // Shear amounts are often small
    private double chance = 1.0;
    private int axis = 0; // Default Shear X by Y

    // init method removed

    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        if (pContext.random() < this.chance) {
            double randMag = pContext.random(); // 0-1
            // Scale shear factor by amount, include random direction/magnitude
            double shearFactor = (randMag - 0.5) * 2.0 * this.amount;

            if (this.axis == 0) { // Shear X by Y
                glitchX = x + shearFactor * y;
            } else { // Shear Y by X
                glitchY = y + shearFactor * x;
            }
        }

        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;
        // Color preservation logic removed
    }

    // getName method without @Override
    public String getName() {
        return "glitch_shear";
    }

    // getParameterNames method without @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    // getParameterValues method without @Override
    public Object[] getParameterValues() {
        return new Object[] { amount, chance, axis };
    }

    // setParameter method without @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_AMOUNT.equalsIgnoreCase(pName)) {
            amount = pValue;
        } else if (PARAM_CHANCE.equalsIgnoreCase(pName)) {
            chance = Math.max(0.0, Math.min(1.0, pValue));
        } else if (PARAM_AXIS.equalsIgnoreCase(pName)) {
            axis = (pValue < 0.5) ? 0 : 1; // Simple 0 or 1 selection
        } else {
            throw new IllegalArgumentException("Parameter '" + pName + "' not supported by " + getName());
        }
    }
    // getParameter method removed
    // getVariationTypes method removed
}