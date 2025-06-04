package org.jwildfire.create.tina.variation;

import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import static org.jwildfire.base.mathlib.MathLib.*; // Imports SMALL_EPSILON, M_PI, etc.
import org.jwildfire.base.Tools;

public class EllipticUber3DFunc extends VariationFunc { // Renamed for clarity
    private static final long serialVersionUID = 5L; // Updated version

    // Parameter Definitions (remain the same as elliptic_uber_3D_plus)
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_WARP_X_AMP = "warp_x_amp";
    private static final String PARAM_WARP_X_FREQ = "warp_x_freq";
    private static final String PARAM_WARP_X_PHASE = "warp_x_phase";
    private static final String PARAM_WARP_X_ZINFLUENCE = "warp_x_zInfluence";
    private static final String PARAM_WARP_Y_AMP = "warp_y_amp";
    private static final String PARAM_WARP_Y_FREQ = "warp_y_freq";
    private static final String PARAM_WARP_Y_PHASE = "warp_y_phase";
    private static final String PARAM_WARP_Y_ZINfluence = "warp_y_zInfluence";
    private static final String PARAM_WARP_Z_AMP = "warp_z_amp";
    private static final String PARAM_WARP_Z_FREQ = "warp_z_freq";
    private static final String PARAM_WARP_Z_PHASE = "warp_z_phase";
    private static final String PARAM_ELLIPTIC_CORE_CONST = "core_const";
    private static final String PARAM_CORE_CONST_Z_MOD_AMP = "core_const_z_mod_amp";
    private static final String PARAM_CORE_CONST_Z_MOD_FREQ = "core_const_z_mod_freq";
    private static final String PARAM_PRECISION_DENOM_CONST = "prec_den_const";
    private static final String PARAM_A_SCALE = "a_scale";
    private static final String PARAM_B_SCALE = "b_scale";
    private static final String PARAM_COMP_X_POWER = "comp_x_power";
    private static final String PARAM_COMP_Y_POWER = "comp_y_power";
    private static final String PARAM_COMP_Z_POWER = "comp_z_power";
    private static final String PARAM_Y_LOG_MULT = "y_log_mult";
    private static final String PARAM_SCALE_X = "scale_x";
    private static final String PARAM_SCALE_Y = "scale_y";
    private static final String PARAM_SCALE_Z = "scale_z";
    private static final String PARAM_SHEAR_XY = "shear_xy";
    private static final String PARAM_SHEAR_XZ = "shear_xz";
    private static final String PARAM_SHEAR_YX = "shear_yx";
    private static final String PARAM_SHEAR_YZ = "shear_yz";
    private static final String PARAM_SHEAR_ZX = "shear_zx";
    private static final String PARAM_SHEAR_ZY = "shear_zy";
    private static final String PARAM_ANGLE_X = "angle_x_deg";
    private static final String PARAM_ANGLE_Y = "angle_y_deg";
    private static final String PARAM_ANGLE_Z = "angle_z_deg";
    private static final String PARAM_TWIST_X_RATE = "twist_x_rate";
    private static final String PARAM_TWIST_Y_RATE = "twist_y_rate";
    private static final String PARAM_TWIST_Z_RATE = "twist_z_rate";
    private static final String PARAM_INVERT_X = "invert_x";
    private static final String PARAM_INVERT_Y = "invert_y";
    private static final String PARAM_INVERT_Z = "invert_z";
    private static final String PARAM_BLEND_ORIGINAL = "blend_original";

    private static final String[] paramNames = {
            PARAM_MODE,
            PARAM_WARP_X_AMP, PARAM_WARP_X_FREQ, PARAM_WARP_X_PHASE, PARAM_WARP_X_ZINFLUENCE,
            PARAM_WARP_Y_AMP, PARAM_WARP_Y_FREQ, PARAM_WARP_Y_PHASE, PARAM_WARP_Y_ZINfluence,
            PARAM_WARP_Z_AMP, PARAM_WARP_Z_FREQ, PARAM_WARP_Z_PHASE,
            PARAM_ELLIPTIC_CORE_CONST, PARAM_CORE_CONST_Z_MOD_AMP, PARAM_CORE_CONST_Z_MOD_FREQ,
            PARAM_PRECISION_DENOM_CONST, PARAM_A_SCALE, PARAM_B_SCALE,
            PARAM_COMP_X_POWER, PARAM_COMP_Y_POWER, PARAM_COMP_Z_POWER, PARAM_Y_LOG_MULT,
            PARAM_SCALE_X, PARAM_SCALE_Y, PARAM_SCALE_Z,
            PARAM_SHEAR_XY, PARAM_SHEAR_XZ, PARAM_SHEAR_YX, PARAM_SHEAR_YZ, PARAM_SHEAR_ZX, PARAM_SHEAR_ZY,
            PARAM_ANGLE_X, PARAM_ANGLE_Y, PARAM_ANGLE_Z,
            PARAM_TWIST_X_RATE, PARAM_TWIST_Y_RATE, PARAM_TWIST_Z_RATE,
            PARAM_INVERT_X, PARAM_INVERT_Y, PARAM_INVERT_Z,
            PARAM_BLEND_ORIGINAL
    };

