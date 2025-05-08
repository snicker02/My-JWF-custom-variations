package org.jwildfire.create.tina.variation;

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class BipolarExtendedFunc extends VariationFunc implements SupportsGPU {
    private static final long serialVersionUID = 4L; // Increment serialVersionUID

    // Existing parameters from previous iterations
    private static final String PARAM_SHIFT = "shift";
    private static final String PARAM_SCALE_X = "scale_x";
    private static final String PARAM_SCALE_Y = "scale_y";
    private static final String PARAM_ANGLE = "angle";
    private static final String PARAM_LOG_POWER = "log_power";
    private static final String PARAM_TERM_POWER = "term_power";
    private static final String PARAM_ATAN_Y_SCALE = "atan_y_scale";
    private static final String PARAM_ATAN_X_SCALE = "atan_x_scale";
    private static final String PARAM_Y_MULTIPLIER = "y_multiplier";
    private static final String PARAM_INVERT_X = "invert_x";
    private static final String PARAM_INVERT_Y = "invert_y";
    private static final String PARAM_BLEND_ORIGINAL = "blend_original";

    // New parameters for this iteration
    private static final String PARAM_WARP_X_AMP = "warp_x_amp";
    private static final String PARAM_WARP_X_FREQ = "warp_x_freq";
    private static final String PARAM_WARP_X_PHASE = "warp_x_phase";
    private static final String PARAM_WARP_Y_AMP = "warp_y_amp";
    private static final String PARAM_WARP_Y_FREQ = "warp_y_freq";
    private static final String PARAM_WARP_Y_PHASE = "warp_y_phase";
    private static final String PARAM_BIPOLAR_CONST = "bipolar_const";

    private static final String[] paramNames = {
            PARAM_SHIFT, PARAM_SCALE_X, PARAM_SCALE_Y,
            PARAM_ANGLE, PARAM_LOG_POWER, PARAM_TERM_POWER,
            PARAM_ATAN_Y_SCALE, PARAM_ATAN_X_SCALE, PARAM_Y_MULTIPLIER,
            PARAM_INVERT_X, PARAM_INVERT_Y, PARAM_BLEND_ORIGINAL,
            PARAM_WARP_X_AMP, PARAM_WARP_X_FREQ, PARAM_WARP_X_PHASE, // New
            PARAM_WARP_Y_AMP, PARAM_WARP_Y_FREQ, PARAM_WARP_Y_PHASE, // New
            PARAM_BIPOLAR_CONST // New
    };

    // Existing fields
    private double shift = 0.0;
    private double scale_x = 1.0;
    private double scale_y = 1.0;
    private double angle_deg = 0.0;
    private double log_power = 1.0;
    private double term_power = 1.0;
    private double atan_y_scale = 1.0;
    private double atan_x_scale = 1.0;
    private double y_multiplier = 1.0;
    private double invert_x = 0.0;
    private double invert_y = 0.0;
    private double blend_original = 0.0;

    // New fields with default values
    private double warp_x_amp = 0.0;
    private double warp_x_freq = 1.0;
    private double warp_x_phase = 0.0;
    private double warp_y_amp = 0.0;
    private double warp_y_freq = 1.0;
    private double warp_y_phase = 0.0;
    private double bipolar_const = 1.0;


    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double currentX = pAffineTP.x;
        double currentY = pAffineTP.y;

        // 1. Apply Pre-Transformation Non-linear Warping
        if (warp_x_amp != 0.0) {
            currentX += warp_x_amp * sin(warp_x_freq * pAffineTP.y + warp_x_phase); // Use original pAffineTP.y for warp calc
        }
        if (warp_y_amp != 0.0) {
            currentY += warp_y_amp * sin(warp_y_freq * pAffineTP.x + warp_y_phase); // Use original pAffineTP.x for warp calc
        }
        // Note: Using pAffineTP.y for X-warp and pAffineTP.x for Y-warp is a common way to do this.
        // Alternatively, you could use currentY for X-warp if you want sequential warping.
        // For simplicity and independent control, using original affine components for frequency input.

        double affX = currentX; // Use these possibly warped coordinates from now on
        double affY = currentY;

        // Main Bipolar Calculations
        double x2y2 = (affX * affX + affY * affY);

        // Use PARAM_BIPOLAR_CONST here
        double atan_arg_y = 2.0 * affY * atan_y_scale;
        double atan_arg_x = (x2y2 - bipolar_const) * atan_x_scale;
        if (atan_x_scale == 0 && x2y2 == bipolar_const) { // Handle potential 0 * Inf if bipolar_const makes term zero
             atan_arg_x = 0;
        }

        double ps = -M_PI_2 * shift;
        double y_transformed = 0.5 * atan2(atan_arg_y, atan_arg_x) + ps;

        if (y_transformed > M_PI_2) {
            y_transformed = -M_PI_2 + fmod(y_transformed + M_PI_2, M_PI);
        } else if (y_transformed < -M_PI_2) {
            y_transformed = M_PI_2 - fmod(M_PI_2 - y_transformed, M_PI);
        }
        y_transformed *= y_multiplier;

        // Use PARAM_BIPOLAR_CONST here for 't'
        double t = x2y2 + bipolar_const;
        double x2_term = 2.0 * affX;
        double term_f = t + x2_term;
        double term_g = t - x2_term;
        double f_powered, g_powered;

        if (term_power == 1.0) {
            f_powered = term_f;
            g_powered = term_g;
        } else if (term_power == 0.0) {
            f_powered = 1.0;
            g_powered = 1.0;
        } else {
            f_powered = sign(term_f) * pow(Math.abs(term_f), term_power);
            g_powered = sign(term_g) * pow(Math.abs(term_g), term_power);
        }

        double x_transformed_component = 0.0;
        if (g_powered != 0) {
            double log_arg = f_powered / g_powered;
            if (log_arg > 0) {
                double log_result = log(log_arg);
                if (log_power == 1.0) {
                    x_transformed_component = log_result;
                } else if (log_result == 0.0 && log_power == 0.0) {
                    x_transformed_component = 1.0;
                } else if (log_result < 0.0 && fmod(log_power, 1.0) != 0.0) {
                    x_transformed_component = -pow(-log_result, log_power);
                } else {
                    x_transformed_component = pow(log_result, log_power);
                }
                x_transformed_component *= 0.25 * M_2_PI;
            }
        }

        double transformed_calc_x = x_transformed_component * scale_x;
        double transformed_calc_y = M_2_PI * y_transformed * scale_y;

        // Rotation
        if (angle_deg != 0.0) {
            double rad = angle_deg * M_PI / 180.0;
            double cos_a = cos(rad);
            double sin_a = sin(rad);
            double rot_x = transformed_calc_x * cos_a - transformed_calc_y * sin_a;
            double rot_y = transformed_calc_x * sin_a + transformed_calc_y * cos_a;
            transformed_calc_x = rot_x;
            transformed_calc_y = rot_y;
        }

        // Inversion
        if (invert_x == 1.0) {
            transformed_calc_x *= -1.0;
        }
        if (invert_y == 1.0) {
            transformed_calc_y *= -1.0;
        }

        // Blending
        double blend_val = blend_original;
        if (blend_val < 0.0) blend_val = 0.0;
        if (blend_val > 1.0) blend_val = 1.0;

        // Blend with original pAffineTP, not the warped affX/affY for true original blending
        double final_x = transformed_calc_x * (1.0 - blend_val) + pAffineTP.x * blend_val;
        double final_y = transformed_calc_y * (1.0 - blend_val) + pAffineTP.y * blend_val;

        pVarTP.x += pAmount * final_x;
        pVarTP.y += pAmount * final_y;

        if (pContext.isPreserveZCoordinate()) {
            pVarTP.z += pAmount * pAffineTP.z;
        }
    }

    @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    @Override
    public Object[] getParameterValues() {
        return new Object[]{
                shift, scale_x, scale_y,
                angle_deg, log_power, term_power,
                atan_y_scale, atan_x_scale, y_multiplier,
                invert_x, invert_y, blend_original,
                warp_x_amp, warp_x_freq, warp_x_phase,
                warp_y_amp, warp_y_freq, warp_y_phase,
                bipolar_const
        };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_SHIFT.equalsIgnoreCase(pName)) shift = pValue;
        else if (PARAM_SCALE_X.equalsIgnoreCase(pName)) scale_x = pValue;
        else if (PARAM_SCALE_Y.equalsIgnoreCase(pName)) scale_y = pValue;
        else if (PARAM_ANGLE.equalsIgnoreCase(pName)) angle_deg = pValue;
        else if (PARAM_LOG_POWER.equalsIgnoreCase(pName)) log_power = pValue;
        else if (PARAM_TERM_POWER.equalsIgnoreCase(pName)) term_power = pValue;
        else if (PARAM_ATAN_Y_SCALE.equalsIgnoreCase(pName)) atan_y_scale = pValue;
        else if (PARAM_ATAN_X_SCALE.equalsIgnoreCase(pName)) atan_x_scale = pValue;
        else if (PARAM_Y_MULTIPLIER.equalsIgnoreCase(pName)) y_multiplier = pValue;
        else if (PARAM_INVERT_X.equalsIgnoreCase(pName)) invert_x = pValue;
        else if (PARAM_INVERT_Y.equalsIgnoreCase(pName)) invert_y = pValue;
        else if (PARAM_BLEND_ORIGINAL.equalsIgnoreCase(pName)) blend_original = pValue;
        else if (PARAM_WARP_X_AMP.equalsIgnoreCase(pName)) warp_x_amp = pValue;
        else if (PARAM_WARP_X_FREQ.equalsIgnoreCase(pName)) warp_x_freq = pValue;
        else if (PARAM_WARP_X_PHASE.equalsIgnoreCase(pName)) warp_x_phase = pValue;
        else if (PARAM_WARP_Y_AMP.equalsIgnoreCase(pName)) warp_y_amp = pValue;
        else if (PARAM_WARP_Y_FREQ.equalsIgnoreCase(pName)) warp_y_freq = pValue;
        else if (PARAM_WARP_Y_PHASE.equalsIgnoreCase(pName)) warp_y_phase = pValue;
        else if (PARAM_BIPOLAR_CONST.equalsIgnoreCase(pName)) bipolar_const = pValue;
        else throw new IllegalArgumentException("Unknown parameter: " + pName);
    }

    @Override
    public String getName() {
        return "bipolar_extreme"; // Consider a new name for this highly parameterized version
    }

    @Override
    public VariationFuncType[] getVariationTypes() {
        return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_SUPPORTS_GPU, VariationFuncType.VARTYPE_SUPPORTED_BY_SWAN};
    }

    @Override
    public String getGPUCode(FlameTransformationContext context) {
        String prefix = "__" + getName() + "_"; // e.g. __bipolar_extreme_
        String varAmount = "__" + getName();

        StringBuilder sb = new StringBuilder();

        // GPU: Capture original affine coordinates for warp and final blend
        sb.append("float initialX_gpu = __x;\n");
        sb.append("float initialY_gpu = __y;\n");
        sb.append("float currentX_gpu = initialX_gpu;\n");
        sb.append("float currentY_gpu = initialY_gpu;\n");

        // GPU: 1. Pre-Transformation Warping
        sb.append("if (").append(prefix).append(PARAM_WARP_X_AMP).append(" != 0.0f) {\n");
        sb.append("  currentX_gpu += ").append(prefix).append(PARAM_WARP_X_AMP).append(" * sinf(").append(prefix).append(PARAM_WARP_X_FREQ).append(" * initialY_gpu + ").append(prefix).append(PARAM_WARP_X_PHASE).append(");\n");
        sb.append("}\n");
        sb.append("if (").append(prefix).append(PARAM_WARP_Y_AMP).append(" != 0.0f) {\n");
        sb.append("  currentY_gpu += ").append(prefix).append(PARAM_WARP_Y_AMP).append(" * sinf(").append(prefix).append(PARAM_WARP_Y_FREQ).append(" * initialX_gpu + ").append(prefix).append(PARAM_WARP_Y_PHASE).append(");\n");
        sb.append("}\n");

        // GPU: Use warped coordinates (affX_gpu, affY_gpu) for main bipolar calculations
        sb.append("float affX_gpu = currentX_gpu;\n");
        sb.append("float affY_gpu = currentY_gpu;\n");
        sb.append("float x2y2_gpu = affX_gpu * affX_gpu + affY_gpu * affY_gpu;\n"); // Recalculate r2 based on warped coords

        // GPU: Use PARAM_BIPOLAR_CONST
        sb.append("float atan_arg_y_gpu = 2.0f * affY_gpu * ").append(prefix).append(PARAM_ATAN_Y_SCALE).append(";\n");
        sb.append("float bipolar_const_gpu = ").append(prefix).append(PARAM_BIPOLAR_CONST).append(";\n");
        sb.append("float atan_arg_x_term_gpu = x2y2_gpu - bipolar_const_gpu;\n");
        sb.append("float atan_arg_x_gpu = atan_arg_x_term_gpu * ").append(prefix).append(PARAM_ATAN_X_SCALE).append(";\n");
        // Handle specific 0*Inf case
        sb.append("if (").append(prefix).append(PARAM_ATAN_X_SCALE).append(" == 0.0f && x2y2_gpu == bipolar_const_gpu) { atan_arg_x_gpu = 0.0f; }\n");


        sb.append("float ps_gpu = -0.5f * PI * ").append(prefix).append(PARAM_SHIFT).append(";\n");
        sb.append("float y0_gpu = 0.5f * atan2(atan_arg_y_gpu, atan_arg_x_gpu) + ps_gpu;\n");

        sb.append("if (y0_gpu > 0.5f*PI) y0_gpu = -0.5f*PI + fmodf(y0_gpu + 0.5f*PI, PI);\n");
        sb.append("else if (y0_gpu < -0.5f*PI) y0_gpu = 0.5f*PI - fmodf(0.5f*PI - y0_gpu, PI);\n");
        sb.append("y0_gpu *= ").append(prefix).append(PARAM_Y_MULTIPLIER).append(";\n");

        // GPU: Use PARAM_BIPOLAR_CONST for 't'
        sb.append("float t_gpu = x2y2_gpu + bipolar_const_gpu;\n");
        sb.append("float x2_term_gpu = 2.0f * affX_gpu;\n");
        sb.append("float term_f_gpu = t_gpu + x2_term_gpu;\n");
        sb.append("float term_g_gpu = t_gpu - x2_term_gpu;\n");
        sb.append("float f_powered_gpu, g_powered_gpu;\n");

        sb.append("if (").append(prefix).append(PARAM_TERM_POWER).append(" == 1.0f) {\n");
        sb.append("  f_powered_gpu = term_f_gpu;\n  g_powered_gpu = term_g_gpu;\n");
        sb.append("} else if (").append(prefix).append(PARAM_TERM_POWER).append(" == 0.0f) {\n");
        sb.append("  f_powered_gpu = 1.0f;\n  g_powered_gpu = 1.0f;\n");
        sb.append("} else {\n");
        sb.append("  f_powered_gpu = sign(term_f_gpu) * pow(fabsf(term_f_gpu), ").append(prefix).append(PARAM_TERM_POWER).append(");\n");
        sb.append("  g_powered_gpu = sign(term_g_gpu) * pow(fabsf(term_g_gpu), ").append(prefix).append(PARAM_TERM_POWER).append(");\n");
        sb.append("}\n");

        sb.append("float x_transformed_gpu = 0.0f;\n");
        sb.append("if (g_powered_gpu != 0.0f) {\n");
        sb.append("  float log_arg_gpu = f_powered_gpu / g_powered_gpu;\n");
        sb.append("  if (log_arg_gpu > 0.0f) {\n");
        sb.append("    float log_result_gpu = logf(log_arg_gpu);\n");
        sb.append("    float base_lp = log_result_gpu;\n    float exp_lp = ").append(prefix).append(PARAM_LOG_POWER).append(";\n");
        sb.append("    if (exp_lp == 1.0f) { x_transformed_gpu = base_lp; }\n");
        sb.append("    else if (base_lp == 0.0f && exp_lp == 0.0f) { x_transformed_gpu = 1.0f; }\n");
        sb.append("    else if (base_lp < 0.0f && fmodf(exp_lp, 1.0f) != 0.0f) { x_transformed_gpu = -pow(fabsf(base_lp), exp_lp); }\n");
        sb.append("    else { x_transformed_gpu = pow(base_lp, exp_lp); }\n");
        sb.append("    x_transformed_gpu *= 0.25f * (2.0f / PI);\n");
        sb.append("  }\n");
        sb.append("}\n");

        sb.append("float transformed_calc_x_gpu = x_transformed_gpu * ").append(prefix).append(PARAM_SCALE_X).append(";\n");
        sb.append("float transformed_calc_y_gpu = (2.0f / PI) * y0_gpu * ").append(prefix).append(PARAM_SCALE_Y).append(";\n");

        // GPU: Rotation
        sb.append("float angle_rad_gpu = ").append(prefix).append(PARAM_ANGLE).append(" * PI / 180.0f;\n");
        sb.append("if (").append(prefix).append(PARAM_ANGLE).append(" != 0.0f) {\n");
        sb.append("  float cos_a_gpu = cosf(angle_rad_gpu);\n  float sin_a_gpu = sinf(angle_rad_gpu);\n");
        sb.append("  float rot_x_gpu = transformed_calc_x_gpu * cos_a_gpu - transformed_calc_y_gpu * sin_a_gpu;\n");
        sb.append("  float rot_y_gpu = transformed_calc_x_gpu * sin_a_gpu + transformed_calc_y_gpu * cos_a_gpu;\n");
        sb.append("  transformed_calc_x_gpu = rot_x_gpu;\n  transformed_calc_y_gpu = rot_y_gpu;\n");
        sb.append("}\n");

        // GPU: Inversion
        sb.append("if (").append(prefix).append(PARAM_INVERT_X).append(" == 1.0f) { transformed_calc_x_gpu *= -1.0f; }\n");
        sb.append("if (").append(prefix).append(PARAM_INVERT_Y).append(" == 1.0f) { transformed_calc_y_gpu *= -1.0f; }\n");

        // GPU: Blending (use initialX_gpu, initialY_gpu for true original blend)
        sb.append("float blend_gpu = clamp(").append(prefix).append(PARAM_BLEND_ORIGINAL).append(", 0.0f, 1.0f);\n");
        sb.append("float final_x_gpu = transformed_calc_x_gpu * (1.0f - blend_gpu) + initialX_gpu * blend_gpu;\n");
        sb.append("float final_y_gpu = transformed_calc_y_gpu * (1.0f - blend_gpu) + initialY_gpu * blend_gpu;\n");

        sb.append("__px += ").append(varAmount).append(" * final_x_gpu;\n");
        sb.append("__py += ").append(varAmount).append(" * final_y_gpu;\n");

        if (context.isPreserveZCoordinate()) {
            sb.append("__pz += ").append(varAmount).append(" * __z;\n"); // __z is initial pAffineTP.z
        }
        return sb.toString();
    }
}
