package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Group;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.RankingTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

public class WndCryptoDonate extends WndTabbed {

    public WndCryptoDonate() {
        EventCollector.logScene(getClass().getCanonicalName());

        resize(WndHelper.getFullscreenWidth(), WndHelper.getFullscreenHeight() - tabHeight() - 2*GAP);

        String[] labels = {
                StringsManager.getVar(R.string.WndCryptoDonate_bitcoin),
                StringsManager.getVar(R.string.WndCryptoDonate_ethereum),
                StringsManager.getVar(R.string.WndCryptoDonate_tron),
                StringsManager.getVar(R.string.WndCryptoDonate_ton)
        };
        
        Group[] pages = {
                new CryptoDonateTab("bitcoin", "bitcoin:bc1qzhg2x9fx79tgtryd4x6wk0fdurfkqgdjrlxal7"),
                new CryptoDonateTab("ethereum", "ethereum:0x2714738C5d3C1a419217b8e31Ca1713786E5b64F"),
                new CryptoDonateTab("tron", "tron:TFsGQKD6swaw4EpVuUdvpnioucbHEe4PnC"),
                new CryptoDonateTab("ton", "ton:UQC_1_qbAvjUBXJtlvkkuXYpzlSE1qNrgSr3P_PTNl--hYss")
        };

        for (int i = 0; i < pages.length; i++) {
            add(pages[i]);

            Tab tab = new RankingTab(this, labels[i], pages[i]);
            tab.setSize(width/pages.length, tabHeight());
            add(tab);
        }

        select(0);
    }

    @Override
    public void select(int index) {
        super.select(index);
        EventCollector.logScene(getClass().getCanonicalName() + ":" + index);
    }

    private class CryptoDonateTab extends Group {
        private String currencyName;
        private String uriScheme;

        CryptoDonateTab(String currencyName, String uriScheme) {
            this.currencyName = currencyName;
            this.uriScheme = uriScheme;

            IconTitle tabTitle = new IconTitle(Icons.get(Icons.SUPPORT), 
                    StringsManager.getVar(R.string.WndCryptoDonate_supportDev));
            tabTitle.setRect(0, 0, width, 0);
            add(tabTitle);

            float pos = tabTitle.bottom() + GAP;

            Text infoText = PixelScene.createMultiline(R.string.WndCryptoDonate_infoText, GuiProperties.regularFontSize());
            infoText.maxWidth(width);
            infoText.setPos(0, pos);
            add(infoText);
            pos += infoText.height() + GAP;

            Text currencyText = PixelScene.createMultiline(
                    StringsManager.getVar(R.string.WndCryptoDonate_currency) + ": " + currencyName,
                    GuiProperties.titleFontSize());
            currencyText.maxWidth(width);
            currencyText.hardlight(Window.TITLE_COLOR);
            currencyText.setPos(0, pos);
            add(currencyText);
            pos += currencyText.height() + GAP;

            Text addressLabel = PixelScene.createMultiline(R.string.WndCryptoDonate_walletAddress, GuiProperties.regularFontSize());
            addressLabel.maxWidth(width);
            addressLabel.setPos(0, pos);
            add(addressLabel);
            pos += addressLabel.height() + GAP;

            // Extract the actual address (without the scheme)
            String address = uriScheme.contains(":") ? uriScheme.substring(uriScheme.indexOf(":") + 1) : uriScheme;

            // Display address as plain text by using single-character wrapping to break _markup_ patterns
            // Each underscore becomes [white:_] which renders as a plain underscore in white color
            String safeAddress = address.replace("_", "[white:_]");

            Text addressText = PixelScene.createMultiline(safeAddress, GuiProperties.regularFontSize());
            addressText.maxWidth(width - 10);
            addressText.setPos(0, pos);
            add(addressText);
            pos += addressText.height() + GAP;

            if (Utils.isAndroid()) {
                RedButton openWalletButton = new RedButton(StringsManager.getVar(R.string.WndCryptoDonate_openWallet)) {
                    @Override
                    protected void onClick() {
                        Game.openUrl(StringsManager.getVar(R.string.WndCryptoDonate_openWallet), uriScheme);
                        EventCollector.logEvent("CryptoDonationOpenWallet", currencyName);
                    }
                };
                openWalletButton.setRect(0, pos, width, BUTTON_HEIGHT);
                add(openWalletButton);
                pos += openWalletButton.height() + GAP;
            }

            RedButton copyAddressButton = makeCopyAddressButton(currencyName, uriScheme, pos);
            add(copyAddressButton);
            pos += copyAddressButton.height() + GAP;

            Text warningText = PixelScene.createMultiline(R.string.WndCryptoDonate_warningText, GuiProperties.smallFontSize());
            warningText.maxWidth(width);
            warningText.hardlight(0xFF0000);
            warningText.setPos(0, pos);
            add(warningText);
        }

        private RedButton makeCopyAddressButton(String currencyName, String uriScheme, float pos) {
            RedButton copyAddressButton = new RedButton(StringsManager.getVar(R.string.WndCryptoDonate_copyAddress)) {
                @Override
                protected void onClick() {
                    // Extract the actual address (without the scheme)
                    String address = uriScheme.contains(":") ? uriScheme.substring(uriScheme.indexOf(":") + 1) : uriScheme;
                    Game.copyToClipboard(currencyName + " Address", address);
                    EventCollector.logEvent("CryptoDonationCopyAddress", currencyName);
                }
            };
            copyAddressButton.setRect(0, pos, width, BUTTON_HEIGHT);
            return copyAddressButton;
        }
    }
}