
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKindWithId;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import lombok.SneakyThrows;


public class Buff extends Actor implements NamedEntityKind, CharModifier {

    protected final Set<String> EMPTY_STRING_SET = new HashSet<>();
    public Char target = CharsList.DUMMY;

    @Packable(defaultValue = "1")
    protected int level = 1;

    @Packable(defaultValue = "-1")
    protected int source = -1;

    @Override
    public String getEntityKind() {
        return getClass().getSimpleName();
    }

    @Override
    public String name() {
        String id = getEntityKind() + "Buff_Name";
        /*
        if(Util.isDebug()) {
            if(StringsManager.getVar(id).equals(Utils.EMPTY_STRING)) {
                throw new TrackedRuntimeException(Utils.format("missing Buff_Name for %s", getEntityKind()));
            }
        }
         */
        return StringsManager.maybeId(id);
    }

    @Override
    public String desc() {
        String id = getEntityKind() + "Buff_Info";
        if(Util.isDebug()) {
            if(StringsManager.getVar(id).equals(Utils.EMPTY_STRING)) {
                throw new TrackedRuntimeException(Utils.format("missing Buff_Info for %s", getEntityKind()));
            }
        }

        return StringsManager.maybeId(id);
    }

    @Override
    public String textureSmall() {
        return Assets.BUFFS_SMALL;
    }

    @Override
    public String textureLarge() {
        return Assets.BUFFS_LARGE;
    }

    public void attachVisual() {
        target.getSprite().add(charSpriteStatus());
    }

    public boolean attachTo(@NotNull Char target) {

        if (target.immunities().contains(getEntityKind())) {
            return false;
        }

        this.target = target;

        return target.add(this);
    }

    public void detach() {
        deactivateActor();
        target.remove(this);
    }

    @Override
    public boolean act() {
        deactivateActor();
        return true;
    }

    public int icon() {
        return BuffIndicator.NONE;
    }

    @SneakyThrows
    public static <T extends Buff> T affect(@NotNull Char target, Class<T> buffClass) {
        T buff = target.buff(buffClass);
        if (buff == null) {
            buff = buffClass.newInstance();
            buff.attachTo(target);
        } else {
            buff.level++;
        }
        return buff;
    }

    @LuaInterface
    public static Buff permanent(Char target, String buffClass) {
        Buff buff = affect(target, buffClass);
        buff.deactivateActor();
        return buff;
    }

    @LuaInterface
    public static <T extends Buff> T permanent(Char target, Class<T> buffClass) {
        T buff = affect(target, buffClass);
        buff.deactivateActor();
        return buff;
    }

    @LuaInterface
    public static Buff affect(Char target, String buffClass) {
        Buff buff = target.buff(buffClass);
        if (buff == null) {
            buff = BuffFactory.getBuffByName(buffClass);
            buff.attachTo(target);
        } else {
            buff.level++;
        }

        return buff;
    }

    @LuaInterface
    public static Buff affect(Char target, String buffClass, float duration) {
        Buff buff = affect(target,buffClass);

        buff.spend(duration);
        GLog.debug("%s cooldown %3.2f", buff.getEntityKind(), buff.cooldown());
        return buff;
    }

    @LuaInterface
    public static <T extends Buff> T affect(Char target, Class<T> buffClass, float duration) {
        T buff = affect(target, buffClass);
        buff.spend(duration);
        GLog.debug("%s cooldown %3.2f", buff.getEntityKind(), buff.cooldown());
        return buff;
    }

    @LuaInterface
    public static Buff prolong(Char target, String buffClass, float duration) {
        Buff buff = BuffFactory.getBuffByName(buffClass);
        buff.attachTo(target);
        buff.postpone(duration);
        return buff;
    }

    @LuaInterface
    public static <T extends Buff> T prolong(Char target, Class<T> buffClass, float duration) {
        T buff = affect(target, buffClass);
        buff.postpone(duration);
        return buff;
    }

