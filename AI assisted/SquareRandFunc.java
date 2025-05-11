/*
 * JWildfire - an image and animation processor written in Java
 * Copyright (C) 1995-2021 Andreas Maschke
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

import org.jwildfire.base.Tools; 
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min; 
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt; 
import static org.jwildfire.base.mathlib.MathLib.floor;


public class SquareRandFunc extends VariationFunc { 
  private static final long serialVersionUID = 12L; // Updated serialVersionUID

  // Original Parameters
  private static final String PARAM_SC = "Sc"; 
  private static final String PARAM_DENS = "Dens"; 
  private static final String PARAM_X_RANGE = "X"; 
  private static final String PARAM_Y_RANGE = "Y"; 
  private static final String PARAM_SEED = "Seed"; 

  // Parameters for Hollow and Atan effects
  private static final String PARAM_HOLLOW = "Hollow"; 
  private static final String PARAM_BORDER_THICKNESS = "BorderThick"; 
  private static final String PARAM_ATAN_MODE = "AtanMode"; 
  private static final String PARAM_ATAN_FACTOR = "AtanFactor"; 
  private static final String PARAM_ATAN_FREQUENCY = "AtanFrequency"; 

  // Parameters for Subdivision Effect
  private static final String PARAM_SUBDIVISION_ACTIVE = "SubdivActive";
  private static final String PARAM_SUBDIVISION_CHANCE = "SubdivChance"; 
  private static final String PARAM_SUBDIVISION_SCALE = "SubdivScale";   
  private static final String PARAM_SUBDIVISION_PLACEMENT = "SubdivPlace"; 

  private static final String[] paramNames = {
      PARAM_SC, PARAM_DENS, PARAM_X_RANGE, PARAM_Y_RANGE, PARAM_SEED,
      PARAM_HOLLOW, PARAM_BORDER_THICKNESS, PARAM_ATAN_MODE, PARAM_ATAN_FACTOR, PARAM_ATAN_FREQUENCY,
      PARAM_SUBDIVISION_ACTIVE, PARAM_SUBDIVISION_CHANCE, PARAM_SUBDIVISION_SCALE, PARAM_SUBDIVISION_PLACEMENT
  };

  // Default parameter values
  private double Sc = 1.0;
  private double Dens = 0.5;
  private double X_Range = 10.0;
  private double Y_Range = 10.0;
  private int Seed = 0;

  // Parameter defaults for effects
  private double Hollow = 0.0; 
  private double BorderThickness = 0.1; 
  private double AtanMode = 0.0; 
  private double AtanFactor = 0.0; // Defaulted to 0 for no initial warping
  private double AtanFrequency = 4.0; 

  // Parameter defaults for Subdivision
  private double SubdivisionActive = 0.0; 
  private double SubdivisionChance = 0.5; 
  private double SubdivisionScale = 0.4;  
  private double SubdivisionPlacement = 0.5; 


  private static final double AM = 1.0 / 2147483647; 

  private double DiscretNoise2(int xPos, int yPos) {
    int n = xPos + yPos * 57;
    n = (n << 13) ^ n;
    return ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) * AM;
  }

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    double currentX, currentY, maxAbsCoord;
    int gridM = 0, gridN = 0; 
    final int maxIter = 100;
    int iter = 0;

    boolean pointSatisfiesShapeLoop;
    
    boolean isHollow = this.Hollow > 0.5;
    boolean isAtanMode = this.AtanMode > 0.5; 
    boolean isSubdivisionActive = this.SubdivisionActive > 0.5; 

    double actualSquareSize; 
    
    double relativeX = 0.0; 
    double relativeY = 0.0;

    do {
      currentX = this.X_Range * (1.0 - 2.0 * pContext.random());
      currentY = this.Y_Range * (1.0 - 2.0 * pContext.random());

      gridM = (int) floor(0.5 * currentX / this.Sc);
      gridN = (int) floor(0.5 * currentY / this.Sc);

      relativeX = currentX - (gridM * 2 + 1) * this.Sc; 
      relativeY = currentY - (gridN * 2 + 1) * this.Sc; 
      
      actualSquareSize = (0.3 + 0.7 * DiscretNoise2(gridM + 10, gridN + 3)) * this.Sc; 
      maxAbsCoord = max(abs(relativeX), abs(relativeY));

      if (isHollow) { 
        double borderInnerEdge = actualSquareSize * (1.0 - this.BorderThickness);
        borderInnerEdge = max(0, borderInnerEdge); 
        pointSatisfiesShapeLoop = (maxAbsCoord <= actualSquareSize) && (maxAbsCoord >= borderInnerEdge);
      } else {
        pointSatisfiesShapeLoop = maxAbsCoord <= actualSquareSize;
      }

      if (++iter > maxIter) {
        return; 
      }
    } while (DiscretNoise2(gridM + this.Seed, gridN) > this.Dens || !pointSatisfiesShapeLoop); 
    
    // Initialize final coordinates with the point on the main square's border
    double finalX = relativeX; 
    double finalY = relativeY;

    boolean subdivisionWasAppliedThisIteration = false;
    double subCenterX = 0.0; // Center of the sub-square in main square's relative coords
    double subCenterY = 0.0;
    double subScaleFactor = 1.0; // Scale of the sub-square

    // --- Stage 1: Determine if subdivision occurs and calculate sub-square parameters ---
    if (isHollow && isSubdivisionActive && pointSatisfiesShapeLoop) {
        double effectiveSubdivisionChance = this.SubdivisionChance * DiscretNoise2(gridM + this.Seed + 100, gridN + this.Seed + 101);
        effectiveSubdivisionChance = Math.max(0.0, Math.min(effectiveSubdivisionChance, 1.0));

        if (pContext.random() < effectiveSubdivisionChance) {
            subdivisionWasAppliedThisIteration = true;

            double scaleNoise = DiscretNoise2(gridM + this.Seed + 200, gridN + this.Seed + 201);
            subScaleFactor = 0.01 + scaleNoise * (this.SubdivisionScale - 0.01); // Actual scale factor
            subScaleFactor = Math.max(0.01, Math.min(subScaleFactor, 1.0));

            double effectiveSubdivisionPlacement = this.SubdivisionPlacement * DiscretNoise2(gridM + this.Seed + 300, gridN + this.Seed + 301);
            effectiveSubdivisionPlacement = Math.max(0.0, Math.min(effectiveSubdivisionPlacement, 1.0));
            
            double offsetMagnitude = actualSquareSize * (1.0 - subScaleFactor) * effectiveSubdivisionPlacement;
            double randCorner = pContext.random();
            if (randCorner < 0.25) { subCenterX = -offsetMagnitude; subCenterY =  offsetMagnitude;}
            else if (randCorner < 0.5) { subCenterX =  offsetMagnitude; subCenterY =  offsetMagnitude;}
            else if (randCorner < 0.75) { subCenterX = -offsetMagnitude; subCenterY = -offsetMagnitude;}
            else { subCenterX =  offsetMagnitude; subCenterY = -offsetMagnitude;}
        }
    }

    // --- Stage 2: Geometric Placement ---
    // If subdivision occurred, remap (finalX, finalY) to be on the sub-square's border.
    if (subdivisionWasAppliedThisIteration) {
        // Local coordinates of the point if it were on the sub-square's border (sub-square centered at origin):
        double localSubX = relativeX * subScaleFactor; // Scale down the original relative point
        double localSubY = relativeY * subScaleFactor;

        // Shift these local sub-square points to the actual sub-square center:
        finalX = localSubX + subCenterX;
        finalY = localSubY + subCenterY;
    }
    // If no subdivision, finalX/finalY are still relativeX/relativeY (on main square border)

    // --- Stage 3: Apply Atan Radial Modulation (if AtanMode is active) ---
    // The modulation is applied to (finalX, finalY) relative to the center of the shape they currently represent.
    if (isHollow && isAtanMode && pointSatisfiesShapeLoop) {
        double centerXForAtan, centerYForAtan;
        double pointXForAtan, pointYForAtan; // Point coordinates relative to the center for Atan

        if (subdivisionWasAppliedThisIteration) {
            centerXForAtan = subCenterX;
            centerYForAtan = subCenterY;
            // Get the point's position relative to the sub-square's center
            pointXForAtan = finalX - subCenterX; 
            pointYForAtan = finalY - subCenterY;
        } else {
            centerXForAtan = 0.0; // Main square is centered at (0,0) in relative coords
            centerYForAtan = 0.0;
            pointXForAtan = finalX; // (which is relativeX)
            pointYForAtan = finalY; // (which is relativeY)
        }

        double radiusForModulation = sqrt(pointXForAtan * pointXForAtan + pointYForAtan * pointYForAtan);

        if (radiusForModulation > 1e-9) {
            double angle = atan2(pointYForAtan, pointXForAtan);
            double m = this.AtanFactor * sin(angle * this.AtanFrequency);
            double scaleFactor;
            if (m >= 0) {
                scaleFactor = 1.0 + m;
            } else { // m < 0
                scaleFactor = 1.0 / (1.0 - m); 
            }
            
            double modulatedRadius = radiusForModulation * scaleFactor;
            
            // Modulated points in the local system (relative to centerXForAtan, centerYForAtan)
            double modulatedLocalX = modulatedRadius * cos(angle); 
            double modulatedLocalY = modulatedRadius * sin(angle);

            // Convert back by adding the center offset
            finalX = modulatedLocalX + centerXForAtan;
            finalY = modulatedLocalY + centerYForAtan;
        }
        // If radiusForModulation is near zero, finalX/finalY remain as they were before Atan.
    }
    
    // (finalX, finalY) is now fully processed, in main square's relative coordinate system.
    pVarTP.x += pAmount * (finalX + (gridM * 2 + 1) * this.Sc);
    pVarTP.y += pAmount * (finalY + (gridN * 2 + 1) * this.Sc);

    if (pContext.isPreserveZCoordinate()) {
      pVarTP.z += pAmount * pAffineTP.z;
    }
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{
        Sc, Dens, X_Range, Y_Range, Seed,
        Hollow, BorderThickness, AtanMode, AtanFactor, AtanFrequency,
        SubdivisionActive, SubdivisionChance, SubdivisionScale, SubdivisionPlacement
    };
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_SC.equalsIgnoreCase(pName)) Sc = pValue;
    else if (PARAM_DENS.equalsIgnoreCase(pName)) Dens = pValue;
    else if (PARAM_X_RANGE.equalsIgnoreCase(pName)) X_Range = pValue;
    else if (PARAM_Y_RANGE.equalsIgnoreCase(pName)) Y_Range = pValue;
    else if (PARAM_SEED.equalsIgnoreCase(pName)) Seed = Tools.FTOI(pValue); 
    else if (PARAM_HOLLOW.equalsIgnoreCase(pName)) Hollow = pValue; 
    else if (PARAM_BORDER_THICKNESS.equalsIgnoreCase(pName)) BorderThickness = Math.max(0.0, Math.min(pValue, 1.0)); 
    else if (PARAM_ATAN_MODE.equalsIgnoreCase(pName)) AtanMode = pValue; 
    else if (PARAM_ATAN_FACTOR.equalsIgnoreCase(pName)) AtanFactor = pValue; 
    else if (PARAM_ATAN_FREQUENCY.equalsIgnoreCase(pName)) AtanFrequency = pValue; 
    else if (PARAM_SUBDIVISION_ACTIVE.equalsIgnoreCase(pName)) SubdivisionActive = pValue;
    else if (PARAM_SUBDIVISION_CHANCE.equalsIgnoreCase(pName)) SubdivisionChance = Math.max(0.0, Math.min(pValue, 1.0));
    else if (PARAM_SUBDIVISION_SCALE.equalsIgnoreCase(pName)) SubdivisionScale = Math.max(0.01, Math.min(pValue, 1.0)); 
    else if (PARAM_SUBDIVISION_PLACEMENT.equalsIgnoreCase(pName)) SubdivisionPlacement = Math.max(0.0, Math.min(pValue, 1.0));
    else throw new IllegalArgumentException("Unknown parameter: " + pName);
  }

  @Override
  public String getName() {
    return "squareRand"; 
  }
}
