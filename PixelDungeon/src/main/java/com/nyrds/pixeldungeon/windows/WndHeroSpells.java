package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.ImageButton;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.IconTitle;

import java.util.ArrayList;

public class WndHeroSpells extends Window {

	private static final String TXT_TITLE   = Game.getVar(R.string.WndSpells_Title);
	private static final String TXT_LVL   = Game.getVar(R.string.WndHero_SkillLevel);

	private static final int MARGIN = 8;
	private static final int WINDOW_MARGIN = 10;

	private Listener listener;

	public WndHeroSpells(Listener listener) {
		super();
		resize(WndHelper.getLimitedWidth(120), WndHelper.getFullscreenHeight() - WINDOW_MARGIN);
		this.listener = listener;

		final Hero hero = Dungeon.hero;

		String affinity = hero.heroClass.getMagicAffinity();

		IconTitle title  = new IconTitle(new Image(Assets.UI_ICONS,16, SpellHelper.iconIdByAffinity(affinity)), affinity);
		title.setRect(0,0,width, WndHelper.getFullscreenHeight());

		add(title);

		Text txtLvl = PixelScene.createText(TXT_LVL +": "+ hero.magicLvl(), GuiProperties.titleFontSize());
		txtLvl.hardlight(Window.TITLE_COLOR);
		txtLvl.x = width - txtLvl.width();
		add(txtLvl);

		float yPos = title.bottom() + MARGIN;

		int col  = 0;
        float nextRowY = yPos;

		ArrayList<String> spells = SpellFactory.getSpellsByAffinity(affinity);
		if(spells != null) {

			for (String spell : spells) {

				nextRowY = addSpell(spell, hero, col, yPos);
				col++;
				if( col * (Spell.textureResolution() + MARGIN) > width) {
				    yPos = nextRowY;
				    col = 0;
                }
			}
		}
		resize(WndHelper.getLimitedWidth(120), (int) nextRowY);
	}

	private float addSpell(String spellName, final Hero hero,  int col, float yPos) {

		final Spell spell = SpellFactory.getSpellByName(spellName);
		if(spell == null /*|| spell.level() > hero.magicLvl()*/) {
			return yPos;
		}

		int xPos = col * (Spell.textureResolution() + MARGIN) + MARGIN;

		Image spellImage = spell.image();
		ImageButton icon = new ImageButton(spellImage) {

			protected ColorBlock bg;

			@Override
			protected void createChildren() {
				super.createChildren();
				bg = new ColorBlock(width + 6 ,height + 6,0xFF4A4D44);
				add(bg);
			}

			@Override
			protected void layout() {
				super.layout();
					bg.x = x - 3;
					bg.y = y - 3;
					bg.size(width + 6, height + 6);

					image.x = x;
					image.y = y;
			}

			@Override
			protected void onClick() {
				super.onClick();
				if(listener != null) {
					listener.onSelect(spell.itemForSlot());
				} else {
					GameScene.show(new WndSpellInfo(WndHeroSpells.this, hero, spell));
				}
			}

            @Override
            protected boolean onLongClick() {
	            hide();
                QuickSlot.selectSlotFor(spell);
                return true;
            }
        };

		icon.setPos(xPos, yPos);
		add( icon );

		return icon.bottom() + MARGIN;
	}

	public interface Listener {
		void onSelect( Spell.SpellItem spell );
	}
}
