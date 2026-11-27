package com.nyrds.pixeldungeon.items;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Lightning;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.ElmoParticle;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.effects.particles.SparkParticle;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.Weapon.Enchantment;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.traps.Electricity;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.mechanics.ShadowCaster;
import com.watabou.pixeldungeon.plants.Seed;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.ArrayList;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;

public class ItemUtils {

    // lua has no instanceof; object scripts filter Presser args with this
    @LuaInterface
    public static boolean isItem(Object obj) {
        return obj instanceof Item;
    }

    @LuaInterface
    public static void throwItemAway(int pos) {
        final Level level = Dungeon.level;

        Heap heap = level.getHeap( pos );
		if(heap!=null) {
			Item item = heap.pickUp();
			int cell = level.getEmptyCellNextTo(pos);
			if (level.cellValid(cell)) {
				level.animatedDrop(item,cell);
			}
		}
	}

	// drop an item by kind with an explicit heap type (e.g. quest items in
	// SKELETON heaps) - the Heap.Type enum is not reachable from the lua sandbox
	@LuaInterface
	public static void dropAt(String itemKind, int pos, String heapType) {
		Dungeon.level.drop(ItemFactory.itemByName(itemKind), pos, Heap.Type.valueOf(heapType));
	}

    @LuaInterface
    public static void evoke(@NotNull Char hero) {
        hero.getSprite().emitter().burst(Speck.factory(Speck.EVOKE), 5);
    }

	// class-family gate for scripts (lua has no instanceof)
	@LuaInterface
	public static boolean isBag(@NotNull Item item) {
		return item instanceof Bag;
	}

	@LuaInterface
	public static boolean isFood(@NotNull Item item) {
		return item instanceof Food;
	}

	// was SpiderCharm's Actor.addDelayed(new Pushing(..), -1): luajava cannot
	// coerce a freshly constructed tweener to the Actor parameter
	@LuaInterface
	public static void pushMob(@NotNull Char mob, int from, int to) {
		Actor.addDelayed(new Pushing(mob, from, to), -1);
	}

	// was DarkGold's new ItemSprite(this).drop(pos): sprite methods are not
	// dispatchable from the sandbox (the headless reflection scan of GL-typed
	// visual classes comes up empty)
	@LuaInterface
	public static void dropItemSprite(@NotNull Item item, int pos) {
		new ItemSprite(item).drop(pos);
	}

	// was Stylus.inscribeArmor: reroll a random glyph that differs from the
	// current one, log and inscribe. False when the item is not armor.
	@LuaInterface
	public static boolean inscribeRandomGlyph(@NotNull Item candidate) {
		if (!(candidate instanceof Armor)) {
			return false;
		}
		Armor armor = (Armor) candidate;

		String oldGlyphKind = armor.glyph != null ? armor.glyph.getEntityKind() : null;
		Armor.Glyph glyph = Armor.Glyph.random();
		while (glyph != null && glyph.getEntityKind().equals(oldGlyphKind)) {
			glyph = Armor.Glyph.random();
		}
		if (glyph == null) {
			// empty random pool (mods only) - keep the java no-glyph semantics
			return false;
		}

		GLog.w(StringsManager.getVar(R.string.Stylus_Inscribed), glyph.name(), armor.name() );

		armor.inscribe( glyph );
		return true;
	}

	public static void equipCursed(@NotNull Char chr) {
		chr.getSprite().emitter().burst( ShadowParticle.CURSE, 6 );
		Sample.INSTANCE.play( Assets.SND_CURSED );
	}

	public static boolean usableAsArmor(@NotNull EquipableItem item) {
    	return item.slot(new Belongings(CharsList.DUMMY)) == Belongings.Slot.ARMOR;
	}

	public static boolean usableAsWeapon(@NotNull EquipableItem item) {
		return item.slot(new Belongings(CharsList.DUMMY)) == Belongings.Slot.WEAPON;
	}

