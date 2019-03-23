package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import androidx.annotation.NonNull;

public class Spell {

    protected      int level             = 1;
    protected      int spellCost         = 5;

    protected float castTime = 1f;
    protected float cooldown = 1f;

    protected String targetingType;
    protected String magicAffinity;

    protected String imageFile = "spellsIcons/common.png";

    protected String name = getClassParam("Name", Game.getVar(R.string.Item_Name));
    protected String info = getClassParam("Info", Game.getVar(R.string.Item_Info));

    protected int image = 0;

    private SpellItem spellItem;

    private Image spellImage;

    protected boolean cast(Char chr, int cell) {
        return true;
    }

    public boolean canCast(@NonNull final Char chr, boolean reallyCast) {
        if (chr instanceof Hero) {
            final Hero hero = (Hero) chr;

            if (!hero.enoughSP(spellCost())) {
                if(reallyCast) {
                    GLog.w(Utils.format(Game.getVar(R.string.Spells_NotEnoughSP), name));
                }
                return false;
            }

            return true;
        }
        return true;
    }

    public boolean cast(@NonNull final Char chr) {

        if (!chr.isAlive()) {
            return false;
        }

        if(!canCast(chr, true)) {
            return false;
        }

        if (chr instanceof Hero) {
            final Hero hero = (Hero) chr;

            if (targetingType.equals(SpellHelper.TARGET_CELL)) {
                GameScene.selectCell(new CellSelector.Listener() {
                    @Override
                    public void onSelect(Integer cell) {
                        if (cell != null) {
                            cast(chr, cell);

                            hero.spend(castTime);
                            hero.busy();
                            hero.getSprite().zap(hero.getPos());
                        }
                    }

                    @Override
                    public String prompt() {
                        return Game.getVar(R.string.Spell_SelectACell);
                    }
                });
                return false;
            }
            hero.spend(castTime);
            hero.busy();
            hero.getSprite().zap(hero.getPos());
        }
        return true;
    }

    protected void castCallback(Char chr) {
        if (chr instanceof Hero) {
            ((Hero) chr).spendSkillPoints(spellCost());
        }
    }

    public String getSpellClass() {
        return getClass().getSimpleName();
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

    public Image image() {
        if(spellImage==null) {
            SmartTexture texture = TextureCache.get(texture());
            spellImage = new Image(texture);
            spellImage.frame(new TextureFilm(texture, 16, 16).get(image));
        }
        return spellImage;
    }

    public int spellCost() {
        return spellCost;
    }

    private String getClassParam(String paramName, String defaultValue) {
        return Utils.getClassParam(this.getClass().getSimpleName(), paramName, defaultValue, false);
    }

    public int level() {
        return level;
    }

    public int getLevelModifier(Char chr) {
        return chr.skillLevel() - level;
    }

    @NonNull
    public SpellItem itemForSlot() {
        if (spellItem == null) {
            spellItem = new SpellItem() {
                @Override
                public String imageFile() {
                    return texture();
                }

                @Override
                public int image() {
                    return Spell.this.image;
                }

                @Override
                public void execute(Hero hero) {
                    Spell.this.cast(hero);
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
                public String getClassName() {
                    return Spell.this.getSpellClass();
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

    public abstract class SpellItem extends Item {
            abstract public Spell spell();
    }
}