<h1>Force Density Method</h1>

<p>This is a standalone application for exploring optimum Mitchell trusses written in Java. It uses the <a href="https://www.processing.org">Processing API</a> for graphic elements, rendering and GUI objects. This implementation builds upon a geometric solution of discrete Mitchell trusses in their basic symmetric form: a discrete optimum truss supported by two fixed points (positioned along the same vertical line) and a vertical point load positioned in between the supports at a fixed distance.</p> 

<p>The geometric solution is described in detail in Mazurek, A., Baker, W. F.,Tort, C.<a href="http://link.springer.com/article/10.1007/s00158-010-0559-x">"Geometrical aspects of optimum truss like structures"</a>, <em>Structural and Multidisciplinary Optimization</em>, 43 (2), 2011. Custom data structures were developed along with appropriate algorithms that provide a parametric framework for generating optimum trusses. More specifically, according to the parameters and the values picked, 21 trusses in total can be calculated. The parameters considered are: <em>h</em>, the vertical distance between the supports, <em>L</em> the horizontal distance between the supports and the point load applied at the tip of the structure, and <em>n</em> the total number of bars in the system. The forces on each member are computed with a custom routine that implements the <a href="https://en.wikibooks.org/wiki/Statics/Method_of_Joints">method of joints</a>.<br><br>
For more information, please read the accompanying writeup (file <em>MitchellStructureWritup.pdf</em>).</p>

<p>Dependencies: <a href="http://math.nist.gov/javanumerics/jama/">JAMA matrix package</a>, <a href="http://www.sojamo.de/libraries/controlP5/">ControlP5</a>, <a href="http://toxiclibs.org">toxiclibs</a>.
</p>

<h3> Acknowledgements </h3>
<p>This application was developed as part of an assignment in the course 4.s48 Computational Structural Design and Optimization (now 4.450J/1.575J) taught by Prof. Caitlin Mueller at MIT in the Spring semester of 2015.</p>
