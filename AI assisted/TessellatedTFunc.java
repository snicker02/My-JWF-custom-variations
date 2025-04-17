/*
 * JWildfire - an image and animation processor written in Java
 * Copyright (C) 1995-2025 Andreas Maschke
 *
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details *
 * You should have received a copy of the GNU Lesser General Public License along with this software;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jwildfire.create.tina.variation;

// Import standard Math explicitly for max(int, int) to avoid ambiguity
import java.lang.Math;

import org.jwildfire.base.mathlib.MathLib; // Ensure base MathLib is imported
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

// Import MathLib static functions, assuming most exist
import static org.jwildfire.base.mathlib.MathLib.*;

/**
 * TessellatedTFunc Variation for JWildfire.
 * Creates various repeating patterns (tessellations) by applying different
 * transformations within grid cells defined by the 'scale' parameter.
 * Supports Square, Hex Offset, True Hexagonal, and Radial tiling modes.
 * Includes multiple distortion types, intra-cell symmetry (including D4 and Dn folding),
 * edge blending (for square/offset modes), grid rotation, and cell rotation.
 *
 * @param mode            Selects tiling & distortion: 0=SqPower, 1=SqSine, 2=HexOffsetPower, 3=SqRadialDistort,
 * 4=TrueHexPower, 5=RadialTiling+RadialDistort, 6=Exp/Log Radial, 7=Tangent Trig,
 * 8=Polynomial Quad Scale, 9=Julia-like.
 * @param scale           Controls cell size/density.
 * @param distortX        Primary distortion parameter (meaning depends on mode).
 * @param distortY        Secondary distortion parameter (meaning depends on mode).
 * @param symmetry        Intra-cell symmetry: 0=None, 1=X-Refl(absY), 2=Y-Refl(absX), 3=Quadrant(absXY), 4=D4(Quad+Diag Fold), 5=N-Fold Polar (Dn).
 * @param fold_n          Number of folds for symmetry=5 (Dn). Default 6. (e.g., 3=D3, 4=D4, 5=D5, 6=D6).
 * @param edge_blend      Smooth attenuation near cell edges (0-0.5). Active for modes using square base cells (0,1,3,6,7,8,9).
 * @param grid_rotate     Rotates the entire grid (degrees).
 * @param cell_rotate     Primary rotation angle for shapes within cells (degrees).
 * @param cell_rotate2    Secondary rotation angle for alternating patterns (degrees).
 * @param rotate_pattern  Cell rotation pattern: 0=Uniform, 1=Checkerboard, 2=Per Row/Ring, 3=Per Col/Sector.
 * @param num_sectors     Number of angular sectors for Mode 5 (Radial Tiling). Default 8.
 * @param julia_iterations Number of iterations for Mode 9 (Julia-like). Default 4.
 */
public class TessellatedTFunc extends VariationFunc implements SupportsGPU {

    // Increment serialVersionUID when members/methods change
    private static final long serialVersionUID = 11L; // Added fold_n and symmetry mode 5

    // Parameter names
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_SCALE = "scale";
    private static final String PARAM_DISTORT_X = "distortX";
    private static final String PARAM_DISTORT_Y = "distortY";
    private static final String PARAM_SYMMETRY = "symmetry";
    private static final String PARAM_FOLD_N = "fold_n"; // New
    private static final String PARAM_EDGE_BLEND = "edge_blend";
    private static final String PARAM_GRID_ROTATE = "grid_rotate";
    private static final String PARAM_CELL_ROTATE = "cell_rotate";
    private static final String PARAM_CELL_ROTATE2 = "cell_rotate2";
    private static final String PARAM_ROTATE_PATTERN = "rotate_pattern";
    private static final String PARAM_NUM_SECTORS = "num_sectors";
    private static final String PARAM_JULIA_ITERATIONS = "julia_iterations";

