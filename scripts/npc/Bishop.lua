--[[
    Created by mike.
    DateTime: 19.01.19 21:24
    This file is part of pixel-dungeon-remix
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

local quest = require"scripts/lib/quest"

local npc
local client

local lesserPrice
local greatPrice
local removeCursePrice

-- stage 1 keeps the name of the retired single-stage quest, so kills
-- already counted by players of the old quest carry over
local stageQuestName = {
    [1] = "BishopUndeadQuest",
    [4] = "BishopQuest_4",
    [5] = "BishopQuest_5"
}

local undeadKinds = {"Skeleton","Zombie","Wraith","Undead","EnslavedSoul","ExplodingSkull","DeathKnight","DreadKnight","JarOfSouls"}

questList = {
    {
        {
            prologue = { text = 'BishopQuest_1_1_Prologue' },
            requirements = { mobKill = { kinds = undeadKinds, quantity = 10 } },
            in_progress = { text = "BishopQuest_1_1_InProgress" },
            reward = { item = { kind = "PotionOfHealing", quantity = 1 } },
            epilogue = { text = 'BishopQuest_1_1_Epilogue' }
        }
    },
    {
        {
            prologue = { text = 'BishopQuest_2_1_Prologue' },
            requirements = { item = { kind = "Sungrass.Seed", quantity = 3 } },
            in_progress = { text = "BishopQuest_2_1_InProgress" },
            reward = { item = { kind = "PotionOfMana", quantity = 1 } },
            epilogue = { text = 'BishopQuest_2_1_Epilogue' }
        },
        {
            prologue = { text = 'BishopQuest_2_2_Prologue' },
            requirements = { item = { kind = "Moongrace.Seed", quantity = 3 } },
            in_progress = { text = "BishopQuest_2_2_InProgress" },
            reward = { item = { kind = "PotionOfMana", quantity = 1 } },
            epilogue = { text = 'BishopQuest_2_2_Epilogue' }
        }
    },
    {
        {
            prologue = { text = 'BishopQuest_3_1_Prologue' },
            requirements = { mob = { kind = "Zombie", quantity = 1 } },
            in_progress = { text = "BishopQuest_3_1_InProgress" },
            reward = { item = { kind = "ScrollOfUpgrade", quantity = 1 } },
            epilogue = { text = 'BishopQuest_3_1_Epilogue' }
        },
        {
            prologue = { text = 'BishopQuest_3_2_Prologue' },
            requirements = { mob = { kind = "Bat", quantity = 1 } },
            in_progress = { text = "BishopQuest_3_2_InProgress" },
            reward = { item = { kind = "ScrollOfUpgrade", quantity = 1 } },
            epilogue = { text = 'BishopQuest_3_2_Epilogue' }
        }
    },
    {
        {
            prologue = { text = 'BishopQuest_4_1_Prologue' },
            requirements = { mobKill = { kinds = {"DeathKnight"}, quantity = 2 } },
            in_progress = { text = "BishopQuest_4_1_InProgress" },
            reward = { item = { kind = "PotionOfMight", quantity = 1 } },
            epilogue = { text = 'BishopQuest_4_1_Epilogue' }
        },
        {
            prologue = { text = 'BishopQuest_4_2_Prologue' },
            requirements = { mobKill = { kinds = {"EnslavedSoul"}, quantity = 4 } },
            in_progress = { text = "BishopQuest_4_2_InProgress" },
            reward = { item = { kind = "PotionOfMight", quantity = 1 } },
            epilogue = { text = 'BishopQuest_4_2_Epilogue' }
        }
    },
    {
        {
            prologue = { text = 'BishopQuest_5_1_Prologue' },
            requirements = { mobKill = { kinds = {"Succubus"}, quantity = 2 } },
            in_progress = { text = "BishopQuest_5_1_InProgress" },
            reward = { final = true },
            epilogue = { text = 'BishopQuest_5_1_Epilogue' }
        }
    }
}

local killsSlain = function(questName, kinds)
    local state = quest.state(questName)
    if not state then
        return 0
    end
    local total = 0
    for _, kind in pairs(kinds) do
        total = total + ((state.kills or {})[kind] or 0)
    end
    return total
end

local initData = function(me, data)
    data["questIndex"] = 1
    data["questInProgress"] = false

    -- carry over the retired single-stage quest (shipped in 32.4.beta.16)
    if quest.isGiven(stageQuestName[1]) and not quest.isCompleted(stageQuestName[1]) then
        data["questInProgress"] = true
        data["questVariant"] = 1
    elseif quest.isCompleted(stageQuestName[1]) then
        data["questIndex"] = #questList + 1
    end

    mob.storeData(me, data)
end

local dialog = function(index)
    if index == 0 then
        if client:gold() >= lesserPrice then
            client:spendGold(lesserPrice)
            client:getSprite():emitter():burst( RPD.Sfx.ShaftParticle.FACTORY, 2 );
            RPD.affectBuff(client,RPD.Buffs.Blessed,100)
            return
        end
        npc:say("Bishop_no_money")
    end

    if index == 1 then
        if client:gold() >= greatPrice then
            client:spendGold(greatPrice)
            client:getSprite():emitter():burst( RPD.Sfx.ShaftParticle.FACTORY, 5 );
            RPD.affectBuff(client,RPD.Buffs.Blessed,500)
            RPD.affectBuff(client,RPD.Buffs.Blessed,500)
            return
        end
        npc:say("Bishop_no_money")
    end

    if index == 2 then
        if client:gold() >= removeCursePrice then
            client:spendGold(removeCursePrice)
            -- RPD.item() swaps forbidden items for gold (NO_SCROLLS challenge), and gold has no uncurse
            local scroll = RPD.ItemFactory:itemByName("ScrollOfRemoveCurse")
            scroll:uncurse(client:getBelongings())
            RPD.glogp("ScrollOfRemoveCurse_Proced")
            client:getSprite():emitter():start(RPD.Sfx.ShadowParticle.UP, 0.05, 10);
            return
        end

        npc:say("Bishop_no_money")
    end

    if index == 3 then
        npc:say("Bishop_bye")
    end
end


return mob.init({
    interact = function(self, chr)
        client = chr
        npc = self

        local data = mob.restoreData(self)

        if not RPD.checkBadge("PRIEST_QUEST_COMPLETED") then
            if data["questIndex"] == nil then
                -- NPC last saved by an older script version
                initData(self, data)
            end

            local questIndex = data["questIndex"]

            if questIndex > #questList then
                RPD.showQuestWindow(self, "BishopQuest_AllDone")
                return
            end

            if not data["questInProgress"] then
                local questVariant = math.random(1, #questList[questIndex])
                data["questInProgress"] = true
                data["questVariant"] = questVariant

                local stageQuest = questList[questIndex][questVariant]
                if stageQuest.requirements.mobKill then
                    quest.give(stageQuestName[questIndex], chr, {kills = stageQuest.requirements.mobKill.kinds})
                end

                RPD.debug("Bishop quest "..questIndex.." "..questVariant.." given")
                mob.storeData(self, data)

                RPD.showQuestWindow(self, stageQuest.prologue.text)
                return
            end

            local questIndex_ = data["questIndex"]
            local stageQuest = questList[questIndex_][data["questVariant"]]

            local function giveReward()
                local rewardItem = stageQuest.reward.item
                if rewardItem then
                    local reward = RPD.item(rewardItem.kind, rewardItem.quantity)
                    chr:collectAnimated(reward)
                end

                if stageQuest.reward.final then
                    RPD.Badges:displayBadge(RPD.Badges.Badge:valueOf("PRIEST_QUEST_COMPLETED"))
                    RPD.affectBuff(chr, RPD.Buffs.Blessed, 500)
                    chr:heal(chr:ht(), self)
                    chr:collectAnimated(RPD.item("Gold", 300))
                end

                RPD.showQuestWindow(self, stageQuest.epilogue.text)

                data["questInProgress"] = false
                data["questIndex"] = questIndex_ + 1
                mob.storeData(self, data)
            end

            local requirements = stageQuest.requirements

            if requirements.item then
                local itemDesc = requirements.item
                local wantedItem = chr:checkItem(itemDesc.kind)
                local wantedQty = itemDesc.quantity

                if wantedItem ~= nil and wantedItem:quantity() >= wantedQty then
                    local actualQty = wantedItem:quantity()
                    if wantedQty == actualQty then
                        wantedItem:removeItem()
                    else
                        wantedItem:quantity(actualQty - wantedQty)
                    end

                    return giveReward()
                end
            end

            if requirements.mob then
                local wantedMob = requirements.mob.kind
                local wantedQty = requirements.mob.quantity

                local pets = chr:getPets_l()

                local actualQty = 0

                for _, pet in pairs(pets) do
                    if pet:getEntityKind() == wantedMob then
                        actualQty = actualQty + 1
                    end
                end

                if actualQty >= wantedQty then
                    for _, pet in pairs(pets) do
                        if pet:getEntityKind() == wantedMob and wantedQty > 0 then
                            wantedQty = wantedQty - 1
                            pet:makePet(self)
                        end
                    end

                    return giveReward()
                end
            end

            if requirements.mobKill then
                local killDesc = requirements.mobKill
                local slain = killsSlain(stageQuestName[questIndex_], killDesc.kinds)

                if slain >= killDesc.quantity then
                    quest.complete(stageQuestName[questIndex_])
                    return giveReward()
                end

                RPD.showQuestWindow(self, RPD.textById(stageQuest.in_progress.text):format(slain, killDesc.quantity))
                return
            end

            RPD.showQuestWindow(self, stageQuest.in_progress.text)
            return
        end

        local priceFactor = RPD.RemixedDungeon:getDifficultyFactor() * math.pow( 1.05, (client:lvl()-1))

        lesserPrice      = math.floor(100 * priceFactor)
        greatPrice       = math.floor(500 * priceFactor)
        removeCursePrice = math.floor(200 * priceFactor)

        RPD.chooseOption( dialog,
                "Bishop_title",
                "Bishop_text",
                RPD.textById("Bishop_lesser_bless"):format(lesserPrice),
                RPD.textById("Bishop_great_bless"):format(greatPrice),
                RPD.textById("Bishop_remove_curse"):format(removeCursePrice),
                "Bishop_not_interested"
        )
    end,
    die = function(self, cause)
        local hero = RPD.Dungeon.hero
        hero:ht(math.max(hero:ht()/2,1))
        hero:damage(hero:ht(), self)

        hero:getSprite():emitter():burst( RPD.Sfx.ShadowParticle.CURSE, 6 )

        RPD.playSound( "snd_cursed" )
    end,

    spawn = function(me, level)
        RPD.setAi(me,"NpcDefault")

        local data = mob.restoreData(me)

        if not data["questIndex"] then
            initData(me, data)
        end

        -- a priest has no need of the initiation trials
        local hero = RPD.Dungeon.hero
        if hero and hero:getHeroClass():getEntityKind() == "PRIEST" then
            me:destroy()
        end
    end
})
