
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.GameControl;
import com.nyrds.pixeldungeon.windows.WndAlchemy;
import com.nyrds.pixeldungeon.windows.WndGameplayCustomization;
import com.nyrds.pixeldungeon.windows.WndRecipeChecker;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.RankingsScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.ui.Icons;

public class WndInGameMenu extends WndMenuCommon {

	@Override
	protected void createItems() {

		Hero hero = Dungeon.hero;

		if (hero.invalid()) {
			GameLoop.switchScene(TitleScene.class);
			return;
		}

		// Add alchemy button for regular gameplay
		menuItems.add(new MenuButton("Alchemy") {
			@Override
			protected void onClick() {
				super.onClick();
				GameScene.show(new WndAlchemy());
			}
		});

		// Add recipe checker button
		menuItems.add(new MenuButton("Check Recipes") {
			@Override
			protected void onClick() {
				super.onClick();
				GameScene.show(new WndRecipeChecker());
			}
		});

		if (BuildConfig.DEBUG) {
			menuItems.add(new MenuButton("AlchemyTest") {
				@Override
				protected void onClick() {
					super.onClick();
					WndInGameMenu.this.add(new WndAlchemy());
				}
			});
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
					GameScene.show( new WndGameplayCustomization( false, WndGameplayCustomization.Mode.BOTH ) );
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
				Dungeon.save(true);
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
}
