package org.jwildfire.create.tina.variation;

import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

public class ArchimedeanFunc extends VariationFunc {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_SCALE = "scale";
  private static final String PARAM_THICKNESS = "thickness";
  private static final String PARAM_INTERNAL = "internal_mode";
  private static final String PARAM_RECURSION = "recursion_depth";
  private static final String PARAM_TYPE = "type";
  private static final String PARAM_SURFACE = "surface_lines";
  
  private static final String[] paramNames = { PARAM_SCALE, PARAM_THICKNESS, PARAM_INTERNAL, PARAM_RECURSION, PARAM_TYPE, PARAM_SURFACE };

  private double scale = 1.0;
  private double thickness = 0.05; 
  private int internalMode = 0;    // 0 = Solid, 1 = Spokes, 2 = Web
  private int recursionDepth = 0; 
  private int type = 0;            // 0=Cuboct, 1=TruncOct, 2=TruncCube, 3=Rhombicuboct, 4=GreatRhomb, 5=TruncTetra, 6=Icosa, 7=Rhombicosidodeca, 8=TruncIcosa, 9=Icosidodeca
  private int surfaceLines = 1;    // 1 = Show Surface

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    // --- 1. SETUP SHAPE DATA ---
    double[][] currentUnitVerts; 
    
    if (type == 8) {
        // --- TYPE 8: TRUNCATED ICOSAHEDRON (60 Vertices) - SOCCER BALL ---
        double phi = (1.0 + Math.sqrt(5.0)) / 2.0;
        currentUnitVerts = new double[60][3];
        int idx = 0;
        
        // Type 1: Even permutations of (0, ±1, ±3?)
        double val1 = 3 * phi;
        for(int p=0; p<3; p++) {
            double[] perm = (p==0) ? new double[]{0,1,val1} : (p==1) ? new double[]{1,val1,0} : new double[]{val1,0,1};
            for(int sy=-1; sy<=1; sy+=2) {
                for(int sz=-1; sz<=1; sz+=2) {
                    double[] v = new double[3];
                    for(int i=0; i<3; i++) {
                        if (Math.abs(perm[i] - 1) < 0.01) v[i] = sy;
                        else if (Math.abs(perm[i] - val1) < 0.01) v[i] = sz * val1;
                        else v[i] = 0;
                    }
                    currentUnitVerts[idx++] = v;
                }
            }
        }
        
        // Type 2: Even permutations of (±2, ±(1+2?), ±?)
        double val2 = 1 + 2 * phi;
        for(int p=0; p<3; p++) {
            double[] perm = (p==0) ? new double[]{2,val2,phi} : (p==1) ? new double[]{val2,phi,2} : new double[]{phi,2,val2};
            for(int sx=-1; sx<=1; sx+=2) {
                for(int sy=-1; sy<=1; sy+=2) {
                    for(int sz=-1; sz<=1; sz+=2) {
                        currentUnitVerts[idx++] = new double[]{sx*perm[0], sy*perm[1], sz*perm[2]};
                    }
                }
            }
        }
        
        // Type 3: Even permutations of (±1, ±(2+?), ±2?)
        double val3 = 2 + phi;
        double val4 = 2 * phi;
        for(int p=0; p<3; p++) {
            double[] perm = (p==0) ? new double[]{1,val3,val4} : (p==1) ? new double[]{val3,val4,1} : new double[]{val4,1,val3};
            for(int sx=-1; sx<=1; sx+=2) {
                for(int sy=-1; sy<=1; sy+=2) {
                    for(int sz=-1; sz<=1; sz+=2) {
                        currentUnitVerts[idx++] = new double[]{sx*perm[0], sy*perm[1], sz*perm[2]};
                    }
                }
            }
        }
    } else if (type == 9) {
        // --- TYPE 9: ICOSIDODECAHEDRON (30 Vertices) ---
        double phi = (1.0 + Math.sqrt(5.0)) / 2.0;
        currentUnitVerts = new double[30][3];
        int idx = 0;
        
        // Type 1: Even permutations of (0, 0, ±2?)
        double val_2phi = 2 * phi;
        for(int p=0; p<3; p++) {
            double[] perm = (p==0) ? new double[]{0,0,val_2phi} : (p==1) ? new double[]{0,val_2phi,0} : new double[]{val_2phi,0,0};
            for(int s=-1; s<=1; s+=2) {
                double[] v = new double[3];
                for(int i=0; i<3; i++) {
                    if (Math.abs(perm[i] - val_2phi) < 0.01) v[i] = s * val_2phi;
                    else v[i] = 0;
                }
                currentUnitVerts[idx++] = v;
            }
        }
        
        // Type 2: Even permutations of (±1, ±?, ±?²)
        double phi2 = phi * phi;
        for(int p=0; p<3; p++) {
            double[] perm = (p==0) ? new double[]{1,phi,phi2} : (p==1) ? new double[]{phi,phi2,1} : new double[]{phi2,1,phi};
            for(int sx=-1; sx<=1; sx+=2) {
                for(int sy=-1; sy<=1; sy+=2) {
                    for(int sz=-1; sz<=1; sz+=2) {
                        currentUnitVerts[idx++] = new double[]{sx*perm[0], sy*perm[1], sz*perm[2]};
                    }
                }
            }
        }
    } else if (type == 7) {
        // --- TYPE 7: RHOMBICOSIDODECAHEDRON (60 Vertices) ---
        double P = (1.0 + Math.sqrt(5.0)) / 2.0;
        double P2 = P * P;
        double P3 = P * P * P;
        currentUnitVerts = new double[60][3];
        int idx = 0;
        // Cyclic permutations of (±1, ±1, ±?³)
        for(int p=0; p<3; p++) {
            double[] c = (p==0) ? new double[]{1,1,P3} : (p==1) ? new double[]{1,P3,1} : new double[]{P3,1,1};
            for(int i=0; i<8; i++) {
                currentUnitVerts[idx++] = new double[]{
                    (i&4)==0 ? c[0] : -c[0],
                    (i&2)==0 ? c[1] : -c[1],
                    (i&1)==0 ? c[2] : -c[2]
                };
            }
        }
        // Cyclic permutations of (±?², ±?, ±2?)
        for(int p=0; p<3; p++) {
            double[] c = (p==0) ? new double[]{P2,P,2*P} : (p==1) ? new double[]{P,2*P,P2} : new double[]{2*P,P2,P};
            for(int i=0; i<8; i++) {
                currentUnitVerts[idx++] = new double[]{
                    (i&4)==0 ? c[0] : -c[0],
                    (i&2)==0 ? c[1] : -c[1],
                    (i&1)==0 ? c[2] : -c[2]
                };
            }
        }
        // Cyclic permutations of (±(2+?), 0, ±?²)
        double a = 2+P, b = P2;
        for(int s1=0; s1<2; s1++) {
            for(int s2=0; s2<2; s2++) {
                currentUnitVerts[idx++] = new double[]{ s1==0?a:-a, 0, s2==0?b:-b };
                currentUnitVerts[idx++] = new double[]{ 0, s1==0?b:-b, s2==0?a:-a };
                currentUnitVerts[idx++] = new double[]{ s1==0?b:-b, s2==0?a:-a, 0 };
            }
        }
    } else if (type == 6) {
        // --- TYPE 6: ICOSAHEDRON (12 Vertices) ---
        double phi = (1.0 + Math.sqrt(5.0)) / 2.0;
        currentUnitVerts = new double[][] {
            {0, 1, phi}, {0, 1, -phi}, {0, -1, phi}, {0, -1, -phi},
            {1, phi, 0}, {1, -phi, 0}, {-1, phi, 0}, {-1, -phi, 0},
            {phi, 0, 1}, {phi, 0, -1}, {-phi, 0, 1}, {-phi, 0, -1}
        };
    } else if (type == 5) {
        // --- TYPE 5: TRUNCATED TETRAHEDRON ---
        currentUnitVerts = new double[][] {
            {3,1,1}, {1,3,1}, {1,1,3}, {3,-1,-1}, {1,-3,-1}, {1,-1,-3}, 
            {-3,1,-1}, {-1,3,-1}, {-1,1,-3}, {-3,-1,1}, {-1,-3,1}, {-1,-1,3}
        };
    } else if (type == 4) {
        // --- TYPE 4: GREAT RHOMBICUBOCTAHEDRON ---
        double A = 1.0;
        double B = 1.0 + Math.sqrt(2.0); 
        double C = 1.0 + 2.0 * Math.sqrt(2.0);
        currentUnitVerts = new double[48][3];
        int idx = 0;
        for(int i=0; i<8; i++) currentUnitVerts[idx++] = new double[]{ (i&4)==0?A:-A, (i&2)==0?B:-B, (i&1)==0?C:-C }; 
        for(int i=0; i<8; i++) currentUnitVerts[idx++] = new double[]{ (i&4)==0?A:-A, (i&2)==0?C:-C, (i&1)==0?B:-B }; 
        for(int i=0; i<8; i++) currentUnitVerts[idx++] = new double[]{ (i&4)==0?B:-B, (i&2)==0?A:-A, (i&1)==0?C:-C }; 
        for(int i=0; i<8; i++) currentUnitVerts[idx++] = new double[]{ (i&4)==0?B:-B, (i&2)==0?C:-C, (i&1)==0?A:-A }; 
        for(int i=0; i<8; i++) currentUnitVerts[idx++] = new double[]{ (i&4)==0?C:-C, (i&2)==0?A:-A, (i&1)==0?B:-B }; 
        for(int i=0; i<8; i++) currentUnitVerts[idx++] = new double[]{ (i&4)==0?C:-C, (i&2)==0?B:-B, (i&1)==0?A:-A }; 
    } else if (type == 3) {
        // --- TYPE 3: RHOMBICUBOCTAHEDRON ---
        double B = 1.0 + Math.sqrt(2.0); 
        currentUnitVerts = new double[][] {
            {1,1,B}, {1,1,-B}, {1,-1,B}, {1,-1,-B}, {-1,1,B}, {-1,1,-B}, {-1,-1,B}, {-1,-1,-B},
            {1,B,1}, {1,B,-1}, {1,-B,1}, {1,-B,-1}, {-1,B,1}, {-1,B,-1}, {-1,-B,1}, {-1,-B,-1},
            {B,1,1}, {B,1,-1}, {B,-1,1}, {B,-1,-1}, {-B,1,1}, {-B,1,-1}, {-B,-1,1}, {-B,-1,-1}
        };
    } else if (type == 2) {
        // --- TYPE 2: TRUNCATED CUBE ---
        double k = Math.sqrt(2.0) - 1.0; 
        currentUnitVerts = new double[][] {
            {1,1,k}, {1,1,-k}, {1,-1,k}, {1,-1,-k}, {-1,1,k}, {-1,1,-k}, {-1,-1,k}, {-1,-1,-k},
            {1,k,1}, {1,k,-1}, {1,-k,1}, {1,-k,-1}, {-1,k,1}, {-1,k,-1}, {-1,-k,1}, {-1,-k,-1},
            {k,1,1}, {k,1,-1}, {k,-1,1}, {k,-1,-1}, {-k,1,1}, {-k,1,-1}, {-k,-1,1}, {-k,-1,-1}
        };
    } else if (type == 1) {
        // --- TYPE 1: TRUNCATED OCTAHEDRON ---
        currentUnitVerts = new double[][] {
            {0, 1, 2}, {0, 1, -2}, {0, -1, 2}, {0, -1, -2},
            {1, 0, 2}, {1, 0, -2}, {-1, 0, 2}, {-1, 0, -2},
            {1, 2, 0}, {1, -2, 0}, {-1, 2, 0}, {-1, -2, 0},
            {2, 0, 1}, {2, 0, -1}, {-2, 0, 1}, {-2, 0, -1},
            {2, 1, 0}, {2, -1, 0}, {-2, 1, 0}, {-2, -1, 0},
            {0, 2, 1}, {0, 2, -1}, {0, -2, 1}, {0, -2, -1}
        };
    } else {
        // --- TYPE 0: CUBOCTAHEDRON ---
        currentUnitVerts = new double[][] {
            {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
            {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1}
        };
    }

    // --- 2. RECURSION ---
    double activeScale = scale;
    double offsetX = 0, offsetY = 0, offsetZ = 0;
    
    for (int i = 0; i < recursionDepth; i++) {
        int v = pContext.random(currentUnitVerts.length);
        activeScale *= 0.5;
        offsetX += currentUnitVerts[v][0] * activeScale;
        offsetY += currentUnitVerts[v][1] * activeScale;
        offsetZ += currentUnitVerts[v][2] * activeScale;
    }

    double[][] activeVerts = new double[currentUnitVerts.length][3];
    for(int i=0; i<currentUnitVerts.length; i++) {
        activeVerts[i][0] = currentUnitVerts[i][0] * activeScale;
        activeVerts[i][1] = currentUnitVerts[i][1] * activeScale;
        activeVerts[i][2] = currentUnitVerts[i][2] * activeScale;
    }

    // --- 3. DRAW ---
    if (type == 9) {
        drawIcosidodecahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    } else if (type == 8) {
        drawTruncatedIcosahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    } else if (type == 7) {
        drawRhombicosidodecahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    } else if (type == 6) {
        drawIcosahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    } else if (type == 5) {
        drawTruncatedTetrahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    } else if (type == 4) {
        drawGreatRhombicuboctahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    } else if (type == 3) {
        drawRhombicuboctahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    } else if (type == 2) {
        drawTruncatedCube(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    } else if (type == 1) {
        drawTruncatedOctahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    } else {
        drawCuboctahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    }
  }

  // =========================================================================
  // TYPE 9: ICOSIDODECAHEDRON
  // =========================================================================
  private void drawIcosidodecahedron(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      // 20 Triangles
      int[][] triangles = {
          {2, 18, 19}, {3, 20, 21}, {10, 18, 26}, {1, 9, 13},
          {12, 20, 28}, {13, 21, 29}, {2, 14, 15}, {11, 19, 27},
          {0, 6, 10}, {3, 16, 17}, {5, 26, 28}, {4, 22, 24},
          {5, 27, 29}, {4, 23, 25}, {6, 14, 22}, {7, 15, 23},
          {8, 16, 24}, {1, 7, 11}, {9, 17, 25}, {0, 8, 12}
      };
      
      // 12 Pentagons
      int[][] pentagons = {
          {1, 11, 27, 29, 13}, {1, 7, 23, 25, 9}, {0, 10, 26, 28, 12}, {2, 14, 6, 10, 18},
          {2, 15, 7, 11, 19}, {5, 28, 20, 21, 29}, {3, 17, 9, 13, 21}, {4, 22, 14, 15, 23},
          {5, 26, 18, 19, 27}, {3, 16, 8, 12, 20}, {0, 6, 22, 24, 8}, {4, 24, 16, 17, 25}
      };
      
      if (internalMode == 0) {
          double r = pContext.random();
          // 20 triangles, 12 pentagons = 32 faces
          // Proportions: 62.5% triangles, 37.5% pentagons
          if (r < 0.625) {
              drawSolidPoly(pContext, verts, triangles[pContext.random(20)], pVarTP, pAmount, ox, oy, oz);
          } else {
              drawSolidPoly(pContext, verts, pentagons[pContext.random(12)], pVarTP, pAmount, ox, oy, oz);
          }
      } else {
          if (surfaceLines == 1 && (internalMode == 0 || pContext.random() < 0.5)) {
              double r = pContext.random();
              if (r < 0.625) {
                  drawPolyEdge(pContext, verts, triangles[pContext.random(20)], pVarTP, pAmount, ox, oy, oz);
              } else {
                  drawPolyEdge(pContext, verts, pentagons[pContext.random(12)], pVarTP, pAmount, ox, oy, oz);
              }
          } else if (internalMode > 0) {
              drawInternalLines(pContext, verts, pVarTP, pAmount, ox, oy, oz);
          }
      }
  }

  // =========================================================================
  // TYPE 8: TRUNCATED ICOSAHEDRON (SOCCER BALL)
  // =========================================================================
  private void drawTruncatedIcosahedron(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      // 12 Pentagons
      int[][] pentagons = {
          {7, 18, 50, 51, 19}, {3, 31, 39, 43, 35}, {11, 25, 57, 59, 27}, {4, 12, 44, 45, 13},
          {8, 20, 52, 54, 22}, {2, 30, 38, 42, 34}, {1, 29, 37, 41, 33}, {5, 14, 46, 47, 15},
          {6, 16, 48, 49, 17}, {0, 28, 36, 40, 32}, {10, 21, 53, 55, 23}, {9, 24, 56, 58, 26}
      };
      
      // 20 Hexagons
      int[][] hexagons = {
          {0, 2, 30, 54, 52, 28}, {13, 37, 29, 53, 21, 45}, {9, 11, 27, 51, 50, 26}, {12, 36, 28, 52, 20, 44},
          {19, 43, 35, 59, 27, 51}, {1, 3, 31, 55, 53, 29}, {4, 6, 16, 40, 36, 12}, {15, 39, 31, 55, 23, 47},
          {14, 38, 30, 54, 22, 46}, {1, 3, 35, 59, 57, 33}, {0, 2, 34, 58, 56, 32}, {5, 7, 19, 43, 39, 15},
          {5, 7, 18, 42, 38, 14}, {16, 40, 32, 56, 24, 48}, {8, 10, 21, 45, 44, 20}, {9, 11, 25, 49, 48, 24},
          {18, 42, 34, 58, 26, 50}, {8, 10, 23, 47, 46, 22}, {4, 6, 17, 41, 37, 13}, {17, 41, 33, 57, 25, 49}
      };
      
      if (internalMode == 0) {
          double r = pContext.random();
          // 12 pentagons, 20 hexagons = 32 faces
          // Proportions: 37.5% pentagons, 62.5% hexagons
          if (r < 0.375) {
              drawSolidPoly(pContext, verts, pentagons[pContext.random(12)], pVarTP, pAmount, ox, oy, oz);
          } else {
              drawSolidPoly(pContext, verts, hexagons[pContext.random(20)], pVarTP, pAmount, ox, oy, oz);
          }
      } else {
          if (surfaceLines == 1 && (internalMode == 0 || pContext.random() < 0.5)) {
              double r = pContext.random();
              if (r < 0.375) {
                  drawPolyEdge(pContext, verts, pentagons[pContext.random(12)], pVarTP, pAmount, ox, oy, oz);
              } else {
                  drawPolyEdge(pContext, verts, hexagons[pContext.random(20)], pVarTP, pAmount, ox, oy, oz);
              }
          } else if (internalMode > 0) {
              drawInternalLines(pContext, verts, pVarTP, pAmount, ox, oy, oz);
          }
      }
  }

  // =========================================================================
  // TYPE 7: RHOMBICOSIDODECAHEDRON
  // =========================================================================
  private void drawRhombicosidodecahedron(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      // 20 Triangles (edge-walked order)
      int[][] triangles = {
          {0, 49, 4}, {1, 52, 5}, {2, 6, 55}, {3, 58, 7},
          {8, 9, 50}, {10, 11, 53}, {12, 56, 13}, {14, 59, 15},
          {16, 48, 18}, {17, 51, 19}, {20, 54, 22}, {21, 57, 23},
          {24, 32, 40}, {25, 33, 41}, {26, 34, 42}, {27, 43, 35},
          {28, 36, 44}, {29, 45, 37}, {30, 46, 38}, {31, 47, 39}
      };
      
      // 30 Squares (edge-walked order)
      int[][] squares = {
          {0, 2, 6, 4}, {0, 24, 32, 49}, {1, 3, 7, 5}, {1, 25, 33, 52},
          {2, 26, 34, 55}, {3, 58, 35, 27}, {4, 49, 36, 28}, {5, 52, 37, 29},
          {6, 30, 38, 55}, {7, 58, 39, 31}, {8, 9, 13, 12}, {8, 32, 40, 50},
          {9, 33, 41, 50}, {10, 11, 15, 14}, {10, 34, 42, 53}, {11, 35, 43, 53},
          {12, 56, 44, 36}, {13, 56, 45, 37}, {14, 59, 46, 38}, {15, 59, 47, 39},
          {16, 17, 19, 18}, {16, 40, 24, 48}, {17, 51, 25, 41}, {18, 48, 26, 42},
          {19, 43, 27, 51}, {20, 21, 23, 22}, {20, 54, 28, 44}, {21, 57, 29, 45},
          {22, 46, 30, 54}, {23, 57, 31, 47}
      };
      
      // 12 Pentagons (edge-walked order)
      int[][] pentagons = {
          {0, 24, 48, 26, 2}, {1, 3, 27, 51, 25}, {4, 28, 54, 30, 6}, {5, 29, 57, 31, 7},
          {8, 32, 49, 36, 12}, {9, 33, 52, 37, 13}, {10, 34, 55, 38, 14}, {11, 35, 58, 39, 15},
          {16, 40, 50, 41, 17}, {18, 42, 53, 43, 19}, {20, 44, 56, 45, 21}, {22, 46, 59, 47, 23}
      };
      
      if (internalMode == 0) {
          double r = pContext.random();
          // 20 triangles, 30 squares, 12 pentagons = 62 faces
          // Proportions: 32% tri, 48% sq, 20% pent
          if (r < 0.32) {
              drawSolidPoly(pContext, verts, triangles[pContext.random(20)], pVarTP, pAmount, ox, oy, oz);
          } else if (r < 0.80) {
              drawSolidPoly(pContext, verts, squares[pContext.random(30)], pVarTP, pAmount, ox, oy, oz);
          } else {
              drawSolidPoly(pContext, verts, pentagons[pContext.random(12)], pVarTP, pAmount, ox, oy, oz);
          }
      } else {
          if (surfaceLines == 1 && (internalMode == 0 || pContext.random() < 0.5)) {
              double r = pContext.random();
              if (r < 0.32) {
                  drawPolyEdge(pContext, verts, triangles[pContext.random(20)], pVarTP, pAmount, ox, oy, oz);
              } else if (r < 0.80) {
                  drawPolyEdge(pContext, verts, squares[pContext.random(30)], pVarTP, pAmount, ox, oy, oz);
              } else {
                  drawPolyEdge(pContext, verts, pentagons[pContext.random(12)], pVarTP, pAmount, ox, oy, oz);
              }
          } else if (internalMode > 0) {
              drawInternalLines(pContext, verts, pVarTP, pAmount, ox, oy, oz);
          }
      }
  }

  // =========================================================================
  // TYPE 6: ICOSAHEDRON
  // =========================================================================
  private void drawIcosahedron(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      int[][] triangles = {
          {0, 8, 4}, {0, 4, 6}, {0, 6, 10}, {0, 10, 2}, {0, 2, 8},
          {3, 9, 1}, {3, 1, 11}, {3, 11, 7}, {3, 7, 5}, {3, 5, 9},
          {8, 4, 9}, {4, 6, 1}, {6, 10, 11}, {10, 2, 7}, {2, 8, 5},
          {4, 1, 9}, {6, 11, 1}, {10, 7, 11}, {2, 5, 7}, {8, 9, 5}
      };
      if (internalMode == 0) {
          drawSolidPoly(pContext, verts, triangles[pContext.random(20)], pVarTP, pAmount, ox, oy, oz);
      } else {
          if (surfaceLines == 1 && (internalMode == 0 || pContext.random() < 0.5)) {
              drawPolyEdge(pContext, verts, triangles[pContext.random(20)], pVarTP, pAmount, ox, oy, oz);
          } else if (internalMode > 0) {
              drawInternalLines(pContext, verts, pVarTP, pAmount, ox, oy, oz);
          }
      }
  }

  // =========================================================================
  // TYPE 5: TRUNCATED TETRAHEDRON
  // =========================================================================
  private void drawTruncatedTetrahedron(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      // 4 Triangles (The Corners)
      int[][] triangles = {
          {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {9, 10, 11}
      };

      // 4 Hexagons (The Faces) - Verified Planar Loops
      int[][] hexagons = {
          {0, 3, 4, 10, 11, 2},   // Face 1
          {1, 7, 8, 5, 3, 0},     // Face 2
          {2, 11, 9, 6, 7, 1},    // Face 3
          {4, 5, 8, 6, 9, 10}     // Face 4
      };

      if (internalMode == 0) {
          if (pContext.random() < 0.5) { 
             drawSolidPoly(pContext, verts, triangles[pContext.random(4)], pVarTP, pAmount, ox, oy, oz);
          } else {
             drawSolidPoly(pContext, verts, hexagons[pContext.random(4)], pVarTP, pAmount, ox, oy, oz);
          }
      } else {
          if (surfaceLines == 1 && (internalMode == 0 || pContext.random() < 0.5)) {
              if (pContext.random() < 0.5) {
                 drawPolyEdge(pContext, verts, triangles[pContext.random(4)], pVarTP, pAmount, ox, oy, oz);
              } else {
                 drawPolyEdge(pContext, verts, hexagons[pContext.random(4)], pVarTP, pAmount, ox, oy, oz);
              }
          } else if (internalMode > 0) {
              drawInternalLines(pContext, verts, pVarTP, pAmount, ox, oy, oz);
          }
      }
  }

  // =========================================================================
  // TYPE 4: GREAT RHOMBICUBOCTAHEDRON
  // =========================================================================
  private void drawGreatRhombicuboctahedron(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      int[][] octagons = {
          {16, 0, 4, 20, 22, 6, 2, 18}, {17, 1, 5, 21, 23, 7, 3, 19},
          {40, 32, 34, 42, 43, 35, 33, 41}, {44, 36, 38, 46, 47, 39, 37, 45},
          {24, 8, 12, 28, 29, 13, 9, 25}, {26, 10, 14, 30, 31, 15, 11, 27}
      };
      int[][] hexagons = {
          {0, 16, 32, 40, 24, 8}, {1, 17, 33, 41, 25, 9}, {2, 18, 34, 42, 26, 10}, {3, 19, 35, 43, 27, 11},
          {4, 20, 36, 44, 28, 12}, {5, 21, 37, 45, 29, 13}, {6, 22, 38, 46, 30, 14}, {7, 23, 39, 47, 31, 15}
      };
      int[][] squares = {
          {0, 4, 12, 8}, {2, 6, 14, 10}, {1, 5, 13, 9}, {3, 7, 15, 11},
          {16, 18, 34, 32}, {20, 22, 38, 36}, {17, 19, 35, 33}, {21, 23, 39, 37},
          {24, 25, 41, 40}, {28, 29, 45, 44}, {26, 27, 43, 42}, {30, 31, 47, 46}
      };
      if (internalMode == 0) {
          double r = pContext.random();
          if (r < 0.23) drawSolidPoly(pContext, verts, octagons[pContext.random(6)], pVarTP, pAmount, ox, oy, oz);
          else if (r < 0.54) drawSolidPoly(pContext, verts, hexagons[pContext.random(8)], pVarTP, pAmount, ox, oy, oz);
          else drawSolidPoly(pContext, verts, squares[pContext.random(12)], pVarTP, pAmount, ox, oy, oz);
      } else {
          if (surfaceLines == 1 && (internalMode == 0 || pContext.random() < 0.5)) {
              double r = pContext.random();
              if (r < 0.23) drawPolyEdge(pContext, verts, octagons[pContext.random(6)], pVarTP, pAmount, ox, oy, oz);
              else if (r < 0.54) drawPolyEdge(pContext, verts, hexagons[pContext.random(8)], pVarTP, pAmount, ox, oy, oz);
              else drawPolyEdge(pContext, verts, squares[pContext.random(12)], pVarTP, pAmount, ox, oy, oz);
          } else if (internalMode > 0) drawInternalLines(pContext, verts, pVarTP, pAmount, ox, oy, oz);
      }
  }

  // =========================================================================
  // TYPE 3: RHOMBICUBOCTAHEDRON
  // =========================================================================
  private void drawRhombicuboctahedron(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      int[][] triangles = { {0, 8, 16}, {1, 9, 17}, {2, 10, 18}, {3, 11, 19}, {4, 12, 20}, {5, 13, 21}, {6, 14, 22}, {7, 15, 23} };
      int[][] capSquares = { {0, 4, 6, 2}, {1, 3, 7, 5}, {8, 9, 13, 12}, {10, 14, 15, 11}, {16, 18, 19, 17}, {20, 21, 23, 22} };
      int[][] beltSquares = { 
          {0, 8, 12, 4}, {2, 6, 14, 10}, {1, 5, 13, 9}, {3, 11, 15, 7},
          {0, 2, 18, 16}, {4, 20, 22, 6}, {1, 17, 19, 3}, {5, 7, 23, 21},
          {8, 16, 17, 9}, {10, 11, 19, 18}, {12, 13, 21, 20}, {14, 22, 23, 15}
      };
      if (internalMode == 0) {
          if (pContext.random() < 0.3) drawSolidPoly(pContext, verts, triangles[pContext.random(8)], pVarTP, pAmount, ox, oy, oz);
          else if (pContext.random() < 0.33) drawSolidPoly(pContext, verts, capSquares[pContext.random(6)], pVarTP, pAmount, ox, oy, oz);
          else drawSolidPoly(pContext, verts, beltSquares[pContext.random(12)], pVarTP, pAmount, ox, oy, oz);
      } else {
          if (surfaceLines == 1 && (internalMode == 0 || pContext.random() < 0.5)) {
              if (pContext.random() < 0.3) drawPolyEdge(pContext, verts, triangles[pContext.random(8)], pVarTP, pAmount, ox, oy, oz);
              else if (pContext.random() < 0.33) drawPolyEdge(pContext, verts, capSquares[pContext.random(6)], pVarTP, pAmount, ox, oy, oz);
              else drawPolyEdge(pContext, verts, beltSquares[pContext.random(12)], pVarTP, pAmount, ox, oy, oz);
          } else if (internalMode > 0) drawInternalLines(pContext, verts, pVarTP, pAmount, ox, oy, oz);
      }
  }

  // =========================================================================
  // TYPE 2: TRUNCATED CUBE
  // =========================================================================
  private void drawTruncatedCube(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      int[][] triangles = { {0, 8, 16}, {1, 17, 9}, {2, 10, 18}, {3, 19, 11}, {4, 20, 12}, {5, 13, 21}, {6, 14, 22}, {7, 15, 23} };
      int[][] octagons = {
          {0, 8, 10, 2, 3, 11, 9, 1}, {4, 5, 13, 15, 7, 6, 14, 12}, {0, 16, 20, 4, 5, 21, 17, 1},
          {2, 3, 19, 23, 7, 6, 22, 18}, {8, 16, 20, 12, 14, 22, 18, 10}, {9, 17, 21, 13, 15, 23, 19, 11}
      };
      if (internalMode == 0) {
          if (pContext.random() < 0.2) drawSolidPoly(pContext, verts, triangles[pContext.random(8)], pVarTP, pAmount, ox, oy, oz);
          else drawSolidPoly(pContext, verts, octagons[pContext.random(6)], pVarTP, pAmount, ox, oy, oz);
      } else {
          if (surfaceLines == 1 && (internalMode == 0 || pContext.random() < 0.5)) {
              if (pContext.random() < 0.2) drawPolyEdge(pContext, verts, triangles[pContext.random(8)], pVarTP, pAmount, ox, oy, oz);
              else drawPolyEdge(pContext, verts, octagons[pContext.random(6)], pVarTP, pAmount, ox, oy, oz);
          } else if (internalMode > 0) drawInternalLines(pContext, verts, pVarTP, pAmount, ox, oy, oz);
      }
  }

  // =========================================================================
  // TYPE 1: TRUNCATED OCTAHEDRON
  // =========================================================================
  private void drawTruncatedOctahedron(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      int[][] squares = { {12, 17, 13, 16}, {14, 18, 15, 19}, {20, 8, 21, 10}, {22, 11, 23, 9}, {0, 4, 2, 6}, {1, 5, 3, 7} };
      int[][] hexagons = {
          {0, 4, 12, 16, 8, 20}, {1, 21, 8, 16, 13, 5}, {2, 22, 9, 17, 12, 4}, {3, 5, 13, 17, 9, 23}, 
          {0, 20, 10, 18, 14, 6}, {1, 7, 15, 18, 10, 21}, {2, 6, 14, 19, 11, 22}, {3, 23, 11, 19, 15, 7}
      };
      if (internalMode == 0) {
          if (pContext.random() < 0.42) drawSolidPoly(pContext, verts, squares[pContext.random(6)], pVarTP, pAmount, ox, oy, oz);
          else drawSolidPoly(pContext, verts, hexagons[pContext.random(8)], pVarTP, pAmount, ox, oy, oz);
      } else {
          if (surfaceLines == 1 && (internalMode == 0 || pContext.random() < 0.5)) {
              if (pContext.random() < 0.42) drawPolyEdge(pContext, verts, squares[pContext.random(6)], pVarTP, pAmount, ox, oy, oz);
              else drawPolyEdge(pContext, verts, hexagons[pContext.random(8)], pVarTP, pAmount, ox, oy, oz);
          } else if (internalMode > 0) drawInternalLines(pContext, verts, pVarTP, pAmount, ox, oy, oz);
      }
  }

  // =========================================================================
  // TYPE 0: CUBOCTAHEDRON
  // =========================================================================
  private void drawCuboctahedron(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      int[][] sq = { {0, 4, 1, 5}, {2, 7, 3, 6}, {0, 9, 2, 8}, {1, 10, 3, 11}, {4, 10, 6, 8}, {5, 9, 7, 11} };
      int[][] tri = { {0, 8, 4}, {0, 5, 9}, {1, 4, 10}, {1, 11, 5}, {2, 6, 8}, {2, 9, 7}, {3, 10, 6}, {3, 7, 11} };
      if (internalMode == 0) {
          if (pContext.random() < 0.42) drawSolidPoly(pContext, verts, sq[pContext.random(6)], pVarTP, pAmount, ox, oy, oz);
          else drawSolidPoly(pContext, verts, tri[pContext.random(8)], pVarTP, pAmount, ox, oy, oz);
      } else {
          if (surfaceLines == 1 && (internalMode == 0 || pContext.random() < 0.5)) {
              if (pContext.random() < 0.42) drawPolyEdge(pContext, verts, sq[pContext.random(6)], pVarTP, pAmount, ox, oy, oz);
              else drawPolyEdge(pContext, verts, tri[pContext.random(8)], pVarTP, pAmount, ox, oy, oz);
          } else if (internalMode > 0) drawInternalLines(pContext, verts, pVarTP, pAmount, ox, oy, oz);
      }
  }

  // =========================================================================
  // HELPERS
  // =========================================================================
  private void drawSolidPoly(FlameTransformationContext pContext, double[][] verts, int[] idx, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      int numTriangles = idx.length - 2;
      int tri = pContext.random(numTriangles);
      interpolate(verts[idx[0]], verts[idx[tri+1]], verts[idx[tri+2]], pContext.random(), pContext.random(), pVarTP, pAmount, ox, oy, oz);
  }

  private void drawPolyEdge(FlameTransformationContext pContext, double[][] verts, int[] idx, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
      int edge = pContext.random(idx.length);
      int v1 = idx[edge];
      int v2 = idx[(edge + 1) % idx.length];
      drawThickLine(pContext, verts[v1], verts[v2], pVarTP, pAmount, ox, oy, oz);
  }

  private void drawInternalLines(FlameTransformationContext pContext, double[][] verts, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
     int v1Index = pContext.random(verts.length);
     double[] p1 = verts[v1Index];
     double[] p2;
     if (internalMode == 1) p2 = new double[]{0, 0, 0};
     else {
         int v2Index = pContext.random(verts.length);
         if (v2Index == v1Index) v2Index = (v2Index + 1) % verts.length;
         p2 = verts[v2Index];
     }
     drawThickLine(pContext, p1, p2, pVarTP, pAmount, ox, oy, oz);
  }

  private void drawThickLine(FlameTransformationContext pContext, double[] pStart, double[] pEnd, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
     double t = pContext.random();
     double nx = pStart[0] + t * (pEnd[0] - pStart[0]);
     double ny = pStart[1] + t * (pEnd[1] - pStart[1]);
     double nz = pStart[2] + t * (pEnd[2] - pStart[2]);
     
     if (thickness > 0.0001) {
         nx += (pContext.random() - 0.5) * thickness * scale;
         ny += (pContext.random() - 0.5) * thickness * scale;
         nz += (pContext.random() - 0.5) * thickness * scale;
     }
     pVarTP.x += pAmount * (nx + ox);
     pVarTP.y += pAmount * (ny + oy);
     pVarTP.z += pAmount * (nz + oz);
  }

  private void interpolate(double[] p0, double[] p1, double[] p2, double u, double v, XYZPoint pVarTP, double pAmount, double ox, double oy, double oz) {
     if (u + v > 1.0) { u = 1.0 - u; v = 1.0 - v; }
     double nx = p0[0] + u * (p1[0] - p0[0]) + v * (p2[0] - p0[0]);
     double ny = p0[1] + u * (p1[1] - p0[1]) + v * (p2[1] - p0[1]);
     double nz = p0[2] + u * (p1[2] - p0[2]) + v * (p2[2] - p0[2]);
     pVarTP.x += pAmount * (nx + ox);
     pVarTP.y += pAmount * (ny + oy);
     pVarTP.z += pAmount * (nz + oz);
  }

  @Override
  public String[] getParameterNames() { return paramNames; }

  @Override
  public Object[] getParameterValues() { 
      return new Object[]{ 
          Double.valueOf(scale), Double.valueOf(thickness), Integer.valueOf(internalMode), Integer.valueOf(recursionDepth), Integer.valueOf(type), Integer.valueOf(surfaceLines)
      }; 
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_SCALE.equalsIgnoreCase(pName)) scale = pValue;
    else if (PARAM_THICKNESS.equalsIgnoreCase(pName)) { thickness = pValue; if (thickness < 0) thickness = 0; }
    else if (PARAM_INTERNAL.equalsIgnoreCase(pName)) { internalMode = (int)pValue; if (internalMode < 0) internalMode = 0; if (internalMode > 2) internalMode = 2; }
    else if (PARAM_RECURSION.equalsIgnoreCase(pName)) { recursionDepth = (int)pValue; if (recursionDepth < 0) recursionDepth = 0; }
    else if (PARAM_TYPE.equalsIgnoreCase(pName)) { type = (int)pValue; if (type < 0) type = 0; if (type > 9) type = 9; }
    else if (PARAM_SURFACE.equalsIgnoreCase(pName)) { surfaceLines = (int)pValue; if (surfaceLines < 0) surfaceLines = 0; if (surfaceLines > 1) surfaceLines = 1; }
  }

  @Override
  public String getName() { return "archimedean"; }
}
