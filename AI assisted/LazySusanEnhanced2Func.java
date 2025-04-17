/*
 * JWildfire - an image and animation processor written in Java
 * Copyright (C) 1995-2025 Andreas Maschke // Year range covers current date: April 12, 2025
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

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

// Reverted class name back to the version before quadrant boost was added
public class LazySusanEnhanced2Func extends VariationFunc implements SupportsGPU {
    // Reverted serialVersionUID back to match the state before quadrant boost
    private static final long serialVersionUID = 4L;

    // Original Parameters
    private static final String PARAM_SPACE = "space";
    private static final String PARAM_TWIST = "twist";
    private static final String PARAM_SPIN = "spin";
    private static final String PARAM_X = "x";
    private static final String PARAM_Y = "y";

    // Kept Enhancement Parameters (Angle Offset and Radius Power)
    private static final String PARAM_ANGLE_OFFSET_FACTOR = "angle_offset";
    private static final String PARAM_RADIUS_POWER = "radius_power";
    // PARAM_BOOST_QUADRANT and PARAM_QUADRANT_BOOST removed

    // Updated parameter list (Quadrant parameters removed)
    private static final String[] paramNames = {
            PARAM_SPACE, PARAM_TWIST, PARAM_SPIN, PARAM_X, PARAM_Y,
            PARAM_ANGLE_OFFSET_FACTOR, PARAM_RADIUS_POWER
            // PARAM_BOOST_QUADRANT, PARAM_QUADRANT_BOOST removed
    };

    // Original Parameter Defaults
    private double space = 0.40;
    private double twist = 0.20;
    private double spin = 0.10;
    private double x = 0.10;
    private double y = 0.20;

    // Kept Enhancement Defaults
    private double angle_offset_factor = 0.0;
    private double radius_power = 1.0;
    // boost_quadrant and quadrant_boost fields removed

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        /* Lazysusan variation with angle offset and non-linear radius power (Quadrant boost removed) */

        double xx = pAffineTP.x - x;
        double yy = pAffineTP.y + y;
        double rr = sqrt(xx * xx + yy * yy);

        double finalX = 0.0;
        double finalY = 0.0;

        if (rr < pAmount) { // Inside the threshold radius
            // Quadrant boost logic removed

            // Reverted angle calculation to use angle_offset_factor directly
            double a = atan2(yy, xx) + spin + twist * (pAmount - rr) + angle_offset_factor * rr;

            double sina = sin(a);
            double cosa = cos(a);

            // Apply non-linear radius scaling using radius_power (this effect remains)
            double rr_powered = pow(rr, radius_power);
            double rr_scaled = pAmount * rr_powered;

            finalX = rr_scaled * cosa + x;
            finalY = rr_scaled * sina - y;

        } else { // Outside the threshold radius
            // Original scaling logic for outside region
            double rr_scaled = pAmount * (1.0 + space / rr);

            finalX = rr_scaled * xx + x;
            finalY = rr_scaled * yy - y;
        }

        // Accumulate results
        pVarTP.x += finalX;
        pVarTP.y += finalY;

        // Handle Z coordinate (original LazySusan behavior)
        if (pContext.isPreserveZCoordinate()) {
            pVarTP.z += pAmount * pAffineTP.z;
        }
    }

    @Override
    public String[] getParameterNames() {
        return paramNames; // Reflects removal of quadrant parameters
    }

    @Override
    public Object[] getParameterValues() {
        // Reflects removal of quadrant parameters
        return new Object[]{
                space, twist, spin, x, y,
                angle_offset_factor, radius_power
        };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_SPACE.equalsIgnoreCase(pName))
            space = pValue;
        else if (PARAM_TWIST.equalsIgnoreCase(pName))
            twist = pValue;
        else if (PARAM_SPIN.equalsIgnoreCase(pName))
            spin = pValue;
        else if (PARAM_X.equalsIgnoreCase(pName))
            x = pValue;
        else if (PARAM_Y.equalsIgnoreCase(pName))
            y = pValue;
        else if (PARAM_ANGLE_OFFSET_FACTOR.equalsIgnoreCase(pName))
            angle_offset_factor = pValue;
        else if (PARAM_RADIUS_POWER.equalsIgnoreCase(pName))
            radius_power = pValue;
        // Cases for PARAM_BOOST_QUADRANT and PARAM_QUADRANT_BOOST removed
        else
            throw new IllegalArgumentException("Unknown parameter: " + pName);
    }

    @Override
    public String getName() {
        // Reverted name back to the version before quadrant boost
        return "lazySusan_enhanced2";
    }
}