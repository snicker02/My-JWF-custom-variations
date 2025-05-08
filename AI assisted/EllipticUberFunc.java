package org.jwildfire.create.tina.variation;

import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import static org.jwildfire.base.mathlib.MathLib.*;
import org.jwildfire.base.Tools;

// Removed "implements SupportsGPU"
public class EllipticUberFunc extends VariationFunc {
    private static final long serialVersionUID = 2L; // Keeping version from previous full implementation

    // Original Elliptic Params
    private static final String PARAM_MODE = "mode";

    // Warping Params
    private static final String PARAM_WARP_X_AMP = "warp_x_amp";
    private static final String PARAM_WARP_X_FREQ = "warp_x_freq";
    private static final String PARAM_WARP_X_PHASE = "warp_x_phase";
    private static final String PARAM_WARP_Y_AMP = "warp_y_amp";
    private static final String PARAM_WARP_Y_FREQ = "warp_y_freq";
    private static final String PARAM_WARP_Y_PHASE = "warp_y_phase";

    // Elliptic-Specific Structure Params
    private static final String PARAM_ELLIPTIC_CORE_CONST = "core_const";
    private static final String PARAM_PRECISION_DENOM_CONST = "prec_den_const";
    private static final String PARAM_A_SCALE = "a_scale";
    private static final String PARAM_B_SCALE = "b_scale";

    // Component Modifiers (before _v)
    private static final String PARAM_COMP_X_POWER = "comp_x_power";
    private static final String PARAM_COMP_Y_POWER = "comp_y_power";
    private static final String PARAM_Y_LOG_MULT = "y_log_mult";

    // Post-Elliptic Transform Params (after _v)
    private static final String PARAM_SCALE_X = "scale_x";
    private static final String PARAM_SCALE_Y = "scale_y";
    private static final String PARAM_ANGLE = "angle";
    private static final String PARAM_INVERT_X = "invert_x";
    private static final String PARAM_INVERT_Y = "invert_y";
    private static final String PARAM_BLEND_ORIGINAL = "blend_original";


    private static final String[] paramNames = {
            PARAM_MODE,
            PARAM_WARP_X_AMP, PARAM_WARP_X_FREQ, PARAM_WARP_X_PHASE,
            PARAM_WARP_Y_AMP, PARAM_WARP_Y_FREQ, PARAM_WARP_Y_PHASE,
            PARAM_ELLIPTIC_CORE_CONST, PARAM_PRECISION_DENOM_CONST,
            PARAM_A_SCALE, PARAM_B_SCALE,
            PARAM_COMP_X_POWER, PARAM_COMP_Y_POWER, PARAM_Y_LOG_MULT,
            PARAM_SCALE_X, PARAM_SCALE_Y, PARAM_ANGLE,
            PARAM_INVERT_X, PARAM_INVERT_Y, PARAM_BLEND_ORIGINAL
    };

    // Original Elliptic fields
    private int mode = MODE_MIRRORY;
    private static final int MODE_ORIGINAL = 0;
    private static final int MODE_MIRRORY = 1;
    private static final int MODE_PRECISION = 2;

    // New fields with defaults
    private double warp_x_amp = 0.0;
    private double warp_x_freq = 1.0;
    private double warp_x_phase = 0.0;
    private double warp_y_amp = 0.0;
    private double warp_y_freq = 1.0;
    private double warp_y_phase = 0.0;

    private double elliptic_core_const = 1.0;
    private double precision_denom_const = 1.0;
    private double a_scale = 1.0;
    private double b_scale = 1.0;

    private double comp_x_power = 1.0;
    private double comp_y_power = 1.0;
    private double y_log_mult = 1.0;

    private double scale_x = 1.0;
    private double scale_y = 1.0;
    private double angle_deg = 0.0;
    private double invert_x = 0.0; // 0 = false, 1 = true
    private double invert_y = 0.0; // 0 = false, 1 = true
    private double blend_original = 0.0;

    private double _v;

    // Helper for sign-preserving power
    private double powSafe(double base, double exp) {
        if (base == 0.0 && exp == 0.0) return 1.0;
        if (base < 0.0 && fmod(exp, 1.0) != 0.0) {
            return -pow(Math.abs(base), exp);
        }
        return pow(base, exp);
    }

