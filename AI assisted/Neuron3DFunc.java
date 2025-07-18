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

import java.util.Random;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * # INFO
 *
 * ## Neuron3DFunc (Generator Version)
 *
 * Creates a self-contained 3D field of neurons with distinct, textured connectors and multiple neuron types.
 *
 * ## Parameters
 *
 * - **zoom**: Controls the overall size/spread of the entire neuron cloud.
 * - **density**: The probability of a cell appearing in a grid location.
 * - **seed**: A random seed to generate different patterns.
 *
 * ### Type 1 Neuron
 * - **radius**: The base radius of Type 1 cell bodies.
 * - **orbColor**: The color index for Type 1 neuron bodies (0.0 to 1.0).
 *
 * ### Type 2 Neuron
 * - **type2_prob**: Probability a neuron will be Type 2.
 * - **radius2**: The base radius of Type 2 cell bodies.
 * - **orbColor2**: The color index for Type 2 neuron bodies (0.0 to 1.0).
 *
 * ### Tendrils & Connectors
 * - **noiseFreq**: The frequency of the noise for tendrils on the cell body.
 * - **noiseAmp**: The amplitude of the noise for tendrils on the cell body.
 * - **turbulence**: Controls the warping for more complex cell body tendrils.
 * - **connectorThickness**: The thickness of the connecting lines.
 * - **connectorProb**: The probability a connection will form between neurons.
 * - **connectorTexture**: The amplitude of the waviness/texture on connectors.
 * - **connectorTexFreq**: The frequency of the texture along the connectors.
 * - **lineColor**: The color index for the connectors (0.0 to 1.0).
 *
 * @author Gemini, inspired by an effect request by Brad Stefanov
 */
public class Neuron3DFunc extends VariationFunc {
  private static final long serialVersionUID = 12L; // Version 12 (Neuron Types)

  // General
  private static final String PARAM_ZOOM = "zoom";
  private static final String PARAM_DENSITY = "density";
  private static final String PARAM_SEED = "seed";
  // Type 1
  private static final String PARAM_RADIUS = "radius";
  private static final String PARAM_ORB_COLOR = "orbColor";
  // Type 2
  private static final String PARAM_TYPE2_PROB = "type2_prob";
  private static final String PARAM_RADIUS2 = "radius2";
  private static final String PARAM_ORB_COLOR2 = "orbColor2";
  // Tendrils
  private static final String PARAM_NOISE_FREQ = "noiseFreq";
  private static final String PARAM_NOISE_AMP = "noiseAmp";
  private static final String PARAM_TURBULENCE = "turbulence";
  // Connectors
  private static final String PARAM_CONNECTOR_THICKNESS = "connectorThickness";
  private static final String PARAM_CONNECTOR_PROB = "connectorProb";
  private static final String PARAM_CONNECTOR_TEXTURE = "connectorTexture";
  private static final String PARAM_CONNECTOR_TEX_FREQ = "connectorTexFreq";
  private static final String PARAM_LINE_COLOR = "lineColor";


  private static final String[] paramNames = {PARAM_ZOOM, PARAM_DENSITY, PARAM_SEED, PARAM_RADIUS, PARAM_ORB_COLOR, PARAM_TYPE2_PROB, PARAM_RADIUS2, PARAM_ORB_COLOR2, PARAM_NOISE_FREQ, PARAM_NOISE_AMP, PARAM_TURBULENCE, PARAM_CONNECTOR_THICKNESS, PARAM_CONNECTOR_PROB, PARAM_CONNECTOR_TEXTURE, PARAM_CONNECTOR_TEX_FREQ, PARAM_LINE_COLOR};

  // General
  private double zoom = 10.0;
  private double density = 0.5;
  private int seed = 12345;
  // Type 1
  private double radius = 0.4;
  private double orbColor = 0.25;
  // Type 2
  private double type2_prob = 0.3;
  private double radius2 = 0.25;
  private double orbColor2 = 0.9;
  // Tendrils
  private double noiseFreq = 12.0;
  private double noiseAmp = 0.15;
  private double turbulence = 0.8;
  // Connectors
  private double connectorThickness = 0.05;
  private double connectorProb = 0.6;
  private double connectorTexture = 0.1;
  private double connectorTexFreq = 5.0;
  private double lineColor = 0.75;