    private static final String[] paramNames = {
            PARAM_MODE, PARAM_SCALE, PARAM_DISTORT_X, PARAM_DISTORT_Y, PARAM_SYMMETRY, PARAM_FOLD_N, // Added fold_n
            PARAM_EDGE_BLEND, PARAM_GRID_ROTATE, PARAM_CELL_ROTATE, PARAM_CELL_ROTATE2,
            PARAM_ROTATE_PATTERN, PARAM_NUM_SECTORS, PARAM_JULIA_ITERATIONS
    };

    // Parameter default values
    private int mode = 0;
    private double scale = 2.0;
    private double distortX = 1.5;
    private double distortY = 1.5;
    private int symmetry = 0; // Now includes mode 5 for Dn
    private int fold_n = 6; // Default N for Dn symmetry (D6)
    private double edge_blend = 0.0;
    private double grid_rotate = 0.0;    // degrees
    private double cell_rotate = 0.0;    // degrees
    private double cell_rotate2 = 0.0;   // degrees
    private int rotate_pattern = 0;
    private int num_sectors = 8; // Default sectors for radial mode
    private int julia_iterations = 4; // Default iterations for Julia mode

    // --- Constants for Hex Grid Math ---
    private static final double SQRT3 = sqrt(3.0);
    private static final double HEX_F0 = SQRT3;
    private static final double HEX_F1 = SQRT3 / 2.0;
    private static final double HEX_F3 = 3.0 / 2.0;
    private static final double HEX_B0 = SQRT3 / 3.0;
    private static final double HEX_B1 = -1.0 / 3.0;
    private static final double HEX_B2 = 0.0;
    private static final double HEX_B3 = 2.0 / 3.0;

    // --- Helper Functions ---
    private double sgn(double arg) { if (arg > 0.0) return 1.0; if (arg < 0.0) return -1.0; return 0.0; }
    private double smoothstep(double edge0, double edge1, double x) { if (edge1 <= edge0) { return (x < edge0) ? 0.0 : 1.0; } double t = max(0.0, min((x - edge0) / (edge1 - edge0), 1.0)); return t * t * (3.0 - 2.0 * t); }
    private boolean isEvenLong(long val) { return (val % 2 == 0); }
    // Assuming MathLib.round(double) exists via static import.

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double dx = 0.0, dy = 0.0;
        double blended_dx, blended_dy;
        double final_dx, final_dy;
        double base_fx = 0.0, base_fy = 0.0;
        long ix_for_pattern = 0, iy_for_pattern = 0;
        boolean allow_edge_blend = false;

        // --- Apply Grid Rotation ---
        if (this.grid_rotate != 0.0) {
            double grid_angle_rad = this.grid_rotate * M_PI / 180.0;
            double cos_g = cos(grid_angle_rad); double sin_g = sin(grid_angle_rad);
            double x0 = x; double y0 = y;
            x = x0 * cos_g - y0 * sin_g; y = x0 * sin_g + y0 * cos_g;
        }