    // Modes
    private static final int MODE_ORIGINAL = 0;
    private static final int MODE_MIRRORY = 1;
    private static final int MODE_PRECISION = 2;
    private int mode = MODE_MIRRORY;

    // Parameter fields (defaults remain the same)
    private double warp_x_amp = 0.0, warp_x_freq = 1.0, warp_x_phase = 0.0, warp_x_zInfluence = 0.0;
    private double warp_y_amp = 0.0, warp_y_freq = 1.0, warp_y_phase = 0.0, warp_y_zInfluence = 0.0;
    private double warp_z_amp = 0.0, warp_z_freq = 1.0, warp_z_phase = 0.0;
    private double elliptic_core_const = 1.0;
    private double core_const_z_mod_amp = 0.0;
    private double core_const_z_mod_freq = 1.0;
    private double precision_denom_const = 1.0;
    private double a_scale = 1.0, b_scale = 1.0;
    private double comp_x_power = 1.0, comp_y_power = 1.0, comp_z_power = 1.0;
    private double y_log_mult = 1.0;
    private double scale_x = 1.0, scale_y = 1.0, scale_z = 1.0;
    private double shear_xy = 0.0, shear_xz = 0.0, shear_yx = 0.0, shear_yz = 0.0, shear_zx = 0.0, shear_zy = 0.0;
    private double angle_x_deg = 0.0, angle_y_deg = 0.0, angle_z_deg = 0.0;
    private double twist_x_rate = 0.0, twist_y_rate = 0.0, twist_z_rate = 0.0;
    private double invert_x = 0.0, invert_y = 0.0, invert_z = 0.0;
    private double blend_original = 0.0;
    private double _v;

    // Helper: sign-preserving power
    private double powSafe(double base, double exp) {
        if (base == 0.0 && exp == 0.0) return 1.0;
        if (base < 0.0 && fmod(exp, 1.0) != 0.0) {
            return -pow(Math.abs(base), exp);
        }
        return pow(base, exp);
    }

    // Helper: Pade approximant for sqrt(1+x)-1
    private double sqrt1pm1(double x) {
        if (-0.0625 < x && x < 0.0625) {
            double num = 0, den = 0;
            num += 1.0/32.0; den += 1.0/256.0; num *= x; den *= x;
            num += 5.0/16.0; den += 5.0/32.0; num *= x; den *= x;
            num += 3.0/4.0; den += 15.0/16.0; num *= x; den *= x;
            num += 1.0/2.0; den += 7.0/4.0; num *= x; den *= x;
            den += 1.0; return num / den;
        }
        return sqrt(1.0 + x) - 1.0;
    }

    // ****** CORRECTED sqrt_safe METHOD ******
    private double sqrt_safe(double x) {
        if (x < 0.0) return 0.0; // Ensure negative inputs don't produce NaN
        return sqrt(x);
    }

