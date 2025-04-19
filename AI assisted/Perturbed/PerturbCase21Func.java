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


// Using basic multiplication, no specific MathLib imports needed beyond the standard structure.
// If sign preservation like sgn(x)*x*x was desired, MathLib.sgn would be needed.

/**
 * Quadratic Perturbation Variation for JWildfire.
 * Perturbs each coordinate based on the square of its own value,
 * scaled by corresponding effect parameters (effectX, effectY, effectZ).
 * perturbation_x = effectX * x^2
 * perturbation_y = effectY * y^2
 * perturbation_z = effectZ * z^2
 * Corresponds to the concept described as "Case 21 (Polynomial)".
 */
public class PerturbCase21Func extends VariationFunc {

    private static final long serialVersionUID = 1L;

    // --- Parameter Definitions ---
    private static final String PARAM_EFFECT_X = "effectX";
    private static final String PARAM_EFFECT_Y = "effectY";
    private static final String PARAM_EFFECT_Z = "effectZ";

    // --- Parameter Name Array ---
    private static final String[] paramNames = { PARAM_EFFECT_X, PARAM_EFFECT_Y, PARAM_EFFECT_Z };

    // --- Member Variables & Defaults ---
    private double effectX = 0.05; // Default coefficient for X perturbation
    private double effectY = 0.05; // Default coefficient for Y perturbation
    private double effectZ = 0.05; // Default coefficient for Z perturbation

    /**
     * Standard JWildfire transformation method.
     * @param pContext      The transformation context.
     * @param pXForm        The current transformation.
     * KpAffineTP     The input point (after affine transformation).
     * @param pVarTP        The output point (initially zero, accumulates variation results).
     * @param pAmount       The variation amount ('variables' slider value).
     */
    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {

        double px = pAffineTP.x;
        double py = pAffineTP.y;
        double pz = pAffineTP.z;

        // Calculate perturbation based on the square of the coordinate
        // multiplied by the effect parameter.
        // Note: px*px is always non-negative. This implements the description literally.
        // If you wanted the perturbation to push away from the axis (preserving sign),
        // you might use something like: effectX * px * MathLib.fabs(px)
        double perturbationX = effectX * px * px;
        double perturbationY = effectY * py * py;
        double perturbationZ = effectZ * pz * pz;

        // Add the calculated perturbation to the output point pVarTP.
        // The result is scaled by the variation's overall amount (pAmount)
        // for blending with other variations/transforms.
        pVarTP.x += perturbationX * pAmount;
        pVarTP.y += perturbationY * pAmount;
        pVarTP.z += perturbationZ * pAmount;
    }

    // --- Standard Variation Methods ---

    @Override
    public String getName() {
        // Name that will appear in the JWildfire UI
        return "aibsperturbCase22_quadratic";
    }

    @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    @Override
    public Object[] getParameterValues() {
        // Return the current values of the parameters
        return new Object[]{ effectX, effectY, effectZ };
    }

    @Override
    public String[] getParameterAlternativeNames() {
        // User-friendly names for the parameters in the UI
        return new String[]{ "Effect X", "Effect Y", "Effect Z" };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        // Set the parameter values from the UI
        if (PARAM_EFFECT_X.equalsIgnoreCase(pName)) {
            effectX = pValue;
        } else if (PARAM_EFFECT_Y.equalsIgnoreCase(pName)) {
            effectY = pValue;
        } else if (PARAM_EFFECT_Z.equalsIgnoreCase(pName)) {
            effectZ = pValue;
        } else {
            // Handle unknown parameters
            throw new IllegalArgumentException("Unknown parameter in " + getName() + ": " + pName);
        }
    }
}