  private transient Random rand = new Random();
  private transient PerlinNoise perlin;

  private void ensureInitialized() {
      if (rand == null) {
          rand = new Random(seed);
      }
      if (perlin == null) {
          perlin = new PerlinNoise(seed);
      }
  }

  // Returns 0 for no neuron, 1 for type 1, 2 for type 2
  private int getNeuronType(double ix, double iy, double iz) {
      long cellHash = (long)ix * 73856093L ^ (long)iy * 19349663L ^ (long)iz * 83492791L ^ seed;
      Random tempRand = new Random(cellHash);
      if (tempRand.nextDouble() < density) {
          if (tempRand.nextDouble() < type2_prob) {
              return 2; // It's a Type 2 neuron
          }
          return 1; // It's a Type 1 neuron
      }
      return 0; // No neuron
  }

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    ensureInitialized();

    for (int i = 0; i < 20; i++) { // Find a point to draw
        double ix = Math.floor((pContext.random() * 2.0 - 1.0) * zoom);
        double iy = Math.floor((pContext.random() * 2.0 - 1.0) * zoom);
        double iz = Math.floor((pContext.random() * 2.0 - 1.0) * zoom);

        int startType = getNeuronType(ix, iy, iz);
        if (startType == 0) {
            continue;
        }

        boolean drawConnector = pContext.random() < 0.5;

        // --- Stage 1: Attempt to draw a connector ---
        if (drawConnector && connectorThickness > 0) {
            int dx = (int)Math.floor(pContext.random() * 3) - 1;
            int dy = (int)Math.floor(pContext.random() * 3) - 1;
            int dz = (int)Math.floor(pContext.random() * 3) - 1;

            if (dx == 0 && dy == 0 && dz == 0) continue;

            double nx = ix + dx;
            double ny = iy + dy;
            double nz = iz + dz;
            
            int neighborType = getNeuronType(nx, ny, nz);
            if(neighborType == 0) continue;

            long connHash = (long)ix * 13L + (long)iy*31L + (long)iz*53L + (long)nx*71L + (long)ny*97L + (long)nz*113L + seed;
            rand.setSeed(connHash);

            if (rand.nextDouble() < connectorProb) {
                double startRadius = (startType == 1) ? radius : radius2;
                double neighborRadius = (neighborType == 1) ? radius : radius2;

                double centerX = ix + 0.5, centerY = iy + 0.5, centerZ = iz + 0.5;
                double neighborX = nx + 0.5, neighborY = ny + 0.5, neighborZ = nz + 0.5;

                double vecToNeighborX = neighborX - centerX;
                double vecToNeighborY = neighborY - centerY;
                double vecToNeighborZ = neighborZ - centerZ;
                double vecLen = Math.sqrt(vecToNeighborX*vecToNeighborX + vecToNeighborY*vecToNeighborY + vecToNeighborZ*vecToNeighborZ);

                if (vecLen > 1e-9) {
                    double normX = vecToNeighborX / vecLen;
                    double normY = vecToNeighborY / vecLen;
                    double normZ = vecToNeighborZ / vecLen;

                    double startX = centerX + normX * startRadius;
                    double startY = centerY + normY * startRadius;
                    double startZ = centerZ + normZ * startRadius;

                    double endX = neighborX - normX * neighborRadius;
                    double endY = neighborY - normY * neighborRadius;
                    double endZ = neighborZ - normZ * neighborRadius;
                    
                    double connVecX = endX - startX;
                    double connVecY = endY - startY;
                    double connVecZ = endZ - startZ;

                    double t = pContext.random();
                    double pointOnLineX = startX + t * connVecX;
                    double pointOnLineY = startY + t * connVecY;
                    double pointOnLineZ = startZ + t * connVecZ;
                    
                    double r = pContext.random() * connectorThickness;
                    double angle = pContext.random() * 2.0 * Math.PI;

                    double perp1X, perp1Y, perp1Z;
                    if (Math.abs(connVecX) < 1e-6 && Math.abs(connVecZ) < 1e-6) {
                        perp1X = 1.0; perp1Y = 0.0; perp1Z = 0.0;
                    } else {
                        perp1X = -connVecZ; perp1Y = 0.0; perp1Z = connVecX;
                    }
                    double len1 = Math.sqrt(perp1X*perp1X + perp1Y*perp1Y + perp1Z*perp1Z);
                    perp1X /= len1; perp1Y /= len1; perp1Z /= len1;

                    double perp2X = connVecY * perp1Z - connVecZ * perp1Y;
                    double perp2Y = connVecZ * perp1X - connVecX * perp1Z;
                    double perp2Z = connVecX * perp1Y - connVecY * perp1X;
                    double len2 = Math.sqrt(perp2X*perp2X + perp2Y*perp2Y + perp2Z*perp2Z);
                    perp2X /= len2; perp2Y /= len2; perp2Z /= len2;

                    double finalX = pointOnLineX + r * (Math.cos(angle) * perp1X + Math.sin(angle) * perp2X);
                    double finalY = pointOnLineY + r * (Math.cos(angle) * perp1Y + Math.sin(angle) * perp2Y);
                    double finalZ = pointOnLineZ + r * (Math.cos(angle) * perp1Z + Math.sin(angle) * perp2Z);

                    if (connectorTexture > 0) {
                        double noiseAngle = t * connectorTexFreq;
                        double noise1 = perlin.noise(noiseAngle, connHash + 1.2, connHash + 3.4);
                        double noise2 = perlin.noise(noiseAngle, connHash + 5.6, connHash + 7.8);
                        
                        finalX += (perp1X * noise1 + perp2X * noise2) * connectorTexture;
                        finalY += (perp1Y * noise1 + perp2Y * noise2) * connectorTexture;
                        finalZ += (perp1Z * noise1 + perp2Z * noise2) * connectorTexture;
                    }
                    
                    pVarTP.x = (finalX / zoom) * pAmount;
                    pVarTP.y = (finalY / zoom) * pAmount;
                    pVarTP.z = (finalZ / zoom) * pAmount;
                    pVarTP.color = lineColor;
                    return;
                }
            }
        }

        // --- Stage 2: Draw a neuron body ---
        double currentRadius = (startType == 1) ? radius : radius2;
        double currentColor = (startType == 1) ? orbColor : orbColor2;

        double r = currentRadius * Math.cbrt(pContext.random());
        double theta = pContext.random() * 2.0 * Math.PI;
        double phi = Math.acos(2.0 * pContext.random() - 1.0);
        
        double cx = r * Math.sin(phi) * Math.cos(theta);
        double cy = r * Math.sin(phi) * Math.sin(theta);
        double cz = r * Math.cos(phi);
        
        double dist = Math.sqrt(cx*cx + cy*cy + cz*cz);

        double freq = cx * noiseFreq;
        double qx = cx + turbulence * perlin.noise(freq, cy * noiseFreq, cz * noiseFreq);
        double qy = cy + turbulence * perlin.noise(freq + 5.2, cy * noiseFreq + 1.3, cz * noiseFreq);
        double qz = cz + turbulence * perlin.noise(freq + 8.7, cy * noiseFreq + 3.4, cz * noiseFreq + 4.6);
        double noise = perlin.noise(qx * noiseFreq, qy * noiseFreq, qz * noiseFreq);
        double newDist = dist + (noise * noiseAmp);
        
        if (dist > 1e-9) {
            double scale = newDist / dist;
            double finalX = ix + 0.5 + cx * scale;
            double finalY = iy + 0.5 + cy * scale;
            double finalZ = iz + 0.5 + cz * scale;

            pVarTP.x = (finalX / zoom) * pAmount;
            pVarTP.y = (finalY / zoom) * pAmount;
            pVarTP.z = (finalZ / zoom) * pAmount;
            pVarTP.color = currentColor;
            return;
        }
    }
    pVarTP.x = 0.0; pVarTP.y = 0.0; pVarTP.z = 0.0;
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{zoom, density, (double)seed, radius, orbColor, type2_prob, radius2, orbColor2, noiseFreq, noiseAmp, turbulence, connectorThickness, connectorProb, connectorTexture, connectorTexFreq, lineColor};
  }

  @Override
  public String[] getParameterAlternativeNames() {
    return new String[]{"n3d_zoom", "n3d_density", "n3d_seed", "n3d_radius", "n3d_orb_color", "n3d_type2_prob", "n3d_radius2", "n3d_orb_color2", "n3d_noise_freq", "n3d_noise_amp", "n3d_turbulence", "n3d_conn_thick", "n3d_conn_prob", "n3d_conn_tex", "n3d_conn_tex_freq", "n3d_line_color"};
  }

  private double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
  }

  @Override
  public void setParameter(String pName, double pValue) {
    ensureInitialized();
      
    if (PARAM_ZOOM.equalsIgnoreCase(pName)) zoom = pValue;
    else if (PARAM_DENSITY.equalsIgnoreCase(pName)) density = clamp(pValue, 0.0, 1.0);
    else if (PARAM_SEED.equalsIgnoreCase(pName)) {
        seed = (int) pValue;
        perlin = new PerlinNoise(seed);
    }
    else if (PARAM_RADIUS.equalsIgnoreCase(pName)) radius = pValue;
    else if (PARAM_ORB_COLOR.equalsIgnoreCase(pName)) orbColor = clamp(pValue, 0.0, 1.0);
    else if (PARAM_TYPE2_PROB.equalsIgnoreCase(pName)) type2_prob = clamp(pValue, 0.0, 1.0);
    else if (PARAM_RADIUS2.equalsIgnoreCase(pName)) radius2 = pValue;
    else if (PARAM_ORB_COLOR2.equalsIgnoreCase(pName)) orbColor2 = clamp(pValue, 0.0, 1.0);
    else if (PARAM_NOISE_FREQ.equalsIgnoreCase(pName)) noiseFreq = pValue;
    else if (PARAM_NOISE_AMP.equalsIgnoreCase(pName)) noiseAmp = pValue;
    else if (PARAM_TURBULENCE.equalsIgnoreCase(pName)) turbulence = pValue;
    else if (PARAM_CONNECTOR_THICKNESS.equalsIgnoreCase(pName)) connectorThickness = pValue;
    else if (PARAM_CONNECTOR_PROB.equalsIgnoreCase(pName)) connectorProb = clamp(pValue, 0.0, 1.0);
    else if (PARAM_CONNECTOR_TEXTURE.equalsIgnoreCase(pName)) connectorTexture = pValue;
    else if (PARAM_CONNECTOR_TEX_FREQ.equalsIgnoreCase(pName)) connectorTexFreq = pValue;
    else if (PARAM_LINE_COLOR.equalsIgnoreCase(pName)) lineColor = clamp(pValue, 0.0, 1.0);
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "neuron3D";
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_3D, VariationFuncType.VARTYPE_DC};
  }

  private static final class PerlinNoise {
    private final int[] p = new int[512];
    public PerlinNoise(int seed) {
      Random rand = new Random(seed);
      int[] permutation = new int[256];
      for (int i = 0; i < 256; i++) { permutation[i] = i; }
      for (int i = 255; i > 0; i--) {
        int index = rand.nextInt(i + 1);
        int temp = permutation[index];
        permutation[index] = permutation[i];
        permutation[i] = temp;
      }
      for (int i = 0; i < 256; i++) { p[i] = p[i + 256] = permutation[i]; }
    }
    public double noise(double x, double y, double z) {
      int X = (int) Math.floor(x) & 255, Y = (int) Math.floor(y) & 255, Z = (int) Math.floor(z) & 255;
      x -= Math.floor(x); y -= Math.floor(y); z -= Math.floor(z);
      double u = fade(x), v = fade(y), w = fade(z);
      int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z;
      int B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;
      return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z), grad(p[BA], x - 1, y, z)),
                             lerp(u, grad(p[AB], x, y - 1, z), grad(p[BB], x - 1, y - 1, z))),
                     lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1), grad(p[BA + 1], x - 1, y, z - 1)),
                             lerp(u, grad(p[AB + 1], x, y - 1, z - 1), grad(p[BB + 1], x - 1, y - 1, z - 1))));
    }
    private static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private static double lerp(double t, double a, double b) { return a + t * (b - a); }
    private static double grad(int hash, double x, double y, double z) {
      int h = hash & 15;
      double u = h < 8 ? x : y;
      double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
      return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
  }
}
