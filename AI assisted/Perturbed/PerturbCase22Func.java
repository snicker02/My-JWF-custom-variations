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
import static org.jwildfire.base.mathlib.MathLib.*; // fabs, sin, pow

public class PerturbCase22Func extends VariationFunc {
    private static final long serialVersionUID = 1L;

    // Parameters
    private static final String PARAM_POWX = "powX";
    private static final String PARAM_POWY = "powY";
    private static final String PARAM_POWZ = "powZ";
    private static final String PARAM_PERTURB_AMOUNT = "perturbAmount";
    // Case 22 uses Frequency and Offset
    private static final String PARAM_EFFECT_FREQ = "effectFreq";     // Was case22_Freq
    private static final String PARAM_EFFECT_OFFSET = "effectOffset"; // Was case22_Offset
    private static final String[] paramNames = { PARAM_POWX, PARAM_POWY, PARAM_POWZ, PARAM_PERTURB_AMOUNT, PARAM_EFFECT_FREQ, PARAM_EFFECT_OFFSET };

    // Member Variables & Defaults
    private double powX = 1.0;
    private double powY = 1.0;
    private double powZ = 1.0;
    private double perturbAmount = 1.0;
    private double effectFreq = 5.0;   // Default for original case22_Freq
    private double effectOffset = 0.0; // Default for original case22_Offset

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double px = pAffineTP.x;
        double py = pAffineTP.y;
        double pz = pAffineTP.z;

        // Base linear power transformation
        double xBase = sgn(px) * pow(fabs(px), this.powX);
        double yBase = sgn(py) * pow(fabs(py), this.powY);
        double zBase = sgn(pz) * pow(fabs(pz), this.powZ);

        // Perturbation (Diamond - Case 22)
        // Use effectFreq, effectOffset here
        double manhattanDist = fabs(px) + fabs(py) + fabs(pz);
        // Original scaling factor 0.1 included
        double diamondFactor = sin(manhattanDist * effectFreq + effectOffset) * 0.1;

        double perturbationX = sgn(px) * diamondFactor * perturbAmount;
        double perturbationY = sgn(py) * diamondFactor * perturbAmount;
        double perturbationZ = sgn(pz) * diamondFactor * perturbAmount;

        // Combine and apply amount
        pVarTP.x += (xBase + perturbationX) * pAmount;
        pVarTP.y += (yBase + perturbationY) * pAmount;
        pVarTP.z += (zBase + perturbationZ) * pAmount;
    }

    @Override
    public String[] getParameterNames() { return paramNames; }

    @Override
    public Object[] getParameterValues() {
        return new Object[] { powX, powY, powZ, perturbAmount, effectFreq, effectOffset };
    }

    @Override
    public String[] getParameterAlternativeNames() {
         return new String[]{ "lT_powX", "lT_powY", "lT_powZ", "Perturb Strength",
                              "Effect Freq (Diamond)", "Effect Offset (Diamond)"};
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_POWX.equalsIgnoreCase(pName)) powX = pValue;
        else if (PARAM_POWY.equalsIgnoreCase(pName)) powY = pValue;
        else if (PARAM_POWZ.equalsIgnoreCase(pName)) powZ = pValue;
        else if (PARAM_PERTURB_AMOUNT.equalsIgnoreCase(pName)) perturbAmount = pValue;
        else if (PARAM_EFFECT_FREQ.equalsIgnoreCase(pName)) effectFreq = pValue;
        else if (PARAM_EFFECT_OFFSET.equalsIgnoreCase(pName)) effectOffset = pValue;
        else throw new IllegalArgumentException("Unknown parameter in " + getName() + ": " + pName);
    }

    @Override
    public String getName() { return "perturbCase22_Diamond"; }

    private double sgn(double arg) { return (arg >= 0) ? 1.0 : -1.0; }
}