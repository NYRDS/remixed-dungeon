
package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.mobs.Golem;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Monk;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.quest.DwarfToken;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.levels.CityLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ImpSprite;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndImp;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Imp extends NPC {

	public Imp() {
		spriteClass = ImpSprite.class;
	}

	private boolean seenBefore = false;
	
	@Override
    public boolean act() {
		
		if (!Quest.given && CharUtils.isVisible(this)) {
			if (!seenBefore) {
                say( Utils.format(R.string.Imp_Hey, Dungeon.hero.className() ) );
			}
			seenBefore = true;
		} else {
			seenBefore = false;
		}

		ItemUtils.throwItemAway(getPos());

		return super.act();
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}
	
	@Override
	public String defenseVerb() {
        return StringsManager.getVar(R.string.Imp_Defense);
    }
	
	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src ) {
	}
	
	@Override
	public boolean add(Buff buff ) {
        return false;
    }
	
	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	public boolean interact(final Char hero) {
		
		getSprite().turnTo( getPos(), hero.getPos() );
		if (Quest.given) {
			
			DwarfToken tokens = hero.getBelongings().getItem( DwarfToken.class );
			if (tokens != null && (tokens.quantity() >= 8 || (!Quest.alternative && tokens.quantity() >= 6))) {
				GameScene.show( new WndImp( this, tokens ) );
			} else {
                tell( Quest.alternative ? StringsManager.getVar(R.string.Imp_Monks2) : StringsManager.getVar(R.string.Imp_Golems2), hero.className() );
			}
			
		} else {
            tell( Quest.alternative ? StringsManager.getVar(R.string.Imp_Monks1) : StringsManager.getVar(R.string.Imp_Golems1));
			Quest.given = true;
			Quest.completed = false;
			
			Journal.add( Journal.Feature.IMP.desc() );
		}
		
		return true;
	}
	
	private void tell( String format, Object...args ) {
		GameScene.show( 
			new WndQuest( this, Utils.format( format, args ) ) );
	}
	
	public void flee() {

        say( Utils.format(R.string.Imp_Cya, Dungeon.hero.className() ) );
		
		destroy();
		getSprite().die();
	}
		
	public static class Quest {
		
		private static boolean alternative;
		
		private static boolean spawned;
		private static boolean given;
		private static boolean completed;
		
		public static Item reward;
		
		public static void reset() {
			spawned = false;

			reward = null;
		}
		
		private static final String NODE		= "demon";
		
		private static final String ALTERNATIVE	= "alternative";
		private static final String SPAWNED		= "spawned";
		private static final String GIVEN		= "given";
		private static final String COMPLETED	= "completed";
		private static final String REWARD		= "reward";
		
		public static void storeInBundle( Bundle bundle ) {
			
			Bundle node = new Bundle();
			
			node.put( SPAWNED, spawned );
			
			if (spawned) {
				node.put( ALTERNATIVE, alternative );
				
				node.put( GIVEN, given );
				node.put( COMPLETED, completed );
				node.put( REWARD, reward );
			}
			
			bundle.put( NODE, node );
		}
		
		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );
			
			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				alternative	= node.getBoolean( ALTERNATIVE );
				
				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
				reward = (Ring)node.get( REWARD );
			}
		}
		
		public static void spawn( CityLevel level, Room room ) {
			if (!spawned && Dungeon.depth > 16 && Random.Int( 20 - Dungeon.depth ) == 0) {
				
				Imp npc = new Imp();
				do {
					npc.setPos(level.randomRespawnCell());
				} while (npc.getPos() == -1 || level.getHeap( npc.getPos() ) != null);
				level.mobs.add( npc );
				Actor.occupyCell( npc );
				
				spawned = true;	
				alternative = Random.Int( 2 ) == 0;
				
				given = false;
				
				do {
					reward = Treasury.getLevelTreasury().random( Treasury.Category.RING );
				} while (reward.isCursed());
				reward.upgrade( 2 );
				reward.setCursed(true);
			}
		}
		
		public static void process( Mob mob ) {
			if (spawned && given && !completed) {
				if ((alternative && mob instanceof Monk) ||
					(!alternative && mob instanceof Golem)) {

					new DwarfToken().doDrop(mob);
				}
			}
		}
		
		public static void complete() {
			reward = null;
			completed = true;
			
			Journal.remove( Journal.Feature.IMP.desc() );
		}
		
		public static boolean isCompleted() {
			return completed;
		}
	}
}
