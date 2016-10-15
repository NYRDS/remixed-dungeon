package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.noosa.audio.Music;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.scenes.GameScene;

abstract public class Boss extends Mob {

	private String battleMusic;

	public Boss() {
		RESISTANCES.add( Death.class );
		RESISTANCES.add( ScrollOfPsionicBlast.class );
	}

	@Override
	public boolean canBePet() {
		return false;
	}

	@Override
	public void setState(AiState state) {
		if( state instanceof Hunting ){
			if(battleMusic!=null) {
				Music.INSTANCE.play(battleMusic, true);
			}
		}
		super.setState(state);
	}

	@Override
	public void die(Object cause) {
		GameScene.playLevelMusic();
		super.die(cause);
	}
}
