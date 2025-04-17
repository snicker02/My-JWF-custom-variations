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
import static org.jwildfire.base.mathlib.MathLib.*; // sin, cos, pow, fabs

public class package org.jwildfire.create.tina.variation;


import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import static org.jwildfire.base.mathlib.MathLib.*; // atan2, sin, cos, pow, fabs

public class PerturbCase10Func extends VariationFunc {
    private static final long serialVersionUID = 1L;

    // Parameters
    private static final String PARAM_POWX = "powX";
    private static final String PARAM_POWY = "powY";
    private static final String PARAM_POWZ = "powZ";
    private static final String PARAM_PERTURB_AMOUNT = "perturbAmount";
    // Case 10 does not have a specific numerical parameter in the original code
    private static final String[] paramNames = { PARAM_POWX, PARAM_POWY, PARAM_POWZ, PARAM_PERTURB_AMOUNT };

    // Member Variables & Defaults
    private double powX = 1.0;
    private double powY = 1.0;
    private double powZ = 1.0;
    private double perturbAmount = 1.0;
    // No specific 'case10' variable needed

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double px = pAffineTP.x;
        double py = pAffineTP.y;
        double pz = pAffineTP.z;

        // Base linear power transformation
        double xBase = sgn(px) * pow(fabs(px), this.powX);
        double yBase = sgn(py) * pow(fabs(py), this.powY);
        double zBase = sgn(pz) * pow(fabs(pz), this.powZ);

        // Perturbation (Tangential XY - Case 10)
        double perturbationX = 0.0;
        double perturbationY = 0.0;
        double perturbationZ = 0.0; // No Z perturbation

        double angleTan = atan2(py, px);
        // Original scaling factor 0.1 included
        perturbationX = -sin(angleTan) * 0.1 * perturbAmount;
        perturbationY = cos(angleTan) * 0.1 * perturbAmount;

        // Combine and apply amount
        pVarTP.x += (xBase + perturbationX) * pAmount;
        pVarTP.y += (yBase + perturbationY) * pAmount;
        pVarTP.z += (zBase + perturbationZ) * pAmount; // zBase only
    }

    @Override
    public String[] getParameterNames() { return paramNames; }

    @Override
    public Object[] getParameterValues() { return new Object[] { powX, powY, powZ, perturbAmount }; }

    @Override
    public String[] getParameterAlternativeNames() {
         return new String[]{ "lT_powX", "lT_powY", "lT_powZ", "Perturb Strength"};
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_POWX.equalsIgnoreCase(pName)) powX = pValue;
        else if (PARAM_POWY.equalsIgnoreCase(pName)) powY = pValue;
        else if (PARAM_POWZ.equalsIgnoreCase(pName)) powZ = pValue;
        else if (PARAM_PERTURB_AMOUNT.equalsIgnoreCase(pName)) perturbAmount = pValue;
        else throw new IllegalArgumentException("Unknown parameter in " + getName() + ": " + pName);
    }

    @Override
    public String getName() { return "perturbCase10_Tangential"; }

    private double sgn(double arg) { return (arg >= 0) ? 1.0 : -1.0; }
} extends VariationFunc {
    private static final long serialVersionUID = 1L;

    // Parameters
    private static final String PARAM_POWX = "powX";
    private static final String PARAM_POWY = "powY";
    private static final String PARAM_POWZ = "powZ";
    private static final String PARAM_PERTURB_AMOUNT = "perturbAmount";
    // Case 9 does not have a specific numerical parameter in the original code
    private static final String[] paramNames = { PARAM_POWX, PARAM_POWY, PARAM_POWZ, PARAM_PERTURB_AMOUNT };

    // Member Variables & Defaults
    private double powX = 1.0;
    private double powY = 1.0;
    private double powZ = 1.0;
    private double perturbAmount = 0.1;
    // No specific 'case9' variable needed

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double px = pAffineTP.x;
        double py = pAffineTP.y;
        double pz = pAffineTP.z;

        // Base linear power transformation
        double xBase = sgn(px) * pow(fabs(px), this.powX);
        double yBase = sgn(py) * pow(fabs(py), this.powY);
        double zBase = sgn(pz) * pow(fabs(pz), this.powZ);

        // Perturbation (Cross XY - Case 9)
        double perturbationX = 0.0;
        double perturbationY = 0.0;
        double perturbationZ = 0.0; // No Z perturbation

        // Original scaling factor 0.15 included
        perturbationX = sin(px) * cos(py) * 0.15 * perturbAmount;
        perturbationY = cos(px) * sin(py) * 0.15 * perturbAmount;

        // Combine and apply amount
        pVarTP.x += (xBase + perturbationX) * pAmount;
        pVarTP.y += (yBase + perturbationY) * pAmount;
        pVarTP.z += (zBase + perturbationZ) * pAmount; // zBase only
    }

    @Override
    public String[] getParameterNames() { return paramNames; }

    @Override
    public Object[] getParameterValues() { return new Object[] { powX, powY, powZ, perturbAmount }; }

    @Override
    public String[] getParameterAlternativeNames() {
         return new String[]{ "lT_powX", "lT_powY", "lT_powZ", "Perturb Strength"};
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_POWX.equalsIgnoreCase(pName)) powX = pValue;
        else if (PARAM_POWY.equalsIgnoreCase(pName)) powY = pValue;
        else if (PARAM_POWZ.equalsIgnoreCase(pName)) powZ = pValue;
        else if (PARAM_PERTURB_AMOUNT.equalsIgnoreCase(pName)) perturbAmount = pValue;
        else throw new IllegalArgumentException("Unknown parameter in " + getName() + ": " + pName);
    }

    @Override
    public String getName() { return "perturbCase9_Cross"; }

    private double sgn(double arg) { return (arg >= 0) ? 1.0 : -1.0; }
}