--
-- User: mike
-- Date: 02.06.2018
-- Time: 20:59
-- This file is part of Remixed Pixel Dungeon.
--

local spells = require "scripts/spells/CustomSpellsList"

local module = {}

function module.getSpellsList(self,affinity)
    local ret = spells[affinity] or {}
    return ret
end

function module.haveSpell(self,spell)
    for _,v in pairs(spells) do
        for __, vv in pairs(v) do
            if spell == vv then
                return true
            end
        end
    end
    return false
end

function module.loadSpells()
    for _,affinity in pairs(spells) do
        for __, spell in pairs(affinity) do
            require("scripts/spells/"..spell)
        end
    end
end

return module