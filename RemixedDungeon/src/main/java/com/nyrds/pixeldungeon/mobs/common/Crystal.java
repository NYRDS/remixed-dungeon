package com.nyrds.pixeldungeon.mobs.common;

import static com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory.PEDESTAL;

import com.nyrds.pixeldungeon.items.chaos.ChaosCommon;
import com.nyrds.pixeldungeon.items.common.WandOfShadowbolt;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ConfusionGas;
import com.watabou.pixeldungeon.actors.blobs.Darkness;
import com.watabou.pixeldungeon.actors.blobs.Foliage;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.wands.SimpleWand;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;



public class Crystal extends MultiKindMob implements IDepthAdjustable, IZapper{

	static private int ctr = 0;

	{
		movable = false;
		carcassChance = 0;
	}

	public Crystal() {

		adjustStats(Dungeon.depth);
		ensureWand(false);
	}

	static public Crystal makeShadowLordCrystal() {
		Crystal crystal = new Crystal();
		crystal.kind = 2;
		crystal.ensureWand(true);
		return crystal;
	}

	@NotNull
	private Wand ensureWand(boolean regen) {
		Wand wand = getBelongings().getItem(Wand.class);
		if (wand != null && !regen) {
			return wand;
		}

		var item = SimpleWand.createRandomSimpleWand().upgrade(Dungeon.depth/3);
		if(kind == 2 && Random.Float(1) < 0.25f) {
			item = new WandOfShadowbolt().upgrade(Dungeon.depth/2);
		}

		item.collect(this); //no treasury check, we want to keep the wand in the crystal

		return ensureWand(false);
	}

	public void adjustStats(int depth) {
		kind = (ctr++) % 2;

		hp(ht(depth * 4 + 1));

		baseDefenseSkill = depth * 2 + 1;
		baseAttackSkill = 35;
		expForKill = depth + 1;
		maxLvl = depth + 2;
		dr = expForKill /3;

		addImmunity(ScrollOfPsionicBlast.class);
		addImmunity(ToxicGas.class);
		addImmunity(Paralysis.class);
		addImmunity(Stun.class);
		addImmunity(ConfusionGas.class);
	}

	@Override
	public int getKind() {
        return super.getKind();
    }

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(hp() / 2, ht() / 2);
	}

	@Override
	public boolean canAttack(@NotNull Char enemy) {
		return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	public int attackSkill(Char target) {
		if (kind < 2) {
			return 1000;
		}
		return super.attackSkill(target);
	}

	@Override
	public boolean attack(@NotNull Char enemy) {
		zap(enemy);
		return true;
	}

	@Override
	public boolean getCloser(int target) {
		return false;
	}

	@Override
    public boolean getFurther(int target) {
		return false;
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		int pos = getPos();

		Level level = level();

		LevelObject obj = level.getTopLevelObject(pos);

		if (obj != null && obj.getEntityKind().equals(PEDESTAL)) {
			level.remove(obj);

			level.set(pos, Terrain.EMBERS);
			int x, y;
			x = level.cellX(pos);
			y = level.cellY(pos);

			level.clearAreaFrom(Darkness.class, x - 2, y - 2, 5, 5);
			level.fillAreaWith(Foliage.class, x - 2, y - 2, 5, 5, 1);

			GameScene.updateMap();
		}
		super.die(cause);
	}

	@Override
	public boolean canBePet() {
		return false;
	}

	@Override
	protected int zapProc(@NotNull Char enemy, int damage) {
		ensureWand(false).mobWandUse(this, enemy.getPos());
		return 0;
	}

	@Override
	public void onActionTarget(String action, Char actor) {
		if(action.equals(CommonActions.MAC_STEAL)) {
			ChaosCommon.doChaosMark(getPos(), Dungeon.depth * 3);
			die(actor);
		}
	}
}
