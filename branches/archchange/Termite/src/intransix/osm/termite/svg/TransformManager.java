package intransix.osm.termite.svg;

//import org.apache.batik.dom.svg.*;
//import org.w3c.dom.svg.*;
//import java.awt.geom.*;
//import java.util.Stack;

/**
 *
 * @author sutter
 */
public class TransformManager {
//	
//	//this holds a list of transforms, the last one being the active one
//	private Stack<AffineTransform> transformStack;
//	
//	/** Constructor */
//	public TransformManager() {
//		//add a single identity tranform to the stack
//		transformStack = new Stack<AffineTransform>();
//		transformStack.push(new AffineTransform());
//	}
//	
//	/** This gets a marker, to be used to restore the transform to this state.*/
//	public int getMark() {
//		return transformStack.size();
//	}
//	
//	/** This restores the transform to the state at the time this mark was read. */
//	public void restoreToMark(int mark) {
//		while(transformStack.size() > mark) {
//			transformStack.pop();
//		}
//	}
//	
//	/** This method loads the transforms from a SVG node. */
//	public void loadTransforms(SVGGraphicsElement node) {
//		AffineTransform activeTransform = transformStack.peek();
//		SVGTransformList tl = node.getTransform().getBaseVal();
//		int tcount = tl.getNumberOfItems();
//		for(int titem = 0; titem < tcount; titem++) {
//			SVGTransform trans = tl.getItem(titem);
//			float a = trans.getMatrix().getA();
//			float b = trans.getMatrix().getB();
//			float c = trans.getMatrix().getC();
//			float d = trans.getMatrix().getD();
//			float e = trans.getMatrix().getE();
//			float f = trans.getMatrix().getF();
//			AffineTransform at = new AffineTransform(a,b,c,d,e,f);
//			AffineTransform newTransform = new AffineTransform(activeTransform);
//			newTransform.concatenate(at);
//			transformStack.add(newTransform);
//		}
//	}
//	
//	/** This method gets the active transform. */
//	public AffineTransform getActiveTransform() {
//		return transformStack.peek();
//	}
}
