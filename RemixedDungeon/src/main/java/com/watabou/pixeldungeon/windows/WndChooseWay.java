
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.common.MasteryItem;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkull;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkullOfMastery;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.Highlighter;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class WndChooseWay extends Window {
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;

	public WndChooseWay(@NotNull Char chr,final Item item, final HeroSubClass way){
		super();
		chooseWay(chr, item, way, null );
	}

	public WndChooseWay(@NotNull Char chr , final Item item, final HeroSubClass way1, final HeroSubClass way2 ) {
		super();
		chooseWay(chr, item, way1, way2 );
	}

	private String getWayDesc(final HeroSubClass way1, final HeroSubClass way2){
		String desc =  way1.desc();
		if (way2 != null){
			desc = desc + "\n\n" + way2.desc();
		}
		if (way1 == HeroSubClass.LICH){
            desc = StringsManager.getVar(R.string.BlackSkullOfMastery_Title) + "\n\n"
					+ desc + "\n\n" + StringsManager.getVar(R.string.BlackSkullOfMastery_RemainHumanDesc);
		}
        desc = desc + "\n\n" + StringsManager.getVar(R.string.WndChooseWay_Message);
		return desc;
	}

	private void chooseWay(@NotNull Char chr, final Item item, final HeroSubClass way1, final HeroSubClass way2) {
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( item ) );
		titlebar.label( item.name() );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		Text normal = Highlighter.addHilightedText(titlebar.left(), titlebar.bottom() +  GAP, width,this,  getWayDesc(way1, way2) );

		RedButton btnWay1 = new RedButton( Utils.capitalize( way1.title() ) ) {
			@Override
			protected void onClick() {
				hide();
				MasteryItem.choose(chr, item, way1 );
			}
		};
		btnWay1.setRect( 0, normal.getY() + normal.height() + GAP, (WIDTH - GAP) / 2, BTN_HEIGHT );
		add( btnWay1 );

		if (way1 != HeroSubClass.LICH){
			RedButton btnWay2 = new RedButton( Utils.capitalize( way2.title() ) ) {
				@Override
				protected void onClick() {
					hide();
					MasteryItem.choose(chr, item, way2 );
				}
			};
			btnWay2.setRect( btnWay1.right() + GAP, btnWay1.top(), btnWay1.width(), BTN_HEIGHT );
			add( btnWay2 );
		} else {
			btnBreakSpell(btnWay1);
		}

        RedButton btnCancel = new RedButton(R.string.WndChooseWay_Cancel) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnCancel.setRect( 0, btnWay1.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnCancel );
		
		resize( WIDTH, (int)btnCancel.bottom() );
	}

	private void btnBreakSpell(RedButton btnWay1){
        RedButton btnWay2 = new RedButton( Utils.capitalize(StringsManager.getVar(R.string.BlackSkullOfMastery_Necromancer)) ) {
			@Override
			protected void onClick() {
				hide();
				Hero hero = Dungeon.hero;
				Item a = hero.getBelongings().getItem( BlackSkullOfMastery.class );
				a.removeItemFrom(hero);
				Item b = new BlackSkull();
				b.doDrop(hero);
			}
		};
		btnWay2.setRect( btnWay1.right() + GAP, btnWay1.top(), btnWay1.width(), BTN_HEIGHT );
		add( btnWay2 );
	}
}
