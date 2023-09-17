
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.GameControl;
import com.nyrds.pixeldungeon.windows.WndGameplayCustomization;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Util;
import com.watabou.noosa.Gizmo;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.RankingsScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.utils.Utils;

public class WndGame extends WndMenuCommon {

	@Override
	protected void createItems() {

		Hero hero = Dungeon.hero;

		if (hero.invalid()) {
			GameLoop.switchScene(TitleScene.class);
			return;
		}

		if(Util.isDebug()) {
			menuItems.add(createIsometricShift());
		}

		menuItems.add( new MenuButton(R.string.WndGame_Settings) {
			@Override
			protected void onClick() {
				hide();
				GameScene.show( new WndSettingsInGame() );
			}
		} );


		final int difficulty = hero.getDifficulty();

		if(difficulty < 2 && hero.isAlive()) {
            menuItems.add( new MenuButton(R.string.WndGame_Save) {
				@Override
				protected void onClick() {
                    GameScene.show(new WndSaveSlotSelect(true, StringsManager.getVar(R.string.WndSaveSlotSelect_SelectSlot)));
				}
			} );
		}

		if(difficulty < 2) {
            menuItems.add( new MenuButton(R.string.WndGame_Load) {
				@Override
				protected void onClick() {
                    GameScene.show(new WndSaveSlotSelect(false, StringsManager.getVar(R.string.WndSaveSlotSelect_SelectSlot)));
				}
			} );
		}

		if (Dungeon.getChallenges() + Dungeon.getFacilitations() > 0) {
            menuItems.add( new MenuButton(R.string.WndGame_Customizations) {
				@Override
				protected void onClick() {
					hide();
					GameScene.show( new WndGameplayCustomization( false ) );
				}
			} );
		}

		if (!hero.isAlive()) {

			final HeroClass heroClass = hero.getHeroClass();

            menuItems.add(new MenuButton(StringsManager.getVar(R.string.WndGame_Start),Icons.get(heroClass) ) {
				@Override
				protected void onClick() {
					GameControl.startNewGame(heroClass.name(), difficulty, false);
				}
			});

            menuItems.add( new MenuButton(R.string.WndGame_Ranking) {
				@Override
				protected void onClick() {
					GameLoop.switchScene( RankingsScene.class );
				}
			} );
		}

        menuItems.add( new MenuButton(R.string.WndGame_menu) {
			@Override
			protected void onClick() {
				if(Dungeon.hero.isAlive()) {
					Dungeon.save(false);
				}
				GameLoop.switchScene( TitleScene.class );
			}
		} );

        menuItems.add( new MenuButton(R.string.WndGame_Exit) {
			@Override
			protected void onClick() {
				Game.shutdown();
			}
		} );

        menuItems.add( new MenuButton(R.string.WndGame_Return) {
			@Override
			protected void onClick() {
				hide();
			}
		} );
	}

	private Selector createIsometricShift() {
		return new Selector(WIDTH, BUTTON_HEIGHT, "Shift", new Selector.PlusMinusDefault() {

			@Override
			public void onPlus(Selector s) {
				Gizmo.isometricModeShift += 1;
				s.setText(Utils.format("Shift: %d", Gizmo.isometricModeShift));
			}

			@Override
			public void onMinus(Selector s) {
				Gizmo.isometricModeShift -= 1;
				s.setText(Utils.format("Shift: %d", Gizmo.isometricModeShift));
			}

			@Override
			public void onDefault(Selector s) {
				Gizmo.isometricModeShift = 0;
				s.setText(Utils.format("Shift: %d", Gizmo.isometricModeShift));
			}

		});
	}
}
