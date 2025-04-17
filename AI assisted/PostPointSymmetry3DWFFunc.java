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

public class PostPointSymmetry3DWFFunc extends VariationFunc{
    private static final long serialVersionUID = 1L;

    // Center parameters
    public static final String PARAM_CENTRE_X = "centre_x";
    public static final String PARAM_CENTRE_Y = "centre_y";
    public static final String PARAM_CENTRE_Z = "centre_z";

    // Order and color parameters
    public static final String PARAM_ORDER = "order";
    private static final String PARAM_COLORSHIFT = "colorshift";

    // Rotation parameters
    private static final String PARAM_Z_ANGLE = "z_angle";
    private static final String PARAM_Y_ANGLE = "y_angle";
    private static final String PARAM_X_ANGLE = "x_angle";

    // Plane reflection parameters
    private static final String PARAM_PLANE = "plane";
    private static final String PARAM_ORDER_MODE = "order_mode";
    private static final String PARAM_PLANE_A = "plane_a";
    private static final String PARAM_PLANE_B = "plane_b";
    private static final String PARAM_PLANE_C = "plane_c";
    private static final String PARAM_PLANE_D = "plane_d";

    // Arbitrary Z-axis rotation parameters
    private static final String PARAM_Z_AXIS_X = "z_axis_x"; // X-component of z-axis direction
    private static final String PARAM_Z_AXIS_Y = "z_axis_y"; // Y-component of z-axis direction

    // Scaling parameters
    private static final String PARAM_SCALE_X = "scale_x"; // New: Scaling along axes
    private static final String PARAM_SCALE_Y = "scale_y";
    private static final String PARAM_SCALE_Z = "scale_z";

    private static final String[] paramNames = {
            PARAM_CENTRE_X, PARAM_CENTRE_Y, PARAM_CENTRE_Z,
            PARAM_ORDER, PARAM_COLORSHIFT,
            PARAM_Z_ANGLE, PARAM_Y_ANGLE, PARAM_X_ANGLE,
            PARAM_PLANE, PARAM_ORDER_MODE, PARAM_PLANE_A, PARAM_PLANE_B, PARAM_PLANE_C, PARAM_PLANE_D,
            PARAM_Z_AXIS_X, PARAM_Z_AXIS_Y,
            PARAM_SCALE_X, PARAM_SCALE_Y, PARAM_SCALE_Z
    };

    // Center parameters
    private double centre_x = 0.25;
    private double centre_y = 0.5;
    private double centre_z = 0.0;

    // Order and color parameters
    private int order = 3;
    private double colorshift = 0;

    // Rotation parameters
    private double z_angle = 0.0;
    private double y_angle = 0.0;
    private double x_angle = 0.0;

    // Plane reflection parameters
    private int plane = 0; // 0: None, 1: XY, 2: XZ, 3: YZ, 4-7: Combined, 8: General
    private int order_mode = 0; // 0: Rotation, then Reflection, 1: Reflection, then Rotation
    private double plane_a = 0.0;
    private double plane_b = 0.0;
    private double plane_c = 1.0;
    private double plane_d = 0.0;

    // Arbitrary Z-axis rotation parameters
    private double z_axis_x = 0.0; // X-component of z-axis direction
    private double z_axis_y = 1.0; // Y-component of z-axis direction

