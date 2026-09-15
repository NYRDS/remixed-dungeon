--[[
    mob lua migration batch 16b: replaces java HealerNPC + WndPriest
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

local phrases = {
    "HealerNPC_Message1",
    "HealerNPC_Message2",
    "HealerNPC_Message3"
}

local FEMININE_GENDER = 2

return mob.init({
    interact = function(self, chr)
        local healCost   = RPD.CharUtils:goldPrice(chr, 75)

        local pets       = chr:getPets_l()
        local minionCost = RPD.CharUtils:goldPrice(chr, 50) * #pets

        local function dialog(index)
            if index == 0 then
                if chr:gold() < healCost then
                    return
                end
                chr:spendGold(healCost)
                RPD.CharUtils:healPatient(chr)
                self:say("HealerNPC_Message2")
                return
            end

            if index == 1 then
                if chr:gold() < minionCost then
                    return
                end
                chr:spendGold(minionCost)
                for _, patient in pairs(pets) do
                    RPD.CharUtils:healPatient(patient)
                end
                self:say("HealerNPC_Message2")
                return
            end
        end

        local instruction = "WndPriest_Instruction2_m"
        if chr:getGender() == FEMININE_GENDER then
            instruction = "WndPriest_Instruction2_f"
        end

        local text = RPD.textById(instruction) .. "\n" .. RPD.textById("WndPriest_Instruction2"):format(healCost)

        if #pets > 0 then
            RPD.chooseOption(dialog,
                    self:getName(),
                    text,
                    RPD.textById("WndPriest_Heal"):format(healCost),
                    RPD.textById("WndPriest_Heal_Minions"):format(minionCost),
                    "WndMovieTheatre_No"
            )
        else
            RPD.chooseOption(dialog,
                    self:getName(),
                    text,
                    RPD.textById("WndPriest_Heal"):format(healCost),
                    "WndMovieTheatre_No"
            )
        end
    end,

    act = function(self)
        local hero = RPD.Dungeon.hero
        if not hero then
            return
        end

        if RPD.Dungeon.level:distanceL2(self:getPos(), hero:getPos()) < 4
                and math.random(20) == 1 then
            self:say(phrases[math.random(#phrases)])
        end
    end
})
