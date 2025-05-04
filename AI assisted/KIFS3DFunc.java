package org.jwildfire.create.tina.variation; // Assumed package

// Base JWildfire classes
import org.jwildfire.create.tina.variation.FlameTransformationContext;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

// Base Variation classes
import org.jwildfire.create.tina.variation.VariationFunc;
import org.jwildfire.create.tina.variation.VariationFuncType;

// Utilities
import org.jwildfire.base.Tools;
// Removed static import for MathLib, using standard java.lang.Math now
// import static org.jwildfire.base.mathlib.MathLib.*;


/**
 * KIFS3DFunc - A JWildfire Variation implementing Kaleidoscopic Iterated Function Systems.
 * Includes non-uniform scaling, post-symmetry option, and multiple color modes.
 * Based on https://web.ics.purdue.edu/~tmcgraw/papers/kifs_mcgraw_2015.pdf by Tim McGraw
 
 
 * * WARNING: Requires testing. Verify 'init' signature and XYZPoint color field.
 */
public class KIFS3DFunc extends VariationFunc {

    // Parameter Definitions (String Constants)
    private static final String PARAM_MAX_ITER = "max_iter";
    private static final String PARAM_BAILOUT_RADIUS = "bailout_radius";
    private static final String PARAM_KIFS_SCALE_X = "kifs_scale_x";
    private static final String PARAM_KIFS_SCALE_Y = "kifs_scale_y";
    private static final String PARAM_KIFS_SCALE_Z = "kifs_scale_z";
    private static final String PARAM_CENTER_X = "center_x";
    private static final String PARAM_CENTER_Y = "center_y";
    private static final String PARAM_CENTER_Z = "center_z";
    private static final String PARAM_OFFSET_X = "offset_x";
    private static final String PARAM_OFFSET_Y = "offset_y";
    private static final String PARAM_OFFSET_Z = "offset_z";
    private static final String PARAM_FOLD_TYPE = "fold_type";
    private static final String PARAM_FOLD_PLANE1_NX = "fold_plane1_nx";
    private static final String PARAM_FOLD_PLANE1_NY = "fold_plane1_ny";
    private static final String PARAM_FOLD_PLANE1_NZ = "fold_plane1_nz";
    private static final String PARAM_ROT_X = "rot_x";
    private static final String PARAM_ROT_Y = "rot_y";
    private static final String PARAM_ROT_Z = "rot_z";
    private static final String PARAM_ROT_ORDER = "rot_order";
    private static final String PARAM_TRANSFORM_ORDER = "transform_order";
    private static final String PARAM_POST_SYMMETRY = "post_symmetry";
    private static final String PARAM_COLOR_MODE = "color_mode";       // NEW Color Mode Param
    private static final String PARAM_COLOR_SCALE = "color_scale";      // NEW Color Scale Param

    // Array defining the parameter names in order (UPDATED)
    private static final String[] paramNames = {
            PARAM_MAX_ITER, PARAM_BAILOUT_RADIUS,
            PARAM_KIFS_SCALE_X, PARAM_KIFS_SCALE_Y, PARAM_KIFS_SCALE_Z,
            PARAM_CENTER_X, PARAM_CENTER_Y, PARAM_CENTER_Z,
            PARAM_OFFSET_X, PARAM_OFFSET_Y, PARAM_OFFSET_Z,
            PARAM_FOLD_TYPE, PARAM_FOLD_PLANE1_NX, PARAM_FOLD_PLANE1_NY, PARAM_FOLD_PLANE1_NZ,
            PARAM_ROT_X, PARAM_ROT_Y, PARAM_ROT_Z, PARAM_ROT_ORDER,
            PARAM_TRANSFORM_ORDER,
            PARAM_POST_SYMMETRY,
            PARAM_COLOR_MODE, // NEW
            PARAM_COLOR_SCALE // NEW
    };

