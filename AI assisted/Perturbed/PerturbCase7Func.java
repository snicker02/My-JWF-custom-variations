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
import static org.jwildfire.base.mathlib.MathLib.*; // atan2, sqrt, sin, cos, pow, fabs

public class PerturbCase7Func extends VariationFunc {
    private static final long serialVersionUID = 1L;

    // Parameters
    private static final String PARAM_POWX = "powX";
    private static final String PARAM_POWY = "powY";
    private static final String PARAM_POWZ = "powZ";
    private static final String PARAM_PERTURB_AMOUNT = "perturbAmount";
    private static final String PARAM_EFFECT = "effect"; // Renamed from PARAM_CASE_7
    private static final String[] paramNames = { PARAM_POWX, PARAM_POWY, PARAM_POWZ, PARAM_PERTURB_AMOUNT, PARAM_EFFECT };

    // Member Variables & Defaults
    private double powX = 1.0;
    private double powY = 1.0;
    private double powZ = 1.0;
    private double perturbAmount = 1.0;
    private double effect = 2.0; // Renamed from case7, default matching original hardcoded value

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double px = pAffineTP.x;
        double py = pAffineTP.y;
        double pz = pAffineTP.z;

        // Base linear power transformation
        double xBase = sgn(px) * pow(fabs(px), this.powX);
        double yBase = sgn(py) * pow(fabs(py), this.powY);
        double zBase = sgn(pz) * pow(fabs(pz), this.powZ);

        // Perturbation (Polar Swirl XY - Case 7)
        double perturbationX = 0.0;
        double perturbationY = 0.0;
        double perturbationZ = 0.0; // No Z perturbation

        double anglePolar = atan2(py, px);
        double radiusPolar = sqrt(px * px + py * py);
        // Use 'effect' variable here
        // Original scaling factor 0.1 included
        perturbationX = sin(anglePolar + radiusPolar * effect) * 0.1 * perturbAmount;
        perturbationY = cos(anglePolar - radiusPolar * effect) * 0.1 * perturbAmount;

        // Combine and apply amount
        pVarTP.x += (xBase + perturbationX) * pAmount;
        pVarTP.y += (yBase + perturbationY) * pAmount;
        pVarTP.z += (zBase + perturbationZ) * pAmount; // zBase only
    }

    @Override
    public String[] getParameterNames() { return paramNames; }

    @Override
    public Object[] getParameterValues() {
        // Return 'effect' variable
        return new Object[] { powX, powY, powZ, perturbAmount, effect };
    }

    @Override
    public String[] getParameterAlternativeNames() {
         // Updated alternative name
         return new String[]{ "lT_powX", "lT_powY", "lT_powZ", "Perturb Strength", "Effect (Polar Factor)"};
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_POWX.equalsIgnoreCase(pName)) powX = pValue;
        else if (PARAM_POWY.equalsIgnoreCase(pName)) powY = pValue;
        else if (PARAM_POWZ.equalsIgnoreCase(pName)) powZ = pValue;
        else if (PARAM_PERTURB_AMOUNT.equalsIgnoreCase(pName)) perturbAmount = pValue;
        // Check for PARAM_EFFECT and set 'effect' variable
        else if (PARAM_EFFECT.equalsIgnoreCase(pName)) effect = pValue;
        else throw new IllegalArgumentException("Unknown parameter in " + getName() + ": " + pName);
    }

    @Override
    public String getName() { return "perturbCase7_PolarSwirl"; }

    private double sgn(double arg) { return (arg >= 0) ? 1.0 : -1.0; }
}