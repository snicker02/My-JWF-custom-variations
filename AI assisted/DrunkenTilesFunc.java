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
 * Lesser General Public License for more details *
 * You should have received a copy of the GNU Lesser General Public License along with this software;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jwildfire.create.tina.variation;

import java.util.Random;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import java.lang.Math; // Using standard Math functions

/**
 * Drunken Tiles Variation for JWildfire. (Formerly PerturbedGridLazyTwistFunc)
 *
 * Applies LazyJess+Twist effects locally around centers based on a grid,
 * where each center's position is randomly perturbed ("drunken" tiles).
 * Confined within various boundary shapes.
 */
// ### Renamed Class ###
public class DrunkenTilesFunc extends VariationFunc {
    private static final long serialVersionUID = 1L;

    // --- Parameters ---
    private static final String PARAM_SEED = "seed";
    private static final String PARAM_CELLSIZE = "cellsize";
    private static final String PARAM_RADIUS_FACTOR = "radius_factor";
    private static final String PARAM_OFFSET_STRENGTH = "offset_strength";
    private static final String PARAM_SHAPE_BOUNDARY_TYPE = "shape_boundary_type"; // 0:Circ, 1:Sq, 2:Elps, 3:Tri, 4:Rhom, 5:Hex, 6:Star5, 7:Cross, 8:Ring
    private static final String PARAM_ASPECT_RATIO = "aspect_ratio";
    private static final String PARAM_INNER_RADIUS_FACTOR = "inner_radius_factor";
    private static final String PARAM_ARM_WIDTH_FACTOR = "arm_width_factor";
    private static final String PARAM_SPACING = "spacing";
    private static final String PARAM_INNER_TWIST = "inner_twist";
    private static final String PARAM_OUTER_TWIST = "outer_twist";


    // Parameter names array (unchanged structure, just used by this class)
    private static final String[] paramNames = {
            PARAM_SEED, PARAM_CELLSIZE, PARAM_RADIUS_FACTOR, PARAM_OFFSET_STRENGTH,
            PARAM_SHAPE_BOUNDARY_TYPE, PARAM_ASPECT_RATIO,
            PARAM_INNER_RADIUS_FACTOR, PARAM_ARM_WIDTH_FACTOR,
            PARAM_SPACING, PARAM_INNER_TWIST, PARAM_OUTER_TWIST
    };

    // Default values (unchanged)
    private int seed = 1; private double cellsize = 0.5; private double radius_factor = 1.0;
    private double offset_strength = 0.25; private int shape_boundary_type = 0;
    private double aspect_ratio = 1.0; private double inner_radius_factor = 0.5;
    private double arm_width_factor = 0.3; private double spacing = 0.1;
    private double inner_twist = 0.0; private double outer_twist = Math.PI;


    // --- Internal State --- (unchanged)
    private transient Random random = new Random();
    private transient boolean needsReinitCalcs = true;
    private transient double radius;
    private transient double _r2;

    // Constants for shape types (unchanged)
    private static final int BOUNDARY_CIRCLE=0; private static final int BOUNDARY_SQUARE=1; private static final int BOUNDARY_ELLIPSE=2; private static final int BOUNDARY_TRIANGLE=3; private static final int BOUNDARY_RHOMBUS=4; private static final int BOUNDARY_HEXAGON=5; private static final int BOUNDARY_STAR5=6; private static final int BOUNDARY_CROSS=7; private static final int BOUNDARY_RING=8;
    private static final int NUM_BOUNDARY_TYPES = 9;


    /**
     * Initializes calculated values based on parameters if needed. (Unchanged)
     */
    private void initializeIfNeeded() {
        if (needsReinitCalcs) {
             if (cellsize <= 1e-9) cellsize = 1e-9;
             radius = Math.abs(cellsize * 0.5 * radius_factor);
             if (radius <= 1e-9) radius = 1e-9;
             _r2 = radius * radius;
             needsReinitCalcs = false;
        }
         if (_r2 <= 0 && radius > 1e-9) _r2 = radius * radius;
         if (radius <= 0 && cellsize > 1e-9 && radius_factor > 1e-9) {
             radius = Math.abs(cellsize * 0.5 * radius_factor);
             _r2 = radius * radius;
         }
    }

