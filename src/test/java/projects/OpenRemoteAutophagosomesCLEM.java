package projects;

import org.embl.mobie.viewer.MoBIE;
import org.embl.mobie.viewer.MoBIESettings;
import net.imagej.ImageJ;

import java.io.IOException;
import org.embl.mobie.io.ImageDataFormat;

public class OpenRemoteAutophagosomesCLEM
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		try {
			new MoBIE("https://github.com/mobie-org/autophagosomes-clem-datasets",
					MoBIESettings.settings().imageDataFormat( ImageDataFormat.BdvN5S3 ) );
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
