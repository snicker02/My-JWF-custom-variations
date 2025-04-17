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
import static org.jwildfire.base.mathlib.MathLib.*; // sqrt, acos, atan2, sin, cos, pow, fabs

public class PerturbCase18Func extends VariationFunc {
    private static final long serialVersionUID = 1L;

    // Parameters
    private static final String PARAM_POWX = "powX";
    private static final String PARAM_POWY = "powY";
    private static final String PARAM_POWZ = "powZ";
    private static final String PARAM_PERTURB_AMOUNT = "perturbAmount";
    // Case 18 uses separate factors for R, Theta, Phi
    private static final String PARAM_EFFECT_R = "effectR"; // Was case18_R
    private static final String PARAM_EFFECT_T = "effectT"; // Was case18_T
    private static final String PARAM_EFFECT_P = "effectP"; // Was case18_P
    private static final String[] paramNames = { PARAM_POWX, PARAM_POWY, PARAM_POWZ, PARAM_PERTURB_AMOUNT, PARAM_EFFECT_R, PARAM_EFFECT_T, PARAM_EFFECT_P };

    // Member Variables & Defaults
    private double powX = 1.0;
    private double powY = 1.0;
    private double powZ = 1.0;
    private double perturbAmount = 1.0;
    private double effectR = 4.0; // Default for original case18_R
    private double effectT = 3.0; // Default for original case18_T
    private double effectP = 2.0; // Default for original case18_P

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double px = pAffineTP.x;
        double py = pAffineTP.y;
        double pz = pAffineTP.z;

        // Base linear power transformation
        double xBase = sgn(px) * pow(fabs(px), this.powX);
        double yBase = sgn(py) * pow(fabs(py), this.powY);
        double zBase = sgn(pz) * pow(fabs(pz), this.powZ);

        // Perturbation (Spherical - Case 18)
        double perturbationX = 0.0;
        double perturbationY = 0.0;
        double perturbationZ = 0.0;

        double rSph = sqrt(px * px + py * py + pz * pz);
        if (rSph > 1e-10) {
            double thetaSph = acos(pz / rSph); // Inclination/Polar angle (0 to PI)
            double phiSph = atan2(py, px);   // Azimuth angle (-PI to PI)

            // Use effectR, effectT, effectP here
            // Original scaling factors 0.05 and 0.02 included
            double radialPerturbSph = sin(rSph * effectR) * 0.05;
            double thetaPerturbSph = sin(thetaSph * effectT) * 0.02;
            double phiPerturbSph = cos(phiSph * effectP) * 0.02; // Note: original used cos for Phi

            double sinTheta = sin(thetaSph); double cosTheta = cos(thetaSph);
            double sinPhi = sin(phiSph); double cosPhi = cos(phiSph);

            // Combine perturbations based on spherical coordinate derivatives (approximation)
            perturbationX = (sinTheta * cosPhi * radialPerturbSph + cosTheta * cosPhi * thetaPerturbSph - sinPhi * phiPerturbSph) * perturbAmount;
            perturbationY = (sinTheta * sinPhi * radialPerturbSph + cosTheta * sinPhi * thetaPerturbSph + cosPhi * phiPerturbSph) * perturbAmount;
            perturbationZ = (cosTheta * radialPerturbSph - sinTheta * thetaPerturbSph) * perturbAmount;
        }

        // Combine and apply amount
        pVarTP.x += (xBase + perturbationX) * pAmount;
        pVarTP.y += (yBase + perturbationY) * pAmount;
        pVarTP.z += (zBase + perturbationZ) * pAmount;
    }

    @Override
    public String[] getParameterNames() { return paramNames; }

    @Override
    public Object[] getParameterValues() {
        return new Object[] { powX, powY, powZ, perturbAmount, effectR, effectT, effectP };
    }

    @Override
    public String[] getParameterAlternativeNames() {
         return new String[]{ "lT_powX", "lT_powY", "lT_powZ", "Perturb Strength",
                              "Effect R (Sph Freq)", "Effect T (Sph Freq)", "Effect P (Sph Freq)"}; // Updated Alt Names
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_POWX.equalsIgnoreCase(pName)) powX = pValue;
        else if (PARAM_POWY.equalsIgnoreCase(pName)) powY = pValue;
        else if (PARAM_POWZ.equalsIgnoreCase(pName)) powZ = pValue;
        else if (PARAM_PERTURB_AMOUNT.equalsIgnoreCase(pName)) perturbAmount = pValue;
        else if (PARAM_EFFECT_R.equalsIgnoreCase(pName)) effectR = pValue;
        else if (PARAM_EFFECT_T.equalsIgnoreCase(pName)) effectT = pValue;
        else if (PARAM_EFFECT_P.equalsIgnoreCase(pName)) effectP = pValue;
        else throw new IllegalArgumentException("Unknown parameter in " + getName() + ": " + pName);
    }

    @Override
    public String getName() { return "perturbCase18_Spherical"; }

    private double sgn(double arg) { return (arg >= 0) ? 1.0 : -1.0; }
}