package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.ml.actions.UseSpell;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModError;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import lombok.val;


public class Spell implements NamedEntityKind {

    protected      int level             = 1;
    protected      int spellCost         = 5;

    protected float castTime = 1f;
    protected float cooldown = 1f;

    protected String targetingType;
    protected String magicAffinity;

    protected String imageFile = "spellsIcons/common.png";

    protected String name = getClassParam("Name", StringsManager.getVar(R.string.Item_Name));
    protected String info = getClassParam("Info", StringsManager.getVar(R.string.Item_Info));

    protected int image = 0;

    private SpellItem spellItem;

    protected boolean cast(@NotNull Char chr, int cell) {
        return true;
    }

    protected boolean cast(@NotNull Char chr, @NotNull Char target) {
        return true;
    }

    @LuaInterface
    public boolean canCast(@NotNull final Char chr, boolean reallyCast) {

        float timeToCast = chr.spellCooldown(getEntityKind())-cooldown;
        if(timeToCast < 0) {

            if(reallyCast) {
                GLog.w(Utils.format(R.string.Spells_NotTooFast, name));
            }

            return false;
        }

        if (chr == Dungeon.hero) {
            final Hero hero = (Hero) chr;

            if(hero.getControlTarget().getId()!=hero.getId()) {
                if(reallyCast) {
                    GLog.w(Utils.format(R.string.Spells_NotInOwnBody, name));
                }
                return false;
            }

            if (!hero.enoughSP(spellCost())) {
                if(reallyCast) {
                    GLog.w(Utils.format(R.string.Spells_NotEnoughSP, name));
                }
                return false;
            }

            return true;
        }
        return true;
    }

    public boolean cast(@NotNull final Char chr) {

        if (!chr.isAlive()) {
            return false;
        }

        if(!canCast(chr, true)) {
            return false;
        }

        EventCollector.setSessionData("spell", getEntityKind());

        if (targetingType.equals(SpellHelper.TARGET_CELL)) {
            chr.selectCell(new SpellCellSelector(this, chr));
            return false;
        }

        if (targetingType.equals(SpellHelper.TARGET_CHAR)
                || targetingType.equals(SpellHelper.TARGET_CHAR_NOT_SELF)
            ) {
            chr.selectCell(new SpellCharSelector(this, chr, targetingType));
            return false;
        }

        chr.spend(castTime);
        chr.busy();
        chr.getSprite().zap(chr.getPos());
        return true;
    }

    @LuaInterface
    @TestOnly
    public void castOnRandomTarget(@NotNull Char caster) {
        if(targetingType.equals(SpellHelper.TARGET_SELF)) {
            cast(caster);
            return;
        }

        if(targetingType.equals(SpellHelper.TARGET_CELL)) {
            int cell = caster.level().getRandomVisibleCell();

            if (caster.level().cellValid(cell)) {
                cast(caster, caster.level().getRandomVisibleCell());
            }
            return;
        }

        if (targetingType.equals(SpellHelper.TARGET_CHAR)
                || targetingType.equals(SpellHelper.TARGET_CHAR_NOT_SELF)
        ) {
            var candidates = caster.level().getCopyOfMobsArray();
            if(candidates.length>0) {
                cast(caster, Random.oneOf(candidates));
            }
            return;
        }
    }

    protected void castCallback(Char chr) {
        chr.spellCasted(this);
        chr.spendSkillPoints(spellCost());
    }

    public String name() {
        return name;
    }

    public String desc() {
        return info;
    }

    public String getMagicAffinity() {
        return magicAffinity;
    }

    public String texture() {
        return imageFile;
    }

    public Image image(Char caster) {
        val texture = TextureCache.get(texture());
        var spellImage = new Image(texture);

        spellImage.frame(new TextureFilm(texture, 16, 16).get(getImage(caster)));

        return spellImage;
    }

    protected int getImage(Char caster) {
        return image;
    }

    public int spellCost() {
        if(spellCost==0) {
            ModError.doReport("Spell cost for "+ getEntityKind() + "must be > 1", new Exception("spell cost is 0"));
            spellCost = 1;
        }
        return spellCost;
    }

    private String getClassParam(String paramName, String defaultValue) {
        return Utils.getClassParam(getEntityKind(), paramName, defaultValue, false);
    }

    public int level() {
        return level;
    }

    public int getLevelModifier(Char chr) {
        return chr.skillLevel() - level;
    }

    @NotNull
    public SpellItem itemForSlot() {
        if (spellItem == null) {
            spellItem = new SpellItem() {
                @Override
                public String imageFile() {
                    return texture();
                }

                @Override
                public int image() {
                    return Spell.this.getImage(Dungeon.hero);
                }

                @Override
                public void execute(@NotNull Char hero) {
                    hero.nextAction(new UseSpell(Spell.this));
                }

                @Override
                public String name() {
                    return Spell.this.name();
                }

                @Override
                public String info() {
                    return Spell.this.desc();
                }

                public Spell spell() {
                    return Spell.this;
                }

                @Override
                public String getEntityKind() {
                    return Spell.this.getEntityKind();
                }

                @Override
                public Item quickSlotContent() {
                    quantity(Dungeon.hero.getSkillPoints()/spellCost());
                    return this;
                }

                @Override
                public boolean usableByHero() {
                    return quantity() > 0 && canCast(Dungeon.hero, false);
                }

                @Override
                public boolean isIdentified() {
                    return true;
                }

            };
        }
        return spellItem;
    }

    public float getCooldownFactor(Char chr) {
        float chrCooldown = chr.spellCooldown(getEntityKind());
        if(chrCooldown > cooldown) {
            return 1;
        }
        return chrCooldown/cooldown;
    }

    @Override
    public String getEntityKind() {
        return getClass().getSimpleName();
    }

    public abstract static class SpellItem extends Item {
            abstract public Spell spell();
    }

}