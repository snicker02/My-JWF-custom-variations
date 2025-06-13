/*
 * JWildfire - an image and animation processor written in Java
 * Copyright (C) 1995-2025 Andreas Maschke
 *
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jwildfire.create.tina.variation;

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import org.jwildfire.create.tina.random.MarsagliaRandomGenerator;

import static java.lang.Math.*;

public class GreeblesFunc extends VariationFunc {
    private static final long serialVersionUID = 1L;

    private static final String PARAM_MODE = "mode";
    private static final String PARAM_BASE_SHAPE = "base_shape";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_SUBDIVISIONS = "subdivisions";
    private static final String PARAM_GREEBLE_SHAPE = "greeble_shape";
    private static final String PARAM_GREEBLE_HEIGHT = "greeble_height";
    private static final String PARAM_SEED = "seed";

    private static final String[] paramNames = { PARAM_MODE, PARAM_BASE_SHAPE, PARAM_SIZE, PARAM_SUBDIVISIONS, PARAM_GREEBLE_SHAPE, PARAM_GREEBLE_HEIGHT, PARAM_SEED };

    // Defaults
    private int mode = 0; // 0=Solid, 1=Flame
    private int base_shape = 1; // 0=Sphere, 1=Cube, 2=Plane
    private double size = 2.0;
    private int subdivisions = 10;
    private int greeble_shape = 0; // 0=Cube, 1=Cylinder, 2=Sphere, 3=Cone
    private double greeble_height = 0.2;
    private int seed = 12345;

    private transient MarsagliaRandomGenerator cell_rand = new MarsagliaRandomGenerator();

    // Helper methods
    private void cross(XYZPoint res, XYZPoint v1, XYZPoint v2) { res.x = v1.y * v2.z - v1.z * v2.y; res.y = v1.z * v2.x - v1.x * v2.z; res.z = v1.x * v2.y - v1.y * v2.x; }
    private void normalize(XYZPoint v) { double len = sqrt(v.x * v.x + v.y * v.y + v.z * v.z); if (len > 1e-9) { v.x /= len; v.y /= len; v.z /= len; } }
    private double frac(double n) { return n - floor(n); }
    
    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        
        XYZPoint p_greeble_base = new XYZPoint();
        XYZPoint p_normal = new XYZPoint();
        double greeble_base_size;

        if (base_shape == 0) { // Sphere - Use a spherical coordinate grid (UV grid)
            double halfSize = size * 0.5;
            
            double u_angle, v_angle;
            if(mode == 1) { // Flame mode
                u_angle = atan2(pAffineTP.y, pAffineTP.x);
                double r_xy = sqrt(pAffineTP.x*pAffineTP.x + pAffineTP.y*pAffineTP.y);
                v_angle = atan2(pAffineTP.z, r_xy);
            } else { // Solid mode
                u_angle = pContext.random() * 2.0 * PI;
                v_angle = acos(2.0 * pContext.random() - 1.0);
            }
            
            int u_divs = max(1, subdivisions * 2);
            int v_divs = max(1, subdivisions);

            double u_cell_size = (2.0 * PI) / u_divs;
            double v_cell_size = PI / v_divs;
            double u_quantized = floor(u_angle / u_cell_size) * u_cell_size + u_cell_size * 0.5;
            double v_quantized = floor(v_angle / v_cell_size) * v_cell_size + v_cell_size * 0.5;
            
            p_greeble_base.x = halfSize * sin(v_quantized) * cos(u_quantized);
            p_greeble_base.y = halfSize * sin(v_quantized) * sin(u_quantized);
            p_greeble_base.z = halfSize * cos(v_quantized);
            
            p_normal.assign(p_greeble_base);
            normalize(p_normal);
            greeble_base_size = (PI * size) / (double)subdivisions;
        }
        else { // Cube and Plane - Use a Cartesian grid
            XYZPoint p_on_surface = new XYZPoint();
            if(mode == 1) { p_on_surface.assign(pAffineTP); } else {
                if(base_shape == 2) { // Plane
                    p_on_surface.x = (pContext.random() - 0.5) * size; p_on_surface.y = (pContext.random() - 0.5) * size; p_on_surface.z = 0.0;
                } else { // Cube
                    int face = (int)(pContext.random() * 6);
                    double u_r = (pContext.random() - 0.5) * size, v_r = (pContext.random() - 0.5) * size;
                    double hs = size * 0.5;
                    switch(face) {
                        case 0: p_on_surface.x=hs; p_on_surface.y=u_r; p_on_surface.z=v_r; break;
                        case 1: p_on_surface.x=-hs; p_on_surface.y=u_r; p_on_surface.z=v_r; break;
                        case 2: p_on_surface.x=u_r; p_on_surface.y=hs; p_on_surface.z=v_r; break;
                        case 3: p_on_surface.x=u_r; p_on_surface.y=-hs; p_on_surface.z=v_r; break;
                        case 4: p_on_surface.x=u_r; p_on_surface.y=v_r; p_on_surface.z=hs; break;
                        case 5: p_on_surface.x=u_r; p_on_surface.y=v_r; p_on_surface.z=-hs; break;
                    }
                }
            }
            
            double halfSize = size*0.5;
            if (base_shape == 2) { p_on_surface.z=0; p_normal.x=0; p_normal.y=0; p_normal.z=1;}
            else {
                double absX=abs(p_on_surface.x), absY=abs(p_on_surface.y), absZ=abs(p_on_surface.z);
                if(absX>=absY && absX>=absZ){p_on_surface.x=(p_on_surface.x>0)?halfSize:-halfSize; p_normal.x=(p_on_surface.x>0)?1:-1;p_normal.y=0;p_normal.z=0;}
                else if(absY>=absX && absY>=absZ){p_on_surface.y=(p_on_surface.y>0)?halfSize:-halfSize; p_normal.x=0; p_normal.y=(p_on_surface.y>0)?1:-1;p_normal.z=0;}
                else{p_on_surface.z=(p_on_surface.z>0)?halfSize:-halfSize; p_normal.x=0; p_normal.y=0;p_normal.z=(p_on_surface.z>0)?1:-1;}
            }

             greeble_base_size = (subdivisions > 0) ? size / subdivisions : size;
             if (greeble_base_size < 1.0E-6) { pVarTP.doHide = true; return; }
             int ix = (int)floor(p_on_surface.x / greeble_base_size);
             int iy = (int)floor(p_on_surface.y / greeble_base_size);
             int iz = (int)floor(p_on_surface.z / greeble_base_size);
             p_greeble_base.x = (double)ix * greeble_base_size + greeble_base_size * 0.5;
             p_greeble_base.y = (double)iy * greeble_base_size + greeble_base_size * 0.5;
             p_greeble_base.z = (double)iz * greeble_base_size + greeble_base_size * 0.5;
        }
        
        long cell_seed = seed + (long)(p_greeble_base.x*100) * 73856093L ^ (long)(p_greeble_base.y*100) * 19349663L ^ (long)(p_greeble_base.z*100) * 83492791L;
        cell_rand.randomize(cell_seed);
        double height = greeble_height * cell_rand.random();
        
        XYZPoint tangent = new XYZPoint(), bitangent = new XYZPoint(), up_vec = new XYZPoint(); up_vec.y = 1.0;
        if (abs(p_normal.y) > 0.999) { up_vec.y = 0.0; up_vec.x = 1.0; }
        cross(tangent, p_normal, up_vec); normalize(tangent);
        cross(bitangent, p_normal, tangent); normalize(bitangent);

        double u=0, v=0, w=0;

        if (mode == 0) { // Solid Mode
            switch(greeble_shape) {
                case 1: // Cylinder
                    double angle_cyl = pContext.random() * 2.0 * PI, radius_cyl = (greeble_base_size * 0.5) * sqrt(pContext.random());
                    u = cos(angle_cyl) * radius_cyl; v = sin(angle_cyl) * radius_cyl; w = pContext.random() * height;
                    break;
                case 2: // Sphere / Ellipsoid
                    double r_x = pContext.random() - 0.5, r_y = pContext.random() - 0.5, r_z = pContext.random() - 0.5;
                    double r_len = sqrt(r_x*r_x + r_y*r_y + r_z*r_z);
                    if (r_len > 1e-9) {
                        double radius = pow(pContext.random(), 1.0/3.0);
                        u = (r_x/r_len) * radius * (greeble_base_size * 0.5); v = (r_y/r_len) * radius * (greeble_base_size * 0.5); w = (r_z/r_len) * radius * height;
                    } break;
                case 3: // Cone / Pyramid
                    double angle_cone = pContext.random() * 2.0 * PI; w = pContext.random() * height;
                    double taper_factor = (height > 1e-9) ? (1.0 - (w / height)) : 1.0;
                    double radius_cone = (greeble_base_size * 0.5) * sqrt(pContext.random()) * taper_factor;
                    u = cos(angle_cone) * radius_cone; v = sin(angle_cone) * radius_cone;
                    break;
                case 0: // Cube
                default:
                    u = (pContext.random() - 0.5) * greeble_base_size; v = (pContext.random() - 0.5) * greeble_base_size; w = pContext.random() * height;
                    break;
            }
        } else { // Flame Mode
            XYZPoint relative_p = new XYZPoint();
            relative_p.x = pAffineTP.x - p_greeble_base.x;
            relative_p.y = pAffineTP.y - p_greeble_base.y;
            relative_p.z = pAffineTP.z - p_greeble_base.z;
            double local_u = relative_p.x * tangent.x + relative_p.y * tangent.y + relative_p.z * tangent.z;
            double local_v = relative_p.x * bitangent.x + relative_p.y * bitangent.y + relative_p.z * bitangent.z;
            double local_w = relative_p.x * p_normal.x + relative_p.y * p_normal.y + relative_p.z * p_normal.z;
            
            switch(greeble_shape) {
                case 1: // Cylinder
                    double angle_cyl = atan2(local_v, local_u);
                    double radius_cyl_base = sqrt(local_u*local_u + local_v*local_v);
                    double radius_cyl = (greeble_base_size * 0.5) * frac(radius_cyl_base);
                    u = cos(angle_cyl) * radius_cyl; v = sin(angle_cyl) * radius_cyl; w = frac(local_w) * height;
                    break;
                case 2: // Sphere
                    double r_len_flame = sqrt(local_u*local_u + local_v*local_v + local_w*local_w);
                    double radius_sphere = (greeble_base_size * 0.5) * frac(r_len_flame);
                    if (r_len_flame > 1e-9) {
                        u = (local_u / r_len_flame) * radius_sphere; v = (local_v / r_len_flame) * radius_sphere; w = (local_w / r_len_flame) * radius_sphere;
                    } break;
                case 3: // Cone
                    double angle_cone = atan2(local_v, local_u);
                    double radius_cone_base = sqrt(local_u*local_u + local_v*local_v);
                    w = frac(local_w) * height;
                    double taper_factor = (height > 1e-9) ? (1.0 - (w / height)) : 1.0;
                    double radius_cone = (greeble_base_size * 0.5) * frac(radius_cone_base) * taper_factor;
                    u = cos(angle_cone) * radius_cone; v = sin(angle_cone) * radius_cone;
                    break;
                case 0: // Cube
                default:
                    u = (frac(local_u) - 0.5) * greeble_base_size; v = (frac(local_v) - 0.5) * greeble_base_size; w = frac(local_w) * height;
                    break;
            }
        }

        double final_x = p_greeble_base.x + (tangent.x * u) + (bitangent.x * v) + (p_normal.x * w);
        double final_y = p_greeble_base.y + (tangent.y * u) + (bitangent.y * v) + (p_normal.y * w);
        double final_z = p_greeble_base.z + (tangent.z * u) + (bitangent.z * v) + (p_normal.z * w);

        pVarTP.x += final_x * pAmount;
        pVarTP.y += final_y * pAmount;
        pVarTP.z += final_z * pAmount;
    }

    @Override
    public String[] getParameterNames() { return paramNames; }

    @Override
    public Object[] getParameterValues() {
        return new Object[]{ mode, base_shape, size, subdivisions, greeble_shape, greeble_height, seed };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_MODE.equalsIgnoreCase(pName)) mode = (int) pValue;
        else if (PARAM_BASE_SHAPE.equalsIgnoreCase(pName)) base_shape = (int) pValue;
        else if (PARAM_SIZE.equalsIgnoreCase(pName)) size = pValue;
        else if (PARAM_SUBDIVISIONS.equalsIgnoreCase(pName)) subdivisions = (int) pValue;
        else if (PARAM_GREEBLE_SHAPE.equalsIgnoreCase(pName)) greeble_shape = (int) pValue;
        else if (PARAM_GREEBLE_HEIGHT.equalsIgnoreCase(pName)) greeble_height = pValue;
        else if (PARAM_SEED.equalsIgnoreCase(pName)) seed = (int) pValue;
        else throw new IllegalArgumentException(pName);
    }
    
    @Override
    public String getName() { return "greebles"; }

    @Override
    public VariationFuncType[] getVariationTypes() { return new VariationFuncType[]{VariationFuncType.VARTYPE_3D, VariationFuncType.VARTYPE_BASE_SHAPE}; }
}
