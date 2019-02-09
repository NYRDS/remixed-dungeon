--[[
    Created by mike.
    DateTime: 23.08.18 23:18
    This file is part of Remixed Pixel Dungeon
]]

local ai = {}

ai.__index = ai

function ai.act(self, me)
    return true
end

function ai.gotDamage(self, me, src, dmg)
end

function ai.status(self, me)
    return "ai status is niy"
end

ai.init = function(desc)
    setmetatable(desc, ai)

    return desc
end

return ai