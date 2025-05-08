package org.jwildfire.create.tina.variation;

// import org.jwildfire.base.Tools; // No longer needed
// import org.jwildfire.base.mathlib.MathLib; // No longer needed

import org.jwildfire.create.tina.base.XYZPoint;
import org.jwildfire.create.tina.base.XForm;

// Note on JWildfire Library Dependencies:
// This code version includes extensive manual workarounds for several fundamental
// JWildfire class methods and standard Java Math usage due to user reports
// of these methods/constructors being "undefined" or "not applicable".
// This is highly unusual and typically points to a significant problem with the
// JWildfire installation, classpath, Java environment, or library files
// in the user's environment. These workarounds aim to make this specific plugin
// as self-contained as possible. If issues persist, please thoroughly check your
// JWildfire setup and logs for compilation or runtime errors.

public class SpherefoldFunc extends VariationFunc {

  private static final long serialVersionUID = 1L;

  // --- Parameter Names ---
  private static final String PARAM_RADIUS_H = "radiusH"; // Outer / Inversion Radius
  private static final String PARAM_RADIUS_L = "radiusL"; // Inner Radius

  // --- Parameter Variables ---
  private double pRadiusH = 1.0; // Default H
  private double pRadiusL = 0.5; // Default L

  // --- Manual Math Helpers (Workaround for reported missing library methods) ---
  private static double _magnitudeSq(XYZPoint p) {
     if (p == null || Double.isNaN(p.x) || Double.isNaN(p.y) || Double.isNaN(p.z) ||
         Double.isInfinite(p.x) || Double.isInfinite(p.y) || Double.isInfinite(p.z)) {
         return Double.NaN; 
     }
    return p.x * p.x + p.y * p.y + p.z * p.z;
  }

  private static double _magnitude(XYZPoint p) {
    double magSq = _magnitudeSq(p); 
    if (Double.isNaN(magSq) || magSq < 0) return Double.NaN; 
    return Math.sqrt(magSq);
  }

  // _normalize not needed for this variation
  // private static void _normalize(XYZPoint p) { ... }

  private static XYZPoint _multiply(XYZPoint p, double factor) {
    XYZPoint result = new XYZPoint();
     if (p == null || Double.isNaN(factor) || Double.isInfinite(factor) || 
         Double.isNaN(p.x) || Double.isNaN(p.y) || Double.isNaN(p.z) ||
         Double.isInfinite(p.x) || Double.isInfinite(p.y) || Double.isInfinite(p.z)) {
         result.x = Double.NaN; result.y = Double.NaN; result.z = Double.NaN;
     } else {
        result.x = p.x * factor;
        result.y = p.y * factor;
        result.z = p.z * factor;
     }
    return result;
  }

  // _negate not needed for this variation
  // private static XYZPoint _negate(XYZPoint p) { ... }

  private static double _limitValueDouble(double value, double min, double max) {
      if(Double.isNaN(value)) return min; 
      // Ensure min <= max before using them
      double actualMin = Math.min(min, max);
      double actualMax = Math.max(min, max);
      double minnedValue = Math.min(actualMax, value); 
      return Math.max(actualMin, minnedValue);        
  }

  // _limitValueInt not needed for this variation
  // private static int _limitValueInt(int value, int min, int max) { ... }


  public SpherefoldFunc() {
    System.err.println("INFO: SpherefoldFunc: Constructor called."); 
  }

  @Override
  public String getName() {
    return "spherefold"; 
  }

  @Override
  public String[] getParameterNames() {
      System.err.println("INFO: SpherefoldFunc: getParameterNames() called."); 
      return new String[]{ PARAM_RADIUS_H, PARAM_RADIUS_L };
  }

  @Override
  public Object[] getParameterValues() {
      System.err.println("INFO: SpherefoldFunc: getParameterValues() called."); 
      return new Object[]{ pRadiusH, pRadiusL };
  }

