
package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.quest.DarkGold;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Room.Type;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.BlacksmithSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBlacksmith;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class Blacksmith extends NPC {

	{
		spriteClass = BlacksmithSprite.class;
	}
	
	@Override
    public boolean act() {
		ItemUtils.throwItemAway(getPos());
		return super.act();
	}
	
	@Override
	public boolean interact(final Char hero) {
		
		getSprite().turnTo( getPos(), hero.getPos() );
		
		if (!Quest.given) {

            GameScene.show( new WndQuest( this,
				Quest.alternative ?
                        StringsManager.getVar(R.string.Blacksmith_Blood1) :
                        StringsManager.getVar(R.string.Blacksmith_Gold1))
			{
				
				@Override
				public void onBackPressed() {
					super.onBackPressed();
					
					Quest.given = true;
					Quest.completed = false;
					
					EquipableItem pick = (EquipableItem) ItemFactory.itemByName("RemixedPickaxe");
					if (pick.doPickUp( hero )) {
						GLog.i( Hero.getHeroYouNowHave(), pick.name() );
					} else {
						pick.doDrop(hero);
					}
				}
			} );
			
			Journal.add( Journal.Feature.TROLL.desc() );
			
		} else if (!Quest.completed) {
			EquipableItem pick = hero.getBelongings().getEquipableItemPartialMatch( "Pickaxe" );
			if(!pick.valid()) {
				tell(StringsManager.getVar(R.string.Blacksmith_Txt2));
			}
			if (Quest.alternative) {
				if (!pick.getBoolean( "bloodStained" )) {
                    tell(StringsManager.getVar(R.string.Blacksmith_Txt4));
				} else {
					if (pick.isEquipped( hero )) {
						pick.doUnequip( hero, false );
					}
					pick.detach( hero.getBelongings().backpack );
                    tell(StringsManager.getVar(R.string.Blacksmith_Completed));
					
					Quest.completed = true;
					Quest.reforged = false;
				}
				
			} else {
				DarkGold gold = hero.getBelongings().getItem( DarkGold.class );
				if (gold == null || gold.quantity() < 15) {
                    tell(StringsManager.getVar(R.string.Blacksmith_Txt3));
				} else {
					if (pick.isEquipped( hero )) {
						pick.doUnequip( hero, false );
					}
					pick.detach( hero.getBelongings().backpack );
					gold.detachAll( hero.getBelongings().backpack );
                    tell(StringsManager.getVar(R.string.Blacksmith_Completed));
					
					Quest.completed = true;
					Quest.reforged = false;
				}
			}
		} else if (!Quest.reforged) {
			GameScene.show( new WndBlacksmith( this) );
		} else {
            tell(StringsManager.getVar(R.string.Blacksmith_GetLost));
		}
		return true;
	}
	
	private void tell( String text ) {
		GameScene.show( new WndQuest( this, text ) );
	}
	
	public static String verify( Item item1, Item item2 ) {
		
		if (item1 == item2) {
            return StringsManager.getVar(R.string.Blacksmith_Verify1);
        }
		
		if (!item1.getEntityKind().equals(item2.getEntityKind())) {
            return StringsManager.getVar(R.string.Blacksmith_Verify2);
        }
		
		if (!item1.isIdentified() || !item2.isIdentified()) {
            return StringsManager.getVar(R.string.Blacksmith_Verify3);
        }
		
		if (item1.isCursed() || item2.isCursed()) {
            return StringsManager.getVar(R.string.Blacksmith_Verify4);
        }
		
		if (item1.level() < 0 || item2.level() < 0) {
            return StringsManager.getVar(R.string.Blacksmith_Verify5);
        }
		
		if (!item1.isUpgradable() || !item2.isUpgradable()) {
            return StringsManager.getVar(R.string.Blacksmith_Verify6);
        }
		
		return null;
	}
	
	public static void upgrade( Item item1, Item item2 ) {
		
		Item first, second;
		if (item2.level() > item1.level()) {
			first = item2;
			second = item1;
		} else {
			first = item1;
			second = item2;
		}

		Sample.INSTANCE.play( Assets.SND_EVOKE );
		ScrollOfUpgrade.upgrade( Dungeon.hero );
		ItemUtils.evoke( Dungeon.hero );
		
		if (first.isEquipped( Dungeon.hero )) {
			((EquipableItem)first).doUnequip( Dungeon.hero, true );
		}
		first.upgrade();
        GLog.p(StringsManager.getVar(R.string.Blacksmith_LooksBetter), first.name() );
		Dungeon.hero.spendAndNext( 2f );
		Badges.validateItemLevelAcquired( first );
		
		if (second.isEquipped( Dungeon.hero )) {
			((EquipableItem)second).doUnequip( Dungeon.hero, false );
		}
		second.detach( Dungeon.hero.getBelongings().backpack );
		
		Quest.reforged = true;
		
		Journal.remove( Journal.Feature.TROLL.desc() );
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
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
	
	public static class Quest {
		
		private static boolean spawned;
		
		private static boolean alternative;
		private static boolean given;
		private static boolean completed;
		private static boolean reforged;
		
		public static void reset() {
			spawned		= false;
			given		= false;
			completed	= false;
			reforged	= false;
		}
		
		private static final String NODE	= "blacksmith";
		
		private static final String SPAWNED		= "spawned";
		private static final String ALTERNATIVE	= "alternative";
		private static final String GIVEN		= "given";
		private static final String COMPLETED	= "completed";
		private static final String REFORGED	= "reforged";
		
		public static void storeInBundle( Bundle bundle ) {
			
			Bundle node = new Bundle();
			
			node.put( SPAWNED, spawned );
			
			if (spawned) {
				node.put( ALTERNATIVE, alternative );
				node.put( GIVEN, given );
				node.put( COMPLETED, completed );
				node.put( REFORGED, reforged );
			}
			
			bundle.put( NODE, node );
		}
		
		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );
			
			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				alternative	=  node.getBoolean( ALTERNATIVE );
				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
				reforged = node.getBoolean( REFORGED );
			} else {
				reset();
			}
		}
		
		public static void spawn( Collection<Room> rooms ) {
			if (!spawned && Dungeon.depth > 11 && Random.Int( 15 - Dungeon.depth ) == 0) {
				
				Room blacksmith;
				for (Room r : rooms) {
					if (r.type == Type.STANDARD && r.width() > 4 && r.height() > 4) {
						blacksmith = r;
						blacksmith.type = Type.BLACKSMITH;
						
						spawned = true;
						alternative = Random.Int( 2 ) == 0;
						
						given = false;
						
						break;
					}
				}
			}
		}
	}
}
