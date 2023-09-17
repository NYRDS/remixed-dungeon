
package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;

public class Archs extends Component {

	private static final float SCROLL_SPEED	= 20f;
	
	private SkinnedBlock arcsBg;
	private SkinnedBlock arcsFg;
	
	private static float offsB = 0;
	private static float offsF = 0;
	
	public boolean reversed = false;

	@Override
	protected void createChildren() {
		arcsBg = new SkinnedBlock( 1, 1, Assets.ARCS_BG );
		arcsBg.offsetTo( 0,  offsB );
		add( arcsBg );
		
		arcsFg = new SkinnedBlock( 1, 1, Assets.ARCS_FG );
		arcsFg.offsetTo( 0,  offsF );
		add( arcsFg );
	}
	
	@Override
	protected void layout() {
		arcsBg.size( width, height );
		arcsBg.offset( arcsBg.texture.width / 4 - (width % arcsBg.texture.width) / 2, 0 );
		
		arcsFg.size( width, height );
		arcsFg.offset( arcsFg.texture.width / 4 - (width % arcsFg.texture.width) / 2, 0 );
	}
	
	@Override
	public void update() {
		
		super.update();
		
		float shift = GameLoop.elapsed * SCROLL_SPEED;
		if (reversed) {
			shift = -shift;
		}
		
		arcsBg.offset( 0, shift );
		arcsFg.offset( 0, shift * 2 );
		
		offsB = arcsBg.offsetY();
		offsF = arcsFg.offsetY();
	}
}
