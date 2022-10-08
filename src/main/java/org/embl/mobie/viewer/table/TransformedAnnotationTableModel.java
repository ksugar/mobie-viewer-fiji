package org.embl.mobie.viewer.table;

import net.imglib2.realtransform.AffineTransform3D;
import org.embl.mobie.viewer.annotation.Annotation;
import org.embl.mobie.viewer.transform.AnnotationTransformer;
import net.imglib2.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class TransformedAnnotationTableModel< A extends Annotation, TA extends A > implements AnnotationTableModel< TA >
{
	private final AnnotationTableModel< A > tableModel;
	private final AnnotationTransformer< A, TA > transformer;

	private Map< TA, Integer > annotationToIndex;
	private Map< Integer, TA > indexToAnnotation;

	public TransformedAnnotationTableModel( AnnotationTableModel< A > tableModel, AnnotationTransformer< A, TA > transformer )
	{
		this.tableModel = tableModel;
		this.transformer = transformer;
	}

	@Override
	public List< String > columnNames()
	{
		return tableModel.columnNames();
	}

	@Override
	public List< String > numericColumnNames()
	{
		return tableModel.numericColumnNames();
	}

	@Override
	public Class< ? > columnClass( String columnName )
	{
		return tableModel.columnClass( columnName );
	}

	@Override
	public int numAnnotations()
	{
		return tableModel.numAnnotations();
	}

	@Override
	public int rowIndexOf( TA annotation )
	{
		update();
		return annotationToIndex.get( annotation );
	}

	@Override
	public TA annotation( int rowIndex )
	{
		update();
		return indexToAnnotation.get( rowIndex );
	}

	@Override
	public void requestColumns( String columnsPath )
	{
		tableModel.requestColumns( columnsPath );
	}

	@Override
	public void setAvailableColumnPaths( Set< String > availableColumnPaths )
	{
		tableModel.setAvailableColumnPaths( availableColumnPaths );
	}

	@Override
	public Collection< String > availableColumnPaths()
	{
		return tableModel.availableColumnPaths();
	}

	@Override
	public LinkedHashSet< String > loadedColumnPaths()
	{
		return tableModel.loadedColumnPaths();
	}

	@Override
	public Pair< Double, Double > getMinMax( String columnName )
	{
		// FIXME
		return null;
	}

	@Override
	public Set< TA > annotations()
	{
		update();
		return annotationToIndex.keySet();
	}

	private synchronized void update()
	{
		if ( annotationToIndex == null )
		{
			annotationToIndex = new ConcurrentHashMap<>();
			indexToAnnotation = new ConcurrentHashMap<>();

			final int numAnnotations = tableModel.numAnnotations();
			for ( int rowIndex = 0; rowIndex < numAnnotations; rowIndex++ )
			{
				final TA transformedAnnotation = transformer.transform( tableModel.annotation( rowIndex ) );
				annotationToIndex.put( transformedAnnotation, rowIndex );
				indexToAnnotation.put( rowIndex, transformedAnnotation );
			}
		}
	}

	@Override
	public void addStringColumn( String columnName )
	{
		// FIXME
		throw new RuntimeException();
	}

	@Override
	public boolean isDataLoaded()
	{
		return tableModel.isDataLoaded();
	}

	@Override
	public String dataStore()
	{
		return tableModel.dataStore();
	}

	@Override
	public void transform( AffineTransform3D affineTransform3D )
	{
		throw new RuntimeException();
	}
}
