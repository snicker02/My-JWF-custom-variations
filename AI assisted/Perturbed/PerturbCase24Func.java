package org.jwildfire.create.tina.variation;

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import static org.jwildfire.base.mathlib.MathLib.*; // sqrt, sin, cos, pow, fabs

public class PerturbCase24Func extends VariationFunc {
    private static final long serialVersionUID = 1L;

    // Parameters
    private static final String PARAM_POWX = "powX";
    private static final String PARAM_POWY = "powY";
    private static final String PARAM_POWZ = "powZ";
    private static final String PARAM_PERTURB_AMOUNT = "perturbAmount";
    // Case 24 uses Frequency and ZFactor
    private static final String PARAM_EFFECT_FREQ = "effectFreq";       // Was case24_Freq
    private static final String PARAM_EFFECT_ZFACTOR = "effectZFactor"; // Was case24_ZFactor
    private static final String[] paramNames = { PARAM_POWX, PARAM_POWY, PARAM_POWZ, PARAM_PERTURB_AMOUNT, PARAM_EFFECT_FREQ, PARAM_EFFECT_ZFACTOR };

    // Member Variables & Defaults
    private double powX = 1.0;
    private double powY = 1.0;
    private double powZ = 1.0;
    private double perturbAmount = 1.0;
    private double effectFreq = 4.0;     // Default for original case24_Freq
    private double effectZFactor = 0.5;  // Default for original case24_ZFactor

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        double px = pAffineTP.x;
        double py = pAffineTP.y;
        double pz = pAffineTP.z;

        // Base linear power transformation
        double xBase = sgn(px) * pow(fabs(px), this.powX);
        double yBase = sgn(py) * pow(fabs(py), this.powY);
        double zBase = sgn(pz) * pow(fabs(pz), this.powZ);

        // Perturbation (Simplified Curl/Flow - Case 24)
        // Use effectFreq, effectZFactor here
        double radiusCurl = sqrt(px * px + py * py);
        // Original scaling factor 0.1 included
        double curlStrength = sin(radiusCurl * effectFreq + pz * effectZFactor) * 0.1;
        double normFactor = (radiusCurl > 1e-9) ? (curlStrength / radiusCurl) : 0.0; // Avoid division by zero

        double perturbationX = -py * normFactor * perturbAmount;
        double perturbationY =  px * normFactor * perturbAmount;
        // Original scaling factor 0.05 included for Z perturbation
        double perturbationZ = cos(pz * effectFreq * 0.5) * 0.05 * perturbAmount; // Note: Uses Freq here as well


        // Combine and apply amount
        pVarTP.x += (xBase + perturbationX) * pAmount;
        pVarTP.y += (yBase + perturbationY) * pAmount;
        pVarTP.z += (zBase + perturbationZ) * pAmount;
    }

    @Override
    public String[] getParameterNames() { return paramNames; }

    @Override
    public Object[] getParameterValues() {
        return new Object[] { powX, powY, powZ, perturbAmount, effectFreq, effectZFactor };
    }

    @Override
    public String[] getParameterAlternativeNames() {
         return new String[]{ "lT_powX", "lT_powY", "lT_powZ", "Perturb Strength",
                              "Effect Freq (Curl)", "Effect Z Factor (Curl)"};
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_POWX.equalsIgnoreCase(pName)) powX = pValue;
        else if (PARAM_POWY.equalsIgnoreCase(pName)) powY = pValue;
        else if (PARAM_POWZ.equalsIgnoreCase(pName)) powZ = pValue;
        else if (PARAM_PERTURB_AMOUNT.equalsIgnoreCase(pName)) perturbAmount = pValue;
        else if (PARAM_EFFECT_FREQ.equalsIgnoreCase(pName)) effectFreq = pValue;
        else if (PARAM_EFFECT_ZFACTOR.equalsIgnoreCase(pName)) effectZFactor = pValue;
        else throw new IllegalArgumentException("Unknown parameter in " + getName() + ": " + pName);
    }

    @Override
    public String getName() { return "perturbCase24_Curl"; }

    private double sgn(double arg) { return (arg >= 0) ? 1.0 : -1.0; }
}