    /**
    * Helper function for Triangle boundary check. (Unchanged)
    */
    private static double pointSign(double p1x, double p1y, double p2x, double p2y, double p3x, double p3y) {
        return (p1x - p3x) * (p2y - p3y) - (p2x - p3x) * (p1y - p3y);
    }

    @Override
    public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
        initializeIfNeeded();

        if (cellsize <= 1e-9 || radius <= 1e-9) {
             pVarTP.x += pAmount * pAffineTP.x; pVarTP.y += pAmount * pAffineTP.y;
             if (pContext.isPreserveZCoordinate()) pVarTP.z += pAmount * pAffineTP.z;
             return;
        }

        double inputX = pAffineTP.x; double inputY = pAffineTP.y;
        double finalX = inputX; double finalY = inputY;

        // Determine Grid Cell and Perturbed Center (Unchanged)
        double invCellSize = 1.0 / cellsize;
        int ix = (int)Math.floor(inputX * invCellSize); int iy = (int)Math.floor(inputY * invCellSize);
        double Cx_reg = (ix + 0.5) * cellsize; double Cy_reg = (iy + 0.5) * cellsize;
        long cellSeed = seed ^ (Integer.hashCode(ix) * 31) ^ Integer.hashCode(iy);
        if(random == null) random = new Random(); random.setSeed(cellSeed);
        double Ox = (random.nextDouble()*2.0-1.0)*offset_strength*cellsize; double Oy = (random.nextDouble()*2.0-1.0)*offset_strength*cellsize;
        double Cx_pert = Cx_reg + Ox; double Cy_pert = Cy_reg + Oy;
        double Lx = inputX - Cx_pert; double Ly = inputY - Cy_pert;
        double localDistSq = -1.0;

