package intransix.osm.termite.svg;

import org.apache.batik.dom.svg.*;
import org.w3c.dom.svg.*;
import java.awt.*;
import java.awt.geom.*;

public class SvgGeometryLoader {
	
	public static Shape loadGeometry(SVGGraphicsElement node, 
			TransformManager transformManager) {
		
		Shape shape = null;
		
		if(node instanceof SVGOMRectElement) {
			shape = loadRectangle((SVGOMRectElement)node);
		}
		else if(node instanceof SVGOMPolygonElement) {
			shape = SvgGeometryLoader.loadPolygon((SVGOMPolygonElement)node);
		}
		else if(node instanceof SVGOMPolylineElement) {
			shape = SvgGeometryLoader.loadPolyline((SVGOMPolylineElement)node);
		}
		else if(node instanceof SVGOMPathElement) {
			shape = SvgGeometryLoader.loadPath((SVGOMPathElement)node);
		}
		else if(node instanceof SVGOMLineElement) {
			shape = SvgGeometryLoader.loadLine((SVGOMLineElement)node);
		}
		else if(node instanceof SVGOMCircleElement) {
			shape = SvgGeometryLoader.loadCircle((SVGOMCircleElement)node);
		}
		else if(node instanceof SVGOMEllipseElement) {
			shape = SvgGeometryLoader.loadEllipse((SVGOMEllipseElement)node);
		}
		
		if(shape != null) {	
			//handle the transforms n this element
			int mark = transformManager.getMark();
			transformManager.loadTransforms(node);
			AffineTransform at = transformManager.getActiveTransform();
			shape = at.createTransformedShape(shape);
			transformManager.restoreToMark(mark);
		}
		
		return shape;
	}

	public static Shape loadRectangle(SVGOMRectElement svgRectElement) {
		//get geometry
		double x = svgRectElement.getX().getBaseVal().getValue();
		double y = svgRectElement.getY().getBaseVal().getValue();
		double w = svgRectElement.getWidth().getBaseVal().getValue();
		double h = svgRectElement.getHeight().getBaseVal().getValue();

		return new Rectangle2D.Double(x,y,w,h);
	}
	
	public static Shape loadCircle(SVGOMCircleElement svgCircleElement) {
		//get geometry
		double cx = svgCircleElement.getCx().getBaseVal().getValue();
		double cy = svgCircleElement.getCy().getBaseVal().getValue();
		double r = svgCircleElement.getR().getBaseVal().getValue();

		return new Ellipse2D.Double(cx, cy, r, r);
	}
	
	public static Shape loadEllipse(SVGOMEllipseElement svgEllipseElement) {
		//get geometry
		double cx = svgEllipseElement.getCx().getBaseVal().getValue();
		double cy = svgEllipseElement.getCy().getBaseVal().getValue();
		double rx = svgEllipseElement.getRx().getBaseVal().getValue();
		double ry = svgEllipseElement.getRy().getBaseVal().getValue();

		return new Ellipse2D.Double(cx, cy, rx, ry);
	}
	
	public static Shape loadLine(SVGOMLineElement svgLineElement) {
		//get geometry
		double x1 = svgLineElement.getX1().getBaseVal().getValue();
		double y1 = svgLineElement.getY1().getBaseVal().getValue();
		double x2 = svgLineElement.getX2().getBaseVal().getValue();
		double y2 = svgLineElement.getY2().getBaseVal().getValue();

		return new Line2D.Double(x1, y1, x2, y2);
	}
	
	public static Shape loadPolygon(SVGOMPolygonElement svgPolyElement) {
		SVGPointList pl = svgPolyElement.getPoints();
		int itemCount = pl.getNumberOfItems();
		Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		boolean firstPoint = true;
		for(int item = 0; item < itemCount; item++) {
			double x = pl.getItem(item).getX();
			double y = pl.getItem(item).getY();
			if(firstPoint) {
				path.moveTo(x, y);
				firstPoint = false;
			}
			else {
				path.lineTo(x, y);
			}
		}
		path.closePath();
		return path;
	}
	
	public static Shape loadPolyline(SVGOMPolylineElement svgPolyElement) {
		SVGPointList pl = svgPolyElement.getPoints();
		int itemCount = pl.getNumberOfItems();
		Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		boolean firstPoint = true;
		for(int item = 0; item < itemCount; item++) {
			double x = pl.getItem(item).getX();
			double y = pl.getItem(item).getY();
			if(firstPoint) {
				path.moveTo(x, y);
				firstPoint = false;
			}
			else {
				path.lineTo(x, y);
			}
		}
		return path;
	}
	
