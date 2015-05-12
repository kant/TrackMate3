package net.trackmate.trackscheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformListener;
import net.imglib2.ui.util.GuiUtil;
import net.imglib2.util.BenchmarkHelper;
import net.trackmate.graph.collection.RefSet;

public class ShowTrackScheme implements TransformListener< ScreenTransform >, SelectionListener
{
	private static final double SELECT_DISTANCE_TOLERANCE = 5.0;

	public class SelectionHandler extends MouseAdapter implements MouseListener, MouseMotionListener
	{

		private final OverlayRenderer selectionBoxOverlay = new SelectionBoxOverlay();

		/**
		 * Whom to notify when selecting stuff.
		 */
		private SelectionListener selectionListener;

		/**
		 * Coordinates where mouse dragging started.
		 */
		private int oX, oY;

		/**
		 * Coordinates where mouse dragging currently is.
		 */
		private int eX, eY;

		private ScreenTransform transform;

		private boolean dragStarted = false;

		@Override
		public void mouseClicked( MouseEvent e )
		{
			if ( e.getButton() == MouseEvent.BUTTON1 )
			{
				selectionListener.selectAt( transform, e.getX(), e.getY() );
				frame.repaint();
			}
		}

		@Override
		public void mouseDragged( MouseEvent e )
		{
			if ( e.getButton() == MouseEvent.BUTTON1 )
			{
				eX = e.getX();
				eY = e.getY();
				if ( dragStarted == false )
				{
					dragStarted = true;
					oX = e.getX();
					oY = e.getY();
				}
				frame.repaint();
			}
		}

		@Override
		public void mouseReleased( MouseEvent e )
		{
			if ( e.getButton() == MouseEvent.BUTTON1 && dragStarted )
			{
				dragStarted = false;
				selectionListener.selectWithin( transform, oX, oY, eX, eY );
				frame.repaint();
			}
		}

		public void setSelectionListener( final SelectionListener selectionListener )
		{
			this.selectionListener = selectionListener;
		}

		public void setTransform( ScreenTransform transform )
		{
			this.transform = transform;
		}

		public OverlayRenderer getSelectionBoxOverlay()
		{
			return selectionBoxOverlay;
		}

		public class SelectionBoxOverlay implements OverlayRenderer
		{

			@Override
			public void drawOverlays( Graphics g )
			{
				if ( !dragStarted ) { return; }
				g.setColor( Color.RED );
				final int x = Math.min( oX, eX );
				final int y = Math.min( oY, eY );
				final int width = Math.abs( eX - oX );
				final int height = Math.abs( eY - oY );
				g.drawRect( x, y, width, height );
			}

			@Override
			public void setCanvasSize( int width, int height )
			{}

		}
	}

	final TrackSchemeGraph graph;

	final LineageTreeLayout layout;

	final VertexOrder order;

	final GraphLayoutOverlay overlay;

	final MyFrame frame;

	private final SelectionHandler selectionHandler;

