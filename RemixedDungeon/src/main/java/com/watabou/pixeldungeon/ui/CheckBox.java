
package com.watabou.pixeldungeon.ui;

import com.watabou.pixeldungeon.scenes.PixelScene;

public class CheckBox extends RedButton {

	private boolean checked;
	
	public CheckBox( String label ) {
		this(label,false);
	}

	public CheckBox (String label, boolean checked) {
		super(label);
		this.checked = checked;
		icon( Icons.get( checked ? Icons.CHECKED : Icons.UNCHECKED ) );
	}

	@Override
	protected void layout() {
		super.layout();
		
		float margin = (height - text.baseLine()) / 2;
		
		text.setX(PixelScene.align( PixelScene.uiCamera, x + margin ));
		text.setY(PixelScene.align( PixelScene.uiCamera, y + margin ));

        icon.setX(PixelScene.align( PixelScene.uiCamera, x + width - margin - icon.width));
		icon.setY(PixelScene.align( PixelScene.uiCamera, y + (height - icon.height()) / 2 ));
	}
	
	public boolean checked() {
		return checked;
	}
	
	public void checked( boolean value ) {
		if (checked != value) {
			checked = value;
			icon.copy( Icons.get( checked ? Icons.CHECKED : Icons.UNCHECKED ) );
		}
	}
	
	@Override
	protected void onClick() {
		super.onClick();
		checked( !checked );
	}
}