	public static Shape loadPath(SVGOMPathElement svgPathElement) {
		SVGPathSegList segList = svgPathElement.getPathSegList();
		int segCount = segList.getNumberOfItems();
		Path2D path = new Path2D.Double();
		//for smooth curves
		double prevControlX = 0;
		double prevControlY = 0;
		char prevCommandType = ' ';
		for(int item = 0; item < segCount; item++) {
			SVGPathSeg ps = segList.getItem(item);
			short type = ps.getPathSegType();
			switch(type) {
			case SVGPathSeg.PATHSEG_MOVETO_ABS:
			{
				double x = ((SVGPathSegMovetoAbs)ps).getX();
				double y = ((SVGPathSegMovetoAbs)ps).getY();
				path.moveTo(x, y);
				break;
			}
			case SVGPathSeg.PATHSEG_MOVETO_REL:
			{
				Point2D cp = path.getCurrentPoint();
				if(cp == null) cp = new Point2D.Double(0,0);
				double x = ((SVGPathSegMovetoRel)ps).getX();
				double y = ((SVGPathSegMovetoRel)ps).getY();
				path.moveTo(cp.getX() + x, cp.getY() + y);
				break;
			}
			case SVGPathSeg.PATHSEG_LINETO_ABS:
			{
				double x = ((SVGPathSegLinetoAbs)ps).getX();
				double y = ((SVGPathSegLinetoAbs)ps).getY();
				path.lineTo(x, y);
				break;
			}
			case SVGPathSeg.PATHSEG_LINETO_REL:
			{
				Point2D cp = path.getCurrentPoint();
				double x = ((SVGPathSegLinetoRel)ps).getX();
				double y = ((SVGPathSegLinetoRel)ps).getY();
				path.lineTo(cp.getX() + x, cp.getY() + y);
				break;
			}
			case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS:
			{
				Point2D cp = path.getCurrentPoint();
				double x = ((SVGPathSegLinetoHorizontalAbs)ps).getX();
				path.lineTo(x, cp.getY());
				break;
			}
			case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL:
			{
				Point2D cp = path.getCurrentPoint();
				double x = ((SVGPathSegLinetoHorizontalRel)ps).getX();
				path.lineTo(cp.getX() + x, cp.getY());
				break;
			}
			case SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS:
			{
				Point2D cp = path.getCurrentPoint();
				double y = ((SVGPathSegLinetoVerticalAbs)ps).getY();
				path.lineTo(cp.getX(),y);
				break;
			}
			case SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL:
			{
				Point2D cp = path.getCurrentPoint();
				float y = ((SVGPathSegLinetoVerticalRel)ps).getY();
				path.lineTo(cp.getX(), cp.getY() + y);
				break;
			}
			case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS:
			{
				double x = ((SVGPathSegCurvetoCubicAbs)ps).getX();
				double y = ((SVGPathSegCurvetoCubicAbs)ps).getY();
				double x1 = ((SVGPathSegCurvetoCubicAbs)ps).getX1();
				double y1 = ((SVGPathSegCurvetoCubicAbs)ps).getY1();
				double x2 = ((SVGPathSegCurvetoCubicAbs)ps).getX2();
				double y2 = ((SVGPathSegCurvetoCubicAbs)ps).getY2();
				path.curveTo(x1, y1, x2, y2, x, y);
				prevControlX = x2;
				prevControlY = y2;
				break;
			}
			case SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL:
			{
				Point2D cp = path.getCurrentPoint();
				double x = ((SVGPathSegCurvetoCubicRel)ps).getX();
				double y = ((SVGPathSegCurvetoCubicRel)ps).getY();
				double x1 = ((SVGPathSegCurvetoCubicRel)ps).getX1();
				double y1 = ((SVGPathSegCurvetoCubicRel)ps).getY1();
				double x2 = ((SVGPathSegCurvetoCubicRel)ps).getX2();
				double y2 = ((SVGPathSegCurvetoCubicRel)ps).getY2();
				path.curveTo(cp.getX() + x1,cp.getY() + y1,cp.getX() + x2,cp.getY() + y2,cp.getX() + x,cp.getY() + y);
				prevControlX = cp.getX() + x2;
				prevControlY = cp.getY() + y2;
				break;
			}
			case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS:
			{
				Point2D cp = path.getCurrentPoint();
				double x1,y1;
				if(isCubicType(prevCommandType)) {
					Point2D inferredControlPoint = getReflection(cp.getX(),cp.getY(),prevControlX,prevControlY);
					x1 = inferredControlPoint.getX();
					y1 = inferredControlPoint.getY();
				}
				else {
					x1 = cp.getX();
					y1 = cp.getY();
				}
				
				double x = ((SVGPathSegCurvetoCubicSmoothAbs)ps).getX();
				double y = ((SVGPathSegCurvetoCubicSmoothAbs)ps).getY();
				double x2 = ((SVGPathSegCurvetoCubicSmoothAbs)ps).getX2();
				double y2 = ((SVGPathSegCurvetoCubicSmoothAbs)ps).getY2();
				path.curveTo(x1, y1, x2, y2, x, y);
				prevControlX = x2;
				prevControlY = y2;
				break;
			}
			case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL:
			{
				Point2D cp = path.getCurrentPoint();
				double x1Abs,y1Abs;
				if(isCubicType(prevCommandType)) {
					Point2D inferredControlPoint = getReflection(cp.getX(),cp.getY(),prevControlX,prevControlY);
					x1Abs = inferredControlPoint.getX();
					y1Abs = inferredControlPoint.getY();
				}
				else {
					x1Abs = cp.getX();
					y1Abs = cp.getY();
				}

				double x = ((SVGPathSegCurvetoCubicSmoothRel)ps).getX();
				double y = ((SVGPathSegCurvetoCubicSmoothRel)ps).getY();
				double x2 = ((SVGPathSegCurvetoCubicSmoothRel)ps).getX2();
				double y2 = ((SVGPathSegCurvetoCubicSmoothRel)ps).getY2();
				path.curveTo(x1Abs, y1Abs, cp.getX() + x2, cp.getY() + y2, cp.getX() + x, cp.getY() + y);
				prevControlX = cp.getX() + x2;
				prevControlY = cp.getY() + y2;
				break;
			}
			case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS:
			{
				double x = ((SVGPathSegCurvetoQuadraticAbs)ps).getX();
				double y = ((SVGPathSegCurvetoQuadraticAbs)ps).getY();
				double x1 = ((SVGPathSegCurvetoQuadraticAbs)ps).getX1();
				double y1 = ((SVGPathSegCurvetoQuadraticAbs)ps).getY1();
				path.quadTo(x1, y1, x, y);
				prevControlX = x1;
				prevControlY = y1;
				break;
			}
			case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL:
			{
				Point2D cp = path.getCurrentPoint();
				double x = ((SVGPathSegCurvetoQuadraticRel)ps).getX();
				double y = ((SVGPathSegCurvetoQuadraticRel)ps).getY();
				double x1 = ((SVGPathSegCurvetoQuadraticRel)ps).getX1();
				double y1 = ((SVGPathSegCurvetoQuadraticRel)ps).getY1();
				path.quadTo(cp.getX() + x1,cp.getY() + y1,cp.getX() + x,cp.getY() + y);
				prevControlX = cp.getX() + x1;
				prevControlY = cp.getY() + y1;
				break;
			}
			case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS:
			{
				Point2D cp = path.getCurrentPoint();
				double x1,y1;
				if(isQuadraticType(prevCommandType)) {
					Point2D inferredControlPoint = getReflection(cp.getX(),cp.getY(),prevControlX,prevControlY);
					x1 = inferredControlPoint.getX();
					y1 = inferredControlPoint.getY();
				}
				else {
					x1 = cp.getX();
					y1 = cp.getY();
				}

				double x = ((SVGPathSegCurvetoQuadraticSmoothAbs)ps).getX();
				double y = ((SVGPathSegCurvetoQuadraticSmoothAbs)ps).getY();
				path.quadTo(x1, y1, x, y);
				prevControlX = x1;
				prevControlY = y1;
				break;
			}
			case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL:
			{
				Point2D cp = path.getCurrentPoint();
				double x1Abs,y1Abs;
				if(isQuadraticType(prevCommandType)) {
					Point2D inferredControlPoint = getReflection(cp.getX(),cp.getY(),prevControlX,prevControlY);
					x1Abs = inferredControlPoint.getX();
					y1Abs = inferredControlPoint.getY();
				}
				else {
					x1Abs = cp.getX();
					y1Abs = cp.getY();
				}

				double x = ((SVGPathSegCurvetoQuadraticSmoothRel)ps).getX();
				double y = ((SVGPathSegCurvetoQuadraticSmoothRel)ps).getY();
				path.quadTo(x1Abs, y1Abs, cp.getX() + x, cp.getY() + y);
				prevControlX = x1Abs;
				prevControlY = y1Abs;
				break;
			}
			case SVGPathSeg.PATHSEG_CLOSEPATH:
			{
				path.closePath();
				break;
			}
			case SVGPathSeg.PATHSEG_ARC_ABS:
			{
				System.out.println("ARC not supported! Replaced with a line segment.");
				double x = ((SVGPathSegArcAbs)ps).getX();
				double y = ((SVGPathSegArcAbs)ps).getY();
				path.lineTo(x, y);
				break;
			}
			case SVGPathSeg.PATHSEG_ARC_REL:
			{
				System.out.println("ARC not supported! Replaced with a line segment.");
				Point2D cp = path.getCurrentPoint();
				double x = ((SVGPathSegArcRel)ps).getX();
				double y = ((SVGPathSegArcRel)ps).getY();
				path.lineTo(cp.getX() + x, cp.getY() + y);
				break;
			}
			default:
			{
				//for now, jsut give up if an unsupported type arises
				throw new RuntimeException("Curve time not supported: " + ps.getPathSegTypeAsLetter());
			}
			
			}
			prevCommandType = ps.getPathSegTypeAsLetter().charAt(0);
		}
		return path;
	}

	private static boolean isCubicType(char type) {
		return ((type == 'C')||(type == 'c')||(type == 'S')||(type == 's'));
	}

	private static boolean isQuadraticType(char type) {
		return ((type == 'Q')||(type == 'q')||(type == 'T')||(type == 't'));
	}

	private static Point2D getReflection(double baseX, double baseY, double reflecteeX, double reflecteeY) {
		return new Point2D.Double(baseX + (baseX - reflecteeX),baseY + (baseY - reflecteeY));
	}
}


