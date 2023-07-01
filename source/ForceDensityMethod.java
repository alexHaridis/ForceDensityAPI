/***************************************************************************
 *  Compilation:  javac ForceDensityMethod.java
 *  Execution:    java ForceDensityMethod
 *  Dependencies: Jama.Matrix  JamaUtils  toxi.geom  BranchNodeGraph.java
 *                processing.core.PApplet
 *
 *  An implementation of the Force Density Method (FDM) for a grid structure.
 *  Provides routines for calculating with branch-node matrix <b>C</b>, and
 *  its sub-matrices <b>Cn</b> and <b>Cf</b> for new and fixed points respe-
 *  ctively, computes vector coordinates x, y, z, the vector F for tension 
 *  forces, and structural performance as Σ*F_i*L_i, where F_i the force on 
 *  each element and L_i the length of the element.   
 *   
 *  For more information on the FDM method implemented here, see:
 *  Klaus Linkwitz, "Force Density Method," Ch.6 in Adriaenssens, S., Block
 *  P., Veenendaal, D. & Williams, C. 2014.<em>Shell Structures for 
 *  Architecture: Form Finding and Optimization</em>, Taylor & Francis. 
 *  
 *  @author Alexandros Haridis | Digital Structures, MIT
 ***************************************************************************/

import processing.core.PApplet;
import java.util.ArrayList;

import edu.umbc.cs.maple.utils.JamaUtils;
import toxi.geom.*;

public class ForceDensityMethod {
	
    private BranchNodeGraph bng_;     // internal branch node graph
    private Vec3D p_;                 // load force
    private Jama.Matrix x_, y_, z_;   // evaluated coordinate vectors & boundary conditions
    private ArrayList<Vec3D> state_;  // state of the system after applying FDM
    private Jama.Matrix sigmaFL_;     // structural performance measure
    private Jama.Matrix F_;           // branch tension forces
    private double[] xp_, yp_, zp_;   // 
    
    private JamaUtils ju;
    
    // constructor
    public ForceDensityMethod(BranchNodeGraph bng) {
        bng_ = bng;
        p_ = new Vec3D(0, 0, -1); // defaults -1 in the z axis
        state_ = new ArrayList<Vec3D>();
        sigmaFL_ = null;
        F_ = null;
    }
    
    // 
    public void setBoundaryConditions(double[] xp, double[] yp, double[] zp) {
        xp_ = (double[]) xp;
        yp_ = (double[]) yp;
        zp_ = (double[]) zp;
    }
    
