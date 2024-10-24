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
package org.embl.mobie.viewer.bdv.view;

import bdv.util.BdvHandle;
import bdv.viewer.SourceAndConverter;
import ij.IJ;
import org.embl.mobie.viewer.MoBIE;
import org.embl.mobie.viewer.bdv.MobieBdvSupplier;
import org.embl.mobie.viewer.bdv.MobieSerializableBdvOptions;
import org.embl.mobie.viewer.bdv.SourceNamesRenderer;
import org.embl.mobie.viewer.bdv.SourcesAtMousePositionSupplier;
import org.embl.mobie.viewer.bdv.ViewerTransformLogger;
import org.embl.mobie.viewer.bdv.render.BlendingMode;
import org.embl.mobie.viewer.color.OpacityAdjuster;
import org.embl.mobie.viewer.command.BigWarpRegistrationCommand;
import org.embl.mobie.viewer.command.ConfigureLabelRenderingCommand;
import org.embl.mobie.viewer.command.ConfigureImageVolumeRenderingCommand;
import org.embl.mobie.viewer.command.ConfigureLabelVolumeRenderingCommand;
import org.embl.mobie.viewer.command.ManualRegistrationCommand;
import org.embl.mobie.viewer.command.ScreenShotMakerCommand;
import org.embl.mobie.viewer.command.ShowRasterImagesCommand;
import org.embl.mobie.viewer.command.SourceAndConverterBlendingModeChangerCommand;
import org.embl.mobie.viewer.display.AbstractSourceDisplay;
import org.embl.mobie.viewer.segment.SliceViewRegionSelector;
import org.embl.mobie.viewer.source.SourceHelper;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.bdvpg.bdv.supplier.IBdvSupplier;
import sc.fiji.bdvpg.behaviour.SourceAndConverterContextMenuClickBehaviour;
import sc.fiji.bdvpg.scijava.services.SourceAndConverterBdvDisplayService;
import sc.fiji.bdvpg.scijava.services.SourceAndConverterService;
import sc.fiji.bdvpg.services.SourceAndConverterServices;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.embl.mobie.viewer.ui.WindowArrangementHelper.setBdvWindowPositionAndSize;

public class SliceViewer
{
	public static final String UNDO_SEGMENT_SELECTIONS = "Undo Segment Selections [ Ctrl Shift N ]";
	public static final String LOAD_ADDITIONAL_VIEWS = "Load Additional Views";
	public static final String SAVE_CURRENT_SETTINGS_AS_VIEW = "Save Current View";
	protected static final String FRAME_TITLE = "MoBIE - BigDataViewer";
	private final SourceAndConverterBdvDisplayService sacDisplayService;
	private BdvHandle bdvHandle;
	private final MoBIE moBIE;
	private final boolean is2D;
	private final ArrayList< String > projectCommands;

	private SourceAndConverterContextMenuClickBehaviour contextMenu;
	private final SourceAndConverterService sacService;
	private SourceNamesRenderer sourceNameRenderer;

	public SliceViewer( MoBIE moBIE, boolean is2D )
	{
		this.moBIE = moBIE;
		this.is2D = is2D;
		this.projectCommands = moBIE.getProjectCommands();

		sacService = ( SourceAndConverterService ) SourceAndConverterServices.getSourceAndConverterService();
		sacDisplayService = SourceAndConverterServices.getBdvDisplayService();

		bdvHandle = createBdv( is2D, FRAME_TITLE );
		setBdvWindowPositionAndSize( bdvHandle );
		sacDisplayService.registerBdvHandle( bdvHandle );

		sourceNameRenderer = new SourceNamesRenderer( bdvHandle, moBIE.initiallyShowSourceNames );

		installContextMenuAndKeyboardShortCuts();
	}

	public SourceNamesRenderer getSourceNameRenderer()
	{
		return sourceNameRenderer;
	}

	public BdvHandle getBdvHandle()
	{
		if ( bdvHandle == null )
		{
			bdvHandle = createBdv( is2D, FRAME_TITLE );
			sacDisplayService.registerBdvHandle( bdvHandle );
		}
		return bdvHandle;
	}

