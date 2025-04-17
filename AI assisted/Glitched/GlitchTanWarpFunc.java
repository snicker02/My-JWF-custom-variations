package org.jwildfire.create.tina.variation;

// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Per-Point Tangent Angle Warp.
 * Distorts the radius based on the tangent of the point's angle.
 * Amount controls effect strength, Chance controls probability.
 * FreqMult controls the angular frequency of the tangent function.
 * Follows user-specified code structure.
 */
public class GlitchTanWarpFunc extends VariationFunc {

    private static final long serialVersionUID = 106L; // Unique ID

    private static final String PARAM_AMOUNT = "amount";       // Warp strength factor
    private static final String PARAM_CHANCE = "chance";       // Probability (0-1)
    private static final String PARAM_FREQ_MULT = "freq_mult"; // Multiplier for angle in tan()

    private static final String[] paramNames = { PARAM_AMOUNT, PARAM_CHANCE, PARAM_FREQ_MULT };

    private double amount = 0.05; // Tan warp is sensitive
    private double chance = 1.0;
    private double freq_mult = 5.0; // Angular frequency multiplier

    // init method removed (not needed for this effect)

    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        if (pContext.random() < this.chance) {
            double r = Math.sqrt(x*x + y*y);
            if (r > 1e-9) { // Avoid origin
                double angle = Math.atan2(y, x);
                double randMag = pContext.random(); // 0-1 for variation

                // Intensity scales the tan effect, randMagnitude provides variation
                double tanEffect = this.amount * (randMag - 0.5) * 2.0 * Math.tan(angle * this.freq_mult);
                double r_new = r + tanEffect;
                r_new = Math.max(0, r_new); // Ensure radius doesn't go negative

                glitchX = r_new * Math.cos(angle);
                glitchY = r_new * Math.sin(angle);
            }
        }

        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;
        // Color preservation logic removed
    }

    // getName method without @Override
    public String getName() {
        return "glitch_tanWarp";
    }

    // getParameterNames method without @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    // getParameterValues method without @Override
    public Object[] getParameterValues() {
        return new Object[] { amount, chance, freq_mult };
    }

    // setParameter method without @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_AMOUNT.equalsIgnoreCase(pName)) {
            amount = pValue;
        } else if (PARAM_CHANCE.equalsIgnoreCase(pName)) {
            chance = Math.max(0.0, Math.min(1.0, pValue));
        } else if (PARAM_FREQ_MULT.equalsIgnoreCase(pName)) {
            freq_mult = pValue;
        } else {
            throw new IllegalArgumentException("Parameter '" + pName + "' not supported by " + getName());
        }
    }
    // getParameter method removed
    // getVariationTypes method removed
}