package develop;

import bdv.util.BdvHandle;
import de.embl.cba.mobie.ui.viewer.MoBIEViewer;
import de.embl.cba.mobie.utils.Utils;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.Scale3D;

public class DevelopNormalisedViewerTransforms
{
	public static void main( String[] args )
	{
		testNormalisationAndReversion();

		final MoBIEViewer moBIEViewer = new MoBIEViewer( "https://github.com/mobie-org/covid-em-datasets" );
//BdvUtils.moveToPosition( moBIEViewer.getSourcesPanel().getBdv(), new double[]{10,10,10}, 0, 500 );
		final BdvHandle bdv = moBIEViewer.getSourcesPanel().getBdv();

		final String s = Utils.createNormalisedViewerTransformString( bdv );
		System.out.println( "Normalised transform");
		System.out.println( s );

		final AffineTransform3D absoluteView = new AffineTransform3D();
		bdv.getViewerPanel().state().getViewerTransform( absoluteView );
		System.out.println( absoluteView );

		final AffineTransform3D normView = Utils.createNormalisedViewerTransform( bdv );
		System.out.println( normView );

		final AffineTransform3D view = Utils.createUnnormalizedViewerTransform( normView, bdv );
		System.out.println( view ); // should be the same as absoluteView above
	}

	public static void testNormalisationAndReversion()
	{
		final AffineTransform3D affineTransform3D = new AffineTransform3D();
		System.out.println( "Identity: " + affineTransform3D );

		// transform the transform

		// translate
		final AffineTransform3D translation = new AffineTransform3D();
		translation.translate( 10, 10, 0 );
		affineTransform3D.preConcatenate( translation );

		// scale
		final Scale3D scale3D = new Scale3D( 0.1, 0.1, 0.1 );
		affineTransform3D.preConcatenate( scale3D );

		System.out.println( "Normalised translated and scaled: " + affineTransform3D );

		// invert above transformations
		affineTransform3D.preConcatenate( scale3D.inverse() );
		affineTransform3D.preConcatenate( translation.inverse() );

		System.out.println( "Reversed: " + affineTransform3D ); // should be identity again
	}

}
