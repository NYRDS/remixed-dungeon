
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.ui.Icons;

public class WndError extends WndTitledMessage {

	public WndError( String message ) {
        super( Icons.WARNING.get(), StringsManager.getVar(R.string.WndError_Title), message );
	}

}