        // --- Calculate Base Coordinates & Cell Indices based on Mode ---
        switch (mode) {
            case 0: case 1: case 3: case 6: case 7: case 8: case 9: // Square Grid Modes
                 allow_edge_blend = true;
                 double scaledX_sq = x * scale; double scaledY_sq = y * scale;
                 ix_for_pattern = (long)floor(scaledX_sq); iy_for_pattern = (long)floor(scaledY_sq);
                 base_fx = scaledX_sq - ix_for_pattern - 0.5; base_fy = scaledY_sq - iy_for_pattern - 0.5;
                 break;
            case 2: // Hex Offset Grid Power
                allow_edge_blend = true;
                double scaledX2 = x * scale; double scaledY2 = y * scale;
                double iy_hex_offset = floor(scaledY2);
                double offset_hex = !isEvenLong((long)iy_hex_offset) ? 0.5 : 0.0;
                double scaledX_hex_offset = scaledX2 + offset_hex;
                ix_for_pattern = (long)floor(scaledX_hex_offset); iy_for_pattern = (long)iy_hex_offset;
                base_fx = scaledX_hex_offset - ix_for_pattern - 0.5; base_fy = scaledY2 - iy_for_pattern - 0.5;
                break;
            case 4: // True Hexagonal Grid
                allow_edge_blend = false;
                double hex_size = (scale == 0.0) ? 1.0 : 1.0 / fabs(scale);
                double q = (HEX_B0 * x + HEX_B1 * y) / hex_size; double r_axial = (HEX_B2 * x + HEX_B3 * y) / hex_size;
                double cx = q; double cz = r_axial; double cy = -cx - cz;
                double rx = round(cx); double ry = round(cy); double rz = round(cz); // Assumes round exists
                double cx_diff = fabs(rx - cx); double cy_diff = fabs(ry - cy); double cz_diff = fabs(rz - cz);
                if (cx_diff > cy_diff && cx_diff > cz_diff) { rx = -ry - rz; } else if (cy_diff > cz_diff) { ry = -rx - rz; } else { rz = -rx - ry; }
                ix_for_pattern = (long)rx; iy_for_pattern = (long)rz;
                double center_q = rx; double center_r = rz;
                double frac_q = q - center_q; double frac_r = r_axial - center_r;
                base_fx = hex_size * (HEX_F0 * frac_q + HEX_F1 * frac_r); base_fy = hex_size * (0.0 + HEX_F3 * frac_r);
                break;
            case 5: // Radial Tiling
                allow_edge_blend = false;
                int sectors = (num_sectors <= 0) ? 1 : num_sectors;
                double r_polar = sqrt(x*x + y*y); double a_polar = atan2(y, x);
                double scaled_r = scale * r_polar; ix_for_pattern = (long)floor(scaled_r);
                double norm_a = (a_polar + M_PI) / M_2PI; iy_for_pattern = (long)floor(norm_a * sectors); iy_for_pattern = (iy_for_pattern % sectors + sectors) % sectors;
                base_fx = (scaled_r - ix_for_pattern) - 0.5; base_fy = (norm_a * sectors - floor(norm_a * sectors)) - 0.5;
                break;
            default: // Fallback
                allow_edge_blend = true;
                double scaledXd = x * scale; double scaledYd = y * scale;
                ix_for_pattern = (long)floor(scaledXd); iy_for_pattern = (long)floor(scaledYd);
                base_fx = scaledXd - ix_for_pattern - 0.5; base_fy = scaledYd - iy_for_pattern - 0.5;
                break;
        }

        // --- Apply Base Symmetry Transformation ---
        double sym_fx = base_fx; double sym_fy = base_fy;
        switch (symmetry) {
            // case 0: break; // None
            case 1: sym_fy = fabs(base_fy); break; // X-Axis Reflection (abs(y))
            case 2: sym_fx = fabs(base_fx); break; // Y-Axis Reflection (abs(x))
            case 3: sym_fx = fabs(base_fx); sym_fy = fabs(base_fy); break; // Quadrant Symmetry (abs(x), abs(y))
            case 4: // D4 Symmetry (Original implementation: Quadrant + Diagonal Fold)
                double ax4 = fabs(base_fx); double ay4 = fabs(base_fy);
                sym_fx = min(ax4, ay4);
                sym_fy = max(ax4, ay4);
                break;
            case 5: // N-Fold Dihedral Symmetry (Dn)
                int N = Math.max(1, this.fold_n); // Use fold_n param, ensure N >= 1
                if (N > 1) {
                    // Use base_fx, base_fy directly for folding around the origin
                    if (fabs(base_fx) > 1e-9 || fabs(base_fy) > 1e-9) { // Avoid atan2(0,0)
                        double r = sqrt(base_fx * base_fx + base_fy * base_fy);
                        double a = atan2(base_fy, base_fx);
                        if (a < 0) a += M_2PI; // Angle -> [0, 2*PI)

                        double wedge_angle = M_PI / (double)N; // Fundamental wedge angle for Dn is PI/N

                        // Map angle to [0, 2 * wedge_angle)
                        double two_wedge = 2.0 * wedge_angle;
                        // Use manual fmod for potentially better portability/precision?
                        // double a_mod = a % two_wedge;
                        double a_mod = a - floor(a / two_wedge) * two_wedge;


                        // Fold into the fundamental wedge [0, wedge_angle)
                        double a_folded = (a_mod > wedge_angle) ? (two_wedge - a_mod) : a_mod;

                        // Convert back - this becomes the new sym_fx, sym_fy
                        sym_fx = r * cos(a_folded);
                        sym_fy = r * sin(a_folded);
                    } else {
                        // Point at origin remains at origin
                        sym_fx = 0.0;
                        sym_fy = 0.0;
                    }
                }
                // If N=1, sym_fx/fy remain base_fx/fy (no change necessary)
                break;
        } // End Symmetry Switch

