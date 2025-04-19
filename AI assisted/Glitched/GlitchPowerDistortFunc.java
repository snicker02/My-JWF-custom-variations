package org.jwildfire.create.tina.variation;

// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Per-Point Power Distortion.
 * Non-linearly scales coordinates by raising them to a random power.
 * Amount controls exponent range, Chance controls probability.
 * Follows user-specified code structure.
 */
public class GlitchPowerDistortFunc extends VariationFunc {

    private static final long serialVersionUID = 107L; // Unique ID

    private static final String PARAM_AMOUNT = "amount";   // Controls exponent range (+/- amount around 1.0)
    private static final String PARAM_CHANCE = "chance";   // Probability (0-1)

    private static final String[] paramNames = { PARAM_AMOUNT, PARAM_CHANCE };

    private double amount = 0.5; // Exponent = 1.0 +/- (rand * amount)
    private double chance = 1.0;

    // init method removed

    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        if (pContext.random() < this.chance) {
             double randMag = pContext.random(); // 0-1 for magnitude variation
             // Exponent varies around 1.0 based on amount
             double exponent = 1.0 + (randMag - 0.5) * 2.0 * this.amount;
             // Clamp exponent to prevent extreme values (e.g., negative or too large)
             exponent = Math.max(0.1, Math.min(5.0, exponent)); // Example clamp range

             glitchX = Math.signum(x) * Math.pow(Math.abs(x), exponent);
             glitchY = Math.signum(y) * Math.pow(Math.abs(y), exponent);
        }

        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;
        // Color preservation logic removed
    }

    // getName method without @Override
    public String getName() {
        return "glitch_powerDistort";
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
}
