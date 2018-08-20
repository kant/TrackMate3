/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2016 Tobias Pietzsch, Stephan Saalfeld, Stephan Preibisch,
 * Jean-Yves Tinevez, HongKee Moon, Johannes Schindelin, Curtis Rueden, John Bogovic
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
package org.mastodon.revised.bvv;

import bdv.viewer.VisibilityAndGrouping;
import java.util.stream.IntStream;
import org.scijava.ui.behaviour.util.Actions;
import tpietzsch.example2.VolumeViewerPanel;

public class NavigationActionsMamut
{
	public static final String TOGGLE_FUSED_MODE = "toggle fused mode";
	public static final String TOGGLE_GROUPING = "toggle grouping";
	public static final String SET_CURRENT_SOURCE = "set current source %d";
	public static final String TOGGLE_SOURCE_VISIBILITY = "toggle source visibility %d";
	public static final String ALIGN_XY_PLANE = "align XY plane";
	public static final String ALIGN_ZY_PLANE = "align ZY plane";
	public static final String ALIGN_XZ_PLANE = "align XZ plane";
	public static final String NEXT_TIMEPOINT = "next timepoint";
	public static final String PREVIOUS_TIMEPOINT = "previous timepoint";

	public static final String[] TOGGLE_FUSED_MODE_KEYS = new String[] { "F" };
	public static final String[] TOGGLE_GROUPING_KEYS = new String[] { "G" };
	public static final String SET_CURRENT_SOURCE_KEYS_FORMAT = "%s";
	public static final String TOGGLE_SOURCE_VISIBILITY_KEYS_FORMAT = "shift %s";
	public static final String[] ALIGN_XY_PLANE_KEYS = new String[] { "shift Z" };
	public static final String[] ALIGN_ZY_PLANE_KEYS = new String[] { "shift X" };
	public static final String[] ALIGN_XZ_PLANE_KEYS = new String[] { "shift Y", "shift A" };
	public static final String[] NEXT_TIMEPOINT_KEYS = new String[] { "CLOSE_BRACKET", "M" };
	public static final String[] PREVIOUS_TIMEPOINT_KEYS = new String[] { "OPEN_BRACKET", "N" };

	private final VolumeViewerPanel viewer;

	private final VisibilityAndGrouping vg;

	public NavigationActionsMamut( final VolumeViewerPanel viewer )
	{
		this.viewer = viewer;
		this.vg = viewer.getVisibilityAndGrouping();
	}

	public static void install( final Actions actions, final VolumeViewerPanel viewer )
	{
		new NavigationActionsMamut( viewer ).install( actions );
	}

	public void install( final Actions actions )
	{
		actions.runnableAction(	this::toggleFusedMode, TOGGLE_FUSED_MODE, TOGGLE_FUSED_MODE_KEYS );
		actions.runnableAction(	this::toggleGroupingMode, TOGGLE_GROUPING, TOGGLE_GROUPING_KEYS );

		final String[] numkeys = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" };
		IntStream.range( 0, numkeys.length ).forEach( i -> {
			actions.runnableAction( () -> vg.setCurrentGroupOrSource( i ), String.format( SET_CURRENT_SOURCE, i ), String.format( SET_CURRENT_SOURCE_KEYS_FORMAT, numkeys[ i ] ) );
			actions.runnableAction( ()-> vg.toggleActiveGroupOrSource( i ), String.format( TOGGLE_SOURCE_VISIBILITY, i ), String.format( TOGGLE_SOURCE_VISIBILITY_KEYS_FORMAT, numkeys[ i ] ) );
		} );

		actions.runnableAction( viewer::nextTimePoint, NEXT_TIMEPOINT, NEXT_TIMEPOINT_KEYS );
		actions.runnableAction( viewer::previousTimePoint, PREVIOUS_TIMEPOINT, PREVIOUS_TIMEPOINT_KEYS );

		// TODO
//		actions.runnableAction( () -> viewer.align( ViewerPanel.AlignPlane.XY ), ALIGN_XY_PLANE, ALIGN_XY_PLANE_KEYS );
//		actions.runnableAction( () -> viewer.align( ViewerPanel.AlignPlane.ZY ), ALIGN_ZY_PLANE, ALIGN_ZY_PLANE_KEYS );
//		actions.runnableAction( () -> viewer.align( ViewerPanel.AlignPlane.XZ ), ALIGN_XZ_PLANE, ALIGN_XZ_PLANE_KEYS );
	}

	private void toggleFusedMode()
	{
		vg.setFusedEnabled( !vg.isFusedEnabled() );
	}

	private void toggleGroupingMode()
	{
		vg.setGroupingEnabled( !vg.isGroupingEnabled() );
	}
}