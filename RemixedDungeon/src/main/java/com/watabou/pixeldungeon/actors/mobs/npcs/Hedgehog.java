package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.food.Pasty;
import com.watabou.pixeldungeon.levels.RegularLevel;

import org.jetbrains.annotations.NotNull;

public class Hedgehog extends NPC {

	{
		setState(MobAi.getStateByClass(Wandering.class));
	}
	
	@Override
	public float speed() {
		return speed;
	}

	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src ) {
	}
	
	@Override
	public boolean add(Buff buff ) {
        return false;
    }
	
	private static boolean spawned;

	@Packable
	private int    action = 0;
	@Packable
	private float  speed  = 0.5f;

	public static void spawn( RegularLevel level ) {
		if (!spawned && Dungeon.depth == 23) {
			int mobPos = level.randomRespawnCell();

			if(level.cellValid(mobPos)) {
				Hedgehog hedgehog = new Hedgehog();
				hedgehog.setPos(mobPos);
				level.mobs.add(hedgehog);
				Actor.occupyCell(hedgehog);

				spawned = true;
			}
		}
	}

	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		
		switch (action)
		{
			case 0:
                say(StringsManager.getVar(R.string.Hedgehog_Info1));
			break;
		
			case 1:
                say(StringsManager.getVar(R.string.Hedgehog_Info2));
			break;
			
			case 2:
                say(StringsManager.getVar(R.string.Hedgehog_Info3));
			break;
			
			case 3:
                say(StringsManager.getVar(R.string.Hedgehog_Info4));
				new Pasty().doDrop(this);
			break;
			
			default:
                say(StringsManager.getVar(R.string.Hedgehog_ImLate));
				action = 4;
				speed  = 3;
		}
		speed += 0.5f;
		action++;
		
		return true;
	}

}
