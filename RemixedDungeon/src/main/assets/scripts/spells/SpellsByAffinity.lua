--
-- User: mike
-- Date: 02.06.2018
-- Time: 20:59
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local spells = {}
spells["Necromancy"] = {"DarkSacrifice","RaiseDead","Exhumation","Possess"}
--spells["Common"] = {"Bless"}

local module = {}

function module.getSpellsList(self,affinity)
    local ret = spells[affinity] or {}
    return ret
end

function module.haveSpell(self,spell)
    for k,v in pairs(spells) do
        for kk, vv in pairs(v) do
            if spell == vv then
                return true
            end
        end
    end
    return false
end

function module.loadSpells()
    require "scripts/spells/RaiseDead"
    require "scripts/spells/DarkSacrifice"
    require "scripts.spells.Exhumation"
end

return module