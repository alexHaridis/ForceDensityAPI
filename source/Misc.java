/*************************************************************************
 *  Compilation:  javac Misc.java
 *  Execution:    java Misc
 *  Dependencies: processing.core.PApplet  processing.core.PFont
 *                Jama.Matrix
 *
 *  Helper functions primarily for displaying geometry, text, operating 
 *  with matrices in Processing API and Java.
 *  
 *  @author Alexandros Charidis, MIT
 *************************************************************************/

import processing.core.PApplet;
import processing.core.PFont;

public class Misc {
	
	/**
	 *  returns the diagonal matrix of the input <>vector<>
	 *  @throws RuntimeException if input not vector
	 */
	public static Jama.Matrix makeDiagonal(Jama.Matrix vector) {
	    if (vector.getColumnDimension() == 1) {
	        Jama.Matrix diag = new Jama.Matrix(vector.getRowDimension(), vector.getRowDimension());
	        for (int i = 0; i < diag.getRowDimension(); i++) {
	            for (int j = 0; j < diag.getColumnDimension(); j++) {
	                if (i == j) diag.set(i, j, vector.get(i, 0));
	            }
	        }
	        return diag;
	    } else {
	        throw new RuntimeException("Matrix is not a column vector");
	    }
	    //return new Jama.Matrix(new double[0][0]);
	}
	
	/**
	 *  raises the input matrix <>m<> to the power <>power<>
	 *  @return the matrix 
	 */
	public static Jama.Matrix powJAMAMatrix(PApplet p5, Jama.Matrix m, float power) {
	    Jama.Matrix output = new Jama.Matrix(m.getRowDimension(), m.getColumnDimension());
	    for (int i = 0; i < m.getRowDimension(); i++) {
	        for (int j = 0; j < m.getColumnDimension(); j++) {
	            double token = m.get(i, j);
	            output.set(i, j, p5.pow((float)token, power));
	        }
	    }
	    return output;
	}

	/**
	 *  Prints the elements of a JAMA matrix using Processing's print methods
	 */
	public static void printJAMAMatrix(PApplet p5, Jama.Matrix m) {
	    for (int i = 0; i < m.getRowDimension(); i++) {
	        for (int j = 0; j < m.getColumnDimension(); j++) {
	            double token = m.get(i, j);
	            int t = (int) token;
	            p5.print(t + " ");
	        }
	        p5.println();
	    }
	}
	
	/**
	 *  Draws the bounding box of the scene 
	 */
	public static void drawBox(final PApplet p5, float x, float y, float z) {
		p5.noFill();
		p5.stroke(180, 90);
		p5.strokeWeight(1);
		p5.pushMatrix();
		p5.translate(x/2, y/2, z/4);
		p5.box(x, y, z/2);
		p5.popMatrix();
	}
	
	/**
	 *  
	 */
	public static void drawSceneGrid(final PApplet p5, int nnX, int nnY, int lngth) {
		p5.stroke(200);
		p5.strokeWeight(0.5f);
	    
	    // horizontal 
	    for (int x = 0; x < nnX - 1; x++) {
	        for (int y = 0; y < nnY; y++) {
	            p5.line(x*lngth, y*lngth, (x + 1) * lngth, y*lngth);
	        }
	    }
	    for (int x = 0; x < nnX; x++) {
	        for (int y = 0; y < nnY - 1; y++) {
	            p5.line(x*lngth, y*lngth, x * lngth, (y + 1)*lngth);
	        }
	    }
	}
	
	/**
	 *  Helper function for displaying texts on screen
	 */
	public static void displaytext(PApplet p5, String _text, PFont _font, int _size, int _color, char _align, int _x, int _y){
	    switch (_align){
	      case 'C':
	    	p5.textAlign(p5.CENTER);
	        break;
	      case 'L':
	    	p5.textAlign(p5.LEFT);
	        break;
	      case 'R':
	    	p5.textAlign(p5.RIGHT);
	        break;
	    }
	    p5.textFont(_font, _size);
	    p5.fill(_color); // blue letters with oppacity
	    p5.text(_text, _x, _y);
	}
	
	/**
	 *  draws information about the current sketch
	 */
	public static void displayMark(PApplet p5, PFont font, int x, int y) {
	    String string0 = "F o r c e  D e n s i t y  M e t h o d";
	    String string1 = "Alexandros Charidis, MIT";
	    String string2 = "charidis@mit.edu";
	    String string3 = "4.s48 Computational Structural Design & Optimization";
	    String string4 = "Spring 2015";
	    
	    String string5 = "Built with Processing";
	    String string6 = "Using: Toxiclibs, PeasyCam, ControlP5 and Jama";
	    
	    int off = 20;
	    
	    displaytext(p5, string0, font, 12, 0, 'L', x, y);
	    displaytext(p5, string1, font, 12, 0, 'L', x, y + off);
	    displaytext(p5, string2, font, 12, 0, 'L', x, y + 2*off);
	    displaytext(p5, string3, font, 10, 0, 'L', x, y + 3*off + 10);
	    displaytext(p5, string4, font, 10, 0, 'L', x, y + 4*off + 10);
	    displaytext(p5, string5, font, 10, 0, 'L', x, y + 6*off + 10);
	    displaytext(p5, string6, font, 10, 0, 'L', x, y + 7*off + 10);
	}
	
	/**
	 *  draws the sum of F * L 
	 */
	public static void displaySigmaFl(PApplet p5, boolean drawSigmaFL, float FL, int H) {
	    if (drawSigmaFL)
	       displaytext(p5, "Sigma FL: " + FL, p5.createFont("Courier", 14), 11, 0, 'L', 290, H - 102);
	   else
	       displaytext(p5, "Sigma FL: ", p5.createFont("Courier", 11), 11, 0, 'C', 180, H - 100);
	}

}
