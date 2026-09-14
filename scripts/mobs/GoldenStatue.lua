local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java GoldenStatue (batch 14): base-statue combat stats (depth-scaled
-- here, same formulas), but its weapon is always a GoldenSword upgraded
-- +4, never rolled. STR is NOT gear-derived (java never touched it).
-- gearGranted one-shot is a deliberate delta: java re-granted a fresh
-- GoldenSword+4 whenever the slot emptied - the same endless-refill farm
-- the one-shot flag killed on the enslaved statue.

local function depthScaled(self, data)
    local depth = RPD.Dungeon.depth

    self:setBaseDefenseSkill(4 + depth)
    self:setBaseAttackSkill(4 + depth)
    self:setDmgMin(math.floor(depth / 4) + 1)
    self:setDmgMax(depth)
    self:ht(15 + depth * 5)

    if not data.born then
        data.born = true
        self:hp(self:ht())
    end
end

return mob.init{
    stats = function(self)
        local data = mob.restoreData(self)

        depthScaled(self, data)

        if data.gearGranted then
            return
        end

        local slot = self:getBelongings():getItemFromSlot(RPD.Slot.WEAPON)
        if slot:valid() then
            -- pre-migration saves carry their java-granted sword already
            data.gearGranted = true
            return
        end
        data.gearGranted = true

        local weapon = RPD.ItemFactory:itemByName("GoldenSword")
        weapon:identify()
        weapon:upgrade(4)
        self:getBelongings():setItemForSlot(weapon, RPD.Slot.WEAPON)
    end,

    act = function(self)
        if not self:isPet() and RPD.CharUtils:isVisible(self) then
            RPD.Journal:add(RPD.textById("Journal_Statue"))
        end
    end,

    destroy = function(self)
        RPD.Journal:remove(RPD.textById("Journal_Statue"))
    end,

    desc = function(self)
        local item = self:getBelongings():getItemFromSlot(RPD.Slot.WEAPON)
        if not item:valid() then
            return self:name()
        end
        return RPD.format("GoldenStatue_Desc", item:name())
    end
}
