package develop;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.RealMaskRealInterval;
import net.imglib2.roi.geom.GeomMasks;

import java.util.Arrays;

public class DevelopRealMaskOfSources
{
	public static void main( String[] args )
	{
		final AffineTransform3D transformA = new AffineTransform3D();
		final RealMaskRealInterval maskA = GeomMasks.closedBox( new double[]{ 0, 0, 0 }, new double[]{ 1, 1, 1 } ).transform( transformA );

		final AffineTransform3D transformB = new AffineTransform3D();
		transformB.translate( 5,5,0 );
		transformB.rotate( 2, 0.5 );
		final RealMaskRealInterval maskB = GeomMasks.closedBox( new double[]{ 0.5, 0.5, 0.5 }, new double[]{ 2, 2, 2 } ).transform( transformB );

		final RealMaskRealInterval maskAB = maskA.or( maskB );

		final RealPoint point = new RealPoint( 1.5, 1.5, 1.5 );
		System.out.println( maskAB.getClass() );
		System.out.println( Arrays.toString( maskAB.minAsDoubleArray() ) );
		System.out.println( Arrays.toString( maskAB.maxAsDoubleArray()) );
		System.out.println( maskAB.test( point ) );
	}
}
