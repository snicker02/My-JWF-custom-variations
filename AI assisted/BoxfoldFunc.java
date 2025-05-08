package org.jwildfire.create.tina.variation;

import org.jwildfire.create.tina.base.XYZPoint;
import org.jwildfire.create.tina.base.XForm;

// Note on JWildfire Library Dependencies:
// This code version includes manual workarounds for potentially missing/problematic
// JWildfire/Java library methods (like Math.max/min used in _limitValueDouble)
// based on user reports. This is highly unusual and typically points to a
// significant problem with the JWildfire installation, classpath, Java environment,
// or library files in the user's environment.

public class BoxfoldFunc extends VariationFunc {

  private static final long serialVersionUID = 1L;

  // --- Parameter Names ---
  private static final String PARAM_FOLD_LIMIT = "foldLimit";

  // --- Parameter Variables ---
  private double pFoldLimit = 1.0; // Default value, equivalent to F=1 in Mandelbox

  // --- Manual Math Helpers (Workaround for reported missing library methods) ---
  // Kept _limitValueDouble for safe parameter setting
  private static double _limitValueDouble(double value, double min, double max) {
      if(Double.isNaN(value)) return min; // Handle NaN input
      double minnedValue = Math.min(max, value); // Standard Java Math.min
      return Math.max(min, minnedValue);        // Standard Java Math.max
  }

  private static int _limitValueInt(int value, int min, int max) {
      int minnedValue = Math.min(max, value);   // Standard Java Math.min
      return Math.max(min, minnedValue);      // Standard Java Math.max
  }

  // Constructor
  public BoxfoldFunc() {
    // Name is set by overriding getName()
    System.err.println("INFO: BoxfoldFunc: Constructor called."); // Logging
  }

  @Override
  public String getName() {
    return "boxfold"; 
  }

  @Override
  public String[] getParameterNames() {
      System.err.println("INFO: BoxfoldFunc: getParameterNames() called."); // Logging
      return new String[]{ PARAM_FOLD_LIMIT };
  }

  @Override
  public Object[] getParameterValues() {
      System.err.println("INFO: BoxfoldFunc: getParameterValues() called."); // Logging
      return new Object[]{ pFoldLimit };
  }

  @Override
  public void setParameter(String pName, double pValue) {
    System.err.println("INFO: BoxfoldFunc: setParameter received: " + pName + " = " + pValue); // Logging
    try {
        if (PARAM_FOLD_LIMIT.equalsIgnoreCase(pName)) {
            // Boxfold limit F should generally be positive
             if(Double.isNaN(pValue) || Double.isInfinite(pValue) || pValue <= 1e-9) { 
                 System.err.println("WARN: Invalid foldLimit value: " + pValue + ". Setting to 1.0");
                 pFoldLimit = 1.0;
             } else { 
                 // Limit upper bound? Boxfold doesn't strictly need one, but large values might be unusual.
                 pFoldLimit = _limitValueDouble(pValue, 1e-9, 10000.0); 
                 System.err.println("  -> pFoldLimit set to: " + pFoldLimit); 
             }
        } else {
          System.err.println("WARN: BoxfoldFunc: Unhandled parameter name in setParameter: " + pName);
        }
    } catch (Exception e) {
        System.err.println("ERROR: BoxfoldFunc: Exception in setParameter for " + pName + " = " + pValue);
        e.printStackTrace(System.err); 
    }
  }

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    
    // Get input point coordinates
    double x = pAffineTP.x;
    double y = pAffineTP.y;
    double z = pAffineTP.z;
    double F = this.pFoldLimit; // Get parameter value

    // Calculate folded coordinates
    double foldedX = x; // Start with original value
    if (x > F) {
        foldedX = 2.0 * F - x;
    } else if (x < -F) {
        foldedX = -2.0 * F - x;
    }

    double foldedY = y; // Start with original value
    if (y > F) {
        foldedY = 2.0 * F - y;
    } else if (y < -F) {
        foldedY = -2.0 * F - y;
    }

    double foldedZ = z; // Start with original value
    if (z > F) {
        foldedZ = 2.0 * F - z;
    } else if (z < -F) {
        foldedZ = -2.0 * F - z;
    }
    
    // Handle potential NaN/Infinity resulting from folding very large numbers
    // Though the folding logic itself shouldn't create NaN/Inf unless input was already NaN/Inf
     if (Double.isNaN(foldedX) || Double.isNaN(foldedY) || Double.isNaN(foldedZ) ||
         Double.isInfinite(foldedX) || Double.isInfinite(foldedY) || Double.isInfinite(foldedZ)) {
         // If folding somehow resulted in NaN/Inf, output 0 to avoid propagating issues
         // (Could also output original x,y,z if preferred)
         System.err.println("WARN: Boxfold resulted in NaN/Inf for input ("+x+","+y+","+z+"). Outputting (0,0,0).");
         pVarTP.x += 0.0;
         pVarTP.y += 0.0;
         pVarTP.z += 0.0;
     } else {
         // Set pVarTP to the fully transformed point.
         // JWildfire's engine handles the pAmount blending:
         // final = pAffineTP * (1-pAmount) + pVarTP * pAmount
         pVarTP.x += foldedX* pAmount;
         pVarTP.y += foldedY* pAmount;
         pVarTP.z += foldedZ* pAmount;
     }
  }


}
