package com.watabou.pixeldungeon.windows.elements;

import com.watabou.noosa.Group;
import com.watabou.pixeldungeon.windows.WndTabbed;

public class RankingTab extends LabeledTab {
	
	private Group page;
	
	public RankingTab(WndTabbed parent, String label, Group page ) {
		super( parent, label );
		this.page = page;
	}
	
	@Override
	public void select( boolean value ) {
		super.select( value );
		if (page != null) {
			page.setVisible(page.setActive(selected));
		}
	}
}