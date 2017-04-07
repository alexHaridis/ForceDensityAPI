/******************************************************************************
 *  Compilation:  javac BranchNodeGraph.java
 *  Execution:    java BranchNodeGraph
 *  Dependencies: Jama.Matrix  JamaUtils  processing.core.PApplet
 *
 *  Data structure for building the <b>C</b> Branch-Node Matrix and its
 *  sub-matrices <b>Cn</b>, and <b>Cf</b> for new and fixed points respectively.
 *  Represents topology of a given structural system as a set of <em>indices</em>
 *  (nodes) and a set of <em>directed links</em> (branches) between the indices.
 *  For instance, the branch that links nodes with indices 1 and 7 is represe-
 *  nted internally with a custom data type <em>Branch</em> that keeps the
 *  branch 17. 
 *  
 *  @author Alexandros Charidis, Digital Structures, MIT
 ******************************************************************************/

import java.util.ArrayList;
import java.util.Arrays;

import edu.umbc.cs.maple.utils.JamaUtils;
import processing.core.PApplet;

public class BranchNodeGraph {
	
    private ArrayList<Branch> adj_;    // internal branch container

    private int nodes_;                // number of nodes
    private int branches_;             // number of branches
    private int[] fixed_;              // indices for fixed nodes
    private int[] unknown_;            // indices for unknown nodes
    
    private Jama.Matrix C_;            // Branch-Node matrix C
    private Jama.Matrix Cn_;           // Sub - matrix of C with unknown nodes
    private Jama.Matrix Cf_;           // Sub - matrix of C with fixed nodes
    
    private JamaUtils ju;
    
    // helper branch class
    private class Branch {
        private int from, to;
        Branch() { /* empty constructor */ }
        Branch(int _from, int _to) {
            from = _from;
            to    = _to;
        }
    }
    
    // validate that v is a valid index
    private void validate(int v) {
        if (v < 0 || v >= nodes_) {
            throw new IndexOutOfBoundsException("index " + v + " is not between 0 and " + nodes_);
        }
    }
    
    // empty Branch Node Graph with N nodes
    public BranchNodeGraph(int N, int[] fixed) {
        if (N < 0) throw new RuntimeException("Number of nodes must be nonnegative");
        nodes_ = N;
        branches_ = 0;
        
        adj_ = new ArrayList<Branch>();
        fixed_ = (int[]) fixed;
        
        // load unknown points based on fixed points
        unknown_ = new int[N - fixed_.length];
        int count = 0;
        for (int i = 0; i < N; i++) {
            if (Arrays.binarySearch(fixed_, i) < 0) { // assumes array sorted
                unknown_[count] = i;
                count++;
            }
        }
    }
    
    // adds directed branch v-w. 
    public void addBranch(int v, int w) {
        validate(v);
        validate(w);
        
        Branch b = new Branch();
        
        b.from = v;
        b.to   = w;
        
        adj_.add(b);
        branches_++;
    }
    
    public void build() {
        if (branches_ < 0) throw new RuntimeException("Number of edges must be nonnegative");
        C_ = new Jama.Matrix(branches_, nodes_);
        
        //  Assign the topological relationships between nodes and branches.
        //  Each row of the matrix consists of +1, -1 and 0 so as:
        //     +1 if branch ends in node
        //     -1 if branch begins in node
        //     0 otherwise
        
        int counter = 0;
        for (Branch b : adj_) {
            int to = b.to;
            int from = b.from;
            
            C_.set(counter, from, 1);
            C_.set(counter, to, -1);
            
            counter++;
        }
        
        // construct sub-matrices Cn and Cf containing
        // new unknown nodes and fixed nodes respectively
        
        Cn_ = ju.getcolumns(C_, unknown_);
        Cf_ = ju.getcolumns(C_, fixed_);
        
        
        //  Construct the Branch-Node Matrix C by concatenating the matrices
        //  with New nodes and Fixed nodes so as: C = [Cn Cf]
         
        C_ = ju.columnAppend(Cn_, Cf_); 

    }
    
    
    // G E T T E R S
    
    // number of nodes and edges
    public int N()    {  return nodes_;     }
    public int B()    {  return branches_;  }
    
    // get Branch Node Matrix and its sub-matrices
    public Jama.Matrix C()  {  return C_;   }
    public Jama.Matrix Cn() {  return Cn_;  }
    public Jama.Matrix Cf() {  return Cf_;  }
    
    // get the indices of fixed and unknown nodes
    public int[] getFixed()   {  return fixed_;    }
    public int[] getUnknown() {  return unknown_;  }
    
    // get the indices of fixed and unknown nodes
    public int fixed()   {  return fixed_.length;    }
    public int unknown() {  return unknown_.length;  }
    
    // P R I N T 
    
    public void showCn(PApplet p5) {
        p5.println("\nNew nodes sub-Matrix");
        Misc.printJAMAMatrix(p5, Cn_);
    }
    
    public void showCf(PApplet p5) {
    	p5.println("\nFixed nodes sub-Matrix");
    	Misc.printJAMAMatrix(p5, Cf_);
    }
    
    public void showC(PApplet p5) {
        p5.println("\nBranch-Node Matrix");
        Misc.printJAMAMatrix(p5, C_);
    }
    
}
