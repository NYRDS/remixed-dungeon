package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.common.Library;
import com.watabou.noosa.CompositeTextureImage;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.ListItem;

public class LibraryListItem extends ListItem {
    private final String finalCategory;
    private final String entryId;

    public LibraryListItem(String category, String entry, Library.EntryHeader desc) {
        finalCategory = category;
        entryId = entry;
        clickable = true;

        if(desc.icon instanceof CompositeTextureImage) {
            sprite.copy((CompositeTextureImage) desc.icon);
        } else {
            sprite.copy(desc.icon);
        }
        label.text(desc.header);
    }

    @Override
    protected void onClick() {
        GameScene.show(Library.infoWindow(finalCategory, entryId));
    }
}
