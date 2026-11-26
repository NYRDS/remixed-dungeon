
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.items.ItemStatusHandlerKinds;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;

/**
 * Data rings: one java kind-parameterized framework, per-kind data and
 * behavior in scripts/items/<Kind>.lua built with scripts/lib/ring.lua
 * (was 14 per-ring java classes). Kind == the legacy class simple name, so
 * name/info resources and old saves resolve unchanged; the identification
 * handler keeps the legacy "class com.watabou...rings.<Kind>" bundle keys
 * verbatim. The equipped buff is {@link KindRingBuff} - runtime-only, kind
 * string "artifactBuff<Buff>" exactly as the per-class buffs had.
 *
 * Lua hooks (all optional, self = the script table, item = the ring):
 *   desc(self)                 -> { buff, price, upgradable, identified,
 *                                   image, imageFile, info }
 *   random(self, item)         -> truthy = handled (Haggler/Thorns level(+1))
 *   afterEquip(self, item, chr)  -> post-equip effect (Detection: search)
 *   doPickUp(self, item, chr)    -> truthy = fully handled; nil/false = default collect
 *                                   (Haggler/Thorns: identify + badge, then default)
 *   regenerationBonus / stealthBonus / attackSkillBonus / defenceSkillBonus
 *   hasteLevel(self, item, buff, chr) -> buff level bonus queries
 *   resistances(self, item, buff, chr) -> table of resisted buff kind strings
 *   durationFactor(self, item, buff)   -> Elements duration scaler
 *   defenceProc(self, item, buff, defender, enemy, damage) -> damage
 */
public class Ring extends Artifact implements UnknownItem {

    private static final String LEGACY_PACKAGE = "com.watabou.pixeldungeon.items.rings";

    private static final String[] rings = {
            "RingOfMending",
            "RingOfDetection",
            "RingOfShadows",
            "RingOfPower",
            "RingOfHerbalism",
            "RingOfAccuracy",
            "RingOfEvasion",
            "RingOfSatiety",
            "RingOfHaste",
            "RingOfHaggler",
            "RingOfElements",
            "RingOfThorns"
    };

    private static final Integer[] images = {
            ItemSpriteSheet.RING_DIAMOND,
            ItemSpriteSheet.RING_OPAL,
            ItemSpriteSheet.RING_GARNET,
            ItemSpriteSheet.RING_RUBY,
            ItemSpriteSheet.RING_AMETHYST,
            ItemSpriteSheet.RING_TOPAZ,
            ItemSpriteSheet.RING_ONYX,
            ItemSpriteSheet.RING_TOURMALINE,
            ItemSpriteSheet.RING_EMERALD,
            ItemSpriteSheet.RING_SAPPHIRE,
            ItemSpriteSheet.RING_QUARTZ,
            ItemSpriteSheet.RING_AGATE};

    private static final Set<String> handledKinds = new HashSet<>(Arrays.asList(rings));

    private static ItemStatusHandlerKinds handler;

    static {
        initGems();
    }

    public static void initGems() {
        handler = new ItemStatusHandlerKinds(LEGACY_PACKAGE, rings, images);
    }

    public static void save(Bundle bundle) {
        handler.save(bundle);
    }

    public static void restore(Bundle bundle) {
        handler = new ItemStatusHandlerKinds(LEGACY_PACKAGE, rings, images, bundle);
    }

    private String gem;

    private int ticksToKnow = 200;

    @NotNull
    private final String ringKind;

    // per-instance wrapper around the shared per-kind module (see Food)
    private final LuaScript kindScript;

    private String buffName = Utils.EMPTY_STRING;
    private boolean alwaysKnown;
    private boolean upgradable = true;
    private float basePrice = 80;

    // bare legacy tag; plain rings resolve as their canonical kind (see Food)
    public Ring() {
        this(rings[0]);
    }

    public Ring(@NotNull String kind) {
        ringKind = kind;
        kindScript = new LuaScript("scripts/items/" + kind, this);
        applyKind();
        if (alwaysKnown) {
            // StoneWalking/FrostAura-era kinds self-identify without touching
            // the ring handler (they never participated in the unknown flow)
            super.identify();
        }
        syncGem();
    }