	private void installContextMenuAndKeyboardShortCuts( )
	{
		final SliceViewRegionSelector sliceViewRegionSelector = new SliceViewRegionSelector( bdvHandle, is2D, () -> moBIE.getViewManager().getAnnotatedRegionDisplays() );

		sacService.registerAction( UNDO_SEGMENT_SELECTIONS, sourceAndConverters -> {
			// TODO: Maybe only do this for the sacs at the mouse position
			sliceViewRegionSelector.clearSelection();
		} );

		sacService.registerAction( LOAD_ADDITIONAL_VIEWS, sourceAndConverters -> {
			// TODO: Maybe only do this for the sacs at the mouse position
			moBIE.getViewManager().getAdditionalViewsLoader().loadAdditionalViewsDialog();
		} );

		sacService.registerAction( SAVE_CURRENT_SETTINGS_AS_VIEW, sourceAndConverters -> {
			// TODO: Maybe only do this for the sacs at the mouse position
			moBIE.getViewManager().getViewsSaver().saveCurrentSettingsAsViewDialog();
		} );

		final Set< String > actionsKeys = sacService.getActionsKeys();

		final ArrayList< String > actions = new ArrayList< String >();
		actions.add( sacService.getCommandName( ScreenShotMakerCommand.class ) );
		actions.add( sacService.getCommandName( ShowRasterImagesCommand.class ) );
		actions.add( sacService.getCommandName( ViewerTransformLogger.class ) );
		actions.add( sacService.getCommandName( BigWarpRegistrationCommand.class ) );
		actions.add( sacService.getCommandName( ManualRegistrationCommand.class ) );
		actions.add( sacService.getCommandName( SourceAndConverterBlendingModeChangerCommand.class ) );
		actions.add( sacService.getCommandName( ConfigureLabelRenderingCommand.class ) );
		actions.add( sacService.getCommandName( ConfigureLabelVolumeRenderingCommand.class ) );
		actions.add( sacService.getCommandName( ConfigureImageVolumeRenderingCommand.class ) );
		actions.add( UNDO_SEGMENT_SELECTIONS );
		actions.add( LOAD_ADDITIONAL_VIEWS );
		actions.add( SAVE_CURRENT_SETTINGS_AS_VIEW );

		if ( projectCommands != null )
		{
			for ( String commandName : projectCommands )
			{
				actions.add( commandName );
			}
		}

		contextMenu = new SourceAndConverterContextMenuClickBehaviour( bdvHandle, new SourcesAtMousePositionSupplier( bdvHandle, is2D ), actions.toArray( new String[0] ) );

		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.behaviour( contextMenu, "Context menu", "button3", "shift P");
		behaviours.install( bdvHandle.getTriggerbindings(), "MoBIE" );

		behaviours.behaviour(
				( ClickBehaviour ) ( x, y ) ->
						new Thread( () -> sliceViewRegionSelector.run() ).start(),
				"Toggle selection", "ctrl button1" ) ;

		behaviours.behaviour(
				( ClickBehaviour ) ( x, y ) ->
						new Thread( () -> sliceViewRegionSelector.clearSelection() ).start(),
				"Clear selection", "ctrl shift N" ) ;

		behaviours.behaviour(
				( ClickBehaviour ) ( x, y ) ->
						new Thread( () ->
						{
							final SourceAndConverter[] sourceAndConverters = sacService.getSourceAndConverters().toArray( new SourceAndConverter[ 0 ] );
							ConfigureLabelRenderingCommand.incrementRandomColorSeed( sourceAndConverters );
						}).start(),
				"Change random color seed", "ctrl L" ) ;
	}

	public static BdvHandle createBdv( boolean is2D, String frameTitle )
	{
		final MobieSerializableBdvOptions sOptions = new MobieSerializableBdvOptions();
		sOptions.is2D = is2D;
		sOptions.frameTitle = frameTitle;

		IBdvSupplier bdvSupplier = new MobieBdvSupplier( sOptions );
		SourceAndConverterServices.getBdvDisplayService().setDefaultBdvSupplier(bdvSupplier);
		BdvHandle bdvHandle = SourceAndConverterServices.getBdvDisplayService().getNewBdv();

		return bdvHandle;
	}

	public Window getWindow()
	{
		return SwingUtilities.getWindowAncestor( bdvHandle.getViewerPanel() );
	}

	public void show( SourceAndConverter< ? > sourceAndConverter, AbstractSourceDisplay display )
	{
		// register
		SourceAndConverterServices.getSourceAndConverterService().register( sourceAndConverter );
		display.sourceNameToSourceAndConverter.put( sourceAndConverter.getSpimSource().getName(), sourceAndConverter );
		moBIE.sourceNameToSourceAndConverter().put( sourceAndConverter.getSpimSource().getName(), sourceAndConverter );

		// blending mode
		SourceAndConverterServices.getSourceAndConverterService().setMetadata( sourceAndConverter, BlendingMode.BLENDING_MODE, display.getBlendingMode() );

		// opacity
		OpacityAdjuster.adjustOpacity( sourceAndConverter, display.getOpacity() );

		// show in Bdv
		SourceAndConverterServices.getBdvDisplayService().show( bdvHandle, display.isVisible(), sourceAndConverter );
		updateTimepointSlider();
	}

	public void updateTimepointSlider( )
	{
		final List< SourceAndConverter< ? > > sources = bdvHandle.getViewerPanel().state().getSources();
		if ( sources.size() == 0 ) return;

		int numTimepoints = 1;
		for ( SourceAndConverter< ? > source : sources )
		{
			int numSourceTimepoints = SourceHelper.getNumTimepoints( source );
			if ( numSourceTimepoints > numTimepoints )
				numTimepoints = numSourceTimepoints;
		}
		bdvHandle.getViewerPanel().state().setNumTimepoints( numTimepoints );
	}

}