    // Parameter Values (Instance Variables) - Defaults (UPDATED)
    private int max_iter = 10;
    private double bailout_radius = 4.0;
    private double kifs_scale_x = 2.0;
    private double kifs_scale_y = 2.0;
    private double kifs_scale_z = 2.0;
    private double center_x = 0.0;
    private double center_y = 0.0;
    private double center_z = 0.0;
    private double offset_x = 1.0;
    private double offset_y = 1.0;
    private double offset_z = 1.0;

    // Fold Type Constants
    private static final int FOLD_TYPE_NONE = 0;
    private static final int FOLD_TYPE_ABS_XYZ = 1;
    private static final int FOLD_TYPE_DIAG_XY_POS = 2;
    private static final int FOLD_TYPE_CUSTOM1 = 3;
    private int fold_type = FOLD_TYPE_ABS_XYZ;
    private double fold_plane1_nx = 1.0;
    private double fold_plane1_ny = 0.0;
    private double fold_plane1_nz = 0.0;

    private double rot_x = 0.0; // Degrees
    private double rot_y = 0.0; // Degrees
    private double rot_z = 0.0; // Degrees

    // Rotation Order Constants
    private static final int ROT_ORDER_XYZ = 0;
    private static final int ROT_ORDER_ZYX = 1;
    private int rot_order = ROT_ORDER_XYZ;

    // Transform Order Constants
    private static final int TRANSFORM_ORDER_FOLD_ROT_SCALE = 0;
    private static final int TRANSFORM_ORDER_ROT_FOLD_SCALE = 1;
    private static final int TRANSFORM_ORDER_SCALE_FOLD_ROT = 2;
    private int transform_order = TRANSFORM_ORDER_FOLD_ROT_SCALE;

    // Post-Symmetry Type Constants (Bitmask)
    private static final int POST_SYM_NONE = 0;
    private static final int POST_SYM_X = 1;
    private static final int POST_SYM_Y = 2;
    private static final int POST_SYM_Z = 4;
    private int post_symmetry = POST_SYM_NONE;

    // Color Mode Constants - NEW
    private static final int COLOR_MODE_ITER = 0;         // Escape time / Iteration count (original)
    private static final int COLOR_MODE_FINAL_R = 1;        // Final Radius
    private static final int COLOR_MODE_FINAL_XY_ANGLE = 2; // Final Angle in XY plane
    private static final int COLOR_MODE_FINAL_X = 3;        // Final X coord
    private static final int COLOR_MODE_FINAL_Y = 4;        // Final Y coord
    private static final int COLOR_MODE_FINAL_Z = 5;        // Final Z coord
    private int color_mode = COLOR_MODE_ITER; // Default to original iteration coloring
    private double color_scale = 1.0;         // Default scale for position-based coloring

    // Precomputed values
    private double bailout_sq;
    private double rot_x_rad, rot_y_rad, rot_z_rad;
    private double fold_norm1_len_sq;

    // --- JWildfire Variation Boilerplate ---

    // Constructor
    public KIFS_Core3DFunc() {
        super();
    }

    // --- Parameter Handling Methods ---

    @Override
    public String[] getParameterNames() {
        return paramNames; // Updated array reference
    }

