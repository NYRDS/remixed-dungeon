package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;

public class ResumeIndicator extends Tag {
	
	private ImageButton btnResume;

	private final Char hero;

	public ResumeIndicator(Char hero) {
		super( 0x00000000);

		this.hero = hero;

		setSize( 24, 26 );
		
		setVisible(true);
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		add(btnResume = new ImageButton(new Image(Assets.UI_ICONS_12,12,0)) {
			@Override
			protected void onClick() {
				ResumeIndicator.this.hero.resume();
			}
		});
	}
	
	@Override
	protected void layout() {
		btnResume.setPos(x+(width() - btnResume.width())/2, y +(height() - btnResume.height())/2 );
		
		super.layout();
	}
	
	
	@Override
	public void update() {
		
		boolean visible = hero.lastAction != null;
		
		btnResume.setVisible(visible);
		setVisible(visible);
		
		super.update();
	}

}