        // --- Apply Distortion ---
        // Operates on the result of the symmetry switch (sym_fx, sym_fy)
        switch (mode) {
            case 0: case 2: case 4: default: // Power Distortions & Fallback
                // Use sign from base coords, magnitude from symmetrized coords
                dx = sgn(base_fx) * pow(fabs(sym_fx), this.distortX);
                dy = sgn(base_fy) * pow(fabs(sym_fy), this.distortY);
                break;
            case 1: // Sinusoidal Distortion
                dx = sym_fx + (this.distortX - 1.0) * sin(sym_fy * M_2PI) * 0.1;
                dy = sym_fy + (this.distortY - 1.0) * sin(sym_fx * M_2PI) * 0.1;
                break;
            case 3: case 5: // Radial Distortions (Sq Cell or Polar Cell)
                double r35 = sqrt(sym_fx * sym_fx + sym_fy * sym_fy); double a35 = atan2(sym_fy, sym_fx);
                double r_distorted35 = pow(r35, this.distortX); double a_distorted35 = a35 + (this.distortY - 1.0) * r35 * 2.0;
                dx = r_distorted35 * cos(a_distorted35); dy = r_distorted35 * sin(a_distorted35);
                break;
            case 6: // Exponential/Logarithmic Radial Distortion
                double r6 = sqrt(sym_fx * sym_fx + sym_fy * sym_fy); double a6 = atan2(sym_fy, sym_fx);
                double r_new6 = r6 * exp(this.distortX * (r6 - this.distortY));
                dx = r_new6 * cos(a6); dy = r_new6 * sin(a6);
                break;
            case 7: // Tangent Trigonometric Distortion
                double tan_arg_x = sym_fx * M_PI_2 * 0.999; double tan_arg_y = sym_fy * M_PI_2 * 0.999;
                dx = sym_fx + this.distortX * tan(tan_arg_y); dy = sym_fy + this.distortY * tan(tan_arg_x);
                break;
            case 8: // Polynomial Distortion (Simple Quadratic Scaling)
                double scale_factor8 = 1.0 + this.distortX * sym_fx + this.distortY * sym_fy;
                dx = sym_fx * scale_factor8; dy = sym_fy * scale_factor8;
                break;
            case 9: // Julia-like Calculation
                double zx = sym_fx; double zy = sym_fy;
                double cx = this.distortX; double cy = this.distortY;
                int iters = Math.max(1, this.julia_iterations); // Use standard Math.max
                for (int i = 0; i < iters; i++) {
                    double zx_new = zx * zx - zy * zy + cx; double zy_new = 2.0 * zx * zy + cy;
                    zx = zx_new; zy = zy_new;
                }
                dx = zx; dy = zy;
                break;
        }

        // --- Apply Edge Blending ---
        if (allow_edge_blend && this.edge_blend > 0.0) {
            double clamped_edge_blend = max(0.0, min(this.edge_blend, 0.5));
            double dist_from_center = max(fabs(base_fx), fabs(base_fy));
            double edge0 = 0.5 - clamped_edge_blend; double edge1 = 0.5;
            double blend_factor = 1.0 - smoothstep(edge0, edge1, dist_from_center);
            blended_dx = base_fx + (dx - base_fx) * blend_factor; blended_dy = base_fy + (dy - base_fy) * blend_factor;
        } else {
            blended_dx = dx; blended_dy = dy;
        }

