
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.npcs.Blacksmith;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

public class WndBlacksmith extends Window {

	private static final int BTN_SIZE	= 36;
	private static final float BTN_GAP	= 10;
	private static final int WIDTH		= 116;
	
	private ItemButton btnPressed;
	
	private final ItemButton btnItem1;
	private final ItemButton btnItem2;
	private final RedButton btnReforge;
	
	public WndBlacksmith(Blacksmith troll) {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( troll.newSprite() );
		titlebar.label( Utils.capitalize( troll.getName() ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

        Text message = PixelScene.createMultiline(R.string.WndBlacksmith_Prompt, GuiProperties.regularFontSize());
		message.maxWidth(WIDTH);
		message.setY(titlebar.bottom() + GAP);
		add( message );
		
		btnItem1 = new ItemButton() {
			@Override
			protected void onClick() {
				btnPressed = btnItem1;
                GameScene.selectItem(Dungeon.hero, itemSelector, WndBag.Mode.UPGRADEABLE, StringsManager.getVar(R.string.WndBlacksmith_Select));
			}
		};
		btnItem1.setRect( (WIDTH - BTN_GAP) / 2 - BTN_SIZE, message.getY() + message.height() + BTN_GAP, BTN_SIZE, BTN_SIZE );
		add( btnItem1 );
		
		btnItem2 = new ItemButton() {
			@Override
			protected void onClick() {
				btnPressed = btnItem2;
                GameScene.selectItem(Dungeon.hero, itemSelector, WndBag.Mode.UPGRADEABLE, StringsManager.getVar(R.string.WndBlacksmith_Select));
			}
		};
		btnItem2.setRect( btnItem1.right() + BTN_GAP, btnItem1.top(), BTN_SIZE, BTN_SIZE );
		add( btnItem2 );

        btnReforge = new RedButton(R.string.WndBlacksmith_Reforge) {
			@Override
			protected void onClick() {
				Blacksmith.upgrade( btnItem1.item, btnItem2.item );
				hide();
			}
		};
		btnReforge.enable( false );
		btnReforge.setRect( 0, btnItem1.bottom() + BTN_GAP, WIDTH, BUTTON_HEIGHT);
		add( btnReforge );
		
		
		resize( WIDTH, (int)btnReforge.bottom() );
	}

	protected final WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect(Item item, Char selector) {
			if (item != null) {
				btnPressed.item( item );
				
				if (btnItem1.item != null && btnItem2.item != null) {
					String result = Blacksmith.verify( btnItem1.item, btnItem2.item );
					if (result != null) {
						GameScene.show( new WndMessage( result ) );
						btnReforge.enable( false );
					} else {
						btnReforge.enable( true );
					}
				}
			}
		}
	};
	
	public static class ItemButton extends Component {
		
		protected NinePatch bg;
		protected ItemSlot slot;
		
		public Item item = null;

		@Override
		protected void createChildren() {
			super.createChildren();
			
			bg = Chrome.get( Chrome.Type.BUTTON );
			add( bg );
			
			slot = new ItemSlot() {
				@Override
				protected void onTouchDown() {
					bg.brightness( 1.2f );
					Sample.INSTANCE.play( Assets.SND_CLICK );
				}

				@Override
				protected void onTouchUp() {
					bg.resetColor();
				}
				@Override
				protected void onClick() {
					ItemButton.this.onClick();
				}
			};
			add( slot );
		}
		
		protected void onClick() {}

		@Override
		protected void layout() {	
			super.layout();
			
			bg.setX(x);
			bg.setY(y);
			bg.size( width, height );
			
			slot.setRect( x + 2, y + 2, width - 4, height - 4 );
		}

		public void item( Item item ) {
			slot.item( this.item = item );
		}
	}
}
