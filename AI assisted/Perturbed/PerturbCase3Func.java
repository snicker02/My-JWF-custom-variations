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
import static org.jwildfire.base.mathlib.MathLib.*; // sqrt, sin, cos, pow, fabs

public class PerturbCase3Func extends VariationFunc {
    private static final long serialVersionUID = 1L;

    // Parameters
    private static final String PARAM_POWX = "powX";
    private static final String PARAM_POWY = "powY";
    private static final String PARAM_POWZ = "powZ";
    private static final String PARAM_PERTURB_AMOUNT = "perturbAmount";
    private static final String PARAM_EFFECT = "effect"; // Renamed from PARAM_SWIRL_FACTOR, was case3
    private static final String[] paramNames = { PARAM_POWX, PARAM_POWY, PARAM_POWZ, PARAM_PERTURB_AMOUNT, PARAM_EFFECT };

    // Member Variables & Defaults
    private double powX = 1.0;
    private double powY = 1.0;
    private double powZ = 1.0;
    private double perturbAmount = 1.0;
    private double effect = 2.0; // Renamed from swirlFactor, Default for case3

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double px = pAffineTP.x;
        double py = pAffineTP.y;
        double pz = pAffineTP.z;

        // Base linear power transformation
        double xBase = sgn(px) * pow(fabs(px), this.powX);
        double yBase = sgn(py) * pow(fabs(py), this.powY);
        double zBase = sgn(pz) * pow(fabs(pz), this.powZ);

        // Perturbation (Swirl XY)
        double perturbationX = 0.0;
        double perturbationY = 0.0;
        double perturbationZ = 0.0; // No Z perturbation

        double radius = sqrt(px * px + py * py);
        // Original used 0.1 multiplier, let's keep it consistent with perturbAmount
        // Using renamed variable 'effect' here
        perturbationX = sin(radius * effect) * py * perturbAmount;
        perturbationY = cos(radius * effect) * px * perturbAmount;

        // Combine and apply amount
        pVarTP.x += (xBase + perturbationX) * pAmount;
        pVarTP.y += (yBase + perturbationY) * pAmount;
        pVarTP.z += (zBase + perturbationZ) * pAmount; // zBase only
    }

    @Override
    public String[] getParameterNames() { return paramNames; } // Uses renamed PARAM_EFFECT in array

    @Override
    public Object[] getParameterValues() { return new Object[] { powX, powY, powZ, perturbAmount, effect }; } // Uses renamed variable 'effect'

    @Override
    public String[] getParameterAlternativeNames() {
         // Updated alternative name string literal
         return new String[]{ "lT_powX", "lT_powY", "lT_powZ", "Perturb Strength", "Effect"};
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_POWX.equalsIgnoreCase(pName)) powX = pValue;
        else if (PARAM_POWY.equalsIgnoreCase(pName)) powY = pValue;
        else if (PARAM_POWZ.equalsIgnoreCase(pName)) powZ = pValue;
        else if (PARAM_PERTURB_AMOUNT.equalsIgnoreCase(pName)) perturbAmount = pValue;
        // Check against renamed PARAM_EFFECT and assign to renamed variable 'effect'
        else if (PARAM_EFFECT.equalsIgnoreCase(pName)) effect = pValue;
        else throw new IllegalArgumentException("Unknown parameter: " + pName);
    }

    @Override
    public String getName() { return "perturbCase3_Swirl"; } // Name of function remains the same

    private double sgn(double arg) { return (arg >= 0) ? 1.0 : -1.0; }
}