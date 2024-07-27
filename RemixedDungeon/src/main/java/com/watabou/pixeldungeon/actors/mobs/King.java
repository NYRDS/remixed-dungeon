
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.necropolis.UndeadMob;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.ArmorKit;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.wands.WandOfDisintegration;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class King extends Boss {
	
	private static final int MAX_ARMY_SIZE	= 5;

	@Packable
	private int lastPedestal;

	@Packable
	private int targetPedestal;

	public King() {
		hp(ht(300));
		expForKill = 40;
		baseDefenseSkill = 25;
		baseAttackSkill  = 32;
		dmgMin = 20;
		dmgMax = 38;
		dr = 14;

		lastPedestal   = -1;
		targetPedestal = -1;

		addResistance( ToxicGas.class );
		addResistance( WandOfDisintegration.class );
		
		addImmunity( Stun.class );
		addImmunity( Paralysis.class );
		
		collect(new SkeletonKey());
		collect(new ArmorKit());
	}

	@Override
	public boolean getCloser(int target, boolean ignorePets) {

 		targetPedestal = level().getNearestTerrain(getPos(),
				(level, cell) -> {
					final LevelObject topLevelObject = level.getTopLevelObject(cell);
					return cell != lastPedestal
						   && topLevelObject !=null
						   && topLevelObject.getEntityKind().equals(LevelObjectsFactory.PEDESTAL);
				});

		if(canTryToSummon()) {
			return super.getCloser( targetPedestal, ignorePets );
		}

		return super.getCloser(target, ignorePets);
	}
	
	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return canTryToSummon() ? 
			getPos() == targetPedestal :
			adjacent(enemy);
	}

	private int countServants() {
		int count = 0;

		for(Mob mob:level().getCopyOfMobsArray()){
			if (mob instanceof Undead) {
				count++;
			}
		}
		return count;
	}

	private boolean canTryToSummon() {
		if (!level().cellValid(targetPedestal)) {
			return false;
		}

		if (countServants() < maxArmySize()) {
			Char ch = Actor.findChar(targetPedestal);
			return ch == this || ch == null;
		} else {
			return false;
		}
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		super.die( cause );
		
		Badges.validateBossSlain(Badges.Badge.BOSS_SLAIN_4);

        yell(Utils.format(R.string.King_Info1, Dungeon.hero.getHeroClass().title()));
	}
	
	private int maxArmySize() {
		return (int) (1 + MAX_ARMY_SIZE * (ht() - hp()) / ht() * GameLoop.getDifficultyFactor());
	}

	@Override
	public boolean zap(@NotNull Char enemy) {
		summon();
		return true;
	}

	private void summon() {
		lastPedestal = targetPedestal;

		getSprite().centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.4f, 2 );		
		Sample.INSTANCE.play( Assets.SND_CHALLENGE );
		
		int undeadsToSummon = maxArmySize() - countServants();

		for (int i=0; i < undeadsToSummon; i++) {
			int pos = level().getEmptyCellNextTo(lastPedestal);

			if (level().cellValid(pos)) {
				Mob servant = new Undead();
				servant.setPos(pos);
				level().spawnMob(servant, 0, lastPedestal);

				WandOfBlink.appear(servant, pos);
				new Flare(3, 32).color(0x000000, false).show(servant.getSprite(), 2f);
			}
		}
        yell(StringsManager.getVar(R.string.King_Info2));
	}
	
	@Override
	public void notice() {
		super.notice();
        yell(StringsManager.getVar(R.string.King_Info3));
	}
	
	public static class Undead extends UndeadMob {

		public Undead() {
			hp(ht(28));
			baseDefenseSkill = 15;
			baseAttackSkill  = 16;

			dmgMin = 12;
			dmgMax = 16;
			dr = 5;
			
			expForKill = 0;
			
			setState(MobAi.getStateByClass(Wandering.class));
		}

		@Override
		public int attackProc(@NotNull Char enemy, int damage ) {
			if (Random.Int( MAX_ARMY_SIZE ) == 0) {
				Buff.prolong( enemy, Stun.class, 1 );
			}
			
			return damage;
		}
		
		@Override
		public void damage(int dmg, @NotNull NamedEntityKind src ) {
			super.damage( dmg, src );
			if (src instanceof ToxicGas) {		
				((ToxicGas)src).clearBlob( getPos() );
			}
		}
		
		@Override
		public void die(@NotNull NamedEntityKind cause) {
			super.die( cause );
			
			if (CharUtils.isVisible(this)) {
				Sample.INSTANCE.play( Assets.SND_BONES );
			}
		}

	}
}
