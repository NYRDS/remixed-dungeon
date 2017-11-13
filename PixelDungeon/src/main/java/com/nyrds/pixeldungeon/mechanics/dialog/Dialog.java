package com.nyrds.pixeldungeon.mechanics.dialog;

import com.watabou.noosa.Image;

public class Dialog {

    // Диалог это объект, имеющий идентификатор, по которому игра должна его искать, после того как диалог будет зарегестрирован через Json
    // Диалог имеет изображение, которое содержит набор спрайтов, которые должны быть показаны во время диалога
    // Диалог имеет массив Диалоговых карточек, каждая из которых хранит описание конкретного "этапа" диалога
    // Диалог движется путём смены диалоговых карточек, будь то последовательное пролистывание или вызов конкретного набора карт при нажатии диалоговой опции ("варианта ответа")

    private Image portrait;

    private DialogCard[] cardTable;

    private String dialogID;

    private void setUpFromJson() {

    }

    public void setPortraitImage(){

    }

    public void setDialogCardTable(){

    }

    public String getDialogID(){
        return dialogID;
    }

    public DialogCard getNextCard() {
        int next = 0;

        return cardTable[next];
    }

}
