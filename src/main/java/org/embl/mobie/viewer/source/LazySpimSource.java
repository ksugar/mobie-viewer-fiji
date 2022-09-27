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
package org.embl.mobie.viewer.source;


import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

public class LazySpimSource< T extends NumericType< T > > implements Source< T >
{
	private final LazySourceAndConverter< T > lazySourceAndConverter;
	private String name;
	private AffineTransform3D sourceTransform;
	private VoxelDimensions voxelDimensions;
	private final double[] min;
	private final double[] max;

	public LazySpimSource( LazySourceAndConverter< T > lazySourceAndConverter, String name, AffineTransform3D sourceTransform, VoxelDimensions voxelDimensions, double[] min, double[] max )
	{
		this.lazySourceAndConverter = lazySourceAndConverter;
		this.name = name;
		this.sourceTransform = sourceTransform;
		this.voxelDimensions = voxelDimensions;
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean isPresent( int t )
	{
		return lazySourceAndConverter.getSourceAndConverter().getSpimSource().isPresent( t );
	}

	@Override
	public RandomAccessibleInterval< T > getSource( int t, int level )
	{
		return lazySourceAndConverter.getSourceAndConverter().getSpimSource().getSource( t, level );
	}

	@Override
	public RealRandomAccessible< T > getInterpolatedSource( int t, int level, Interpolation method )
	{
		return lazySourceAndConverter.getSourceAndConverter().getSpimSource().getInterpolatedSource( t, level, method );
	}

	@Override
	public void getSourceTransform( int t, int level, AffineTransform3D transform )
	{
		if ( lazySourceAndConverter.isOpen() )
			lazySourceAndConverter.getSourceAndConverter().getSpimSource().getSourceTransform( t, level, transform );
		else
			transform.set( sourceTransform );
	}

	@Override
	public T getType()
	{
		return lazySourceAndConverter.getSourceAndConverter().getSpimSource().getType();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public VoxelDimensions getVoxelDimensions()
	{
		return voxelDimensions;
	}

	@Override
	public int getNumMipmapLevels()
	{
		return lazySourceAndConverter.getSourceAndConverter().getSpimSource().getNumMipmapLevels();
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public void setSourceTransform( AffineTransform3D sourceTransform )
	{
		this.sourceTransform = sourceTransform;
	}

	public double[] getMin()
	{
		return min;
	}

	public double[] getMax()
	{
		return max;
	}
}