        // Determine Boundary Type (Global) and Check if Inside (Unchanged logic)
        boolean apply_effect = false;
        int currentBoundaryType = shape_boundary_type;
        switch (currentBoundaryType) {
            case BOUNDARY_CIRCLE: localDistSq=Lx*Lx+Ly*Ly; if(localDistSq<=_r2) apply_effect=true; break;
            case BOUNDARY_SQUARE: if(Math.abs(Lx)<=radius && Math.abs(Ly)<=radius) { apply_effect=true; localDistSq=Lx*Lx+Ly*Ly;} break;
            case BOUNDARY_ELLIPSE: { double ar=Math.max(1e-9,Math.abs(aspect_ratio)); double rx=radius; double ry=radius/ar; double rx_sq=rx*rx; double ry_sq=ry*ry; if(rx_sq>1e-12 && ry_sq>1e-12){ if(((Lx*Lx)/rx_sq+(Ly*Ly)/ry_sq)<=1.0){apply_effect=true; localDistSq=Lx*Lx+Ly*Ly;}} break; }
            case BOUNDARY_TRIANGLE: { double r=radius; double s32=Math.sqrt(3.0)/2.0; double v1x=0,v1y=r; double v2x=r*s32,v2y=-r/2.0; double v3x=-r*s32,v3y=-r/2.0; double d1=pointSign(Lx,Ly,v1x,v1y,v2x,v2y); double d2=pointSign(Lx,Ly,v2x,v2y,v3x,v3y); double d3=pointSign(Lx,Ly,v3x,v3y,v1x,v1y); boolean hn=(d1<0)||(d2<0)||(d3<0); boolean hp=(d1>0)||(d2>0)||(d3>0); if(!(hn&&hp)){apply_effect=true; localDistSq=Lx*Lx+Ly*Ly;} break; }
            case BOUNDARY_RHOMBUS: { double a=-Math.PI/4.0; double c=Math.cos(a); double s=Math.sin(a); double rLx=c*Lx-s*Ly; double rLy=s*Lx+c*Ly; if(Math.abs(rLx)<=radius && Math.abs(rLy)<=radius){apply_effect=true; localDistSq=Lx*Lx+Ly*Ly;} break; }
            case BOUNDARY_HEXAGON: { double r=radius; double angleStep=Math.PI/3.0; boolean inside=true; double px=Lx; double py=Ly; double v1x=0,v1y=0,v2x=0,v2y=0; v2x=0; v2y=r; for(int i=0;i<6;i++){ v1x=v2x; v1y=v2y; double currentAngle=(Math.PI/2.0)+(i+1)*angleStep; v2x=r*Math.cos(currentAngle); v2y=r*Math.sin(currentAngle); if((v2x-v1x)*(py-v1y)-(v2y-v1y)*(px-v1x)<0){inside=false;break;} } if(inside){apply_effect=true; localDistSq=Lx*Lx+Ly*Ly;} break; }
            case BOUNDARY_STAR5: { double rO=radius; double rI=radius*Math.max(0.01,Math.min(1.0,inner_radius_factor)); double angleStep=Math.PI/5.0; boolean inside=true; double px=Lx; double py=Ly; double v1x=0,v1y=0,v2x=0,v2y=0; v2x=0; v2y=rO; for(int i=0;i<10;i++){ v1x=v2x; v1y=v2y; double currentAngle=(Math.PI/2.0)+(i+1)*angleStep; double cR=((i+1)%2==0)?rO:rI; v2x=cR*Math.cos(currentAngle); v2y=cR*Math.sin(currentAngle); if((v2x-v1x)*(py-v1y)-(v2y-v1y)*(px-v1x)<0){inside=false;break;} } if(inside){apply_effect=true; localDistSq=Lx*Lx+Ly*Ly;} break; }
            case BOUNDARY_CROSS: { double arm=radius*Math.max(0.01,Math.min(1.0,arm_width_factor)); if((Math.abs(Lx)<=arm && Math.abs(Ly)<=radius)||(Math.abs(Lx)<=radius && Math.abs(Ly)<=arm)){apply_effect=true; localDistSq=Lx*Lx+Ly*Ly;} break; }
            case BOUNDARY_RING: { double rI=radius*Math.max(0.0,Math.min(0.99,inner_radius_factor)); double inner_r2=rI*rI; localDistSq=Lx*Lx+Ly*Ly; if(localDistSq>=inner_r2 && localDistSq<=_r2){apply_effect=true;} break; }
            default: localDistSq=Lx*Lx+Ly*Ly; if(localDistSq<=_r2) apply_effect=true; break;
        }

        // Apply Local LazyJess Logic + Twist if Inside Boundary (Unchanged logic)
        if (apply_effect) {
            double scaleFactor = (radius < 1e-9) ? 1.0 : 1.0 / radius;
            double scaledLx = Lx * scaleFactor; double scaledLy = Ly * scaleFactor;
            if (scaledLx > 1.0) scaledLx -= 2.0; if (scaledLx < -1.0) scaledLx += 2.0;
            if (scaledLy > 1.0) scaledLy -= 2.0; if (scaledLy < -1.0) scaledLy += 2.0;
            double k = 1.0 + spacing;
            double ljLx = scaledLx * k * radius; double ljLy = scaledLy * k * radius;

            double twist_norm;
            if (currentBoundaryType == BOUNDARY_SQUARE) { twist_norm = (radius < 1e-9) ? 0.0 : Math.min(1.0, Math.max(Math.abs(Lx), Math.abs(Ly)) / radius); }
            else { if (localDistSq < 0) localDistSq = Lx * Lx + Ly * Ly; twist_norm = (radius < 1e-9 || localDistSq < 0) ? 0.0 : Math.min(1.0, Math.sqrt(localDistSq) / radius); }
            double theta = inner_twist * (1.0 - twist_norm) + outer_twist * twist_norm;
            double s = Math.sin(theta); double c = Math.cos(theta);
            double twistedLx = c * ljLx - s * ljLy; double twistedLy = s * ljLx + c * ljLy;
            finalX = Cx_pert + twistedLx; finalY = Cy_pert + twistedLy;
        }

