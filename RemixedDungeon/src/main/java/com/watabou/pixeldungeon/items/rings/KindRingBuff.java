
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.EntityIdSource;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * The one buff for every data-kind ring (was one inner class per RingOf*).
 * Runtime-only like its per-class predecessors: packed only without a source,
 * and the per-kind behavior routes through the owning ring's kind script
 * (scripts/items/RingOf<Kind>.lua). Kind string stays
 * "artifactBuff<Buff>" so existing kind-keyed queries are unaffected.
 *
 * Lua hooks (self = the ring's script table, item = the ring, buff = this):
 *   regenerationBonus / stealthBonus / attackSkillBonus / defenceSkillBonus
 *   hasteLevel(self, item, buff, chr)
 *   resistances(self, item, buff, chr) -> table of buff kind strings
 *   durationFactor(self, item, buff)
 *   defenceProc(self, item, buff, defender, enemy, damage) -> damage
 *   icon(self, item, buff) / name / info / onHeroDeath
 */
public class KindRingBuff extends ArtifactBuff implements Doom {

    private final String buffKind;

    private static final Set<String> EMPTY_STRING_SET = new HashSet<>();

    public KindRingBuff(@NotNull Ring source, @NotNull String buffKind) {
        this.buffKind = buffKind;
        level(source.level());
        setSource(source);
    }

    @Override
    public String getEntityKind() {
        return "artifactBuff" + buffKind;
    }

    private Ring ring() {
        if (getSourceId() == EntityIdSource.INVALID_ID) {
            return null;
        }
        Item item = ItemsList.getById(getSourceId());
        if (item instanceof Ring) {
            return (Ring) item;
        }
        return null;
    }

    @Override
    public boolean attachTo(@NotNull Char target) {

        Ring ring = ring();
        if (target.getHeroClass() == HeroClass.ROGUE && ring != null && !ring.isKnown()) {
            ring.setKnown();
            GLog.i(StringsManager.getVar(R.string.Ring_BuffKnown), ring.trueName());
            Badges.validateItemLevelAcquired(ring);
        }

        return super.attachTo(target);
    }

    @Override
    public void act() {

        Ring ring = ring();
        if (ring != null) {
            ring.tickTowardsIdentify(this);
        }

        spend(TICK);
    }

    @Override
    public int regenerationBonus(Char chr) {
        Ring ring = ring();
        if (ring == null) {
            return 0;
        }
        return ring.kindScript().runOptional("regenerationBonus", 0, this, chr);
    }

    @Override
    public int stealthBonus(Char chr) {
        Ring ring = ring();
        if (ring == null) {
            return 0;
        }
        return ring.kindScript().runOptional("stealthBonus", 0, this, chr);
    }

    @Override
    public int attackSkillBonus(Char chr) {
        Ring ring = ring();
        if (ring == null) {
            return 0;
        }
        return ring.kindScript().runOptional("attackSkillBonus", 0, this, chr);
    }

    @Override
    public int defenceSkillBonus(Char chr) {
        Ring ring = ring();
        if (ring == null) {
            return 0;
        }
        return ring.kindScript().runOptional("defenceSkillBonus", 0, this, chr);
    }

    @Override
    public float hasteLevel(Char chr) {
        Ring ring = ring();
        if (ring == null) {
            return 0;
        }
        return ring.kindScript().runOptional("hasteLevel", 0f, this, chr);
    }

    @Override
    public Set<String> resistances(Char chr) {
        Ring ring = ring();
        if (ring == null) {
            return EMPTY_STRING_SET;
        }
        Set<String> ret = new HashSet<>();
        LuaEngine.forEach(
                ring.kindScript().runOptional("resistances", LuaEngine.emptyTable, this, chr),
                (key, val) -> ret.add(val.tojstring()));
        return ret;
    }

    public float durationFactor() {
        Ring ring = ring();
        if (ring == null) {
            return 1f;
        }
        return ring.kindScript().runOptional("durationFactor", 1f, this);
    }

    @Override
    public int defenceProc(Char defender, Char enemy, int damage) {
        Ring ring = ring();
        if (ring == null) {
            return damage;
        }
        return ring.kindScript().runOptional("defenceProc", damage, this, defender, enemy, damage);
    }

    @Override
    public int icon() {
        Ring ring = ring();
        if (ring == null) {
            return BuffIndicator.NONE;
        }
        return ring.kindScript().runOptional("icon", BuffIndicator.NONE, this);
    }

    // StoneWalking-era display strings live in the kind script; kinds without
    // the hooks keep the Buff defaults (maybeId of "<kind>Buff_Name/Info")
    @Override
    public String name() {
        Ring ring = ring();
        if (ring == null || !ring.kindScript().hasMethod("name")) {
            return super.name();
        }
        return StringsManager.maybeId(ring.kindScript().runOptional("name", super.name()));
    }

    @Override
    public String desc() {
        Ring ring = ring();
        if (ring == null || !ring.kindScript().hasMethod("info")) {
            return super.desc();
        }
        return StringsManager.maybeId(ring.kindScript().runOptional("info", super.desc()));
    }

    @Override
    public void onHeroDeath() {
        Ring ring = ring();
        if (ring != null) {
            ring.kindScript().runOptionalNoRet("onHeroDeath", this);
        }
    }
}
