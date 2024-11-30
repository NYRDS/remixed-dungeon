
package com.watabou.pixeldungeon.items.quest;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.mobs.Bat;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.levels.CavesBossLevel;
import com.watabou.pixeldungeon.levels.CavesLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Pickaxe extends Weapon {

	public static final String AC_MINE	= "Pickaxe_ACMine";

	public static final float TIME_TO_MINE = 2;
	
	private static final Glowing BLOODY = new Glowing( 0x550000 );
	
	{
		image = ItemSpriteSheet.PICKAXE;

		setDefaultAction(AC_MINE);
		
		setSTR(14);
		MIN = 3;
		MAX = 12;
		animation_class = SWORD_ATTACK;
	}

	@Packable
	public boolean bloodStained = false;

	@Override
	public boolean getBoolean(String key) {
		if (key.equals("bloodStained"))
			return bloodStained;

		return super.getBoolean(key);
	}

	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_MINE );
		return actions;
	}
	
	@Override
	public void _execute(@NotNull final Char chr, @NotNull String action ) {
		
		if (action.equals(AC_MINE)) {

			Level level = chr.level();

			if (!(level instanceof CavesLevel || level instanceof CavesBossLevel) ) {
				GLog.w( StringsManager.getVar(R.string.Pickaxe_NoVein) );
				return;
			}

			for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
				
				final int pos = chr.getPos() + Level.NEIGHBOURS8[i];
				if (level.map[pos] == Terrain.WALL_DECO) {
				
					chr.spend( TIME_TO_MINE );
					chr.playAttack(pos);

					CellEmitter.center( pos ).burst( Speck.factory( Speck.STAR ), 7 );
					Sample.INSTANCE.play( Assets.SND_EVOKE );

					level.set( pos, Terrain.WALL );
					GameScene.updateMap( pos );

					DarkGold gold = new DarkGold();
					chr.collectAnimated(gold);

					chr.hunger().satisfy( -Hunger.STARVING / 10 );
					return;
				}
			}
            GLog.w(StringsManager.getVar(R.string.Pickaxe_NoVein));
			
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
	public void attackProc(Char attacker, Char defender, int damage ) {
		if (!bloodStained && defender instanceof Bat && (defender.hp() <= damage)) {
			bloodStained = true;
            QuickSlot.refresh(attacker);
        }
	}

	@Override
	public Glowing glowing() {
		return bloodStained ? BLOODY : null;
	}
	
	@Override
	public String info() {
        return StringsManager.getVar(R.string.Pickaxe_Info);
    }
}
