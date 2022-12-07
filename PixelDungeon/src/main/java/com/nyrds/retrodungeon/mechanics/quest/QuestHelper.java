package com.nyrds.retrodungeon.mechanics.quest;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * Created by DeadDie on 14.05.2017
 */
public class QuestHelper {
    static private JSONObject initQuest = JsonHelper.readJsonFromAsset("questDesc/quests.json");
    static private JSONObject initQuestList = JsonHelper.readJsonFromAsset("questDesc/questList.json");

    public static HashSet<Quest> questMap;

    public void initQuest(String questName) {
        if (initQuest.has(questName)) {
            try {
                JSONObject questDesc = initQuest.getJSONObject(questName);

                int quantity = Math.max(questDesc.optInt("quantity",1),1);

                if (questDesc.has("type")) {
                    createQuest(questDesc.getString("type"));
                }

                if (questDesc.has("target")) {

                }

                if (questDesc.has("startMessage")) {

                }

                if (questDesc.has("reminderMessage")) {

                }

                if (questDesc.has("turnInMessage")) {

                }
                if (questDesc.has("aftermathMessage")) {

                }

            } catch (JSONException e) {
                throw new TrackedRuntimeException(e);
           /* } catch (InstantiationException e) {
                throw new TrackedRuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new TrackedRuntimeException(e);*/
            }
        }
    }

    public void getQuestList(){

    }

    private void createQuest(String questType){
        Quest quest;
        if (questType.equals("FetchQuest")){
            quest = new FetchQuest();
        } else {
            quest = new HuntQuest();
        }
        questMap.add(quest);
    }
}