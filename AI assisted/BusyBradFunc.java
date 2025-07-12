/*
 * JWildfire - an image and animation processor written in Java
 * Copyright (C) 1995-2021 Andreas Maschke
 *
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jwildfire.create.tina.variation;

import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class BusyBradFunc extends VariationFunc {
    private static final long serialVersionUID = 1L;

    // --- Parameters ---
    private static final String PARAM_MODE = "mode"; // 0=Susan, 1=Jess, 2=Combined
    private static final String PARAM_GRID_SIZE = "gridSize";

    // Base Transformation Parameters
    private static final String PARAM_X_OFFSET = "xOffset";
    private static final String PARAM_Y_OFFSET = "yOffset";
    private static final String PARAM_SPIN = "spin";
    private static final String PARAM_TWIST = "twist";
    private static final String PARAM_SPACE = "space";
    private static final String PARAM_N = "n";
    private static final String PARAM_CORNER = "corner";

    // Modulation Parameters (for Mode 2)
    private static final String PARAM_MOD_SPIN_STRENGTH = "mod_spin_strength";
    private static final String PARAM_MOD_TWIST_STRENGTH = "mod_twist_strength";

    // Sensen Post-Effect Parameters
    private static final String PARAM_SENSEN_POST_EFFECT = "sensen_post_effect";
    private static final String PARAM_SENSEN_FOLD = "sensen_fold";

    private static final String[] paramNames = {
            PARAM_MODE, PARAM_GRID_SIZE, PARAM_X_OFFSET, PARAM_Y_OFFSET,
            PARAM_SPIN, PARAM_TWIST, PARAM_SPACE, PARAM_N, PARAM_CORNER,
            PARAM_MOD_SPIN_STRENGTH, PARAM_MOD_TWIST_STRENGTH,
            PARAM_SENSEN_POST_EFFECT, PARAM_SENSEN_FOLD
    };

    // --- Variables ---
    private int mode = 2; // Default mode is now 2 (Combined)
    private double gridSize = 1.0;
    private double xOffset = 0.0;
    private double yOffset = 0.0;
    private double spin = 0.10;
    private double twist = 0.20;
    private double space = 0.40;
    private int n = 4;
    private int corner = 1;
    private double mod_spin_strength = 0.0;
    private double mod_twist_strength = 0.0;
    private int sensen_post_effect = 0;
    private double sensen_fold = 1.0;

    // Pre-calculated values for Jess/Combined modes
    private double vertex, sin_vertex, pie_slice, half_slice, corner_rotation;

    @Override
    public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
        // Initialization is needed for Jess and Combined modes
        if (mode == 1 || mode == 2) {
            if (n < 2) n = 2;
            vertex = M_PI * (n - 2) / (2.0 * n);
            sin_vertex = sin(vertex);
            pie_slice = M_2PI / n;
            half_slice = pie_slice / 2.0;
            corner_rotation = (corner - 1.0) * pie_slice;
        }
    }

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        // 1. Grid deconstruction using round() to get the cell CENTER
        double cellCenterX = (gridSize == 0.0) ? 0.0 : gridSize * round(pAffineTP.x / gridSize);
        double cellCenterY = (gridSize == 0.0) ? 0.0 : gridSize * round(pAffineTP.y / gridSize);

        // Coordinates relative to the cell center
        double x = pAffineTP.x - cellCenterX;
        double y = pAffineTP.y - cellCenterY;

        // Apply local offsets
        double local_x = x - xOffset;
        double local_y = y + yOffset;

        double transformedX = 0, transformedY = 0;

        // 2. Select and apply the base transformation
        if (mode == 0) { // ========== SUSAN MODE ==========
            double rr = sqrt(local_x * local_x + local_y * local_y);
            if (rr < pAmount) {
                double a = atan2(local_y, local_x) + spin + twist * (pAmount - rr);
                double r2 = pAmount * rr;
                transformedX = r2 * cos(a);
                transformedY = r2 * sin(a);
            } else {
                double r2 = (rr != 0) ? pAmount * (1.0 + space / rr) : 0;
                transformedX = r2 * local_x;
                transformedY = r2 * local_y;
            }
        } else if (mode == 1) { // ========== JESS MODE ==========
            double modulus = sqrt(local_x * local_x + local_y * local_y);
            double n_d = (double) (n == 0 ? 1 : n);
            double theta_check = atan2(local_y, local_x);
            double r_poly = pAmount * cos(M_PI / n_d) / cos(theta_check - M_2PI / n_d * floor(n_d * theta_check / M_2PI + 0.5));

            if (modulus < r_poly) {
                double twist_effect = (r_poly > 1e-9) ? twist * (r_poly - modulus) / r_poly : 0.0;
                double theta = atan2(local_y, local_x) + spin + twist_effect;
                transformedX = modulus * cos(theta);
                transformedY = modulus * sin(theta);
            } else {
                double new_modulus = (modulus != 0) ? pAmount * (1.0 + space / modulus) : 0;
                transformedX = new_modulus * local_x;
                transformedY = new_modulus * local_y;
            }
        } else { // ========== COMBINED MODE (Mode 2) ==========
            // Step A: Get a modulation value from the Susan logic
            double rr = sqrt(local_x * local_x + local_y * local_y);
            double modulation_value = (rr != 0) ? pAmount * (1.0 + space / rr) : 0;

            // Step B: Create dynamic parameters based on modulation
            double dynamic_spin = spin + (modulation_value * mod_spin_strength);
            double dynamic_twist = twist + (modulation_value * mod_twist_strength);

            // Step C: Run Jess logic with the dynamic parameters
            double modulus = rr; // Same as rr
            double n_d = (double) (n == 0 ? 1 : n);
            double theta_check = atan2(local_y, local_x);
            double r_poly = pAmount * cos(M_PI / n_d) / cos(theta_check - M_2PI / n_d * floor(n_d * theta_check / M_2PI + 0.5));

            if (modulus < r_poly) {
                double twist_effect = (r_poly > 1e-9) ? dynamic_twist * (r_poly - modulus) / r_poly : 0.0;
                double theta = atan2(local_y, local_x) + dynamic_spin + twist_effect;
                transformedX = modulus * cos(theta);
                transformedY = modulus * sin(theta);
            } else {
                transformedX = modulation_value * local_x; // Use the already calculated modulation value
                transformedY = modulation_value * local_y;
            }
        }

        // 3. Optionally apply Sensen folding as a final post-processing step
        if (sensen_post_effect == 1 && sensen_fold != 0.0) {
            // Fold X coordinate
            double nr_x = floor(transformedX * sensen_fold);
            if ((nr_x >= 0 && nr_x % 2 == 1) || (nr_x < 0 && nr_x % 2 == 0)) {
                transformedX = -transformedX;
            }
            // Fold Y coordinate
            double nr_y = floor(transformedY * sensen_fold);
            if ((nr_y >= 0 && nr_y % 2 == 1) || (nr_y < 0 && nr_y % 2 == 0)) {
                transformedY = -transformedY;
            }
        }

        // 4. Final placement in grid
        pVarTP.x += cellCenterX + transformedX - xOffset;
        pVarTP.y += cellCenterY + transformedY + yOffset;

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
        return new Object[]{mode, gridSize, xOffset, yOffset, spin, twist, space, n, corner,
                mod_spin_strength, mod_twist_strength, sensen_post_effect, sensen_fold};
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_MODE.equalsIgnoreCase(pName)) mode = Tools.FTOI(pValue);
        else if (PARAM_GRID_SIZE.equalsIgnoreCase(pName)) gridSize = pValue;
        else if (PARAM_X_OFFSET.equalsIgnoreCase(pName)) xOffset = pValue;
        else if (PARAM_Y_OFFSET.equalsIgnoreCase(pName)) yOffset = pValue;
        else if (PARAM_SPIN.equalsIgnoreCase(pName)) spin = pValue;
        else if (PARAM_TWIST.equalsIgnoreCase(pName)) twist = pValue;
        else if (PARAM_SPACE.equalsIgnoreCase(pName)) space = pValue;
        else if (PARAM_N.equalsIgnoreCase(pName)) n = Tools.FTOI(pValue);
        else if (PARAM_CORNER.equalsIgnoreCase(pName)) corner = Tools.FTOI(pValue);
        else if (PARAM_MOD_SPIN_STRENGTH.equalsIgnoreCase(pName)) mod_spin_strength = pValue;
        else if (PARAM_MOD_TWIST_STRENGTH.equalsIgnoreCase(pName)) mod_twist_strength = pValue;
        else if (PARAM_SENSEN_POST_EFFECT.equalsIgnoreCase(pName)) sensen_post_effect = Tools.FTOI(pValue);
        else if (PARAM_SENSEN_FOLD.equalsIgnoreCase(pName)) sensen_fold = pValue;
        else throw new IllegalArgumentException(pName);
    }

    @Override
    public String getName() {
        return "busybrad";
    }

    @Override
    public VariationFuncType[] getVariationTypes() {
        return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_SUPPORTED_BY_SWAN};
    }
}
