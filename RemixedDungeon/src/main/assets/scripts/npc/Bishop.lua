--[[
    Created by mike.
    DateTime: 19.01.19 21:24
    This file is part of pixel-dungeon-remix
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

local npc
local client

local dialog = function(index)
    if index == 0 then
        RPD.affectBuff(client,RPD.Buffs.Blessed,100)
    end

    if index == 1 then
        RPD.affectBuff(client,RPD.Buffs.Blessed,500)
        RPD.affectBuff(client,RPD.Buffs.Blessed,500)
    end

    if index == 2 then
        npc:say(Bishop_bye)
    end
end


return mob.init({
    interact = function(self, chr)
        client = chr
        npc = self
        RPD.chooseOption( dialog,
                "Bishop_title",
                "Bishop_text",
                "Bishop_lesser_bless",
                "Bishop_great_bless",
                "Bishop_not_interested"
        )
    end
})
