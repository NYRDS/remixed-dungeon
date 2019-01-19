--[[
    Created by mike.
    DateTime: 19.01.19 21:24
    This file is part of pixel-dungeon-remix
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

local npc
local client

local lesserPrice
local greatPrice

local dialog = function(index)
    if index == 0 then
        if client:gold() >= lesserPrice then
            client:spendGold(lesserPrice)
            RPD.affectBuff(client,RPD.Buffs.Blessed,100)
            return
        end
        npc:say("Bishop_no_money")
    end

    if index == 1 then
        if client:gold() >= greatPrice then
            client:spendGold(greatPrice)
            RPD.affectBuff(client,RPD.Buffs.Blessed,500)
            RPD.affectBuff(client,RPD.Buffs.Blessed,500)
            return
        end
        npc:say("Bishop_no_money")
    end

    if index == 2 then
        npc:say("Bishop_bye")
    end
end


return mob.init({
    interact = function(self, chr)
        client = chr
        npc = self

        local priceFactor = RPD.RemixedDungeon:getDifficultyFactor() * math.pow(client:lvl(), 1.1)

        lesserPrice = 100 * priceFactor
        greatPrice  = 500 * priceFactor

        RPD.chooseOption( dialog,
                "Bishop_title",
                "Bishop_text",
                RPD.textById("Bishop_lesser_bless"):format(lesserPrice),
                RPD.textById("Bishop_great_bless"):format(greatPrice),
                "Bishop_not_interested"
        )
    end
})
