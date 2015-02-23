package net.trackmate.trackscheme;

import gnu.trove.list.array.TIntArrayList;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.trackscheme.ScreenEdge.ScreenEdgePool;

public class ScreenEdgeList extends PoolObjectList< ScreenEdge, ByteMappedElement >
{
	public ScreenEdgeList( final ScreenEdgePool pool )
	{
		super( pool );
	}

	public ScreenEdgeList( final ScreenEdgePool pool, final int initialCapacity )
	{
		super( pool, initialCapacity );
	}

	protected ScreenEdgeList( final ScreenEdgeList list, final TIntArrayList indexSubList )
	{
		super( list, indexSubList );
	}

	@Override
	public ScreenEdgeList subList( final int fromIndex, final int toIndex )
	{
		return new ScreenEdgeList( this, ( TIntArrayList ) getIndexCollection().subList( fromIndex, fromIndex ) );
	}
}