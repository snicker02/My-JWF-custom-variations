/*
 * JWildfire - an image and animation processor written in Java
 * Copyright (C) 1995-2025 Andreas Maschke (andreas.maschke@gmail.com) // Year updated
 * ... (rest of license header) ...
 */
package org.jwildfire.create.tina.variation;

import org.jwildfire.base.Tools;

import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*; // Includes atan2, cos, sin, hypot, M_2PI

// No longer needs precomputed sin/cos, so no SupportsGPU needed even if we added it back
public class PostPointSymmetryWFKIFSFunc extends VariationFunc {

    // Increment SerialVersionUID because internal logic and fields changed significantly
    private static final long serialVersionUID = 4L;

    public static final String PARAM_CENTRE_X = "centre_x";
    public static final String PARAM_CENTRE_Y = "centre_y";
    public static final String PARAM_ORDER = "order";
    private static final String PARAM_COLORSHIFT = "colorshift";
    private static final String[] paramNames = {PARAM_CENTRE_X, PARAM_CENTRE_Y, PARAM_ORDER, PARAM_COLORSHIFT};

    private double centre_x = 0.0;
    private double centre_y = 0.0;
    private int order = 6; // Default order often higher for kaleidoscopes
    private double colorshift = 0;

    // We don't need _sina, _cosa arrays for this reflection logic
    // private double _sina[], _cosa[];

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP,
                          double pAmount) {
        // pContext variable is not used inside this method's logic.

        // Calculate vector from center
        double dx = pAffineTP.x - centre_x;
        double dy = pAffineTP.y - centre_y;

        // Calculate distance (radius) from center
        double r = Math.hypot(dx, dy); // hypot(x,y) = sqrt(x*x + y*y)

        // If point is at the center or order is invalid, just scale and return
        if (r == 0.0 || order <= 1) {
             pVarTP.x = centre_x + dx * pAmount; // dx is 0 if r is 0
             pVarTP.y = centre_y + dy * pAmount; // dy is 0 if r is 0
             pVarTP.color = pAffineTP.color;
        } else {
            // Calculate the angle of the point relative to the center
            double originalAngle = atan2(dy, dx);

            // Normalize angle to [0, 2*PI) range (optional but can sometimes help stability)
            // Note: atan2 result is usually [-PI, PI]. Reflection math works either way.
            // Let's keep the standard atan2 range for simplicity here.

            // Calculate the width of each angular sector
            double sectorAngleWidth = M_2PI / (double) order;

            // Determine which sector the point falls into.
            // We need to map angle [-PI, PI] to index [0, order-1]
            // Add PI to shift range to [0, 2*PI), then divide and floor.
            double normalizedAngle = originalAngle + M_PI; // Shift to [0, 2*PI)
            int idx = (int) floor(normalizedAngle / sectorAngleWidth);

            // Clamp index to valid range [0, order - 1] just in case
            idx = Math.max(0, Math.min(order - 1, idx));

            // Calculate the angle of the starting boundary of this sector
            // Need angle relative to the [-PI, PI] range if we didn't normalize originalAngle fully
            // Easier: use the index directly. Boundary angle = -PI + idx * sectorAngleWidth;
            // Let's use the boundary angle based on the [0, 2pi) logic used for index:
            double sectorBoundaryAngle = idx * sectorAngleWidth - M_PI; // Angle of the line we reflect across relative to [-PI, PI] range


            // Reflect the original angle across the sector boundary line
            // Formula: newAngle = 2 * reflectionLineAngle - originalAngle
            double reflectedAngle = 2.0 * sectorBoundaryAngle - originalAngle;

            // Calculate new coordinates based on original radius and reflected angle
            double reflected_dx = r * cos(reflectedAngle);
            double reflected_dy = r * sin(reflectedAngle);

            // Apply the variation amount to the reflected vector and add back the center
            pVarTP.x = centre_x + reflected_dx * pAmount;
            pVarTP.y = centre_y + reflected_dy * pAmount;

            // Apply color shift based on the sector index
            pVarTP.color = fmod(pAffineTP.color + idx * colorshift, 1.0);
        }

        // Pass through z coordinate if it exists
         if (pVarTP.z != 0.0 || pAffineTP.z != 0.0) {
             pVarTP.z += pAffineTP.z * pAmount;
         }
    }

    @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    @Override
    public Object[] getParameterValues() {
        return new Object[]{centre_x, centre_y, order, colorshift};
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_CENTRE_X.equalsIgnoreCase(pName))
            centre_x = pValue;
        else if (PARAM_CENTRE_Y.equalsIgnoreCase(pName))
            centre_y = pValue;
        else if (PARAM_ORDER.equalsIgnoreCase(pName)) {
            // Ensure order is at least 2 for reflection to make sense
            order = Math.max(2, Tools.FTOI(pValue));
             // No need to invalidate sin/cos arrays anymore
        } else if (PARAM_COLORSHIFT.equalsIgnoreCase(pName))
            colorshift = pValue;
        else
            throw new IllegalArgumentException("Unknown parameter: " + pName);
    }

    @Override public String getName() { return "post_point_symmetry_wf"; }
    @Override public int getPriority() { return 1; }

    // init method no longer needs to calculate sin/cos.
    // It might still be called by the framework, so we keep it empty or minimal.
    @Override
    public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
        // Pre-calculations are no longer needed for this reflection logic.
        // Ensure order is valid (though setParameter should handle it)
        if (order < 2) {
            // System.out.println("Warning: post_point_symmetry_wf order < 2, reflection disabled.");
             order = 2; // Force minimum order for reflection
        }
    }




    // No GPU Code
}