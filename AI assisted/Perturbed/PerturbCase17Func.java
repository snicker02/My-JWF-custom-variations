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
import static org.jwildfire.base.mathlib.MathLib.*; // pow, fabs

public class PerturbCase17Func extends VariationFunc {
    private static final long serialVersionUID = 1L;

    // Parameters
    private static final String PARAM_POWX = "powX";
    private static final String PARAM_POWY = "powY";
    private static final String PARAM_POWZ = "powZ";
    private static final String PARAM_PERTURB_AMOUNT = "perturbAmount";
    // Case 17 uses separate factors for XY and Z shear
    private static final String PARAM_EFFECT_XY = "effectXY"; // Was case17_XY
    private static final String PARAM_EFFECT_Z = "effectZ";   // Was case17_Z
    private static final String[] paramNames = { PARAM_POWX, PARAM_POWY, PARAM_POWZ, PARAM_PERTURB_AMOUNT, PARAM_EFFECT_XY, PARAM_EFFECT_Z };

    // Member Variables & Defaults
    private double powX = 1.0;
    private double powY = 1.0;
    private double powZ = 1.0;
    private double perturbAmount = 1.0;
    private double effectXY = 0.5; // Default for original case17_XY
    private double effectZ = 0.3;  // Default for original case17_Z

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double px = pAffineTP.x;
        double py = pAffineTP.y;
        double pz = pAffineTP.z;

        // Base linear power transformation
        double xBase = sgn(px) * pow(fabs(px), this.powX);
        double yBase = sgn(py) * pow(fabs(py), this.powY);
        double zBase = sgn(pz) * pow(fabs(pz), this.powZ);

        // Perturbation (Shear - Case 17)
        // Use effectXY and effectZ here
        // Original scaling factors 0.1 and 0.05 included
        double perturbationX = py * effectXY * 0.1 * perturbAmount;
        double perturbationY = px * effectXY * 0.1 * perturbAmount;
        double perturbationZ = (px + py) * effectZ * 0.05 * perturbAmount;

        // Combine and apply amount
        pVarTP.x += (xBase + perturbationX) * pAmount;
        pVarTP.y += (yBase + perturbationY) * pAmount;
        pVarTP.z += (zBase + perturbationZ) * pAmount;
    }

    @Override
    public String[] getParameterNames() { return paramNames; }

    @Override
    public Object[] getParameterValues() {
        return new Object[] { powX, powY, powZ, perturbAmount, effectXY, effectZ };
    }

    @Override
    public String[] getParameterAlternativeNames() {
         return new String[]{ "lT_powX", "lT_powY", "lT_powZ", "Perturb Strength",
                              "Effect XY (Shear)", "Effect Z (Shear)"}; // Updated Alt Names
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_POWX.equalsIgnoreCase(pName)) powX = pValue;
        else if (PARAM_POWY.equalsIgnoreCase(pName)) powY = pValue;
        else if (PARAM_POWZ.equalsIgnoreCase(pName)) powZ = pValue;
        else if (PARAM_PERTURB_AMOUNT.equalsIgnoreCase(pName)) perturbAmount = pValue;
        else if (PARAM_EFFECT_XY.equalsIgnoreCase(pName)) effectXY = pValue;
        else if (PARAM_EFFECT_Z.equalsIgnoreCase(pName)) effectZ = pValue;
        else throw new IllegalArgumentException("Unknown parameter in " + getName() + ": " + pName);
    }

    @Override
    public String getName() { return "perturbCase17_Shear"; }

    private double sgn(double arg) { return (arg >= 0) ? 1.0 : -1.0; }
}