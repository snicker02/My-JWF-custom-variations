package org.jwildfire.create.tina.variation;

// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Per-Point Rotation.
 * Applies a random rotation around a center point.
 * Amount controls max angle (scaled by PI), Chance controls probability.
 * CenterX/CenterY set the rotation origin.
 * Follows user-specified code structure.
 */
public class package org.jwildfire.create.tina.variation;

// import org.jwildfire.create.tina.base.FlameTransformationContext; // User preference
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * Glitch: Per-Point Twirl.
 * Applies a rotational warp around a center point, increasing with distance.
 * Amount controls twirl strength, Chance controls probability.
 * CenterX/CenterY set the twirl origin.
 * Follows user-specified code structure.
 */
public class GlitchTwirlFunc extends VariationFunc {

    private static final long serialVersionUID = 105L; // Unique ID

    private static final String PARAM_AMOUNT = "amount";   // Twirl strength factor
    private static final String PARAM_CHANCE = "chance";   // Probability (0-1)
    private static final String PARAM_CENTER_X = "centerX"; // Twirl center X
    private static final String PARAM_CENTER_Y = "centerY"; // Twirl center Y

    private static final String[] paramNames = { PARAM_AMOUNT, PARAM_CHANCE, PARAM_CENTER_X, PARAM_CENTER_Y };

    private double amount = 0.5; // Twirl strength often > rotation amount
    private double chance = 1.0;
    private double centerX = 0.0;
    private double centerY = 0.0;

    // init method removed

    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        if (pContext.random() < this.chance) {
            double dx = x - this.centerX;
            double dy = y - this.centerY;
            double r = Math.sqrt(dx*dx + dy*dy);

            if (r > 1e-9) { // Avoid singularity at center
                double angle = Math.atan2(dy, dx);
                // Twirl angle offset increases linearly with radius (r) and amount
                // Add small random variation to twirl strength per point
                double randMag = pContext.random() * 0.4 + 0.8; // Random factor 0.8-1.2
                double angleOffset = this.amount * r * randMag;

                angle += angleOffset; // Add twirl offset to original angle

                glitchX = r * Math.cos(angle) + this.centerX;
                glitchY = r * Math.sin(angle) + this.centerY;
            }
        }

        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;
        // Color preservation logic removed
    }

    // getName method without @Override
    public String getName() {
        return "glitch_twirl";
    }

    // getParameterNames method without @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    // getParameterValues method without @Override
    public Object[] getParameterValues() {
        return new Object[] { amount, chance, centerX, centerY };
    }

    // setParameter method without @Override
    public void setParameter(String pName, double pValue) {
         if (PARAM_AMOUNT.equalsIgnoreCase(pName)) {
            amount = pValue;
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
} extends VariationFunc {

    private static final long serialVersionUID = 104L; // Unique ID

    private static final String PARAM_AMOUNT = "amount";   // Scales max angle relative to PI
    private static final String PARAM_CHANCE = "chance";   // Probability (0-1)
    private static final String PARAM_CENTER_X = "centerX"; // Rotation center X
    private static final String PARAM_CENTER_Y = "centerY"; // Rotation center Y

    private static final String[] paramNames = { PARAM_AMOUNT, PARAM_CHANCE, PARAM_CENTER_X, PARAM_CENTER_Y };

    private double amount = 0.1; // e.g., 0.1 = up to +/- 0.1*PI radians
    private double chance = 1.0;
    private double centerX = 0.0;
    private double centerY = 0.0;

    // init method removed

    // transform method without @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmountVar) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double glitchX = x;
        double glitchY = y;

        if (pContext.random() < this.chance) {
            double randMag = pContext.random(); // 0-1
            // Angle range is +/- amount * PI radians
            double angle = (randMag - 0.5) * 2.0 * this.amount * Math.PI;

            double dx = x - this.centerX;
            double dy = y - this.centerY;

            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);

            glitchX = dx * cosA - dy * sinA + this.centerX;
            glitchY = dx * sinA + dy * cosA + this.centerY;
        }

        pVarTP.x = pAmountVar * glitchX;
        pVarTP.y = pAmountVar * glitchY;
        // Color preservation logic removed
    }

    // getName method without @Override
    public String getName() {
        return "glitch_rotate";
    }

    // getParameterNames method without @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    // getParameterValues method without @Override
    public Object[] getParameterValues() {
        return new Object[] { amount, chance, centerX, centerY };
    }

    // setParameter method without @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_AMOUNT.equalsIgnoreCase(pName)) {
            amount = pValue;
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