    public static void detach(Buff buff) {
        if (buff != null) {
            buff.detach();
        }
    }

    @LuaInterface
    public static void detach(@NotNull Char target, String cl) {
        detach(target.buff(cl));
    }

    @LuaInterface
    public static void detach(@NotNull Char target, Class<? extends Buff> cl) {
        detach(target.buff(cl));
    }

    @LuaInterface
    public static void detachAllBySource(@NotNull Char target, NamedEntityKindWithId source) {

        if(target==null) { // mods may not honor @NotNull
            return;
        }

        var buffsToRemove = new HashSet<Buff>();
        for(Buff buff:target.buffs()) {
            if(buff.getSourceId() == source.getId()) {
                buffsToRemove.add(buff);
            }
        }

        for(Buff buff:buffsToRemove) {
            buff.detach();
        }
    }

    @LuaInterface
    public NamedEntityKindWithId getSource() {
        NamedEntityKindWithId ret = ItemsList.getById(source);
        if(ret.valid()) {
            return ret;
        }
        ret = CharsList.getById(source);
        if(ret.valid()) {
            return ret;
        }
        return ItemsList.DUMMY;
    }

    public int getSourceId() {
        return source;
    }

    public void level(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public int drBonus() {
        return 0;
    }

    public int stealthBonus() {
        return 0;
    }

    public float speedMultiplier() {
        return 1;
    }

    @Override
    public float hasteLevel() {
        return 0;
    }

    public int defenceProc(Char defender, Char enemy, int damage) {
        return damage;
    }

    @Override
    public int attackProc(Char attacker, Char defender, int damage) {
        return damage;
    }

    @Override
    public int charGotDamage(int damage, NamedEntityKind src, Char target) {
        return damage;
    }

    @Override
    public int regenerationBonus() {
        return 0;
    }

    @Override
    public int manaRegenerationBonus() {
        return 0;
    }

    @Override
    public void charAct() { }

    @Override
    public void spellCasted(Char caster, Spell spell) { }

    @Override
    public int dewBonus() {
        return 0;
    }

    @Override
    public int defenceSkillBonus() {
        return 0;
    }

    @Override
    public int attackSkillBonus() {
        return 0;
    }

    @Override
    public Set<String> resistances() {
        return EMPTY_STRING_SET;
    }

    @Override
    public Set<String> immunities() {
        return EMPTY_STRING_SET;
    }

    public CharSprite.State charSpriteStatus() {
        return CharSprite.State.NONE;
    }

    private void collectOrDropItem(Item item) {
        if (!item.collect(target.getBelongings().backpack)) {
            item.doDrop(target);
        }
    }

    @LuaInterface
    public void setSource(@NotNull NamedEntityKindWithId source) {
        setSource(source.getId());
    }

    private void setSource(int id) {
        source = id;
    }

    protected void applyToCarriedItems(ItemAction action) {

        if(Dungeon.isLoading()) { // already applied
            return;
        }

        int n = 1;

        if (GameLoop.getDifficulty() >= 3) {
            n = 5;
        }

        for (int i = 0; i < n; i++) {
            Item item = target.getBelongings().randomUnequipped();

            if (item == ItemsList.DUMMY || item instanceof Bag || item instanceof Gold) {
                continue;
            }

            Item srcItem = item.detach(target.getBelongings().backpack);

            if (srcItem == null) {
                EventCollector.logException(item.getEntityKind());
                continue;
            }

            item = action.act(srcItem);

            if (item == srcItem) { // item unaffected by buff
                collectOrDropItem(item);
                continue;
            }

            String actionText = null;

            if (item == null || item == ItemsList.DUMMY) {
                actionText = action.actionText(srcItem);
                action.carrierFx();
            } else {
                if (!item.equals(srcItem)) {
                    actionText = action.actionText(srcItem);
                    collectOrDropItem(item);

                    action.carrierFx();
                }
            }

            if (actionText != null || target == Dungeon.hero) {
                GLog.w(actionText);
            }
        }
    }
}
