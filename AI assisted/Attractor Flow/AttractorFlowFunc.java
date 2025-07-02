/*
  JWildfire - an image and animation processor written in Java
  Copyright (C) 1995-2025 Andreas Maschke

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

//import org.jwildfire.create.tina.base.FlameTransformationContext;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class AttractorFlowFunc extends VariationFunc {
  private static final long serialVersionUID = 11L; // Version incremented

  // Mode selectors
  private static final String PARAM_PRESET = "preset";
  private static final String PARAM_ATTRACTOR_MODE = "attractorMode";
  private static final String PARAM_WAVE_MODE = "waveMode";
  private static final String PARAM_Z_MODE = "zMode";

  // Parameters
  private static final String PARAM_Y_SCALE = "yScale";
  private static final String PARAM_X_SCALE = "xScale";
  private static final String PARAM_Z_SCALE = "zScale";
  private static final String PARAM_X_AMPLITUDE = "xAmplitude";
  private static final String PARAM_Y_AMPLITUDE = "yAmplitude";
  private static final String PARAM_Z_AMPLITUDE = "zAmplitude";
  private static final String PARAM_FREQ_X = "freqX";
  private static final String PARAM_FREQ_Y = "freqY";
  private static final String PARAM_PHASE_X = "phaseX";
  private static final String PARAM_PHASE_Y = "phaseY";

  private static final String[] paramNames = {PARAM_PRESET, PARAM_ATTRACTOR_MODE, PARAM_WAVE_MODE, PARAM_Z_MODE, PARAM_Y_SCALE, PARAM_X_SCALE, PARAM_Z_SCALE, PARAM_X_AMPLITUDE, PARAM_Y_AMPLITUDE, PARAM_Z_AMPLITUDE, PARAM_FREQ_X, PARAM_FREQ_Y, PARAM_PHASE_X, PARAM_PHASE_Y};

  // Default values
  private int preset = 0, attractorMode = 0, waveMode = 0, zMode = 0;
  private double yScale = -1.7, xScale = 1.8, zScale = 1.5;
  private double xAmplitude = -0.5, yAmplitude = -1.9, zAmplitude = 1.2;
  private double freqX = 1.0, freqY = 1.0, phaseX = 0.0, phaseY = 0.0;

  private void applyPreset(int pValue) {
    this.preset = pValue;
    switch(pValue) {
      case 1: // Clifford Classic
        attractorMode = 0; waveMode = 0; zMode = 0;
        yScale = -1.7; xScale = 1.8; xAmplitude = -0.5; yAmplitude = -1.9;
        freqX = 1.0; freqY = 1.0; phaseX = 0.0; phaseY = 0.0;
        break;
      case 2: // De Jong Circuits
        attractorMode = 1; waveMode = 1; zMode = 0;
        yScale = -2.0; xScale = -2.0; xAmplitude = 1.2; yAmplitude = -1.9;
        break;
      case 3: // Svensson Flames
        attractorMode = 2; waveMode = 2; zMode = 0;
        yScale = 1.6; xScale = -1.9; xAmplitude = 1.2; yAmplitude = -1.8;
        break;
      case 4: // 3D Warped Grid
        attractorMode = 0; waveMode = 0; zMode = 1;
        yScale = 1.2; xScale = 1.2; zScale = 1.4;
        xAmplitude = -1.8; yAmplitude = -1.3; zAmplitude = 1.1;
        break;
    }
  }

  private double selectWave(double value) {
    switch (waveMode) {
      case 1: return Math.signum(sin(value));
      case 2: return asin(sin(value)) * (2.0 / M_PI);
      case 3: return 2.0 * (value / (2.0 * M_PI) - floor(0.5 + value / (2.0 * M_PI))); // Sawtooth
      default: return sin(value);
    }
  }

  private double selectCosWave(double value) { return selectWave(value + M_PI_2); }

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    double x = pAffineTP.x, y = pAffineTP.y, z = pAffineTP.z;
    double newX = 0.0, newY = 0.0, newZ = 0.0;

    switch (attractorMode) {
      case 1: // Peter de Jong
        newX = selectWave(yScale * y) - selectCosWave(xScale * x);
        newY = selectWave(xAmplitude * (zMode == 1 ? z : x)) - selectCosWave(yAmplitude * y);
        break;
      case 2: // Svensson
        newX = yAmplitude * selectWave(yScale * x) - selectWave(xScale * y);
        newY = xAmplitude * selectCosWave(yScale * (zMode == 1 ? z : x)) + selectCosWave(xScale * y);
        break;
      case 3: // Gumowski-Mira (using xAmplitude for 'a' and yScale for 'b')
        double g_x = xAmplitude * x + (2.0 * (1.0 - xAmplitude) * x * x) / (1.0 + x * x);
        newY = (yScale * y) + g_x;
        newX = -x + g_x;
        break;
      default: // Clifford
        newX = selectWave(yScale * y * freqY + phaseY) + xAmplitude * selectCosWave(yScale * x * freqX + phaseX);
        newY = selectWave(xScale * (zMode == 1 ? z : x) * freqX + phaseX) + yAmplitude * selectCosWave(xScale * y * freqY + phaseY);
        break;
    }

    pVarTP.x += (newX - x) * pAmount;
    pVarTP.y += (newY - y) * pAmount;

    if (pContext.isPreserveZCoordinate()) {
        if (zMode == 1) { // Coupled 3D Mode
            newZ = selectWave(zScale * x) + zAmplitude * selectCosWave(zScale * z);
            pVarTP.z += (newZ - z) * pAmount;
        } else { // Legacy 2D Extrusion
            newZ = selectWave(xScale * z) + xAmplitude * selectCosWave(yAmplitude * z);
            pVarTP.z += newZ * pAmount;
        }
    }
  }

  @Override
  public String getName() { return "attractorFlow"; }
  
  @Override
  public String[] getParameterNames() { return paramNames; }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{preset, attractorMode, waveMode, zMode, yScale, xScale, zScale, xAmplitude, yAmplitude, zAmplitude, freqX, freqY, phaseX, phaseY};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_PRESET.equalsIgnoreCase(pName)) {
      applyPreset((int) pValue);
    } else {
      preset = 0; // Manual change resets preset
      if      (PARAM_ATTRACTOR_MODE.equalsIgnoreCase(pName)) attractorMode = (int) pValue;
      else if (PARAM_WAVE_MODE.equalsIgnoreCase(pName)) waveMode = (int) pValue;
      else if (PARAM_Z_MODE.equalsIgnoreCase(pName)) zMode = (int) pValue;
      else if (PARAM_Y_SCALE.equalsIgnoreCase(pName)) yScale = pValue;
      else if (PARAM_X_SCALE.equalsIgnoreCase(pName)) xScale = pValue;
      else if (PARAM_Z_SCALE.equalsIgnoreCase(pName)) zScale = pValue;
      else if (PARAM_X_AMPLITUDE.equalsIgnoreCase(pName)) xAmplitude = pValue;
      else if (PARAM_Y_AMPLITUDE.equalsIgnoreCase(pName)) yAmplitude = pValue;
      else if (PARAM_Z_AMPLITUDE.equalsIgnoreCase(pName)) zAmplitude = pValue;
      else if (PARAM_FREQ_X.equalsIgnoreCase(pName)) freqX = pValue;
      else if (PARAM_FREQ_Y.equalsIgnoreCase(pName)) freqY = pValue;
      else if (PARAM_PHASE_X.equalsIgnoreCase(pName)) phaseX = pValue;
      else if (PARAM_PHASE_Y.equalsIgnoreCase(pName)) phaseY = pValue;
      else throw new IllegalArgumentException(pName);
    }
  }

  @Override
  public VariationFuncType[] getVariationTypes() { return new VariationFuncType[]{VariationFuncType.VARTYPE_3D}; }
}
