
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Passive;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.IZapper;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.Ooze;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.wands.WandOfFirebolt;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.LarvaSprite;
import com.watabou.pixeldungeon.sprites.RottingFistSprite;
import com.watabou.pixeldungeon.sprites.YogSprite;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


@Deprecated
public class Yog extends Boss {

	public Yog() {
		spriteClass = YogSprite.class;

		hp(ht(1000));
		movable = false;
		expForKill = 50;

		baseDefenseSkill = 20;
		baseAttackSkill  = 20;

		setState(MobAi.getStateByClass(Passive.class));

		addImmunity(Death.class);
		addImmunity(Terror.class);
		addImmunity(Amok.class);
		addImmunity(Charm.class);
		addImmunity(Sleep.class);
		addImmunity(Burning.class);
		addImmunity(ToxicGas.class);
		addImmunity(ScrollOfPsionicBlast.class);

		collect(new SkeletonKey());
	}

	public void spawnFists() {
        String [] secondaryBossArray = {"RottingFist", "BurningFist", "YogsBrain", "YogsHeart", "YogsTeeth"};
        var names = new ArrayList<String>();

        int organsCount = GameLoop.getDifficulty() > 2 ? 3 : 2;

        do {
			var candidate = Random.oneOf(secondaryBossArray);
			if(!names.contains(candidate)) {
				names.add(candidate);
			}
		} while (names.size() < organsCount);

        for(var candidate:names) {
        	var organ = MobFactory.mobByName(candidate);
        	organ.setPos(level().getNearestTerrain(getPos(),
					(level, cell) -> level.passable[cell] && Actor.findChar(cell) == null)
			);
        	level().spawnMob(organ);
		}
	}

	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src) {

		int damageShift = 0;
		for (Mob mob : Dungeon.level.mobs) {
			if (mob.isBoss() && !(mob instanceof Yog)) {
				mob.beckon(getPos());
				damageShift++;
			}
		}

		dmg >>= damageShift;

		super.damage(dmg, src);
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		CharUtils.spawnOnNextCell(this, "Larva", (int) (10 * GameLoop.getDifficultyFactor()));

		return super.defenseProc(enemy, damage);
	}

	@Override
	public void beckon(int cell) {
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		Mob mob = level().getRandomMob();
		while(mob != null){
			mob.remove();
			mob = level().getRandomMob();
		}

		Badges.validateBossSlain(Badges.Badge.YOG_SLAIN);
		super.die(cause);

        yell(StringsManager.getVar(R.string.Yog_Info1));
	}

	@Override
	public void notice() {
		super.notice();
        yell(StringsManager.getVar(R.string.Yog_Info2));
	}

	public static class RottingFist extends Mob {

		private static final int REGENERATION = 10;

		{
			spriteClass = RottingFistSprite.class;

			hp(ht(500));
			baseDefenseSkill = 25;
			baseAttackSkill  = 36;

			expForKill = 0;

			dmgMin = 34;
			dmgMax = 46;
			dr = 15;

			setState(MobAi.getStateByClass(Wandering.class));

			addResistance(ToxicGas.class);

			addImmunity(Amok.class);
			addImmunity(Sleep.class);
			addImmunity(Terror.class);
			addImmunity(Poison.class);

			addImmunity(Burning.class);
		}

		public RottingFist() {
		}

		@Override
		public int attackProc(@NotNull Char enemy, int damage) {
			if (Random.Int(3) == 0) {
				Buff.affect(enemy, Ooze.class);
				enemy.getSprite().burst(0xFF000000, 5);
			}

			return damage;
		}

		@Override
		public boolean act() {

			if (Dungeon.level.water[getPos()] && hp() < ht()) {
				getSprite().emitter().burst(ShadowParticle.UP, 2);
				heal(REGENERATION, this, true);
			}

			return super.act();
		}

		@Override
		public String getDescription() {
            return StringsManager.getVar(R.string.Yog_Desc);
        }

		@Override
		public boolean canBePet() {
			return false;
		}
	}

	public static class BurningFist extends Mob implements IZapper {

		{

			hp(ht(400));
			baseDefenseSkill = 25;
			baseAttackSkill  = 26;

			expForKill = 0;

			dmgMin = 40;
			dmgMax = 62;
			dr = 15;

			setState(MobAi.getStateByClass(Wandering.class));

			addResistance(ToxicGas.class);

			addImmunity(Amok.class);
			addImmunity(Sleep.class);
			addImmunity(Terror.class);
			addImmunity(Fire.class );
			addImmunity(Burning.class );
			addImmunity(WandOfFirebolt.class );
		}

		public BurningFist() {
		}

		@Override
        public boolean canAttack(@NotNull Char enemy) {
			return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
		}

		@Override
		public boolean attack(@NotNull Char enemy) {
			if(super.attack(enemy)) {
				if (!adjacent(enemy)) {
					enemy.getSprite().flash();
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean act() {

			for (int i = 0; i < Level.NEIGHBOURS9.length; i++) {
				GameScene.add(Blob.seed(getPos() + Level.NEIGHBOURS9[i], 2, Fire.class));
			}

			return super.act();
		}

		@Override
		public String getDescription() {
            return StringsManager.getVar(R.string.Yog_Desc);
        }

		@Override
		public boolean canBePet() {
			return false;
		}
	}

	public static class Larva extends Mob {
		{
			spriteClass = LarvaSprite.class;

			hp(ht(120));
			baseDefenseSkill = 20;
			baseAttackSkill  = 30;

			dmgMin = 25;
			dmgMax = 30;
			dr = 8;
			expForKill = 0;

			setState(MobAi.getStateByClass(Hunting.class));
		}

		@Override
		public String getDescription() {
            return StringsManager.getVar(R.string.Yog_Desc);

        }
	}
}
