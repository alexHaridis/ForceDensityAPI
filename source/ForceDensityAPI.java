/*******************************************************************************
 *  Compilation:  javac ForceDensityAPI.java
 *  Execution:    java ForceDensityAPI
 *  Dependencies: BranchNodeGraph.java  ForceDensityMethod.java  Misc.java
 *                Misc.java  Jama.Matrix  Jama.Utils  processing.core.PApplet
 *                processing.core.PFont  PeasyCam  toxi.geom.Vec3D  controlP5 
 *
 *  This is a standalone application for exploring the <em>Force Density Method
 *  (FDM)</em> with a regular grid structure written in Java using the Processing 
 *  API for graphic elements, geometry, rendering and GUI. This application
 *  solves a grid with 36 number of nodes, 25 number of faces and 60 number of
 *  edges. Other combinations of values can be explored by changing the
 *  appropriate variables.
 *  
 *  For more information on the FDM method implemented here, see:
 *  Klaus Linkwitz, "Force Density Method," Ch.6 in Adriaenssens, S., Block
 *  P., Veenendaal, D. & Williams, C. 2014.<em>Shell Structures for 
 *  Architecture: Form Finding and Optimization</em>, Taylor & Francis.
 *  
 *  Please refer to the accompanying pdf "Form Finding and Surface Structures" 
 *  which explains this implementation.
 *  
 *  This is written in Java 8 and Processing 2.2.1 using Eclipse Luna 4.*. 
 *  Processing can be used within the Eclipse IDE following this tutorial:
 *  https://processing.org/tutorials/eclipse/
 *  
 *  @author Alexandros Haridis | Digital Structures, MIT
 *******************************************************************************/

import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.*;
import toxi.processing.*;
import toxi.geom.*;
import toxi.geom.mesh.*;
import edu.umbc.cs.maple.utils.*;

import java.util.Arrays;
import java.util.ArrayList;

import controlP5.*;
import peasy.*;

public class ForceDensityAPI extends PApplet {

	/**    System variables & controls    **/

	ControlP5 cp5;
	ToxiclibsSupport gfx;
	PeasyCam cam;
	JamaUtils ju;

	// Window
	int W = 1200, H = 600;

	// Scene
	int WIDTH = 1000, LENGTH = 800;
	int backgroundColor = color(255, 255, 255);

	// Fonts
	PFont signatureFont;

	/**    Force density constructors & parameters   */

	int numNodesX = 6;
	int numNodesY = 6;
	int V = numNodesX * numNodesY;
	int lngth     = 10 * 10;

	ArrayList<Vec3D> sysstate, sysstateunknown, sysstatefixed;
	int[] fixedN;

	BranchNodeGraph bng;
	ForceDensityMethod fdm;

	//
	Mesh3D mesh;
	
	boolean drawAsMesh = true;
	boolean drawSigmaFL = false;
	
