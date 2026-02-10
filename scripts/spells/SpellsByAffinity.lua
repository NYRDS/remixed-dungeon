--
-- User: mike
-- Date: 02.06.2018
-- Time: 20:59
-- This file is part of Remixed Pixel Dungeon.
--

local spells = require "scripts/spells/CustomSpellsList"

local RPD = require "scripts.lib.commonClasses"

local module = {}

function module.getSpellsList(self,affinity)

    if affinity then
        if not spells[affinity] then
            RPD.debug("Missing spells list for ".. affinity)
        end

        return spells[affinity] or {}
    end

    local ret = {}

    for _,  aff in pairs(spells) do
        for _,spell in pairs(aff) do
            table.insert(ret,spell)
        end
    end
    return ret
end

function module.haveSpell(self,spell)
    for _,v in pairs(spells) do
        for _, vv in pairs(v) do
            if spell == vv then
                return true
            end
        end
    end
    return false
end

function module.loadSpells()
    for _,affinity in pairs(spells) do
        for _, spell in pairs(affinity) do
            require("scripts/spells/"..spell)
        end
    end
end

return module