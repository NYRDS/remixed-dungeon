package com.nyrds.pixeldungeon.mechanics.dialog;

public class DialogCard {
    //Диалоговая карточка - это объект содержащий текст который должен быть показан на данный момент, картинку, которая данный текст сопровождает и Загаловок

    int index = 0;
    String title = "Guy";
    String text = "Debug text. Which is, presumably, talking for the sake of testing the dialog and not for debuging, as you might have been mislead to believe.";

    public String getText(){
        return text;
    }

    // Портрет диалога содержится в объекте диалога, как набор картинок, которые должны быть показаны на разных этапах диалога
    // Каждая карточка имеет свой индекс портрета, который должен быть показан во время конкретной реплики
    // Внутри карточки портрет может распологаться либо слево от текста, либо справа
    public int getPortraitIndex(){
        return index;
    }
}