	@SuppressWarnings("deprecation")
	public void setup() {
		size(W, H, OPENGL); 
	    smooth();
	    
	    // create font for signature text
	    signatureFont = createFont("Courier-48", 14, true);
	    
	    gfx = new ToxiclibsSupport(this);
	    cp5 = new ControlP5(this);
	    cam = new PeasyCam(this, 40, 300, 150, 1000);
	    cam.setMinimumDistance(40);
	    cam.setMaximumDistance(1000);
	    
	    sysstate = new ArrayList<Vec3D>();
	    sysstateunknown = new ArrayList<Vec3D>();
	    sysstatefixed   = new ArrayList<Vec3D>();
	    
	    // set boundary conditions
	    // the four corners of the plane are pinned (supported)
	    fixedN = new int[4];
	    fixedN[0] = 0;
	    fixedN[1] = numNodesX - 1;
	    fixedN[2] = numNodesX * (numNodesY - 1);
	    fixedN[3] = (numNodesX * numNodesY) - 1;
	    
	    bng = new BranchNodeGraph(V, fixedN);
	    
	    // horizontal
	    for (int y = 0; y < numNodesY; y++) {
	        for (int x = 0; x < numNodesX - 1; x++) {
	            bng.addBranch(y * numNodesY + x, y * numNodesY + x + 1);
	        }
	    }
	    
	    // vertical
	    for (int x = 0; x < numNodesX; x++) {
	        for (int y = 0; y < numNodesY - 1; y++) {
	            //int v = y * numNodesX + x;
	            //int w = (y + 1) * numNodesX + x;
	            
	            bng.addBranch(y * numNodesX + x, (y + 1) * numNodesX + x);
	        }
	    }
	    
	    bng.build();
	    
	    fdm = new ForceDensityMethod(bng);
	    
	    // build position vectors for all nodes
	    double[] _x = new double[V]; 
	    double[] _y = new double[V];  
	    double[] _z = new double[V]; 
	    
	    for (int j = 0; j < numNodesX; j++) {
	        for (int i = 0; i < numNodesY; i++) {
	            _x[j*numNodesX + i] = i*lngth;
	            _y[j*numNodesX + i] = j*lngth;
	            _z[j*numNodesX + i] = 0.;
	        }
	    }

	    fdm.setBoundaryConditions(_x, _y, _z);
	    
	    // Matrix related operations handle
	    ju = new JamaUtils();
	    
	    // Controls
	    cp5.setAutoDraw(false);
	    cp5.addSlider("sl_loadZ")
	       .setLabel("z: ")
	       .setPosition(150, H - 200)
	       .setRange(-80, 80)
	       .setColorActive(color(0, 255, 0))
	       .setColorForeground(color(0, 0, 255))
	       .setColorBackground(color(255,255,255))
	       .setSize(20, 100)
	       .setId(1);
	       
	    cp5.addButton("btn_evalSys")
	       .setLabel("EVALUATE SYSTEM")
	       .setBroadcast(false)
	       .setPosition(180, H - 140)
	       .setSize(100, 40)
	       .setValue(1)
	       .setColorCaptionLabel(0)
	       .setColorForeground(color(255, 255, 0))
	       .setColorBackground(color(240))
	       .setBroadcast(true)
	       .getCaptionLabel().align(CENTER,CENTER);
	       
	    cp5.addButton("btn_drawAsMesh")
	       .setLabel("Draw Mesh")
	       .setBroadcast(false)
	       .setPosition(W - 180, H - 140)
	       .setSize(100, 40)
	       .setValue(1)
	       .setColorCaptionLabel(0)
	       .setColorForeground(color(255, 255, 0))
	       .setColorBackground(color(240))
	       .setBroadcast(true)
	       .getCaptionLabel().align(CENTER,CENTER);
	      
	    cp5.addTextlabel("load")
	       .setText("External Load")
	       .setPosition(35, H - 230)
	       .setColorBackground(color(50,50,0))
	       .setFont(createFont("Courier",13));
	       
	    cp5.addSlider2D("sl2D_loadXY")
	         .setPosition(40, H - 200)
	         .setSize(100,100)
	         .setColorBackground(color(255, 0, 0))
	         .setColorForeground(color(0, 0, 255))
	         .setColorActive(color(0, 255, 0))
	         .setMinX(-50)
	         .setMinY(-50)
	         .setMaxX(50)
	         .setMaxY(50)
	         //.setArrayValue(new float[] {0,0})
	         .disableCrosshair();
	}

	public void draw() {
		if (cp5.isMouseOver()) {
	        cam.setActive(false);
	    } else {
	        cam.setActive(true);
	    }
	    
	    background(backgroundColor);
	    
	    drawMeshGrid();
	    //buildBox(WIDTH, LENGTH, 500);
	    
	    // draw origini axis
	    gfx.origin(500);
	    Misc.drawSceneGrid(this, numNodesX, numNodesY, lngth);
	    
	    // graphic user interface setup
	    gui();
	}
	
	// 
	public void gui() {
	    if (cp5.isMouseOver()) 
	    	cursor(CROSS);
	    else 
	    	cursor(ARROW);
	    
	    hint(DISABLE_DEPTH_TEST);
	    cam.beginHUD();
	    cp5.draw();
	    Misc.displayMark(this, signatureFont, 30, 30);
	    Misc.displaySigmaFl(this, drawSigmaFL, fdm.getSigmaFL(), H);
	    cam.endHUD();
	    hint(ENABLE_DEPTH_TEST);
	}
	
