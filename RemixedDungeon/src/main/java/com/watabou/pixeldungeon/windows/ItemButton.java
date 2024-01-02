package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.platform.audio.Sample;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.ColorBlock;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.food.RottenFood;
import com.watabou.pixeldungeon.items.scrolls.BlankScroll;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.Boomerang;
import com.watabou.pixeldungeon.plants.Seed;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.QuickSlot;

class ItemButton extends ItemSlot {
    
    private static final int NORMAL		= 0xFF4A4D44;
    private static final int EQUIPPED	= 0xFF63665B;

    private final WndBag wndBag;
    private final Item item;
    private ColorBlock bg;
    
    public ItemButton(WndBag wndBag, Item item) {
        
        super( item );
        this.wndBag = wndBag;
        this.item = item;

        if (item instanceof Gold) {
            bg.setVisible(false);
        }

        placeItem(item, wndBag.getMode());

        width = height = WndBag.SLOT_SIZE;
    }
    
    @Override
    protected void createChildren() {	
        bg = new ColorBlock( WndBag.SLOT_SIZE, WndBag.SLOT_SIZE, NORMAL );
        add( bg );
        
        super.createChildren();
    }
    
    @Override
    protected void layout() {
        bg.setX(x);
        bg.setY(y);
        
        super.layout();
    }

    public void placeItem( Item item, WndBag.Mode mode ) {
        if (item != null) {

            bg.texture( TextureCache.createSolid( item.isEquipped( Dungeon.hero ) ? EQUIPPED : NORMAL ) );
            ItemUtils.tintBackground(item, bg);

            if(item.selectedForAction() || item instanceof ItemPlaceholder) {
                enable(false);
            } else {
                boolean enableItem = false;
                switch (mode) {

                    case ALL:
                    case QUICKSLOT:
                    case FOR_BUY:
                        enableItem = true;
                        break;
                    case UNIDENTIFED:
                        enableItem = !item.isIdentified();
                        break;
                    case UPGRADEABLE:
                        enableItem = item.isUpgradable();
                        break;
                    case FOR_SALE:
                        enableItem=(item.price() > 0) && (!item.isEquipped(Dungeon.hero) || !item.isCursed());
                        break;
                    case WEAPON:
                        enableItem=(!(item instanceof KindOfBow) && (item instanceof MeleeWeapon || item instanceof Boomerang));
                        break;
                    case ARMOR:
                        enableItem=(item instanceof Armor);
                        break;
                    case WAND:
                        enableItem=item instanceof Wand;
                        break;
                    case SEED:
                        enableItem=(item instanceof Seed);
                        break;
                    case INSCRIBABLE:
                        enableItem=(item instanceof Armor || item instanceof BlankScroll);
                        break;
                    case MOISTABLE:
                        enableItem=(item instanceof Arrow || item instanceof Scroll || item instanceof RottenFood);
                        break;
                    case FUSEABLE:
                        enableItem=((item instanceof Scroll || item instanceof MeleeWeapon || item instanceof Armor || item instanceof KindOfBow || item instanceof Wand || item.getEntityKind().contains("Shield")));
                        break;
                    case UPGRADABLE_WEAPON:
                        enableItem=((item instanceof MeleeWeapon || item instanceof Boomerang) && (item.isUpgradable()));
                        break;
                    case ARROWS:
                        enableItem=(item instanceof Arrow);
                        break;
                }
                enable(enableItem);
            }
        } else {
            bg.color( NORMAL );
        }
    }

    @Override
    protected void onTouchDown() {
        bg.brightness( 1.5f );
        Sample.INSTANCE.play( Assets.SND_CLICK, 0.7f, 0.7f, 1.2f );
    }

    protected void onTouchUp() {
        bg.brightness( 1.0f );
    }

    @Override
    protected void onClick() {
        if (wndBag.getListener() != null) {
            if(wndBag.hideOnSelect()) {
                wndBag.hide();
            }
            wndBag.getListener().onSelect( item, Dungeon.hero);
        } else {
            wndBag.add( new WndItem(wndBag, item ) );
        }
    }
    
    @Override
    protected boolean onLongClick() {
        if (wndBag.getListener() == null) {
            if(wndBag.hideOnSelect()) {
                wndBag.hide();
            }
            QuickSlot.selectSlotFor(item.quickSlotContent());
            return true;
        } else {
            return false;
        }
    }
}
