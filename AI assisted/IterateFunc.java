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

import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class IterateFunc extends VariationFunc {
    private static final long serialVersionUID = 1L;

    private static final String PARAM_ITERATIONS = "iterations";
    private static final String PARAM_X_ANGLE_INC = "x_angle_inc";
    private static final String PARAM_Y_ANGLE_INC = "y_angle_inc";
    private static final String PARAM_Z_ANGLE_INC = "z_angle_inc";
    private static final String PARAM_CENTRE_X_INC = "centre_x_inc";
    private static final String PARAM_CENTRE_Y_INC = "centre_y_inc";
    private static final String PARAM_CENTRE_Z_INC = "centre_z_inc";
    private static final String PARAM_ANGLE_INC_SCALE = "angle_inc_scale";
    private static final String PARAM_CENTRE_INC_SCALE = "centre_inc_scale"; // Changed: Corrected parameter name

    private static final String[] paramNames = {
            PARAM_ITERATIONS, PARAM_X_ANGLE_INC, PARAM_Y_ANGLE_INC, PARAM_Z_ANGLE_INC,
            PARAM_CENTRE_X_INC, PARAM_CENTRE_Y_INC, PARAM_CENTRE_Z_INC,
            PARAM_ANGLE_INC_SCALE, PARAM_CENTRE_INC_SCALE // Changed: Corrected parameter name
    };

    private int iterations = 1;
    private double x_angle_inc = 0.0;
    private double y_angle_inc = 0.0;
    private double z_angle_inc = 0.0;
    private double centre_x_inc = 0.0;
    private double centre_y_inc = 0.0;
    private double centre_z_inc = 0.0;
    private double angle_inc_scale = 10.0;
    private double centre_inc_scale = 1000000.0;

    private double x_angle = 0.0;
    private double y_angle = 0.0;
    private double z_angle = 0.0;
    private double centre_x = 0.0;
    private double centre_y = 0.0;
    private double centre_z = 0.0;

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP,
                        double pAmount) {

        double x = pAffineTP.x;
        double y = pAffineTP.y;
        double z = pAffineTP.z;

        for (int i = 0; i < iterations; i++) {
            double rotatedX, rotatedY, rotatedZ;

            rotatedX = x * cos(z_angle) - y * sin(z_angle);
            rotatedY = x * sin(z_angle) + y * cos(z_angle);
            rotatedZ = z;

            double tempX = rotatedX * cos(y_angle) + rotatedZ * sin(y_angle);
            rotatedZ = -rotatedX * sin(y_angle) + rotatedZ * cos(y_angle);
            rotatedX = tempX;

            double tempY = rotatedY * cos(x_angle) - rotatedZ * sin(x_angle);
            rotatedZ = rotatedY * sin(x_angle) + rotatedZ * cos(x_angle);
            rotatedY = tempY;

            x = centre_x + (rotatedX - centre_x) * pAmount;
            y = centre_y + (rotatedY - centre_y) * pAmount;
            z = centre_z + (rotatedZ - centre_z) * pAmount;

            x_angle += x_angle_inc / angle_inc_scale;
            y_angle += y_angle_inc / angle_inc_scale;
            z_angle += z_angle_inc / angle_inc_scale;
            centre_x += centre_x_inc / centre_inc_scale;
            centre_y += centre_y_inc / centre_inc_scale;
            centre_z += centre_z_inc / centre_inc_scale;
        }

        pVarTP.x += x;
        pVarTP.y += y;
        pVarTP.z += z;

    }

    @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    @Override
    public Object[] getParameterValues() {
        return new Object[]{
                iterations, x_angle_inc, y_angle_inc, z_angle_inc,
                centre_x_inc, centre_y_inc, centre_z_inc,
                angle_inc_scale, centre_inc_scale
        };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_ITERATIONS.equalsIgnoreCase(pName))
            iterations = (int) limitVal(pValue, 1, 100);
        else if (PARAM_X_ANGLE_INC.equalsIgnoreCase(pName))
            x_angle_inc = pValue;
        else if (PARAM_Y_ANGLE_INC.equalsIgnoreCase(pName))
            y_angle_inc = pValue;
        else if (PARAM_Z_ANGLE_INC.equalsIgnoreCase(pName))
            z_angle_inc = pValue;
        else if (PARAM_CENTRE_X_INC.equalsIgnoreCase(pName))
            centre_x_inc = pValue;
        else if (PARAM_CENTRE_Y_INC.equalsIgnoreCase(pName))
            centre_y_inc = pValue;
        else if (PARAM_CENTRE_Z_INC.equalsIgnoreCase(pName))
            centre_z_inc = pValue;
        else if (PARAM_ANGLE_INC_SCALE.equalsIgnoreCase(pName))
            angle_inc_scale = pValue;
        else if (PARAM_CENTRE_INC_SCALE.equalsIgnoreCase(pName))
            centre_inc_scale = pValue;
        else
            throw new IllegalArgumentException(pName);
    }

    @Override
    public String getName() {
        return "iterate";
    }

}