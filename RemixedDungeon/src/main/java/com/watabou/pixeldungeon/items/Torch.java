
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Light;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Torch extends Item {

	public static final String AC_LIGHT	= "Torch_ACLight";

	public static final float TIME_TO_LIGHT = 1;
	
	{
        name = StringsManager.getVar(R.string.Torch_Name);
		image = ItemSpriteSheet.TORCH;
		
		stackable = true;

        info = StringsManager.getVar(R.string.Torch_Info2);

		setDefaultAction(AC_LIGHT);
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_LIGHT );
		return actions;
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		
		if (action.equals(AC_LIGHT)) {
			
			chr.doOperate(TIME_TO_LIGHT );
			
			detach( chr.getBelongings().backpack );
			Buff.affect(chr, Light.class, Light.DURATION );
			
			Emitter emitter = chr.getSprite().centerEmitter();
			emitter.start( FlameParticle.FACTORY, 0.2f, 3 );
			
		} else {
			
			super._execute(chr, action );
			
		}
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int price() {
		return 10 * quantity();
	}
}
