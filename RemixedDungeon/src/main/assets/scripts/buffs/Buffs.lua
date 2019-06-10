--
-- User: mike
-- Date: 24.03.2019
-- Time: 20:59
-- This file is part of Remixed Pixel Dungeon.
--

local buffs = require "scripts/buffs/CustomBuffsList"

local module = {}

function module.getBuffsList()
    return buffs or {}
end

function module.haveBuff(self,buff)
    for _,v in pairs(buffs) do
        if buff == v then
            return true
        end
    end
    return false
end

function module.loadBuffs()
    for _,buff in pairs(buffs) do
        require("scripts/buffs/"..buff)
    end
end

return module