/*-
 * #%L
 * Fiji viewer for MoBIE projects
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.viewer.transform;

import bdv.viewer.SourceAndConverter;
import net.imglib2.Volatile;
import org.embl.mobie.viewer.source.TransformedTimepointSource;
import sc.fiji.bdvpg.sourceandconverter.SourceAndConverterHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimepointSourceTransformer extends AbstractSourceTransformer
{
	// Serialisation
	protected List< List< Integer > > timePoints;
	protected List< String > sources;
	protected List< String > sourceNamesAfterTransform;

	public TimepointSourceTransformer( String name, List< List< Integer > > timepoints, List< String > sources ) {
		this( name, timepoints, sources, null );
	}

	public TimepointSourceTransformer( String name, List< List< Integer > > timepoints, List< String > sources, List< String > sourceNamesAfterTransform )
	{
		this.name = name;
		this.timePoints = timepoints;
		this.sources = sources;
		this.sourceNamesAfterTransform = sourceNamesAfterTransform;
	}

//	public TimepointSourceTransformer( TransformedSource< ? > transformedSource )
//	{
//		AffineTransform3D fixedTransform = new AffineTransform3D();
//		transformedSource.getFixedTransform( fixedTransform );
//		name = "manualTransform";
//		timePoints = fixedTransform.getRowPackedCopy();
//		sources	= Arrays.asList( transformedSource.getWrappedSource().getName() );
//		sourceNamesAfterTransform =	Arrays.asList( transformedSource.getName() );
//	}

	@Override
	public void transform( Map< String, SourceAndConverter< ? > > sourceNameToSourceAndConverter )
	{
		// Convert to Map (it comes as List< List< Integer > >,
		// because this works better for the serialisation;
		// in a Map the key in JSON always is a String, which
		// is not appropriate here)
		final HashMap< Integer, Integer > timepointMap = new HashMap<>();
		for ( List< Integer > pair : timePoints )
		{
			timepointMap.put( pair.get( 0 ), pair.get( 1 ) );
		}

		for ( String sourceName : sourceNameToSourceAndConverter.keySet() )
		{
			if ( ! sources.contains( sourceName ) ) continue;

			final SourceAndConverter< ? > sac = sourceNameToSourceAndConverter.get( sourceName );

			if ( sourceNamesAfterTransform != null )
				sourceName =  sourceNamesAfterTransform.get( sources.indexOf( sourceName ) );

			final TransformedTimepointSource transformedSource = new TransformedTimepointSource( sourceName, sac.getSpimSource(), timepointMap );

			SourceAndConverter transformedSac;
			if ( sac.asVolatile() != null )
			{
				final SourceAndConverter< ? extends Volatile< ? > > vSac = sac.asVolatile();
				TransformedTimepointSource vTransformedSource = new TransformedTimepointSource( name, vSac.getSpimSource(), timepointMap );
				SourceAndConverter vTransformedSac = new SourceAndConverter<>( vTransformedSource, SourceAndConverterHelper.cloneConverter( vSac.getConverter(), vSac ) );
				transformedSac = new SourceAndConverter( transformedSource, SourceAndConverterHelper.cloneConverter( sac.getConverter(), sac ), vTransformedSac );
			}
			else
			{
				transformedSac = new SourceAndConverter<>( transformedSource, SourceAndConverterHelper.cloneConverter( sac.getConverter(), sac ) );
			}

			sourceNameToSourceAndConverter
		}
	}

	@Override
	public List< String > getSources()
	{
		return sources;
	}
}
