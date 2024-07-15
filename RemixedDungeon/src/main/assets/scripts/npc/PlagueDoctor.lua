--[[
    Created by mike.
    DateTime: 2024.05.31
    This file is part of pixel-dungeon-remix
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

questList = {
    {
        {
            prologue = { text = 'PlagueDoctorQuest_1_1_Prologue' },
            requirements = { item = { kind = "Carcass of Rat", quantity = 5 } },
            in_progress = { text = "PlagueDoctorQuest_1_1_InProgress" },
            reward = { item = { kind = "RatArmor", quantity = 1 } },
            epilogue = { text = "PlagueDoctorQuest_1_1_Epilogue" }
        },
        {
            prologue = { text = 'PlagueDoctorQuest_1_2_Prologue' },
            requirements = { item = { kind = "Carcass of Snail", quantity = 10 } },
            in_progress = { text = "PlagueDoctorQuest_1_2_InProgress" },
            reward = { item = { kind = "Gold", quantity = 50 } },
            epilogue = { text = "PlagueDoctorQuest_1_2_Epilogue" }
        }
    },
    {
        {
            prologue = { text = 'PlagueDoctorQuest_2_1_Prologue' },
            requirements = { item = { kind = "Moongrace.Seed", quantity = 2 } },
            in_progress = { text = "PlagueDoctorQuest_2_1_InProgress" },
            reward = { item = { kind = "PotionOfMana", quantity = 1 } },
            epilogue = { text = "PlagueDoctorQuest_2_1_Epilogue" }
        },
        {
            prologue = { text = 'PlagueDoctorQuest_2_2_Prologue' },
            requirements = { item = { kind = "Sungrass.Seed", quantity = 2 } },
            in_progress = { text = "PlagueDoctorQuest_2_2_InProgress" },
            reward = { item = { kind = "PotionOfHealing", quantity = 1 } },
            epilogue = { text = "PlagueDoctorQuest_2_2_Epilogue" }
        }
    },
    {
        {
            prologue = { text = 'PlagueDoctorQuest_3_1_Prologue' },
            requirements = { mob = { kind = "Bat", quantity = 1 } },
            in_progress = { text = "PlagueDoctorQuest_3_1_InProgress" },
            reward = { item = { kind = "ScrollOfUpgrade", quantity = 1 } },
            epilogue = { text = "PlagueDoctorQuest_3_1_Epilogue" }
        },
        {
            prologue = { text = 'PlagueDoctorQuest_3_2_Prologue' },
            requirements = { mob = { kind = "DeathKnight", quantity = 1 } },
            in_progress = { text = "PlagueDoctorQuest_3_2_InProgress" },
            reward = { item = { kind = "PotionOfStrength", quantity = 1 } },
            epilogue = { text = "PlagueDoctorQuest_3_2_Epilogue" }
        }
    },
    {
        {
            prologue = { text = 'PlagueDoctorQuest_4_1_Prologue' },
            requirements = { item = { kind = "Carcass of Warlock", quantity = 5 } },
            in_progress = { text = "PlagueDoctorQuest_4_1_InProgress" },
            reward = { item = { kind = "PotionOfMight", quantity = 1 } },
            epilogue = { text = "PlagueDoctorQuest_4_1_Epilogue" }
        },
        {
            prologue = { text = 'PlagueDoctorQuest_4_2_Prologue' },
            requirements = { item = { kind = "Carcass of KoboldIcemancer", quantity = 5 } },
            in_progress = { text = "PlagueDoctorQuest_4_2_InProgress" },
            reward = { item = { kind = "PotionOfMight", quantity = 1 } },
            epilogue = { text = "PlagueDoctorQuest_4_2_Epilogue" }
        }
    },
    {
        {
            prologue = { text = 'PlagueDoctorQuest_5_1_Prologue' },
            requirements = { mob = { kind = "Succubus", quantity = 2 } },
            in_progress = { text = "PlagueDoctorQuest_5_1_InProgress" },
            reward = { special = {} },
            epilogue = { text = "PlagueDoctorQuest_5_1_Epilogue" }
        }
    }

}

return mob.init({
    interact = function(self, chr)
        local data = mob.restoreData(self)

        local questIndex = data["questIndex"]
        local questVariant = data["questVariant"]

        if data['needToGiveSpecialReward'] then
            local npc = luajava.bindClass("com.nyrds.pixeldungeon.mobs.npc.PlagueDoctorNPC")
            npc:questCompleted()

            data['needToGiveSpecialReward'] = false
            mob.storeData(self, data)
            return
        end

        if data["questIndex"] > #questList then
            RPD.showQuestWindow(self, "PlagueDoctorQuest_AllDone")
            return
        end

        if not data["questInProgress"] then
            questVariant = math.random(1, #questList[questIndex])
            RPD.debug("Quest "..questIndex.." "..questVariant.." "..#questList[questIndex])

            RPD.showQuestWindow(self, questList[questIndex][questVariant].prologue.text)

            data["questInProgress"] = true
            data["questVariant"] = questVariant

            mob.storeData(self, data)
            return
        else
            local quest = questList[questIndex][questVariant]

            local function giveReward()
                local rewardItem = quest.reward.item
                if rewardItem then
                    local reward = RPD.item(rewardItem.kind, rewardItem.quantity)
                    chr:collectAnimated(reward)
                end

                if quest.reward.special then
                    data['needToGiveSpecialReward'] = true
                end

                RPD.showQuestWindow(self, quest.epilogue.text)

                data["questInProgress"] = false
                data["questIndex"] = questIndex + 1
                mob.storeData(self, data)


            end

            local function inProgress()
                RPD.showQuestWindow(self, quest.in_progress.text)
            end

            local requirements = quest.requirements

            if requirements.item then

                local itemDesc = requirements.item
                local wantedItem = chr:checkItem(itemDesc.kind)
                local wantedQty = itemDesc.quantity
                local actualQty = wantedItem:quantity()

                if actualQty >= wantedQty then
                    if wantedQty == actualQty then
                        wantedItem:removeItem()
                    else
                        wantedItem:quantity(actualQty - wantedQty)
                    end

                    return giveReward()
                end
                return inProgress()
            end

            if requirements.mob then
                local wantedMob = requirements.mob.kind
                local wantedQty = requirements.mob.quantity

                local pets = chr:getPets_l()

                local actualQty = 0

                for _, mob in pairs(pets) do
                    if mob:getEntityKind() == wantedMob then
                        actualQty = actualQty + 1
                    end
                end

                if actualQty >= wantedQty then

                    for _, mob in pairs(pets) do
                        if mob:getEntityKind() == wantedMob and wantedQty > 0 then
                            wantedQty = wantedQty - 1
                            mob:makePet(self)
                        end
                    end

                    return giveReward()
                end
                return inProgress()
            end
        end
    end,

    spawn = function(self, level)
        level:setCompassTarget(self:getPos())
        local data = mob.restoreData(self)
        local questIndex = data["questIndex"]
        if not questIndex then
            data["questIndex"] = 1
            data["questInProgress"] = false
            mob.storeData(self, data)
        end
    end
})
