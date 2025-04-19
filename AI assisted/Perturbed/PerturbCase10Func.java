/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2021 Andreas Maschke

  This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
  General Public License as published by the Free Software Foundation; either version 2.1 of the 
  License, or (at your option) any later version.
 
  This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this software; 
  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jwildfire.create.tina.variation;

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;


// Import necessary MathLib functions (assuming atan2, sin, cos are available)
import static org.jwildfire.base.mathlib.MathLib.atan2;
import static org.jwildfire.base.mathlib.MathLib.sin;
import static org.jwildfire.base.mathlib.MathLib.cos;

/**
 * Tangential Perturbation Variation for JWildfire.
 * Calculates the angle of the input point in the XY plane and adds a perturbation
 * perpendicular to the line connecting the origin to the point (tangential).
 * The magnitude is controlled by the 'scale' parameter.
 * This is essentially the logic from 'Case 10' in the previous multi-perturb variation.
 */
public class PerturbCase10Func extends VariationFunc {

    private static final long serialVersionUID = 1L;

    // --- Parameter Definitions ---
    private static final String PARAM_SCALE = "scale";

    // --- Parameter Name Array ---
    private static final String[] paramNames = { PARAM_SCALE };

    // --- Member Variables & Defaults ---
    private double scale = 0.1; // Default scale factor, matches the original 'case 10' hardcoded value

    /**
     * Standard JWildfire transformation method.
     * @param pContext      The transformation context.
     * @param pXForm        The current transformation.
     * @param pAffineTP     The input point (after affine transformation).
     * @param pVarTP        The output point (initially zero, accumulates variation results).
     * @param pAmount       The variation amount ('variables' slider value).
     */
    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {

        double px = pAffineTP.x;
        double py = pAffineTP.y;
        // Z coordinate (pAffineTP.z) is ignored for this XY-plane perturbation

        double perturbationX = 0.0;
        double perturbationY = 0.0;
        // Perturbation Z is zero

        // Calculate the angle in the XY plane using atan2 for quadrant correctness
        double angleTan = atan2(py, px);

        // Calculate the perturbation vector components perpendicular to the radius vector.
        // (-sin(angle), cos(angle)) gives a unit vector tangential (counter-clockwise).
        // Scale this unit vector by the 'scale' parameter.
        perturbationX = -sin(angleTan) * scale;
        perturbationY = cos(angleTan) * scale;

        // Add the calculated perturbation to the output point pVarTP.
        // The result is scaled by the variation's overall amount (pAmount)
        // which allows blending this variation with others.
        pVarTP.x += perturbationX * pAmount;
        pVarTP.y += perturbationY * pAmount;
        // pVarTP.z remains unchanged as this perturbation only affects the XY plane.
    }

    // --- Standard Variation Methods ---

    @Override
    public String getName() {
        // Choose a descriptive name for the variation in the JWildfire UI
        return "aibaperturbCase10_tangential";
    }

    @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    @Override
    public Object[] getParameterValues() {
        // Return the current value of the 'scale' parameter
        return new Object[]{ scale };
    }

    @Override
    public String[] getParameterAlternativeNames() {
        // Provide user-friendly names for the parameters in the UI
        return new String[]{ "Scale" };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        // Set the 'scale' parameter value from the UI
        if (PARAM_SCALE.equalsIgnoreCase(pName)) {
            scale = pValue;
        } else {
            // Throw an error if an unknown parameter name is provided
            throw new IllegalArgumentException("Unknown parameter in " + getName() + ": " + pName);
        }
    }
}
