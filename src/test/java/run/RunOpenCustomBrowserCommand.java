package run;

import de.embl.cba.mobie2.command.OpenMoBIEProjectCommand;
import net.imagej.ImageJ;

public class RunOpenCustomBrowserCommand
{
	public static void main(final String... args)
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		ij.command().run( OpenMoBIEProjectCommand.class, true );
	}
}