  @Override
  public void setParameter(String pName, double pValue) {
    System.err.println("INFO: SpherefoldFunc: setParameter received: " + pName + " = " + pValue); 
    try {
        if (PARAM_RADIUS_H.equalsIgnoreCase(pName)) {
            if(Double.isNaN(pValue) || Double.isInfinite(pValue) || pValue <= 1e-9) { System.err.println("WARN: Invalid radiusH value: " + pValue); }
            else { 
                pRadiusH = _limitValueDouble(pValue, 1e-9, 10000.0); 
                // Ensure L remains smaller than H after H changes
                if (pRadiusL >= pRadiusH) {
                    pRadiusL = pRadiusH * 0.9999;
                     System.err.println("  -> Adjusted pRadiusL to maintain L < H: " + pRadiusL);
                }
                System.err.println("  -> pRadiusH set to: " + pRadiusH); 
            }
        } else if (PARAM_RADIUS_L.equalsIgnoreCase(pName)) {
            if(Double.isNaN(pValue) || Double.isInfinite(pValue) || pValue <= 1e-9) { System.err.println("WARN: Invalid radiusL value: " + pValue); }
            else { 
                pRadiusL = _limitValueDouble(pValue, 1e-9, 10000.0); 
                 // Ensure L is smaller than H after L changes
                if (pRadiusL >= pRadiusH) {
                    pRadiusL = pRadiusH * 0.9999;
                    System.err.println("  -> Adjusted pRadiusL to maintain L < H: " + pRadiusL);
                }
                System.err.println("  -> pRadiusL set to: " + pRadiusL); 
            }
        } else {
          System.err.println("WARN: SpherefoldFunc: Unhandled parameter name in setParameter: " + pName);
        }
    } catch (Exception e) {
        System.err.println("ERROR: SpherefoldFunc: Exception in setParameter for " + pName + " = " + pValue);
        e.printStackTrace(System.err); 
    }
  }

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    
    // Get parameters H and L
    double H = this.pRadiusH;
    double L = this.pRadiusL; 
    // Ensure L < H due to potential precision issues or direct setting if checks failed
    if (L >= H) L = H * 0.9999;
    if (L < 1e-9) L = 1e-9; // Ensure L is positive for division

    // Precompute squares
    double H_sq = H * H;
    double L_sq = L * L; 

    // Get input point and magnitude squared
    XYZPoint p = pAffineTP; 
    double rSq = _magnitudeSq(p);

    // Handle invalid input magnitude early
    if (Double.isNaN(rSq) || Double.isInfinite(rSq) || rSq < 0) { // Added rSq < 0 check just in case
        // Pass through invalid input without modification
        pVarTP.x = p.x; pVarTP.y = p.y; pVarTP.z = p.z; 
        if(!Double.isNaN(rSq)) System.err.println("WARN: Spherefold input magnitudeSq invalid: " + rSq);
        return;
    }

    // Calculate magnitude r (needed for comparisons)
    // Avoid sqrt(0) if possible, though L check helps
    double r = (rSq < 1e-18) ? 0.0 : Math.sqrt(rSq); 
    
    XYZPoint resultPoint = new XYZPoint(); // Using default constructor + manual assignment
    double factor = 1.0; // Default factor if unchanged

    try { // Add try-catch around calculations
        if (r > H) {
            // Case 1: r > H -> Unchanged (factor remains 1.0)
            resultPoint.x = p.x; resultPoint.y = p.y; resultPoint.z = p.z;

        } else if (r > L) {
            // Case 2: L < r <= H -> Invert by H^2/r^2
            if (rSq < 1e-18) { // Should not happen if r > L and L>0, but safety check
                 // Undefined: Close to origin but > L implies L is tiny. What should happen?
                 // Let's scale by a large factor instead of dividing by near-zero.
                 // factor = H_sq / 1e-18; // Very large factor
                 // Safer: treat as unchanged or error? Let's output 0,0,0 for this undefined case.
                 System.err.println("WARN: Spherefold rSq near zero in Case 2 (r=" + r + ", L=" + L + "). Outputting zero.");
                 resultPoint.x = 0.0; resultPoint.y = 0.0; resultPoint.z = 0.0;
            } else {
                factor = H_sq / rSq;
                XYZPoint tempResult = _multiply(p, factor); 
                resultPoint.x = tempResult.x; resultPoint.y = tempResult.y; resultPoint.z = tempResult.z;
            }
        } else {
            // Case 3: r <= L -> Scale by H^2/L^2
            // L_sq is guaranteed positive here
            factor = H_sq / L_sq; 
            XYZPoint tempResult = _multiply(p, factor); 
            resultPoint.x = tempResult.x; resultPoint.y = tempResult.y; resultPoint.z = tempResult.z;
        }

        // Final check for NaN/Inf in result (could happen from factor * p)
        if (Double.isNaN(resultPoint.x) || Double.isNaN(resultPoint.y) || Double.isNaN(resultPoint.z) ||
            Double.isInfinite(resultPoint.x) || Double.isInfinite(resultPoint.y) || Double.isInfinite(resultPoint.z)) {
             System.err.println("WARN: Spherefold resulted in NaN/Inf. Outputting (0,0,0). Input r=" + r + ", factor=" + factor);
             pVarTP.x = 0.0; pVarTP.y = 0.0; pVarTP.z = 0.0;
        } else {
            // Set output (JWildfire handles pAmount)
            pVarTP.x = resultPoint.x * pAmount;
            pVarTP.y = resultPoint.y* pAmount;
            pVarTP.z = resultPoint.z* pAmount;
        }

    } catch (Exception e) {
         System.err.println("ERROR: Spherefold calculation error for r=" + r + ", H=" + H + ", L=" + L);
         e.printStackTrace(System.err);
         // Output something safe
         pVarTP.x = p.x; pVarTP.y = p.y; pVarTP.z = p.z; // Pass through original point on error
    }
  }



}