	// statue gear roll gate - the exact java Statue.getItem candidate filter,
	// lua side has no instanceof
	@LuaInterface
	public static boolean statueWeaponCandidate(@NotNull Item candidate) {
		return candidate instanceof EquipableItem
				&& ((EquipableItem) candidate).goodForMelee()
				&& usableAsWeapon((EquipableItem) candidate)
				&& !(candidate instanceof MissileWeapon)
				&& candidate.level() >= 0;
	}

	// only real melee weapons take enchantments
	@LuaInterface
	public static void enchantStatueWeapon(@NotNull EquipableItem weapon) {
		if (weapon instanceof MeleeWeapon) {
			((MeleeWeapon) weapon).enchant(Enchantment.random());
		}
	}

	// statue armor roll gate - the java ArmoredStatue.getItem candidate filter
	@LuaInterface
	public static boolean statueArmorCandidate(@NotNull Item candidate) {
		return candidate instanceof EquipableItem
				&& usableAsArmor((EquipableItem) candidate)
				&& candidate.level() >= 0;
	}

	// only classed armor takes glyphs
	@LuaInterface
	public static void inscribeStatueArmor(@NotNull EquipableItem armor) {
		if (armor instanceof Armor) {
			((Armor) armor).inscribe(Armor.Glyph.random());
		}
	}

    public static void tintBackground(@NotNull Item item, @NotNull ColorBlock bg) {
        if (item.isCursed() && item.isCursedKnown()) {
            bg.ra = +0.2f;
            bg.ga = -0.1f;
        } else if (!item.isIdentified()) {
            bg.ra = 0.1f;
            bg.ba = 0.1f;
        }
    }

    public static Item random(Item item) {
        if (Random.Float() < 0.4) {
            int n = 1;
            if (Random.Int( 3 ) == 0) {
                n++;
                if (Random.Int( 3 ) == 0) {
                    n++;
                }
            }
            if (Random.Int( 2 ) == 0) {
                item.upgrade( n );
            } else {
                item.degrade( n );
                item.setCursed(true);
            }
        }
        return item;
    }


	// ---- zap-effect bridges for the wand data kinds (was WandOf*.java) ----
	// Grid sweeps and GL-typed visuals can't run from the sandbox: java arrays
	// (PathFinder/Ballistica maps), java.util collections, and sprite/actor
	// layer access. Each bridge is an exact port of the old kind's onZap/fx.