	public ShowTrackScheme( final TrackSchemeGraph graph )
	{
		this.graph = graph;

		layout = new LineageTreeLayout( graph );
//		layout.reset();
//		layout.layoutX();

		System.out.println( "benchmarking layout of the full graph:" );
		BenchmarkHelper.benchmarkAndPrint( 10, true, new Runnable()
		{
			@Override
			public void run()
			{
				layout.reset();
				layout.layoutX();
			}
		} );
		System.out.println();

//		System.out.println( graph );

		order = new VertexOrder( graph );
		order.build();
//		order.print();

		overlay = new GraphLayoutOverlay();
		overlay.setCanvasSize( 800, 600 );

		final InteractiveDisplayCanvasComponent< ScreenTransform > canvas = new InteractiveDisplayCanvasComponent< ScreenTransform >( 800, 600, ScreenTransform.ScreenTransformEventHandler.factory() );
		final double minY = order.getMinTimepoint() - 0.5;
		final double maxY = order.getMaxTimepoint() + 0.5;
		final double minX = order.getMinX() - 1.0;
		final double maxX = order.getMaxX() + 1.0;
		final int w = overlay.getWidth();
		final int h = overlay.getHeight();

		selectionHandler = new SelectionHandler();
		canvas.addMouseListener( selectionHandler );
		canvas.addMouseMotionListener( selectionHandler );
		selectionHandler.setSelectionListener( this );

		final ScreenTransform screenTransform = new ScreenTransform( minX, maxX, minY, maxY, w, h );
		canvas.getTransformEventHandler().setTransform( screenTransform );
		canvas.getTransformEventHandler().setTransformListener( this );

		frame = new MyFrame( "trackscheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		frame.getContentPane().add( canvas, BorderLayout.CENTER );
		frame.pack();
		frame.setVisible( true );
		canvas.addOverlayRenderer( selectionHandler.getSelectionBoxOverlay() );
		canvas.addOverlayRenderer( overlay );
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		selectionHandler.setTransform( transform );

//		System.out.println( "transformChanged" );
		final double minX = transform.minX;
		final double maxX = transform.maxX;
		final double minY = transform.minY;
		final double maxY = transform.maxY;
		final int w = transform.screenWidth;
		final int h = transform.screenHeight;
		final ScreenEntities entities = order.cropAndScale( minX, maxX, minY, maxY, w, h );
		overlay.setScreenEntities( entities );
		frame.repaint();
	}

	@Override
	public void selectAt( final ScreenTransform transform, final int x, final int y )
	{
		final double lx = transform.screenToLayoutX( x );
		final double ly = transform.screenToLayoutY( y );

		final TrackSchemeVertex closestVertex = order.getClosestVertex( lx, ly, SELECT_DISTANCE_TOLERANCE, graph.vertexRef() );

		if ( null == closestVertex )
		{
			final TrackSchemeEdge closestEdge = order.getClosestEdge( lx, ly, SELECT_DISTANCE_TOLERANCE, graph.edgeRef() );

			if ( null != closestEdge )
			{
				final boolean selected = !closestEdge.isSelected();
				closestEdge.setSelected( selected );
				final ScreenEdge screenEdge = order.getScreenEdgeFor( closestEdge );
				if ( null != screenEdge )
				{
					screenEdge.setSelected( selected );
				}
			}
		}
		else
		{
			final boolean selected = !closestVertex.isSelected();
			closestVertex.setSelected( selected );
			final ScreenVertex screenVertex = order.getScreenVertexFor( closestVertex );
			if ( null != screenVertex )
			{
				screenVertex.setSelected( selected );
			}
		}
	}

	@Override
	public void selectWithin( ScreenTransform transform, int x1, int y1, int x2, int y2 )
	{
		final double lx1 = transform.screenToLayoutX( x1 );
		final double ly1 = transform.screenToLayoutY( y1 );
		final double lx2 = transform.screenToLayoutX( x2 );
		final double ly2 = transform.screenToLayoutY( y2 );

		final RefSet< TrackSchemeVertex > vs = order.getVerticesWithin( lx1, ly1, lx2, ly2 );
		TrackSchemeVertex t = graph.vertexRef();
		final boolean selected = true;
		for ( final TrackSchemeVertex v : vs )
		{
			v.setSelected( selected );
			final ScreenVertex sv = order.getScreenVertexFor( v );
			if ( null != sv )
			{
				sv.setSelected( selected );
			}

			for ( final TrackSchemeEdge e : v.outgoingEdges() )
			{
				t = e.getTarget( t );
				if ( vs.contains( t ) )
				{
					e.setSelected( selected );
					final ScreenEdge se = order.getScreenEdgeFor( e );
					if ( null != se )
					{
						se.setSelected( selected );
					}
				}
			}
		}

		graph.releaseRef( t );
	}

	static class MyFrame extends JFrame implements PainterThread.Paintable
	{
		private static final long serialVersionUID = 1L;

		private final PainterThread painterThread;

		public MyFrame( final String title, final GraphicsConfiguration gc )
		{
			super( title, gc );
			painterThread = new PainterThread( this );
			setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( final WindowEvent e )
				{
					painterThread.interrupt();
				}
			} );
		}

		@Override
		public void paint()
		{
			repaint();
		}
	}

	public static void main( final String[] args )
	{
		final TrackSchemeGraph graph = new TrackSchemeGraph();

		final TrackSchemeVertex v0 = graph.addVertex().init( "0", 0, false );
		final TrackSchemeVertex v1 = graph.addVertex().init( "1", 1, false );
		final TrackSchemeVertex v2 = graph.addVertex().init( "2", 1, false );;
		final TrackSchemeVertex v3 = graph.addVertex().init( "3", 2, false );;
		final TrackSchemeVertex v4 = graph.addVertex().init( "4", 3, false );;
		final TrackSchemeVertex v5 = graph.addVertex().init( "5", 4, false );;

		graph.addEdge( v0, v1 );
		graph.addEdge( v0, v2 );
		graph.addEdge( v1, v3 );
		graph.addEdge( v4, v5 );

		final ShowTrackScheme showTrackScheme = new ShowTrackScheme( graph );
	}
}
