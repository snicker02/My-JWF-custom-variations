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
  private int type = 0;            // 0=Cuboct, 1=TruncOct, 2=TruncCube, 3=Rhombicuboct, 4=GreatRhomb, 5=TruncTetra, 6=Rhombicosidodeca, 7=TruncIcosa, 8=Icosidodeca
  private int surfaceLines = 1;    // 1 = Show Surface

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    // --- 1. SETUP SHAPE DATA ---
    double[][] currentUnitVerts; 
    
    if (type == 8) {
        // --- TYPE 8: ICOSIDODECAHEDRON (30 Vertices) ---
        double phi = (1.0 + Math.sqrt(5.0)) / 2.0;
        currentUnitVerts = new double[30][3];
        int idx = 0;
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
        // --- TYPE 7: TRUNCATED ICOSAHEDRON (60 Vertices) ---
        double phi = (1.0 + Math.sqrt(5.0)) / 2.0;
        currentUnitVerts = new double[60][3];
        int idx = 0;
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
    } else if (type == 6) {
        // --- TYPE 6: RHOMBICOSIDODECAHEDRON (60 Vertices) ---
        double P = (1.0 + Math.sqrt(5.0)) / 2.0;
        double P2 = P * P;
        double P3 = P * P * P;
        currentUnitVerts = new double[60][3];
        int idx = 0;
        for(int p=0; p<3; p++) {
            double[] c = (p==0) ? new double[]{1,1,P3} : (p==1) ? new double[]{1,P3,1} : new double[]{P3,1,1};
            for(int i=0; i<8; i++) {
                currentUnitVerts[idx++] = new double[]{ (i&4)==0?c[0]:-c[0], (i&2)==0?c[1]:-c[1], (i&1)==0?c[2]:-c[2] };
            }
        }
        for(int p=0; p<3; p++) {
            double[] c = (p==0) ? new double[]{P2,P,2*P} : (p==1) ? new double[]{P,2*P,P2} : new double[]{2*P,P2,P};
            for(int i=0; i<8; i++) {
                currentUnitVerts[idx++] = new double[]{ (i&4)==0?c[0]:-c[0], (i&2)==0?c[1]:-c[1], (i&1)==0?c[2]:-c[2] };
            }
        }
        double a = 2+P, b = P2;
        for(int s1=0; s1<2; s1++) {
            for(int s2=0; s2<2; s2++) {
                currentUnitVerts[idx++] = new double[]{ s1==0?a:-a, 0, s2==0?b:-b };
                currentUnitVerts[idx++] = new double[]{ 0, s1==0?b:-b, s2==0?a:-a };
                currentUnitVerts[idx++] = new double[]{ s1==0?b:-b, s2==0?a:-a, 0 };
            }
        }
    } else if (type == 5) {
        // --- TYPE 5: TRUNCATED TETRAHEDRON ---
        currentUnitVerts = new double[][] {
            {3,1,1}, {1,3,1}, {1,1,3}, {3,-1,-1}, {1,-3,-1}, {1,-1,-3}, 
            {-3,1,-1}, {-1,3,-1}, {-1,1,-3}, {-3,-1,1}, {-1,-3,1}, {-1,-1,3}
        };
    } else if (type == 4) {
        // --- TYPE 4: GREAT RHOMBICUBOCTAHEDRON ---
        double A = 1.0, B = 1.0 + Math.sqrt(2.0), C = 1.0 + 2.0 * Math.sqrt(2.0);
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

    // --- 2. RECURSION & SCALING ---
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
    if (type == 8) drawIcosidodecahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    else if (type == 7) drawTruncatedIcosahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    else if (type == 6) drawRhombicosidodecahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    else if (type == 5) drawTruncatedTetrahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    else if (type == 4) drawGreatRhombicuboctahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    else if (type == 3) drawRhombicuboctahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    else if (type == 2) drawTruncatedCube(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    else if (type == 1) drawTruncatedOctahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
    else drawCuboctahedron(pContext, activeVerts, pVarTP, pAmount, offsetX, offsetY, offsetZ);
  }

  // [Draw Methods for 8, 7, 6, 5, 4, 3, 2, 1, 0 go here...]

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_SCALE.equalsIgnoreCase(pName)) scale = pValue;
    else if (PARAM_THICKNESS.equalsIgnoreCase(pName)) { thickness = pValue; if (thickness < 0) thickness = 0; }
    else if (PARAM_INTERNAL.equalsIgnoreCase(pName)) { internalMode = (int)pValue; if (internalMode < 0) internalMode = 0; if (internalMode > 2) internalMode = 2; }
    else if (PARAM_RECURSION.equalsIgnoreCase(pName)) { recursionDepth = (int)pValue; if (recursionDepth < 0) recursionDepth = 0; }
    else if (PARAM_TYPE.equalsIgnoreCase(pName)) { type = (int)pValue; if (type < 0) type = 0; if (type > 8) type = 8; }
    else if (PARAM_SURFACE.equalsIgnoreCase(pName)) { surfaceLines = (int)pValue; if (surfaceLines < 0) surfaceLines = 0; if (surfaceLines > 1) surfaceLines = 1; }
  }

  @Override
  public String getName() { return "archimedean"; }
}
