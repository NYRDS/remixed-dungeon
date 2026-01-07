package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.input.Touchscreen;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.utils.GameMath;
import com.watabou.utils.PointF;

public abstract class Slider extends Component {

    private TouchArea pointerArea;

    private Text title;
    private Text minTxt;
    private Text maxTxt;

    private int minVal;
    private int maxVal;
    private int selectedVal;

    private NinePatch sliderNode;
    private NinePatch BG;
    private ColorBlock sliderBG;
    private ColorBlock[] sliderTicks;
    private float tickDist;


    public Slider(int title, String minTxt, String maxTxt, int minVal, int maxVal){
        super();

        this.title.text(StringsManager.getVar(title));
        this.minTxt.text(minTxt);
        this.maxTxt.text(maxTxt);

        this.minVal = minVal;
        this.maxVal = maxVal;

        sliderTicks = new ColorBlock[(maxVal - minVal) + 1];
        for (int i = 0; i < sliderTicks.length; i++){
            add(sliderTicks[i] = new ColorBlock(1, 9, 0xFF222222));
        }
        add(sliderNode);
    }

    public Slider(String title, String minTxt, String maxTxt, int minVal, int maxVal){
        super();

        this.title.text(title);
        this.minTxt.text(minTxt);
        this.maxTxt.text(maxTxt);

        this.minVal = minVal;
        this.maxVal = maxVal;

        sliderTicks = new ColorBlock[(maxVal - minVal) + 1];
        for (int i = 0; i < sliderTicks.length; i++){
            add(sliderTicks[i] = new ColorBlock(1, 9, 0xFF222222));
        }
        add(sliderNode);
    }

    protected abstract void onChange();

    public int getSelectedValue(){
        return selectedVal;
    }

    public void setSelectedValue(int val) {
        this.selectedVal = val;
        sliderNode.x = (int)(x + tickDist*(selectedVal-minVal)) + 0.5f;
        sliderNode.y = sliderBG.y-4;
        PixelScene.align(sliderNode);
    }

    @Override
    protected void createChildren() {
        super.createChildren();

        add( BG = Chrome.get(Chrome.Type.BUTTON));
        BG.alpha(0.5f);

        add(title = PixelScene.createText(9));
        add(this.minTxt = PixelScene.createText(6));
        add(this.maxTxt = PixelScene.createText(6));

        add(sliderBG = new ColorBlock(1, 1, 0xFF222222));
        sliderNode = Chrome.get(Chrome.Type.BUTTON);
        sliderNode.size(4, 7);

        pointerArea = new TouchArea(0, 0, 0, 0){
            boolean pressed = false;

            @Override
            protected void onTouchDown(Touchscreen.Touch event) {
                pressed = true;
                PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
                sliderNode.x = GameMath.gate(sliderBG.x-2, p.x - sliderNode.width()/2, sliderBG.x+sliderBG.width()-2);
                sliderNode.dirtyMatrix = true;
                sliderNode.brightness(1.5f);
            }

            @Override
            protected void onTouchUp( Touchscreen.Touch event) {
                if (pressed) {
                    PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
                    sliderNode.x = GameMath.gate(sliderBG.x - 2, p.x - sliderNode.width()/2, sliderBG.x + sliderBG.width() - 2);
                    sliderNode.resetColor();
                    //sets the selected value
                    selectedVal = minVal + Math.round((sliderNode.x - x) / tickDist);
                    sliderNode.x = x + tickDist * (selectedVal - minVal) + 0.5f;
                    PixelScene.align(sliderNode);
                    onChange();
                    pressed = false;
                    sliderNode.dirtyMatrix = true;
                }
            }

            @Override
            protected void onDrag( Touchscreen.Touch  event ) {
                if (pressed) {
                    PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
                    sliderNode.x = GameMath.gate(sliderBG.x - 2, p.x - sliderNode.width()/2, sliderBG.x + sliderBG.width() - 2);
                    sliderNode.dirtyMatrix = true;
                }
            }
        };
        add(pointerArea);

    }

    @Override
    public void layout() {

        if (title.width() > 0.7f*width){
            String titleText = title.text();
            remove(title);
            title = PixelScene.createText(6);
            add(title);
            title.text(titleText);
        }

        title.setPos(
                x + (width-title.width())/2,
                y+2
        );
        PixelScene.align(title);
        sliderBG.y = y + height() - 7;
        sliderBG.x = x+2;
        sliderBG.size(width-5, 1);
        tickDist = sliderBG.width()/(maxVal - minVal);
        for (int i = 0; i < sliderTicks.length; i++){
            sliderTicks[i].y = sliderBG.y-4;
            sliderTicks[i].x = x + 2 + (tickDist*i);
            PixelScene.align(sliderTicks[i]);
        }

        minTxt.setPos(
                x+1,
                sliderBG.y-5-minTxt.height()
        );
        maxTxt.setPos(
                x+width()-maxTxt.width()-1,
                sliderBG.y-5-minTxt.height()
        );

        sliderNode.x = x + tickDist*(selectedVal-minVal) + 0.5f;
        sliderNode.y = sliderBG.y-3;
        PixelScene.align(sliderNode);

        pointerArea.x = x;
        pointerArea.y = y;
        pointerArea.width = width();
        pointerArea.height = height();

        BG.size(width(), height());
        BG.x = x;
        BG.y = y;
    }
}