        // --- Apply Cell Rotation ---
        double current_cell_rotate = this.cell_rotate;
        if (this.rotate_pattern > 0) {
            boolean ix_is_even = isEvenLong(ix_for_pattern); boolean iy_is_even = isEvenLong(iy_for_pattern);
            switch (this.rotate_pattern) {
                case 1: if (ix_is_even ^ iy_is_even) { current_cell_rotate = this.cell_rotate2; } break;
                case 2: if (!iy_is_even) { current_cell_rotate = this.cell_rotate2; } break;
                case 3: if (!ix_is_even) { current_cell_rotate = this.cell_rotate2; } break;
            }
        }
        if (current_cell_rotate != 0.0) {
            double cell_angle_rad = current_cell_rotate * M_PI / 180.0;
            double cos_c = cos(cell_angle_rad); double sin_c = sin(cell_angle_rad);
            final_dx = blended_dx * cos_c - blended_dy * sin_c; final_dy = blended_dx * sin_c + blended_dy * cos_c;
        } else {
            final_dx = blended_dx; final_dy = blended_dy;
        }

        // --- Apply Final Transformation ---
        pVarTP.x += final_dx * pAmount;
        pVarTP.y += final_dy * pAmount;
        if (pContext.isPreserveZCoordinate()) {
            pVarTP.z += pAmount * pAffineTP.z;
        }
    } // End transform()


    @Override 
	public String[] getParameterNames() { return paramNames; }

    @Override
	public Object[] getParameterValues() {
        return new Object[]{
                mode, scale, distortX, distortY, symmetry, fold_n, // Added fold_n
                edge_blend, grid_rotate, cell_rotate, cell_rotate2,
                rotate_pattern, num_sectors, julia_iterations
        };
    }

    @Override
	public void setParameter(String pName, double pValue) {
        if (PARAM_MODE.equalsIgnoreCase(pName)) { mode = (int) pValue; }
        else if (PARAM_SCALE.equalsIgnoreCase(pName)) { scale = pValue; }
        else if (PARAM_DISTORT_X.equalsIgnoreCase(pName)) { distortX = pValue; }
        else if (PARAM_DISTORT_Y.equalsIgnoreCase(pName)) { distortY = pValue; }
        else if (PARAM_SYMMETRY.equalsIgnoreCase(pName)) { symmetry = (int) pValue; }
        else if (PARAM_FOLD_N.equalsIgnoreCase(pName)) { fold_n = (int) Math.max(1.0, pValue); } // Added fold_n
        else if (PARAM_EDGE_BLEND.equalsIgnoreCase(pName)) { edge_blend = pValue; }
        else if (PARAM_GRID_ROTATE.equalsIgnoreCase(pName)) { grid_rotate = pValue; }
        else if (PARAM_CELL_ROTATE.equalsIgnoreCase(pName)) { cell_rotate = pValue; }
        else if (PARAM_CELL_ROTATE2.equalsIgnoreCase(pName)) { cell_rotate2 = pValue; }
        else if (PARAM_ROTATE_PATTERN.equalsIgnoreCase(pName)) { rotate_pattern = (int) pValue; }
        else if (PARAM_NUM_SECTORS.equalsIgnoreCase(pName)) { num_sectors = (int) Math.max(1.0, pValue); }
        else if (PARAM_JULIA_ITERATIONS.equalsIgnoreCase(pName)) { julia_iterations = (int) Math.max(1.0, pValue); }
        else { throw new IllegalArgumentException("Parameter `" + pName + "` is not valid for `" + getName() + "`"); }
    }

    @Override 
	public String[] getParameterAlternativeNames() {
        return new String[]{
                "tess_mode", "tess_scale", "tess_distortX", "tess_distortY", "tess_symmetry", "tess_fold_n", // Added fold_n
                "tess_edge_blend", "tess_grid_rotate", "tess_cell_rotate", "tess_cell_rotate2",
                "tess_rotate_pattern", "tess_num_sectors", "tess_julia_iters"
        };
    }

    @Override
	public String getName() { return "tessellatedT"; }

} // End class