    @Override
    public Object[] getParameterValues() {
        // Return values in the SAME order as paramNames (UPDATED)
        return new Object[]{
                max_iter, bailout_radius,
                kifs_scale_x, kifs_scale_y, kifs_scale_z,
                center_x, center_y, center_z,
                offset_x, offset_y, offset_z,
                fold_type, fold_plane1_nx, fold_plane1_ny, fold_plane1_nz,
                rot_x, rot_y, rot_z, rot_order,
                transform_order,
                post_symmetry,
                color_mode, // NEW
                color_scale // NEW
        };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        // Set the value of the parameter identified by pName (UPDATED)
        if (PARAM_MAX_ITER.equalsIgnoreCase(pName))
            max_iter = (int) pValue;
        else if (PARAM_BAILOUT_RADIUS.equalsIgnoreCase(pName))
            bailout_radius = pValue;
        else if (PARAM_KIFS_SCALE_X.equalsIgnoreCase(pName))
            kifs_scale_x = pValue;
        else if (PARAM_KIFS_SCALE_Y.equalsIgnoreCase(pName))
            kifs_scale_y = pValue;
        else if (PARAM_KIFS_SCALE_Z.equalsIgnoreCase(pName))
            kifs_scale_z = pValue;
        else if (PARAM_CENTER_X.equalsIgnoreCase(pName))
            center_x = pValue;
        else if (PARAM_CENTER_Y.equalsIgnoreCase(pName))
            center_y = pValue;
        else if (PARAM_CENTER_Z.equalsIgnoreCase(pName))
            center_z = pValue;
        else if (PARAM_OFFSET_X.equalsIgnoreCase(pName))
            offset_x = pValue;
        else if (PARAM_OFFSET_Y.equalsIgnoreCase(pName))
            offset_y = pValue;
        else if (PARAM_OFFSET_Z.equalsIgnoreCase(pName))
            offset_z = pValue;
        else if (PARAM_FOLD_TYPE.equalsIgnoreCase(pName))
            fold_type = (int) pValue;
        else if (PARAM_FOLD_PLANE1_NX.equalsIgnoreCase(pName))
            fold_plane1_nx = pValue;
        else if (PARAM_FOLD_PLANE1_NY.equalsIgnoreCase(pName))
            fold_plane1_ny = pValue;
        else if (PARAM_FOLD_PLANE1_NZ.equalsIgnoreCase(pName))
            fold_plane1_nz = pValue;
        else if (PARAM_ROT_X.equalsIgnoreCase(pName))
            rot_x = pValue;
        else if (PARAM_ROT_Y.equalsIgnoreCase(pName))
            rot_y = pValue;
        else if (PARAM_ROT_Z.equalsIgnoreCase(pName))
            rot_z = pValue;
        else if (PARAM_ROT_ORDER.equalsIgnoreCase(pName))
            rot_order = (int) pValue;
        else if (PARAM_TRANSFORM_ORDER.equalsIgnoreCase(pName))
            transform_order = (int) pValue;
        else if (PARAM_POST_SYMMETRY.equalsIgnoreCase(pName))
            post_symmetry = (int) pValue;
        else if (PARAM_COLOR_MODE.equalsIgnoreCase(pName)) // NEW
             color_mode = (int) pValue;
        else if (PARAM_COLOR_SCALE.equalsIgnoreCase(pName)) // NEW
             color_scale = pValue;
        else
            throw new IllegalArgumentException("Unknown parameter name: " + pName);
    }

    // --- Initialization ---
    /**
     * Initializes precomputed values.
     * WARNING: Assumes FlameTransformationContext is the correct context object.
     * Please verify the signature in your JWildfire version's VariationFunc class.
     */
    @Override
    public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
        // This signature assumes FlameTransformationContext is needed/correct. Verify!
        bailout_sq = bailout_radius * bailout_radius;
        if (bailout_sq <= 0) bailout_sq = 1e-6;

        // Use standard Java Math.toRadians
        rot_x_rad = Math.toRadians(rot_x);
        rot_y_rad = Math.toRadians(rot_y);
        rot_z_rad = Math.toRadians(rot_z);

