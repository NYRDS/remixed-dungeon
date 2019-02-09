--
-- User: mike
-- Date: 11.06.2018
-- Time: 23:33
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"


return spell.init{
    desc  = function ()
        return {
            image         = 3,
            imageFile     = "spellsIcons/necromancy.png",
            name          = "Exhumation_Name",
            info          = "Exhumation_Info",
            magicAffinity = "Necromancy",
            targetingType = "cell",
            level         = 2,
            castTime      = 2,
            spellCost     = 10
        }
    end,
    castOnCell = function(self, spell, chr, cell)
        local level = RPD.Dungeon.level

        local heap = level:getHeap(cell)

        if heap == nil then
            RPD.glogn("Exhumation_NoGrave")
            return false
        end

        if heap.type == RPD.Heap.Type.TOMB or heap.type == RPD.Heap.Type.SKELETON then
            heap:open(chr)
            local p = chr:getPos()
            local cellToCheck = {p+1, p-1, p+level:getWidth(), p-level:getWidth() }

            for k,v in pairs(cellToCheck) do
                local soul = RPD.Actor:findChar(v)
                if soul ~=nil and soul:getMobClassName() == "Wraith" then
                    if math.random() > 1/(chr:magicLvl() + 1 ) then
                        RPD.Mob:makePet(soul, chr)
                        soul:say("Exhumation_Ok")
                    end
                end
            end

            return true
        end

        RPD.glogn("Exhumation_NoGrave")
        return false

    end
}