	private static void wandSelfKillReport(Wand wand) {
		// the shared self-kill fail report (Avalanche/Firebolt/Lightning/
		// MagicMissile/Icebolt): owner==hero && dead -> Dungeon.fail(WAND)
		if ((wand.getOwner() instanceof Hero) && !wand.getOwner().isAlive()) {
			Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.WAND), wand.name(), Dungeon.depth ) );
			GLog.n(StringsManager.getVar(R.string.WandOfAvalanche_Info1));
		}
	}

	/**
	 * WandOfAvalanche.onZap: rockfall around the impact.
	 */
	@LuaInterface
	public static void avalancheZap(Wand wand, int cell) {
		Sample.INSTANCE.play( Assets.SND_ROCKS );

		int level = wand.effectiveLevel();

		Ballistica.distance = Math.min( Ballistica.distance, 8 + level );

		int size = 1 + level / 3;
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), size );

		for (int i=0; i < Dungeon.level.getLength(); i++) {
			int d = PathFinder.distance[i];
			if (d < Integer.MAX_VALUE) {
				Char ch = Actor.findChar( i );
				if (ch != null) {
					ch.getSprite().flash();
					ch.damage( Random.Int( 2, 6 + (size - d) * 2 ), wand );
					if (ch.isAlive() && Random.Int( 2 + d ) == 0) {
						Buff.prolong( ch, BuffFactory.STUN, Random.IntRange( 2, 6 ) );
					}
				}
				CellEmitter.get( i ).start( Speck.factory( Speck.ROCK ), 0.07f, 3 + (size - d) );
				Camera.main.shake( 3, 0.07f * (3 + (size - d)) );
			}
		}

		wandSelfKillReport(wand);
	}

	/**
	 * WandOfRegrowth.onZap: grass along the beam, Regrowth blob at the target.
	 */
	@LuaInterface
	public static void regrowthZap(Wand wand, int cell) {
		Level level = Dungeon.level;
		for (int i = 1; i < Ballistica.distance-1; i++) {
			int p = Ballistica.trace[i];
			int c = level.map[p];
			if (c == Terrain.EMPTY ||
				c == Terrain.EMBERS ||
				c == Terrain.EMPTY_DECO) {
				level.set( p, Terrain.GRASS );
			}
		}

		int c = level.map[cell];
		if (c == Terrain.EMPTY ||
			c == Terrain.EMBERS ||
			c == Terrain.EMPTY_DECO ||
			c == Terrain.GRASS ||
			c == Terrain.HIGH_GRASS) {
			GameScene.add( Blob.seed( cell, (wand.effectiveLevel() + 2) * 20, "Regrowth" ) );
		} else {
			GLog.i(StringsManager.getVar(R.string.WandOfRegrowth_Info1));
		}
	}

	/**
	 * WandOfDisintegration.onZap: the beam damages chars and burns through
	 * doors/barricades/high grass.
	 */
	@LuaInterface
	public static void disintegrationZap(Wand wand, int cell) {
		boolean terrainAffected = false;

		int level = wand.effectiveLevel();

		int maxDistance = level + 4;
		Ballistica.distance = Math.min( Ballistica.distance, maxDistance );

		ArrayList<Char> chars = new ArrayList<>();

		for (int i=1; i < Ballistica.distance; i++) {
			int c = Ballistica.trace[i];

			Char ch;
			if ((ch = Actor.findChar( c )) != null) {
				chars.add( ch );
			}

			Level level1 = Dungeon.level;
			int terr = level1.map[c];
			if (terr == Terrain.DOOR || terr == Terrain.BARRICADE) {
				level1.set( c, Terrain.EMBERS );
				GameScene.updateMapPair( c );
				terrainAffected = true;
			} else if (terr == Terrain.HIGH_GRASS) {
				level1.set( c, Terrain.GRASS );
				GameScene.updateMap( c );
				terrainAffected = true;
			}

			CellEmitter.center( c ).burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
		}

		if (terrainAffected) {
			Dungeon.observe();
		}

		int lvl = level + chars.size();
		int dmgMin = lvl;
		int dmgMax = 8 + lvl * lvl / 3;
		for (Char ch : chars) {
			ch.damage( Random.NormalIntRange( dmgMin, dmgMax ), wand );
			ch.getSprite().centerEmitter().burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
			ch.getSprite().flash();
		}
	}

	/**
	 * WandOfFirebolt.onZap: fire seeds along the beam, ignite + burn the victim.
	 */
	@LuaInterface
	public static void fireboltZap(Wand wand, int cell) {
		int wandLevel = wand.effectiveLevel();
		Level level = wand.getOwner().level();
		for (int i=1; i < Ballistica.distance - 1; i++) {
			int c = Ballistica.trace[i];
			if (level.cellValid(c)) {
				if (level.flammable[c]) {
					GameScene.add(Blob.seed(c, 1, "Fire"));
				}
				Heap heap = level.getHeap(c);
				if(heap!=null) {
					heap.burn();
				}
			}
		}

		GameScene.add( Blob.seed( cell, 1, "Fire" ) );

		Char ch = Actor.findChar(cell);
		if (ch != null) {
			ch.damage( Random.Int( 1, 8 + wandLevel * wandLevel ), wand );
			CharUtils.ignite( ch );
			ch.getSprite().emitter().burst( FlameParticle.FACTORY, 5 );
			wandSelfKillReport(wand);
		}
	}

	/**
	 * WandOfFlock.onZap: a ring of sheep around the impact.
	 */
	@LuaInterface
	public static void flockZap(Wand wand, int cell) {
		int spellLevel = wand.effectiveLevel();

		int n = spellLevel + 2;

		if (Actor.findChar( cell ) != null && Ballistica.distance > 2) {
			cell = Ballistica.trace[Ballistica.distance - 2];
		}
		Level level = Dungeon.level;
		boolean[] passable = BArray.or( level.passable, level.avoid, null );
		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				passable[((Char)actor).getPos()] = false;
			}
		}

		PathFinder.buildDistanceMap( cell, passable, n );
		int dist = 0;

		if (Actor.findChar( cell ) != null) {
			PathFinder.distance[cell] = Integer.MAX_VALUE;
			dist = 1;
		}

		float lifespan = spellLevel + 3;
		sheepLabel:
		for (int i=0; i < n; i++) {
			do {
				for (int j=0; j < level.getLength(); j++) {
					if (PathFinder.distance[j] == dist) {
						// Sheep kind is lua data now (mobsDesc/Sheep.json, batch 17d-5);
						// the lifetime rides the script via setLifespan
						Mob sheep = MobFactory.mobByName(MobFactory.SHEEP);
						sheep.runInScript("setLifespan", lifespan);
						sheep.setPos(j);
						level.spawnMob(sheep);
						level.press(sheep.getPos(), sheep );
						CellEmitter.get( j ).burst( Speck.factory( Speck.WOOL ), 4 );
						PathFinder.distance[j] = Integer.MAX_VALUE;
						continue sheepLabel;
					}
				}
				dist++;
			} while (dist < n);
		}
	}

	/**
	 * WandOfLightning.fx: arc damage happens in the fx pass, then the
	 * Lightning visual completes the callback (was WandOfLightning.hit/fx).
	 */
	@LuaInterface
	public static void lightningArc(Wand wand, int cell, Callback callback) {
		int[] points = new int[20];
		int nPoints = 0;
		ArrayList<Char> affected = new ArrayList<>();

		points[nPoints++] = wand.getOwner().getPos();

		Char ch = Actor.findChar( cell );
		if (ch != null) {
			int lvl = wand.effectiveLevel();
			nPoints = lightningHit(wand, ch, Random.Int( 5 + lvl / 2, 10 + lvl ), affected, points, nPoints);
		} else {
			points[nPoints++] = cell;
			CellEmitter.center( cell ).burst( SparkParticle.FACTORY, 3 );
		}
		GameScene.addToMobLayer(new Lightning(java.util.Arrays.copyOfRange(points, 0, nPoints), callback));

		wandSelfKillReport(wand);
	}

	private static int lightningHit(Wand wand, Char ch, int damage, ArrayList<Char> affected, int[] points, int nPoints) {
		if (damage < 1) {
			return nPoints;
		}

		if (ch == Dungeon.hero) {
			Camera.main.shake( 2, 0.3f );
		}

		affected.add( ch );
		ch.damage( Dungeon.level.water[ch.getPos()] && !ch.isFlying() ? damage * 2 : damage, Electricity.INSTANCE  );

		ch.getSprite().centerEmitter().burst( SparkParticle.FACTORY, 3 );
		ch.getSprite().flash();

		points[nPoints++] = ch.getPos();

		HashSet<Char> ns = new HashSet<>();
		for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
			Char n = Actor.findChar( ch.getPos() + Level.NEIGHBOURS8[i] );
			if (n != null && !affected.contains( n )) {
				ns.add( n );
			}
		}

		if (!ns.isEmpty()) {
			return lightningHit( wand, Random.element( ns ), Random.Int( damage / 2, damage ), affected, points, nPoints );
		}
		return nPoints;
	}

	/**
	 * WandOfTelekinesis.onZap: the force beam pushes chars, grabs heaps,
	 * opens chests and discharges traps along its path.
	 */
	@LuaInterface
	public static void telekinesisZap(Wand wand, int cell, Char victim) {
		boolean mapUpdated = false;
		int maxDistance = wand.effectiveLevel() + 4;
		// caveman: the beam trace was computed at aim time, but onZap runs when the
		// missile lands - a distant victim can step off the trace meanwhile and the
		// zap whiffs entirely. Re-acquire the aimed victim so it gets hit like with
		// every other wand (those hold the victim reference instead of the trace).
		if (victim != null && victim.valid() && victim.getPos() != cell) {
			Ballistica.cast(wand.getOwner().getPos(), victim.getPos(), true, false, true);
		}
		Ballistica.distance = Math.min(Ballistica.distance, maxDistance);
		Char ch;
		Heap heap = null;
		Level level = Dungeon.level;
		for (int i = 1; i < Ballistica.distance; i++) {
			int c = Ballistica.trace[i];
			int before = level.map[c];
			if ((ch = Actor.findChar(c)) != null) {
				if (i == Ballistica.distance - 1) {
					ch.damage(maxDistance - 1 - i, wand);
				} else {
					int next = Ballistica.trace[i + 1];
					if (ch.isMovable() && (level.passable[next] || level.avoid[next]) && Actor.findChar(next) == null) {
						ch.placeTo(next);
						ch.getSprite().move(ch.getPos(), next);
						ch.observe();
					} else {
						ch.damage(maxDistance - 1 - i, wand);
					}
				}
			}
			if (heap == null && (heap = level.getHeap(c)) != null) {
				switch (heap.type) {
					case HEAP:
						if (wand.getOwner() instanceof Hero) {
							telekinesisTransport(wand, heap);
						}
						break;
					case CHEST:
					case MIMIC:
						heap.open(wand.getOwner());
						break;
					default:
				}
			}
			level.press(c, new TelekinesisEffect());
			if (before == Terrain.OPEN_DOOR && Actor.findChar(c) == null) {
				level.set(c, Terrain.DOOR);
				GameScene.updateMap(c);
				GameScene.updateMap(c - level.getWidth());
			} else if (level.water[c]) {
				GameScene.ripple(c);
			}
			if (!mapUpdated && level.map[c] != before) {
				mapUpdated = true;
			}
			LevelObject levelObject;
			if((levelObject = level.getTopLevelObject(c) )!=null) {
				levelObject.push(wand.getOwner());
			}
		}
		if (mapUpdated) {
			Dungeon.observe();
		}
	}

	private static void telekinesisTransport(Wand wand, Heap heap) {
		Item item = heap.pickUp();
		Char owner = wand.getOwner();
		item = item.pick(owner, heap.pos);
		if (item != null) {
			owner.collectAnimated(item);
		}
	}

	// was WandOfTelekinesis.Effect
	public static class TelekinesisEffect implements Presser {
		@Override
		public boolean affectLevelObjects() {
			return true;
		}
		@Override
		public boolean dischargesTraps() {
			return true; // caveman: description promises "discharges traps"
		}
	}
	// ---- class-armor special bridges (was the per-class armor classes) ----
	// FoV mob iteration is java-side (java.util collections + sprite effects).

	/**
	 * Mage/BattleMage/Warlock: ignite and root every visible enemy (1 turn).
	 */
	@LuaInterface
	public static void mageSpecial(@NotNull Char user) {
		Level level = user.level();

		for (Mob mob : level.getCopyOfMobsArray()) {
			if (level.fieldOfView[mob.getPos()]) {
				CharUtils.ignite( mob );
				Buff.prolong( mob, BuffFactory.ROOTS, 3 );
			}
		}

		user.doOperate(Actor.TICK);

		user.getSprite().centerEmitter().start( ElmoParticle.FACTORY, 0.15f, 4 );
		Sample.INSTANCE.play( Assets.SND_READ );
	}

	/**
	 * Huntress/Sniper/Warden: shuriken volley at every visible enemy in
	 * casting range; each hit lands as a regular attack (1 delay total).
	 */
	@LuaInterface
	public static void huntressVolley(@NotNull Char user) {
		Level level = user.level();

		Item proto = ItemFactory.itemByName("Shuriken");
		java.util.Map<Callback, Char> targets = new java.util.HashMap<>();

		for (Mob mob : level.getCopyOfMobsArray()) {
			if (level.fieldOfView[mob.getPos()] && level.distanceL2(user, mob) <= ShadowCaster.MAX_DISTANCE) {
				Callback callback = new Callback() {
					@Override
					public void call() {
						user.attack( targets.get( this ) );
						targets.remove( this );
						if (targets.isEmpty()) {
							user.spend( user.attackDelay() );
						}
					}
				};

				((MissileSprite) user.getSprite().getParent().recycle( MissileSprite.class )).
					reset( user.getPos(), mob.getPos(), proto, callback );

				targets.put( callback, mob );
			}
		}

		if (targets.isEmpty()) {
			GLog.w(StringsManager.getVar(R.string.HuntressArmor_NoEnemies));
			return;
		}

		user.getSprite().zap( user.getPos() );
	}

	/**
	 * Elf/Scout/Shaman: regrowth seeds on every visible cell occupied by a
	 * mob + a big seed at the caster's feet (1 turn).
	 */
	@LuaInterface
	public static void elfRegrowth(@NotNull Char user) {
		Level level = user.level();

		for (Mob mob : level.getCopyOfMobsArray()) {
			if (level.fieldOfView[mob.getPos()]) {
				GameScene.add( Blob.seed( mob.getPos(), 100, "Regrowth" ) );
			}
		}

		user.doOperate(Actor.TICK);
		Sample.INSTANCE.play( Assets.SND_READ );

		GameScene.add( Blob.seed( user.getPos(), 100, "Regrowth" ) );
	}

	/**
	 * Gnoll/Guardian/Witchdoctor: dominate up to lvl/6 visible petable mobs.
	 */
	@LuaInterface
	public static void gnollDomination(@NotNull Char user) {
		Level level = user.level();

		int mobsDominated = 0;

		for (Mob mob : level.getCopyOfMobsArray()) {
			if (level.fieldOfView[mob.getPos()] && level.distanceL2(user, mob) <= ShadowCaster.MAX_DISTANCE) {
				if(mobsDominated > user.lvl() / 6) {
					break;
				}
				if(mob.canBePet()) {
					Mob.makePet(mob, user.getId());
					new Flare(3, 32).show(mob.getSprite(), 2f);
					mobsDominated++;
				}
			}
		}

		user.spend( Actor.TICK );
	}

	/**
	 * Priest: sanctifying blast - full damage to undead, half to demons
	 * within radius 2 - and a leveled Blessing.
	 */
	@LuaInterface
	public static void priestBlast(@NotNull Char user) {
		Level level = user.level();

		int damage = 5 + user.lvl();

		for (Mob mob : level.getCopyOfMobsArray()) {
			if (!user.friendly(mob) && level.distance(mob.getPos(), user.getPos()) <= 2) {
				int dmg = mob.undead ? damage : (mob.demon ? Math.max(1, damage / 2) : 0);
				if (dmg > 0) {
					mob.getSprite().emitter().start(ShaftParticle.GOLD_FACTORY, 0.1f, 5);
					mob.damage(dmg, user);
				}
			}
		}

		Buff.affect(user, BuffFactory.BLESSED, 20).level(1 + user.lvl() / 5);
		user.getSprite().emitter().start(ShaftParticle.GOLD_FACTORY, 0.1f, 8);

		user.doOperate(Actor.TICK);
		Sample.INSTANCE.play(Assets.SND_LEVELUP);
	}
	// ---- artifact-buff attach bridges (was Artifact.buff() setSource+attach) ----

	/**
	 * Attach the named buff to target with the item as its source (equipped
	 * artifact flow: the buff packs/attaches by source and detaches on
	 * unequip).
	 */
	@LuaInterface
	public static void attachArtifactBuff(@NotNull Char target, @NotNull Item source, @NotNull String buffKind) {
		Buff buff = BuffFactory.getBuffByName(buffKind);
		if (buff == null) {
			EventCollector.logException("attachArtifactBuff: no buff " + buffKind);
			return;
		}
		buff.setSource(source);
		buff.attachTo(target);
	}

	/** Detach every buff sourced by the given item (unequip flow). */
	@LuaInterface
	public static void detachArtifactBuff(@NotNull Char target, @NotNull Item source) {
		Buff.detachAllBySource(target, source);
	}

	// ---- SpellBook bridges (was artifacts/SpellBook.java, a CustomItem now) ----

	/**
	 * Raw spell kind stored in a SpellBook; "" = never rolled. No resolve, no
	 * persistence - for kind-comparison sites (Spell casting reveal, icon).
	 */
	@LuaInterface
	public static String spellBookSpell(@NotNull Item item) {
		if (!item.getEntityKind().equals(ItemFactory.SPELL_BOOK)) {
			return Utils.EMPTY_STRING;
		}
		return ((CustomItem) item).itemStringHook("getSpellName", Utils.EMPTY_STRING);
	}

	/**
	 * SpellBook.spell() equivalent: resolve the stored kind ("" rolls a random
	 * Common spell) and persist the resolved kind back into the book.
	 */
	@LuaInterface
	public static Spell spellBookResolveSpell(@NotNull Item item) {
		Spell spell = SpellFactory.getSpellByName(spellBookSpell(item));
		((CustomItem) item).itemHook("setSpellName", spell.getEntityKind());
		return spell;
	}

	// ---- ChaosCrystal fuse routing (was the itemSelector lambda) ----

	/**
	 * The fused counterpart of item, or null when nothing matches (java
	 * parity: both items are consumed silently). Consumes nothing itself -
	 * the caller detaches beforehand.
	 */
	@LuaInterface
	public static Item chaosFuseResult(@NotNull Item item) {
		if (item instanceof Scroll) {
			Item newItem = new ScrollOfWeaponUpgrade();
			GLog.p(StringsManager.getVar(R.string.ChaosCrystal_ScrollFused), newItem.name());
			return newItem;
		}

		if (item instanceof KindOfBow) {
			Item newItem = ItemFactory.itemByName("ChaosBow");
			GLog.p(StringsManager.getVar(R.string.ChaosCrystal_BowFused));
			return newItem;
		}

		if (item instanceof MeleeWeapon) {
			Item newItem;
			if (((MeleeWeapon) item).blockSlot() == Belongings.Slot.LEFT_HAND) {
				newItem = ItemFactory.itemByName("ChaosSword");
				GLog.p(StringsManager.getVar(R.string.ChaosCrystal_SwordFused));
			} else {
				newItem = ItemFactory.itemByName("ChaosBlade");
				GLog.p(StringsManager.getVar(R.string.ChaosCrystal_BladeFused));
			}
			return newItem;
		}

		if (item instanceof Armor) {
			Item newItem = ItemFactory.itemByName("ChaosArmor");
			GLog.p(StringsManager.getVar(R.string.ChaosCrystal_ArmorFused));
			return newItem;
		}

		if (item instanceof Wand) {
			Item newItem = ItemFactory.itemByName("ChaosStaff");
			GLog.p(StringsManager.getVar(R.string.ChaosCrystal_StaffFused));
			return newItem;
		}

		if (item.getEntityKind().contains("Shield")) {
			Item newItem = ItemFactory.itemByName(ItemFactory.CHAOS_SHIELD);
			GLog.p(StringsManager.getVar(R.string.ChaosCrystal_ShieldFused));
			return newItem;
		}

		return null;
	}

	// ---- the alchemy pot + the well of transmutation FFI ----

	/**
	 * The per-item brew weights of the heap (each seed weighs its quantity),
	 * or null when the heap holds anything but seeds. Structural: the heap
	 * scan needs instanceof + java.util iteration, both sandbox-blocked -
	 * the brew decision stays in the Alchemy kind script.
	 */
	@LuaInterface
	public static float[] heapSeedChances(@NotNull Heap heap) {
		if (heap.items.isEmpty()) {
			return null;
		}
		float[] chances = new float[heap.items.size()];
		int index = 0;
		for (Item item : heap.items) {
			if (!(item instanceof Seed)) {
				return null;
			}
			chances[index++] = item.quantity();
		}
		return chances;
	}

	/**
	 * The WaterOfTransmutation instanceof ladder as a string (null = not
	 * transmutable). Structural like heapSeedChances - the reroll mechanics
	 * live in the kind script.
	 */
	@LuaInterface
	public static String transmutationClass(@NotNull Item item) {
		if (item instanceof MeleeWeapon) {
			return "MeleeWeapon";
		}
		if (item instanceof Scroll) {
			return "Scroll";
		}
		if (item instanceof Potion) {
			return "Potion";
		}
		if (item instanceof Ring) {
			return "Ring";
		}
		if (item instanceof Wand) {
			return "Wand";
		}
		if (item instanceof Seed) {
			return "Seed";
		}
		return null;
	}
}