        // Precompute length squared for custom fold normal(s)
        fold_norm1_len_sq = fold_plane1_nx * fold_plane1_nx + fold_plane1_ny * fold_plane1_ny + fold_plane1_nz * fold_plane1_nz;
    }

    // --- The Core Transformation Logic ---
    /**
     * Applies the KIFS transformation.
     */
    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {

        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double z = pAffineTP.z;

        double iter_color = 0.0; // Color based on iteration count (escape time)
        boolean escaped = false;

        // KIFS internal iteration loop
        for (int i = 0; i < max_iter; i++) {
            double cur_x = x;
            double cur_y = y;
            double cur_z = z;

            // Apply Transformation Sequence based on transform_order
            if (transform_order == TRANSFORM_ORDER_FOLD_ROT_SCALE) {
                Point3D folded = applyFolding(cur_x, cur_y, cur_z);
                Point3D rotated = applyRotation(folded.x, folded.y, folded.z);
                Point3D scaled = applyScaleTranslate(rotated.x, rotated.y, rotated.z);
                x = scaled.x; y = scaled.y; z = scaled.z;
            } else if (transform_order == TRANSFORM_ORDER_ROT_FOLD_SCALE) {
                Point3D rotated = applyRotation(cur_x, cur_y, cur_z);
                Point3D folded = applyFolding(rotated.x, rotated.y, rotated.z);
                Point3D scaled = applyScaleTranslate(folded.x, folded.y, folded.z);
                x = scaled.x; y = scaled.y; z = scaled.z;
            } else if (transform_order == TRANSFORM_ORDER_SCALE_FOLD_ROT) {
                Point3D scaled = applyScaleTranslate(cur_x, cur_y, cur_z);
                Point3D folded = applyFolding(scaled.x, scaled.y, scaled.z);
                Point3D rotated = applyRotation(folded.x, folded.y, folded.z);
                x = rotated.x; y = rotated.y; z = rotated.z;
            }

            // Bailout Check
            double r_sq = x * x + y * y + z * z;
            if (r_sq > bailout_sq) {
                iter_color = (double) i / max_iter; // Store escape time color index
                escaped = true;
                break;
            }
        } // End of internal iteration loop

        if (!escaped) {
            iter_color = 1.0; // Assign full color if it didn't escape
        }

        // --- Apply Post-Loop Symmetry (Random Sign Flips) ---
        if (post_symmetry > POST_SYM_NONE) {
            double signX = (pContext.random() < 0.5) ? -1.0 : 1.0;
            double signY = (pContext.random() < 0.5) ? -1.0 : 1.0;
            double signZ = (pContext.random() < 0.5) ? -1.0 : 1.0;

            if ((post_symmetry & POST_SYM_X) != 0) { x *= signX; }
            if ((post_symmetry & POST_SYM_Y) != 0) { y *= signY; }
            if ((post_symmetry & POST_SYM_Z) != 0) { z *= signZ; }
        }
        // --- End Post-Loop Symmetry ---


        // --- Calculate Final Color based on color_mode --- NEW ---
        double color_value = 0.0; // Value to be assigned to pVarTP.color
        double temp_val = 0.0;    // Temporary value for calculations

        switch (color_mode) {
            case COLOR_MODE_FINAL_R: // Final Radius
                temp_val = Math.sqrt(x * x + y * y + z * z) * color_scale;
                color_value = temp_val - Math.floor(temp_val); // Fractional part
                break;
            case COLOR_MODE_FINAL_XY_ANGLE: // Final XY Angle
                temp_val = Math.atan2(y, x); // Range -PI to PI
                color_value = ((temp_val + Math.PI) / (2.0 * Math.PI)) * color_scale; // Map to 0..1 range and scale
                color_value = color_value - Math.floor(color_value); // Fractional part
                break;
            case COLOR_MODE_FINAL_X: // Final X
                temp_val = x * color_scale;
                color_value = temp_val - Math.floor(temp_val); // Fractional part
                break;
            case COLOR_MODE_FINAL_Y: // Final Y
                temp_val = y * color_scale;
                color_value = temp_val - Math.floor(temp_val); // Fractional part
                break;
            case COLOR_MODE_FINAL_Z: // Final Z
                temp_val = z * color_scale;
                color_value = temp_val - Math.floor(temp_val); // Fractional part
                break;
            case COLOR_MODE_ITER: // Iteration Count (Original) - Default
            default:
                 color_value = iter_color; // Use the escape time color
                 break;
        }
        // Ensure color value is reasonably within 0-1, although fract() should handle it.
        // color_value = Math.max(0.0, Math.min(1.0, color_value)); // Optional clamping

        // --- End Color Calculation ---


        // Apply the variation amount (pAmount) to the final coordinates
        double finalX = x * pAmount;
        double finalY = y * pAmount;
        double finalZ = z * pAmount;

        // Write the final coordinates and calculated color index back to pVarTP
        pVarTP.x = finalX;
        pVarTP.y = finalY;
        pVarTP.z = finalZ;
        // Set the color index - ASSUMES pVarTP.color field exists and is assignable!
        pVarTP.color = color_value;
    }

    // --- Helper Methods for Transformations ---

    // Internal class to return multiple values
    private static class Point3D {
        double x, y, z;
        Point3D(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
    }

    // applyFolding
    private Point3D applyFolding(double x, double y, double z) {
        switch (fold_type) {
            case FOLD_TYPE_ABS_XYZ: return new Point3D(Math.abs(x), Math.abs(y), Math.abs(z));
            case FOLD_TYPE_DIAG_XY_POS:
                if (x + y < 0.0) { return new Point3D(-y, -x, z); }
                return new Point3D(x, y, z);
            case FOLD_TYPE_CUSTOM1:
                if (fold_norm1_len_sq > 1e-9) {
                    double dot_p_n = x * fold_plane1_nx + y * fold_plane1_ny + z * fold_plane1_nz;
                    if (dot_p_n < 0.0) {
                        double scale_factor = 2.0 * dot_p_n / fold_norm1_len_sq;
                        return new Point3D(x - scale_factor * fold_plane1_nx,
                                           y - scale_factor * fold_plane1_ny,
                                           z - scale_factor * fold_plane1_nz);
                    }
                } return new Point3D(x, y, z);
            case FOLD_TYPE_NONE: default: return new Point3D(x, y, z);
        }
    }

    // applyRotation
    private Point3D applyRotation(double x, double y, double z) {
        if (rot_x_rad == 0.0 && rot_y_rad == 0.0 && rot_z_rad == 0.0) {
             return new Point3D(x, y, z);
        }

        double cosX = Math.cos(rot_x_rad); double sinX = Math.sin(rot_x_rad);
        double cosY = Math.cos(rot_y_rad); double sinY = Math.sin(rot_y_rad);
        double cosZ = Math.cos(rot_z_rad); double sinZ = Math.sin(rot_z_rad);
        double rx = x, ry = y, rz = z; double tx, ty, tz;
        if (rot_order == ROT_ORDER_XYZ) {
            tx = rx * cosZ - ry * sinZ; ty = rx * sinZ + ry * cosZ; rx = tx; ry = ty;
            tx = rx * cosY + rz * sinY; tz = -rx * sinY + rz * cosY; rx = tx; rz = tz;
            ty = ry * cosX - rz * sinX; tz = ry * sinX + rz * cosX; ry = ty; rz = tz;
        } else if (rot_order == ROT_ORDER_ZYX) {
            ty = ry * cosX - rz * sinX; tz = ry * sinX + rz * cosX; ry = ty; rz = tz;
            tx = rx * cosY + rz * sinY; tz = -rx * sinY + rz * cosY; rx = tx; rz = tz;
            tx = rx * cosZ - ry * sinZ; ty = rx * sinZ + ry * cosZ; rx = tx; ry = ty;
        }
        return new Point3D(rx, ry, rz);
    }

    // applyScaleTranslate - REVERTED to Centered Scale then Offset
    private Point3D applyScaleTranslate(double x, double y, double z) {
        // Apply scaling relative to the center point using per-axis factors
        double sx = kifs_scale_x * (x - center_x) + center_x; // Uses center_x
        double sy = kifs_scale_y * (y - center_y) + center_y; // Uses center_y
        double sz = kifs_scale_z * (z - center_z) + center_z; // Uses center_z

        // Apply translation/offset AFTER centered scaling
        sx += offset_x;
        sy += offset_y;
        sz += offset_z;

        return new Point3D(sx, sy, sz);
    }

    // --- Variation Naming & Type ---

    @Override
    public String getName() {
        return "kifs3d"; // Name in JWildfire UI
    }

    @Override
    public VariationFuncType[] getVariationTypes() {
        return new VariationFuncType[]{VariationFuncType.VARTYPE_3D};
    }
}