    private void applyKind() {
        // Item's name/info initialize before the kind is known - re-resolve
        name = getClassParam("Name", StringsManager.getVar(R.string.Item_Name), false);
        info = getClassParam("Info", StringsManager.getVar(R.string.Item_Info), false);

        if (!kindScript.hasMethod("desc")) {
            return;
        }
        LuaValue desc = kindScript.run("desc").checktable();
        buffName    = desc.rawget("buff").optjstring(Utils.EMPTY_STRING);
        basePrice   = (float) desc.rawget("price").optdouble(80);
        upgradable  = desc.rawget("upgradable").optboolean(true);
        alwaysKnown = desc.rawget("identified").optboolean(false);

        int imageOverride = desc.rawget("image").optint(-1);
        if (imageOverride >= 0) {
            image = imageOverride;
        }
        LuaValue imageFileOverride = desc.rawget("imageFile");
        if (!imageFileOverride.isnil()) {
            imageFile = imageFileOverride.tojstring();
        }
    }

    private boolean inHandler() {
        return !alwaysKnown && handledKinds.contains(ringKind);
    }

    public void syncGem() {
        if (inHandler() && handler != null) {
            image = handler.index(ringKind);
        }
        gem = StringsManager.getVars(R.array.Ring_Gems)[ItemStatusHandler.indexByImage(image, images)];
    }

    @Override
    public String getEntityKind() {
        return ringKind;
    }

    @Override
    public Item upgrade() {
        super.upgrade();

        Buff.detachAllBySource(getOwner(), this);

        Buff buff = buff();
        if (buff != null) {
            buff.setSource(this);
            buff.attachTo(getOwner());
        }

        return this;
    }

    public boolean isKnown() {
        if (alwaysKnown) {
            return true;
        }
        return handler.isKnown(ringKind);
    }

    @LuaInterface
    public static boolean isKindKnown(String kind) {
        return handler.isKnown(kind);
    }

    public void setKnown() {
        if (!isKnown()) {
            handler.know(ringKind);
        }

        Badges.validateAllRingsIdentified();
    }

    @Override
    public String name() {
        if (alwaysKnown) {
            return super.name();
        }
        return isKnown() ? name : Utils.format(R.string.Ring_Name, gem);
    }

    @Override
    public String desc() {
        if (alwaysKnown) {
            return super.desc();
        }
        return isKnown() ? StringsManager.getVar(ringKind + "_Info") : Utils.format(R.string.Ring_Info, gem);
    }

    @Override
    public String info() {
        if (alwaysKnown) {
            return super.info();
        }
        if (isEquipped(Dungeon.hero)) {
            return Utils.format(R.string.Ring_Info3a, desc(), name(), (isCursed() ? StringsManager.getVar(R.string.Ring_Info3b) : "."));
        } else if (isCursed() && isCursedKnown()) {
            return Utils.format(R.string.Ring_Info4, desc(), name());
        } else {
            return desc();
        }
    }

    @Override
    public boolean isIdentified() {
        return super.isIdentified() && isKnown();
    }

    @Override
    public boolean isUpgradable() {
        return upgradable;
    }

    @Override
    public Item identify() {
        setKnown();
        return super.identify();
    }

    @Override
    public Item random() {
        if (kindScript.hasMethod("random") && kindScript.run("random").toboolean()) {
            return this;
        }
        level(Random.Int(1, 3));
        if (Random.Float() < 0.3f) {
            level(-level());
            setCursed(true);
        }
        return this;
    }

    @Override
    public boolean doEquip(@NotNull Char hero) {
        if (super.doEquip(hero)) {
            kindScript.runOptionalNoRet("afterEquip", hero);
            return true;
        }
        return false;
    }

    @Override
    public boolean doPickUp(@NotNull Char hero) {
        if (kindScript.hasMethod("doPickUp") && kindScript.run("doPickUp", hero).toboolean()) {
            return true;
        }
        return super.doPickUp(hero);
    }

    // old RingBuff.act ran the identify countdown against the ITEM-level
    // counter, so it survives buff re-attaches on upgrade
    void tickTowardsIdentify(KindRingBuff buff) {
        if (!isIdentified() && --ticksToKnow <= 0) {
            String gemName = name();
            identify();
            GLog.w(StringsManager.getVar(R.string.Ring_Identify), gemName, trueName());
            Badges.validateItemLevelAcquired(this);
        }
    }

    public static boolean allKnown() {
        return handler.known().size() == rings.length - 2;
    }

    @Override
    public int price() {
        return adjustPrice((int) basePrice);
    }

    @Override
    public KindRingBuff buff() {
        if (buffName.isEmpty()) {
            return null;
        }
        return new KindRingBuff(this, buffName);
    }

    LuaScript kindScript() {
        return kindScript;
    }

    @Override
    public String bag() {
        return "Keyring";
    }
}