        pVarTP.x += pAmount * finalX; pVarTP.y += pAmount * finalY;
        if (pContext.isPreserveZCoordinate()) pVarTP.z += pAmount * pAffineTP.z;
    }

    // --- Parameter Handling ---

    @Override
    public String[] getParameterNames() { return paramNames; }

    @Override
    public Object[] getParameterValues() {
        return new Object[]{ seed, cellsize, radius_factor, offset_strength, shape_boundary_type, aspect_ratio, inner_radius_factor, arm_width_factor, spacing, inner_twist, outer_twist };
    }

    @Override
    public void setParameter(String pName, double pValue) {
        if (PARAM_SEED.equalsIgnoreCase(pName)) { seed = (int) pValue; }
        else if (PARAM_CELLSIZE.equalsIgnoreCase(pName)) { double v=Math.max(1e-9, pValue); if(Math.abs(cellsize-v)>1e-12){cellsize=v; needsReinitCalcs=true;} }
        else if (PARAM_RADIUS_FACTOR.equalsIgnoreCase(pName)) { double v=Math.max(1e-9, pValue); if(Math.abs(radius_factor-v)>1e-12){radius_factor=v; needsReinitCalcs=true;} }
        else if (PARAM_OFFSET_STRENGTH.equalsIgnoreCase(pName)) { offset_strength = pValue; }
        else if (PARAM_SHAPE_BOUNDARY_TYPE.equalsIgnoreCase(pName)) { shape_boundary_type = (int) pValue; }
        else if (PARAM_ASPECT_RATIO.equalsIgnoreCase(pName)) { aspect_ratio = pValue; }
        else if (PARAM_INNER_RADIUS_FACTOR.equalsIgnoreCase(pName)) { inner_radius_factor = pValue; }
        else if (PARAM_ARM_WIDTH_FACTOR.equalsIgnoreCase(pName)) { arm_width_factor = pValue; }
        else if (PARAM_SPACING.equalsIgnoreCase(pName)) { spacing = pValue; }
        else if (PARAM_INNER_TWIST.equalsIgnoreCase(pName)) { inner_twist = pValue; }
        else if (PARAM_OUTER_TWIST.equalsIgnoreCase(pName)) { outer_twist = pValue; }
        else { throw new IllegalArgumentException("Unknown parameter: " + pName); }
    }

    // ### Renamed ###
    @Override
    public String getName() {
        return "drunkenTiles";
    }

    @Override
    public VariationFuncType[] getVariationTypes() {
        return new VariationFuncType[]{VariationFuncType.VARTYPE_2D};
    }

    // ### Renamed prefix to dt_ ###
    @Override
    public String[] getParameterAlternativeNames() {
       return new String[]{
               "dt_seed", "dt_cellsize", "dt_radius_factor", "dt_offset_str",
               "dt_shape_boundary", // Boundary (0=Circ, 1=Sq, 2=Elps, 3=Tri, 4=Rhom, 5=Hex, 6=Star5, 7=Cross, 8=Ring)
               "dt_aspect_ratio",   // Ellipse(2) Aspect Ratio (W/H)
               "dt_inner_radius",   // Star(6)/Ring(8) Inner Radius Factor
               "dt_arm_width",    // Cross(7) Arm Width Factor
               "dt_spacing",        // LazyJess scaling factor
               "dt_inner_twist", "dt_outer_twist" // Twist params
       };
    }

    /** Handles reading the object during deserialization. */
    @Override
    public void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        random = new Random();
        needsReinitCalcs = true;
    }
}