    @Override
    public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
        _v = pAmount * M_2_PI;
    }

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double initialX = pAffineTP.x;
        double initialY = pAffineTP.y;
        double initialZ = pAffineTP.z;

        double currentX = initialX;
        double currentY = initialY;
        double currentZ = initialZ;

        // 1. Pre-Transformation Warping
        double warpedX = currentX, warpedY = currentY, warpedZ = currentZ;
        if (warp_x_amp != 0.0) warpedX += warp_x_amp * sin(warp_x_freq * (initialY + initialZ * warp_x_zInfluence) + warp_x_phase);
        if (warp_y_amp != 0.0) warpedY += warp_y_amp * sin(warp_y_freq * (initialX + initialZ * warp_y_zInfluence) + warp_y_phase);
        if (warp_z_amp != 0.0) warpedZ += warp_z_amp * sin(warp_z_freq * (initialX + initialY) + warp_z_phase);
        currentX = warpedX; currentY = warpedY; currentZ = warpedZ;

        double dynamic_core_const = this.elliptic_core_const;
        if (core_const_z_mod_amp != 0.0) {
            dynamic_core_const += core_const_z_mod_amp * sin(core_const_z_mod_freq * initialZ);
        }

        double raw_x_comp = 0.0, raw_y_comp = 0.0, raw_z_comp;
        int y_sign_factor = (currentY >= 0) ? 1 : -1;

        // 2. Core "Elliptic" part
        if (mode == MODE_PRECISION) {
            double sq = currentY * currentY + currentX * currentX;
            double x2 = 2.0 * currentX;
            double xmaxm1 = 0.5 * (sqrt1pm1(sq + x2) + sqrt1pm1(sq - x2));
            double ssx = sqrt_safe(xmaxm1); // Use corrected sqrt_safe (though xmaxm1 is often >=0 here)
            
            double a_val_denom = precision_denom_const + xmaxm1;
            double a_val = (a_val_denom == 0.0) ? 0.0 : currentX / a_val_denom;
            a_val *= a_scale;
            raw_x_comp = asin(max(-1.0, min(1.0, a_val)));

            // Guarded log1p for raw_y_comp:
            double log1pArg = xmaxm1 + ssx;
            if (log1pArg <= -1.0 + SMALL_EPSILON) {
                raw_y_comp = 0.0; 
            } else {
                raw_y_comp = (double)y_sign_factor * Math.log1p(log1pArg);
            }
        } else { // MODE_ORIGINAL or MODE_MIRRORY
            double tmp = currentY * currentY + currentX * currentX + dynamic_core_const;
            double x2 = 2.0 * currentX;

            // Corrected xmax calculation with corrected sqrt_safe:
            double xmax_arg1 = tmp + x2;
            double xmax_arg2 = tmp - x2;
            double xmax = 0.5 * (sqrt_safe(xmax_arg1) + sqrt_safe(xmax_arg2));

            double a_val = (xmax == 0.0) ? 0.0 : currentX / xmax;
            a_val *= a_scale;
            
            double b_val_sq = 1.0 - a_val * a_val;
            double b_val = sqrt_safe(b_val_sq); // Use corrected sqrt_safe
            b_val *= b_scale;

            if (b_val == 0.0 && a_val != 0.0 && a_scale != 0.0) b_val = SMALL_EPSILON;
            if (mode == MODE_MIRRORY) y_sign_factor = (pContext.random() < 0.5) ? 1 : -1;
            raw_x_comp = atan2(a_val, b_val);

            // Guarded log for raw_y_comp:
            double logArg = xmax + sqrt_safe(xmax - 1.0); // Use corrected sqrt_safe
            if (logArg < SMALL_EPSILON) { 
                raw_y_comp = 0.0; 
            } else {
                raw_y_comp = (double)y_sign_factor * log(logArg);
            }
        }
        raw_z_comp = currentZ;

        // 3. Apply Component Powers and Y-Log Multiplier
        raw_x_comp = powSafe(raw_x_comp, comp_x_power);
        raw_y_comp = powSafe(raw_y_comp, comp_y_power) * y_log_mult;
        raw_z_comp = powSafe(raw_z_comp, comp_z_power);

        // 4. Apply _v
        double processed_x = _v * raw_x_comp;
        double processed_y = _v * raw_y_comp;
        double processed_z = _v * raw_z_comp;

        // 5. Apply Post-Elliptic Scaling
        processed_x *= scale_x;
        processed_y *= scale_y;
        processed_z *= scale_z;

        // 6. Apply Shearing
        double shx = processed_x, shy = processed_y, shz = processed_z;
        if(shear_xy != 0.0) processed_x += shear_xy * shy;
        if(shear_xz != 0.0) processed_x += shear_xz * shz; // Corrected from previous thought: this uses shz, which is fine.
                                                          // For X's calculation, current values of Y and Z are used.
        // For Y's calculation, it should use values *before* X was modified by Y and Z, if strict independence is desired.
        // However, sequential shearing is also common. Let's use the `shx, shy, shz` for original pre-shear step coords.
        processed_x = shx + (shear_xy * shy) + (shear_xz * shz); // Recalculate X shear based on original Y, Z for this step
        processed_y = shy + (shear_yx * shx) + (shear_yz * shz); // Y shear based on original X, Z
        processed_z = shz + (shear_zx * shx) + (shear_zy * shy); // Z shear based on original X, Y

        // 7. Apply 3D Rotation
        double rad_x = angle_x_deg * M_PI / 180.0;
        double rad_y = angle_y_deg * M_PI / 180.0;
        double rad_z = angle_z_deg * M_PI / 180.0;
        double temp_rx, temp_ry, temp_rz;

        if (angle_z_deg != 0.0) {
            double cosA = cos(rad_z); double sinA = sin(rad_z);
            temp_rx = processed_x * cosA - processed_y * sinA;
            temp_ry = processed_x * sinA + processed_y * cosA;
            processed_x = temp_rx; processed_y = temp_ry;
        }
        if (angle_y_deg != 0.0) {
            double cosB = cos(rad_y); double sinB = sin(rad_y);
            temp_rx = processed_x * cosB + processed_z * sinB;
            temp_rz = -processed_x * sinB + processed_z * cosB;
            processed_x = temp_rx; processed_z = temp_rz;
        }
        if (angle_x_deg != 0.0) {
            double cosC = cos(rad_x); double sinC = sin(rad_x);
            temp_ry = processed_y * cosC - processed_z * sinC;
            temp_rz = processed_y * sinC + processed_z * cosC;
            processed_y = temp_ry; processed_z = temp_rz;
        }

        // 8. Apply Twisting
        if (twist_x_rate != 0.0) {
            double twistAngle = processed_x * twist_x_rate; // Twist based on current x
            double cosT = cos(twistAngle); double sinT = sin(twistAngle);
            temp_ry = processed_y * cosT - processed_z * sinT;
            temp_rz = processed_y * sinT + processed_z * cosT;
            processed_y = temp_ry; processed_z = temp_rz;
        }
        if (twist_y_rate != 0.0) {
            double twistAngle = processed_y * twist_y_rate; // Twist based on current y
            double cosT = cos(twistAngle); double sinT = sin(twistAngle);
            temp_rx = processed_x * cosT + processed_z * sinT;
            temp_rz = -processed_x * sinT + processed_z * cosT;
            processed_x = temp_rx; processed_z = temp_rz;
        }
        if (twist_z_rate != 0.0) {
            double twistAngle = processed_z * twist_z_rate; // Twist based on current z
            double cosT = cos(twistAngle); double sinT = sin(twistAngle);
            temp_rx = processed_x * cosT - processed_y * sinT;
            temp_ry = processed_x * sinT + processed_y * cosT;
            processed_x = temp_rx; processed_y = temp_ry;
        }

        // 9. Apply Inversion
        if (invert_x == 1.0) processed_x *= -1.0;
        if (invert_y == 1.0) processed_y *= -1.0;
        if (invert_z == 1.0) processed_z *= -1.0;

        // 10. Apply Blending
        double blend_val = max(0.0, min(1.0, blend_original));
        double final_x = processed_x * (1.0 - blend_val) + initialX * blend_val;
        double final_y = processed_y * (1.0 - blend_val) + initialY * blend_val;
        double final_z = processed_z * (1.0 - blend_val) + initialZ * blend_val;

        pVarTP.x += final_x;
        pVarTP.y += final_y;
        pVarTP.z += final_z;
    }

    @Override
    public String getName() {
        return "elliptic_3D"; // Updated name
    }

    @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    @Override
    public Object[] getParameterValues() {
        return new Object[]{
                mode,
                warp_x_amp, warp_x_freq, warp_x_phase, warp_x_zInfluence,
                warp_y_amp, warp_y_freq, warp_y_phase, warp_y_zInfluence,
                warp_z_amp, warp_z_freq, warp_z_phase,
                elliptic_core_const, core_const_z_mod_amp, core_const_z_mod_freq,
                precision_denom_const, a_scale, b_scale,
                comp_x_power, comp_y_power, comp_z_power, y_log_mult,
                scale_x, scale_y, scale_z,
                shear_xy, shear_xz, shear_yx, shear_yz, shear_zx, shear_zy,
                angle_x_deg, angle_y_deg, angle_z_deg,
                twist_x_rate, twist_y_rate, twist_z_rate,
                invert_x, invert_y, invert_z,
                blend_original
        };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        // This needs to be complete and match paramNames
        if (PARAM_MODE.equalsIgnoreCase(pName)) mode = Tools.limitValue(Tools.FTOI(pValue), 0, 2);
        else if (PARAM_WARP_X_AMP.equalsIgnoreCase(pName)) warp_x_amp = pValue;
        else if (PARAM_WARP_X_FREQ.equalsIgnoreCase(pName)) warp_x_freq = pValue;
        else if (PARAM_WARP_X_PHASE.equalsIgnoreCase(pName)) warp_x_phase = pValue;
        else if (PARAM_WARP_X_ZINFLUENCE.equalsIgnoreCase(pName)) warp_x_zInfluence = pValue;
        else if (PARAM_WARP_Y_AMP.equalsIgnoreCase(pName)) warp_y_amp = pValue;
        else if (PARAM_WARP_Y_FREQ.equalsIgnoreCase(pName)) warp_y_freq = pValue;
        else if (PARAM_WARP_Y_PHASE.equalsIgnoreCase(pName)) warp_y_phase = pValue;
        else if (PARAM_WARP_Y_ZINfluence.equalsIgnoreCase(pName)) warp_y_zInfluence = pValue;
        else if (PARAM_WARP_Z_AMP.equalsIgnoreCase(pName)) warp_z_amp = pValue;
        else if (PARAM_WARP_Z_FREQ.equalsIgnoreCase(pName)) warp_z_freq = pValue;
        else if (PARAM_WARP_Z_PHASE.equalsIgnoreCase(pName)) warp_z_phase = pValue;
        else if (PARAM_ELLIPTIC_CORE_CONST.equalsIgnoreCase(pName)) elliptic_core_const = pValue;
        else if (PARAM_CORE_CONST_Z_MOD_AMP.equalsIgnoreCase(pName)) core_const_z_mod_amp = pValue;
        else if (PARAM_CORE_CONST_Z_MOD_FREQ.equalsIgnoreCase(pName)) core_const_z_mod_freq = pValue;
        else if (PARAM_PRECISION_DENOM_CONST.equalsIgnoreCase(pName)) precision_denom_const = pValue;
        else if (PARAM_A_SCALE.equalsIgnoreCase(pName)) a_scale = pValue;
        else if (PARAM_B_SCALE.equalsIgnoreCase(pName)) b_scale = pValue;
        else if (PARAM_COMP_X_POWER.equalsIgnoreCase(pName)) comp_x_power = pValue;
        else if (PARAM_COMP_Y_POWER.equalsIgnoreCase(pName)) comp_y_power = pValue;
        else if (PARAM_COMP_Z_POWER.equalsIgnoreCase(pName)) comp_z_power = pValue;
        else if (PARAM_Y_LOG_MULT.equalsIgnoreCase(pName)) y_log_mult = pValue;
        else if (PARAM_SCALE_X.equalsIgnoreCase(pName)) scale_x = pValue;
        else if (PARAM_SCALE_Y.equalsIgnoreCase(pName)) scale_y = pValue;
        else if (PARAM_SCALE_Z.equalsIgnoreCase(pName)) scale_z = pValue;
        else if (PARAM_SHEAR_XY.equalsIgnoreCase(pName)) shear_xy = pValue;
        else if (PARAM_SHEAR_XZ.equalsIgnoreCase(pName)) shear_xz = pValue;
        else if (PARAM_SHEAR_YX.equalsIgnoreCase(pName)) shear_yx = pValue;
        else if (PARAM_SHEAR_YZ.equalsIgnoreCase(pName)) shear_yz = pValue;
        else if (PARAM_SHEAR_ZX.equalsIgnoreCase(pName)) shear_zx = pValue;
        else if (PARAM_SHEAR_ZY.equalsIgnoreCase(pName)) shear_zy = pValue;
        else if (PARAM_ANGLE_X.equalsIgnoreCase(pName)) angle_x_deg = pValue;
        else if (PARAM_ANGLE_Y.equalsIgnoreCase(pName)) angle_y_deg = pValue;
        else if (PARAM_ANGLE_Z.equalsIgnoreCase(pName)) angle_z_deg = pValue;
        else if (PARAM_TWIST_X_RATE.equalsIgnoreCase(pName)) twist_x_rate = pValue;
        else if (PARAM_TWIST_Y_RATE.equalsIgnoreCase(pName)) twist_y_rate = pValue;
        else if (PARAM_TWIST_Z_RATE.equalsIgnoreCase(pName)) twist_z_rate = pValue;
        else if (PARAM_INVERT_X.equalsIgnoreCase(pName)) invert_x = pValue;
        else if (PARAM_INVERT_Y.equalsIgnoreCase(pName)) invert_y = pValue;
        else if (PARAM_INVERT_Z.equalsIgnoreCase(pName)) invert_z = pValue;
        else if (PARAM_BLEND_ORIGINAL.equalsIgnoreCase(pName)) blend_original = pValue;
        else throw new IllegalArgumentException("Unknown parameter: " + pName);
    }

    @Override
    public boolean enableRandomizeButton() {
        return true;
    }

    @Override
    public VariationFuncType[] getVariationTypes() {
        return new VariationFuncType[]{ VariationFuncType.VARTYPE_3D };
    }
}