    // Scaling parameters
    private double scale_x = 1.0; // New: Scaling along axes
    private double scale_y = 1.0;
    private double scale_z = 1.0;

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP,
                        double pAmount) {
        double x = pVarTP.x; // Start with the pre-transformed coordinates
        double y = pVarTP.y;
        double z = pVarTP.z;

        double rotatedX, rotatedY, rotatedZ;

        // Apply scaling
        x *= scale_x;
        y *= scale_y;
        z *= scale_z;

        // Rotate around the z-axis (arbitrary direction)
        double zAxisNorm = Math.sqrt(z_axis_x * z_axis_x + z_axis_y * z_axis_y);
        double ux = z_axis_x / zAxisNorm; // Unit vector for z-axis direction
        double uy = z_axis_y / zAxisNorm;

        rotatedX = x * cos(z_angle) - (y * ux - x * uy) * sin(z_angle) * uy;
        rotatedY = x * sin(z_angle) * ux + y * (ux * ux + cos(z_angle) * uy * uy) + z * sin(z_angle) * ux;
        rotatedZ = -x * sin(z_angle) * uy + y * sin(z_angle) * ux + z * cos(z_angle);

        // Rotate around the y-axis
        double tempX = rotatedX * cos(y_angle) + rotatedZ * sin(y_angle);
        rotatedZ = -rotatedX * sin(y_angle) + rotatedZ * cos(y_angle);
        rotatedX = tempX;

        // Rotate around the x-axis
        double tempY = rotatedY * cos(x_angle) - rotatedZ * sin(x_angle);
        rotatedZ = rotatedY * sin(x_angle) + rotatedZ * cos(x_angle);
        rotatedY = tempY;

        double dx = (rotatedX - centre_x) * pAmount;
        double dy = (rotatedY - centre_y) * pAmount;
        double dz = (rotatedZ - centre_z) * pAmount;
        int idx = pContext.random(order);

        // Apply reflection (if needed) *before* rotation
        if (order_mode == 1) {
            if ((plane & 1) != 0) z = -z; // XY
            if ((plane & 2) != 0) y = -y; // XZ
            if ((plane & 4) != 0) x = -x; // YZ
            if (plane == 8) reflectPoint(x, y, z, plane_a, plane_b, plane_c, plane_d, pVarTP); // General
        }

        // Apply rotational symmetry
        pVarTP.x = centre_x + dx * _cosa[idx] + dy * _sina[idx];
        pVarTP.y = centre_y + dy * _cosa[idx] - dx * _sina[idx];
        pVarTP.z = centre_z + dz;

        // Apply reflection (if needed) *after* rotation
        if (order_mode == 0) {
            if ((plane & 1) != 0) pVarTP.z = -pVarTP.z; // XY
            if ((plane & 2) != 0) pVarTP.y = -pVarTP.y; // XZ
            if ((plane & 4) != 0) pVarTP.x = -pVarTP.x; // YZ
            if (plane == 8) reflectPoint(pVarTP.x, pVarTP.y, pVarTP.z, plane_a, plane_b, plane_c, plane_d, pVarTP); // General
        }

        // Rotate back around the x-axis
        double tempY2 = pVarTP.y * cos(-x_angle) + pVarTP.z * sin(-x_angle);
        pVarTP.z = -pVarTP.y * sin(-x_angle) + pVarTP.z * cos(-x_angle);
        pVarTP.y = tempY2;

        // Rotate back around the y-axis
        double tempX2 = pVarTP.x * cos(-y_angle) - pVarTP.z * sin(-y_angle);
        pVarTP.z = pVarTP.x * sin(-y_angle) + pVarTP.z * cos(-y_angle);
        pVarTP.x = tempX2;

        // Rotate back around the z-axis (arbitrary direction)
        double finalX = pVarTP.x * cos(-z_angle) - (pVarTP.y * ux + pVarTP.x * uy) * sin(-z_angle) * uy;
        double finalY = -pVarTP.x * sin(-z_angle) * uy + pVarTP.y * (uy * uy + cos(-z_angle) * ux * ux) + pVarTP.z * sin(-z_angle) * ux;
        double finalZ = pVarTP.x * sin(-z_angle) * uy - pVarTP.y * sin(-z_angle) * ux + pVarTP.z * cos(-z_angle);
        pVarTP.x = finalX;
        pVarTP.y = finalY;
        pVarTP.z = finalZ;

        pVarTP.color = fmod(pVarTP.color + idx * colorshift, 1.0);
    }

    private void reflectPoint(double x, double y, double z, double a, double b, double c, double d, XYZPoint pVarTP) {
        double norm = Math.sqrt(a * a + b * b + c * c);
        double distance = (a * x + b * y + c * z + d) / norm;

        pVarTP.x = x - 2 * a * distance / norm;
        pVarTP.y = y - 2 * b * distance / norm;
        pVarTP.z = z - 2 * c * distance / norm;
    }

    @Override
    public String[] getParameterNames() {
        return paramNames;
    }

    @Override
    public Object[] getParameterValues() {
        return new Object[]{
                centre_x, centre_y, centre_z,
                order, colorshift,
                z_angle, y_angle, x_angle,
                plane, order_mode, plane_a, plane_b, plane_c, plane_d,
                z_axis_x, z_axis_y,
                scale_x, scale_y, scale_z
        };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_CENTRE_X.equalsIgnoreCase(pName))
            centre_x = pValue;
        else if (PARAM_CENTRE_Y.equalsIgnoreCase(pName))
            centre_y = pValue;
        else if (PARAM_CENTRE_Z.equalsIgnoreCase(pName))
            centre_z = pValue;
        else if (PARAM_ORDER.equalsIgnoreCase(pName))
            order = limitIntVal(Tools.FTOI(pValue), 1, Integer.MAX_VALUE);
        else if (PARAM_COLORSHIFT.equalsIgnoreCase(pName))
            colorshift = pValue;
        else if (PARAM_Z_ANGLE.equalsIgnoreCase(pName))
            z_angle = pValue;
        else if (PARAM_Y_ANGLE.equalsIgnoreCase(pName))
            y_angle = pValue;
        else if (PARAM_X_ANGLE.equalsIgnoreCase(pName))
            x_angle = pValue;
        else if (PARAM_PLANE.equalsIgnoreCase(pName))
            plane = (int) limitVal(pValue, 0, 8);
        else if (PARAM_ORDER_MODE.equalsIgnoreCase(pName))
            order_mode = (int) limitVal(pValue, 0, 1);
        else if (PARAM_PLANE_A.equalsIgnoreCase(pName))
            plane_a = pValue;
        else if (PARAM_PLANE_B.equalsIgnoreCase(pName))
            plane_b = pValue;
        else if (PARAM_PLANE_C.equalsIgnoreCase(pName))
            plane_c = pValue;
        else if (PARAM_PLANE_D.equalsIgnoreCase(pName))
            plane_d = pValue;
        else if (PARAM_Z_AXIS_X.equalsIgnoreCase(pName))
            z_axis_x = pValue;
        else if (PARAM_Z_AXIS_Y.equalsIgnoreCase(pName))
            z_axis_y = pValue;
        else if (PARAM_SCALE_X.equalsIgnoreCase(pName))
            scale_x = pValue;
        else if (PARAM_SCALE_Y.equalsIgnoreCase(pName))
            scale_y = pValue;
        else if (PARAM_SCALE_Z.equalsIgnoreCase(pName))
            scale_z = pValue;
        else
            throw new IllegalArgumentException(pName);
    }

    @Override
    public String getName() {
        return "post_point_symmetry_3d_wf"; // Extended name
    }

    @Override
    public int getPriority() {
        return 1;
    }

    private double _sina[], _cosa[];

    @Override
    public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
        _sina = new double[order];
        _cosa = new double[order];
        double da = M_2PI / (double) order;
        double angle = 0.0;
        for (int i = 0; i < order; i++) {
            _sina[i] = sin(angle);
            _cosa[i] = cos(angle);
            angle += da;
        }
    }
}