    //
    public void evalFD(PApplet p5, final BranchNodeGraph bng) { 
        
        x_ = new Jama.Matrix(xp_, bng_.N());
        y_ = new Jama.Matrix(yp_, bng_.N());
        z_ = new Jama.Matrix(zp_, bng_.N());
         
        int[] n = (int[]) bng_.getUnknown();
        int[] f = (int[]) bng_.getFixed();
        
        Jama.Matrix xn = ju.getrows(x_, n);
        Jama.Matrix xf = ju.getrows(x_, f);
        Jama.Matrix yn = ju.getrows(y_, n);
        Jama.Matrix yf = ju.getrows(y_, f);
        Jama.Matrix zn = ju.getrows(z_, n);
        Jama.Matrix zf = ju.getrows(z_, f); 
       
        x_ = ju.rowAppend(xn, xf);
        y_ = ju.rowAppend(yn, yf);
        z_ = ju.rowAppend(zn, zf); 
        
        // Determine force densities
        
        Jama.Matrix q = ju.ones(bng_.B(), 1);
        Jama.Matrix Q = Misc.makeDiagonal(q);
        
        Jama.Matrix Dn = (bng.Cn()).transpose().times(Q).times(bng.Cn());
        Jama.Matrix Df = (bng.Cn()).transpose().times(Q).times(bng.Cf());
        
        // Solve Ax = b
        
        Jama.Matrix DfXf = (Df.times(xf)).uminus();
        Jama.Matrix DfYf = (Df.times(yf)).uminus();
        Jama.Matrix DfZf = (Df.times(zf)).uminus();
        
        Jama.Matrix tmpX = ju.ones(DfXf.getRowDimension(), DfXf.getColumnDimension());
        Jama.Matrix tmpY = ju.ones(DfYf.getRowDimension(), DfYf.getColumnDimension());
        Jama.Matrix tmpZ = ju.ones(DfZf.getRowDimension(), DfZf.getColumnDimension());
        tmpX.timesEquals(p_.x());
        tmpY.timesEquals(p_.y());
        tmpZ.timesEquals(p_.z());
        
        Jama.Matrix Bx = DfXf.plus(tmpX);
        Jama.Matrix By = DfYf.plus(tmpY);
        Jama.Matrix Bz = DfZf.plus(tmpZ);
        
        xn = Dn.solve(Bx);
        yn = Dn.solve(By);
        zn = Dn.solve(Bz);
        
        // concatenate 
        
        x_ = ju.rowAppend(xn, xf);
        y_ = ju.rowAppend(yn, yf);
        z_ = ju.rowAppend(zn, zf);
        
        //
        
        Jama.Matrix u = (bng_.C()).times(x_);
        Jama.Matrix v = (bng_.C()).times(y_);
        Jama.Matrix w = (bng_.C()).times(z_);
        
        Jama.Matrix U = Misc.makeDiagonal(u);
        Jama.Matrix V = Misc.makeDiagonal(v);
        Jama.Matrix W = Misc.makeDiagonal(w); 
        
        Jama.Matrix L = Misc.powJAMAMatrix(p5, U.times(U).plus(V.times(V)).plus(W.times(W)), 1f/2f);
        
        // compute tension forces acting on each branch

        F_ = L.times(q);
        
        // compute performance
         
        sigmaFL_ = F_.transpose().times(L);
        
    }
    
    // set the loading on each node
    public void setLoad(Vec3D p) {  p_ = p;  }
    
    // @return a container of Vec3D with coordinates the coordinate vectors computed with FDM
    public ArrayList<Vec3D> getState() {
    	
        state_ = new ArrayList<Vec3D>(); 
        for (int i = 0; i < bng_.N(); i++) {
            state_.add(new Vec3D((float)x_.get(i, 0), (float)y_.get(i, 0), (float)z_.get(i, 0)));
        }
        return state_;
        
    }
    
    // @return a container of Vec3D with coordinates the unknown points' coordinate vectors computed with FDM
    public ArrayList<Vec3D> getStateUnknown() {
    	
        ArrayList<Vec3D> stateUnknown = new ArrayList<Vec3D>();
        for (int i = 0; i < bng_.unknown(); i++) {
            stateUnknown.add(new Vec3D((float)x_.get(i, 0), (float)y_.get(i, 0), (float)z_.get(i, 0)));
        }
        return stateUnknown;
        
    }
    
    // @return a container of Vec3D with coordinates the fixed points' coordinate vectors computed with FDM
    public ArrayList<Vec3D> getStateFixed() {
    	
        ArrayList<Vec3D> stateFixed = new ArrayList<Vec3D>();
        for (int i = 0; i < bng_.fixed(); i++) {
            int tmpInd = i + bng_.unknown(); // get all elements with indices: unknown -> unknown + fixed
            stateFixed.add(new Vec3D((float)x_.get(tmpInd, 0), (float)y_.get(tmpInd, 0), (float)z_.get(tmpInd, 0)));
        }
        return stateFixed;
        
    }
    
    // @return a column matrix of branch tension forces
    public Jama.Matrix getBranchForces() {  return F_;  }
    
    // @return the current load vector
    public Vec3D getLoad() {  return p_;  }
    
    // @return the sum of F * L
    public float getSigmaFL() {
      
        double sum = 0;
        if (sigmaFL_ != null) {
	        for (int i = 0; i < sigmaFL_.getRowDimension(); i++) {
	            for (int j = 0; j < sigmaFL_.getColumnDimension(); j++) {
	                if ( i == j) {
	                    sum += sigmaFL_.get(i, j);
	                }
	            }
	        }
        }
        return (float)sum;
        
    }
    
}
