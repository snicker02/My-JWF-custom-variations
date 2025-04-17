package org.jwildfire.create.tina.variation;

// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Per-Point Vertical Shift.
 * Applies a random vertical displacement to each point.
 * Amount controls max shift, Chance controls probability.
 * Follows user-specified code structure.
 */
public class GlitchVShiftFunc extends VariationFunc {

    private static final long serialVersionUID = 102L; // Unique ID

    private static final String PARAM_AMOUNT = "amount"; // Max shift distance
    private static final String PARAM_CHANCE = "chance"; // Probability (0-1)

    private static final String[] paramNames = { PARAM_AMOUNT, PARAM_CHANCE };

    private double amount = 0.1;
    private double chance = 1.0;

    // init method removed

    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        // Apply effect based on chance for this point
        if (pContext.random() < this.chance) {
            double randMag = pContext.random(); // 0-1
            double shiftY = (randMag - 0.5) * 2.0 * this.amount; // Range [-amount, +amount]
            glitchY = y + shiftY;
        }
        // glitchX remains x

        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;
        // Color preservation logic removed
    }

    // getName method without @Override
    public String getName() {
        return "glitch_vShift";
    }

    // getParameterNames method without @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    // getParameterValues method without @Override
    public Object[] getParameterValues() {
        return new Object[] { amount, chance };
    }

    // setParameter method without @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_AMOUNT.equalsIgnoreCase(pName)) {
            amount = pValue;
        } else if (PARAM_CHANCE.equalsIgnoreCase(pName)) {
            chance = Math.max(0.0, Math.min(1.0, pValue));
        } else {
            throw new IllegalArgumentException("Parameter '" + pName + "' not supported by " + getName());
        }
    }
    // getParameter method removed
    // getVariationTypes method removed
}