    private double sqrt_safe(double x) {
        return (x < SMALL_EPSILON) ? 0.0 : sqrt(x);
    }

    private double sqrt1pm1(double x) {
        if (-0.0625 < x && x < 0.0625) {
            double num = 0, den = 0;
            num += 1.0 / 32.0; den += 1.0 / 256.0; num *= x; den *= x;
            num += 5.0 / 16.0; den += 5.0 / 32.0; num *= x; den *= x;
            num += 3.0 / 4.0; den += 15.0 / 16.0; num *= x; den *= x;
            num += 1.0 / 2.0; den += 7.0 / 4.0; num *= x; den *= x;
            den += 1.0; return num / den;
        }
        return sqrt(1 + x) - 1;
    }

    @Override
    public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
        _v = pAmount * M_2_PI;
    }

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double initialX = pAffineTP.x;
        double initialY = pAffineTP.y;

        double currentX = initialX;
        double currentY = initialY;

        // 1. Pre-Transformation Warping
        if (warp_x_amp != 0.0) {
            currentX += warp_x_amp * sin(warp_x_freq * initialY + warp_x_phase);
        }
        if (warp_y_amp != 0.0) {
            currentY += warp_y_amp * sin(warp_y_freq * initialX + warp_y_phase);
        }

        double raw_x_comp = 0.0;
        double raw_y_comp = 0.0;
        int y_sign_factor = (currentY > 0) ? 1 : -1;

        if (mode == MODE_PRECISION) {
            double sq = currentY * currentY + currentX * currentX;
            double x2 = 2.0 * currentX;
            double xmaxm1 = 0.5 * (sqrt1pm1(sq + x2) + sqrt1pm1(sq - x2));
            double ssx = (xmaxm1 < 0) ? 0 : sqrt(xmaxm1);
            
            double a_val = currentX / (precision_denom_const + xmaxm1);
            a_val *= a_scale;

            raw_x_comp = asin(max(-1.0, min(1.0, a_val)));
            raw_y_comp = (double)y_sign_factor * Math.log1p(xmaxm1 + ssx);

        } else { // MODE_ORIGINAL or MODE_MIRRORY
            double tmp = currentY * currentY + currentX * currentX + elliptic_core_const;
            double x2 = 2.0 * currentX;
            double xmax = 0.5 * (sqrt(tmp + x2) + sqrt(tmp - x2));

            double a_val = currentX / xmax;
            a_val *= a_scale;

            double b_val = sqrt_safe(1.0 - a_val * a_val);
            b_val *= b_scale;
            if (b_val == 0.0 && a_val != 0.0 && a_scale != 0.0) b_val = SMALL_EPSILON;


            if (mode == MODE_MIRRORY) {
                y_sign_factor = (pContext.random() < 0.5) ? 1 : -1;
            }

            raw_x_comp = atan2(a_val, b_val);
            raw_y_comp = (double)y_sign_factor * log(xmax + sqrt_safe(xmax - 1.0));
        }

        // 2. Apply Component Powers and Y-Log Multiplier
        raw_x_comp = powSafe(raw_x_comp, comp_x_power);
        raw_y_comp = powSafe(raw_y_comp, comp_y_power);
        raw_y_comp *= y_log_mult;

        // 3. Apply _v (base scale from pAmount)
        double processed_x = _v * raw_x_comp;
        double processed_y = _v * raw_y_comp;

        // 4. Apply Post-Elliptic Scaling
        processed_x *= scale_x;
        processed_y *= scale_y;

        // 5. Apply Rotation
        if (angle_deg != 0.0) {
            double rad = angle_deg * M_PI / 180.0;
            double cos_a = cos(rad);
            double sin_a = sin(rad);
            double temp_x = processed_x * cos_a - processed_y * sin_a;
            processed_y = processed_x * sin_a + processed_y * cos_a; // original currentY used by mistake, should be processed_y
            processed_x = temp_x;
        }

        // 6. Apply Inversion
        if (invert_x == 1.0) processed_x *= -1.0;
        if (invert_y == 1.0) processed_y *= -1.0;

        // 7. Apply Blending
        double blend_val = blend_original;
        if (blend_val < 0.0) blend_val = 0.0;
        if (blend_val > 1.0) blend_val = 1.0;

        double final_x = processed_x * (1.0 - blend_val) + initialX * blend_val;
        double final_y = processed_y * (1.0 - blend_val) + initialY * blend_val;

        pVarTP.x += final_x;
        pVarTP.y += final_y;

        if (pContext.isPreserveZCoordinate()) {
            pVarTP.z += pAmount * pAffineTP.z;
        }
    }

    @Override
    public String getName() {
        return "elliptic_uber"; // Renamed slightly to indicate CPU only if needed
    }

    @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    @Override
    public Object[] getParameterValues() {
        return new Object[]{
                mode,
                warp_x_amp, warp_x_freq, warp_x_phase,
                warp_y_amp, warp_y_freq, warp_y_phase,
                elliptic_core_const, precision_denom_const,
                a_scale, b_scale,
                comp_x_power, comp_y_power, y_log_mult,
                scale_x, scale_y, angle_deg,
                invert_x, invert_y, blend_original
        };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_MODE.equalsIgnoreCase(pName)) mode = Tools.limitValue(Tools.FTOI(pValue), 0, 2);
        else if (PARAM_WARP_X_AMP.equalsIgnoreCase(pName)) warp_x_amp = pValue;
        else if (PARAM_WARP_X_FREQ.equalsIgnoreCase(pName)) warp_x_freq = pValue;
        else if (PARAM_WARP_X_PHASE.equalsIgnoreCase(pName)) warp_x_phase = pValue;
        else if (PARAM_WARP_Y_AMP.equalsIgnoreCase(pName)) warp_y_amp = pValue;
        else if (PARAM_WARP_Y_FREQ.equalsIgnoreCase(pName)) warp_y_freq = pValue;
        else if (PARAM_WARP_Y_PHASE.equalsIgnoreCase(pName)) warp_y_phase = pValue;
        else if (PARAM_ELLIPTIC_CORE_CONST.equalsIgnoreCase(pName)) elliptic_core_const = pValue;
        else if (PARAM_PRECISION_DENOM_CONST.equalsIgnoreCase(pName)) precision_denom_const = pValue;
        else if (PARAM_A_SCALE.equalsIgnoreCase(pName)) a_scale = pValue;
        else if (PARAM_B_SCALE.equalsIgnoreCase(pName)) b_scale = pValue;
        else if (PARAM_COMP_X_POWER.equalsIgnoreCase(pName)) comp_x_power = pValue;
        else if (PARAM_COMP_Y_POWER.equalsIgnoreCase(pName)) comp_y_power = pValue;
        else if (PARAM_Y_LOG_MULT.equalsIgnoreCase(pName)) y_log_mult = pValue;
        else if (PARAM_SCALE_X.equalsIgnoreCase(pName)) scale_x = pValue;
        else if (PARAM_SCALE_Y.equalsIgnoreCase(pName)) scale_y = pValue;
        else if (PARAM_ANGLE.equalsIgnoreCase(pName)) angle_deg = pValue;
        else if (PARAM_INVERT_X.equalsIgnoreCase(pName)) invert_x = pValue;
        else if (PARAM_INVERT_Y.equalsIgnoreCase(pName)) invert_y = pValue;
        else if (PARAM_BLEND_ORIGINAL.equalsIgnoreCase(pName)) blend_original = pValue;
        else throw new IllegalArgumentException(pName);
    }

    @Override
    public boolean enableRandomizeButton() {
        return false;
    }

    @Override
    public VariationFuncType[] getVariationTypes() {
        // Removed VARTYPE_SUPPORTS_GPU and VARTYPE_SUPPORTED_BY_SWAN if SWAN relies on GPU code.
        // If SWAN can use CPU, VARTYPE_SUPPORTED_BY_SWAN might still be applicable.
        // For now, assuming SWAN might be tied to GPU capability for some variations.
        // A minimal set would be VARTYPE_2D.
        return new VariationFuncType[]{VariationFuncType.VARTYPE_2D};
        // If it can be used by SWAN on CPU:
        // return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_SUPPORTED_BY_SWAN};
    }

    // Removed getGPUCode()
    // Removed getGPUFunctions()
}