	public void controlEvent(ControlEvent theEvent) {
		  
	  if (theEvent.isFrom(cp5.getController("btn_evalSys"))) {
	      fdm.evalFD(this, bng);
	      sysstate = fdm.getState();
	      sysstateunknown = fdm.getStateUnknown();
	      sysstatefixed = fdm.getStateFixed();
	      drawSigmaFL = true;
	  }
	  
	  if (theEvent.isFrom(cp5.getController("btn_drawAsMesh"))) {
	      drawAsMesh = !drawAsMesh;
	  }
	  
	  if (theEvent.isFrom(cp5.getController("sl_loadZ"))) {
	      Vec3D currentLoad = fdm.getLoad();
	      fdm.setLoad(new Vec3D(currentLoad.x(), currentLoad.y(), theEvent.getController().getValue()));
	  }
	  
	  if (theEvent.isFrom(cp5.getController("sl2D_loadXY"))) {
	      Vec3D currentLoad = fdm.getLoad();
	      fdm.setLoad(new Vec3D(theEvent.getController().getArrayValue(0), theEvent.getController().getArrayValue(1), currentLoad.z()));
	  }

	}
	
	/**
	 *  Draw the surface using the state returned from FDM
	 */
	public void drawMeshGrid() {

	   if (sysstate.size() != 0) {
	     
	       stroke(0);
	       fill(20, 250, 70);
	       for (int i = 0; i < V; i++) {
	          if (sysstate.size() > 0) {
	              Sphere s = new Sphere(sysstate.get(i), 4);
	              gfx.sphere(s, 3, true);
	          }
	       } 
	       
	       // Reconstruct the original topology
	       
	       ArrayList<Vec3D> reconstructed = new ArrayList<Vec3D>();
	       int cntfxd = 0, cntunkn = 0;
	   
	       for (int i = 0; i < V; i++) {
	           // binarySearch guarantees that the return value will be >= 0 if and only if the key is found.
	           if (Arrays.binarySearch(fixedN, i) >= 0) {
	               reconstructed.add(sysstatefixed.get(cntfxd++));
	           } else {
	               reconstructed.add(sysstateunknown.get(cntunkn++));
	           }
	       }
	       
	       // Now that the original topology is reconstructed
	       // drawing the new grid is just a matter of repeating
	       // the original grid's topology construction
	       
	       if (!drawAsMesh) { // Draw as wireframe polygon mesh
	            
	           strokeWeight(1);
	           
	           for (int y = 0; y < numNodesY; y++) {
	              for (int x = 0; x < numNodesX - 1; x++) {
	                  Vec3D p0 = reconstructed.get(y * numNodesY + x);
	                  Vec3D p1 = reconstructed.get(y * numNodesY + x + 1);
	                  
	                  line(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z());
	              }
	           }
	           
	           for (int x = 0; x < numNodesX; x++) {
	              for (int y = 0; y < numNodesY - 1; y++) {
	                  Vec3D p0 = reconstructed.get(y * numNodesX + x);
	                  Vec3D p1 = reconstructed.get((y + 1) * numNodesX + x);
	                  
	                  line(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z());
	              }
	           }
	       
	       } else { // draw as a shaded polygon mesh
	           
	           strokeWeight(1);
	           
	           for (int y = 0; y < numNodesY; y++) {
	              for (int x = 0; x < numNodesX - 1; x++) {
	                  Vec3D p0 = reconstructed.get(y * numNodesY + x);
	                  Vec3D p1 = reconstructed.get(y * numNodesY + x + 1);
	                  
	                  line(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z());
	              }
	           }
	           
	           for (int x = 0; x < numNodesX; x++) {
	              for (int y = 0; y < numNodesY - 1; y++) {
	                  Vec3D p0 = reconstructed.get(y * numNodesX + x);
	                  Vec3D p1 = reconstructed.get((y + 1) * numNodesX + x);
	                  
	                  line(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z());
	              }
	           }
	           
	           mesh = new TriangleMesh();
	           for(int y = 0; y < numNodesX - 1; y++) {
	              for(int x = 0; x < numNodesY - 1; x++) {
	                int i = y * numNodesX + x;
	                
	                Vec3D p0 = reconstructed.get(i);
	                Vec3D p1 = reconstructed.get(i + 1);
	                Vec3D p2 = reconstructed.get(i + 1 + numNodesX);
	                Vec3D p3 = reconstructed.get(i + numNodesX);
	                
	                mesh.addFace(p0,p3,p2);
	                mesh.addFace(p0,p2,p1);
	              }
	           }
	           
	           fill(255,160,0);
	           noStroke();
	           gfx.mesh(mesh, false);
	           
	       }
	   }
	}
	
	public void keyPressed() {
	    saveFrame("fdm-######.png");
	}
	
	/**
	 *  Test client and sample execution.
	 */
	public static void main(String[] args) {
	}
}
