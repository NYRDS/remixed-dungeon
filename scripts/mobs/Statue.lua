local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Statue (batch 13): passive animated statue, depth-scaled stats, rolls
-- one melee weapon from the treasury into its weapon slot at spawn. The
-- sprite is the hero-layers statue build (json heroSprite flag) carrying the
-- rolled weapon. gearGranted is a one-shot flag: no endless refill after the
-- item leaves the slot (the enslaved statue was an item farm).
--
-- depth-scaled values are re-derived on every load (restore re-applies json
-- fallbacks first, then this runs; saved hp is applied from the bundle over
-- anything the ctor-time fillStats did).

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
            -- pre-migration saves carry their java-rolled weapon already
            data.gearGranted = true
            return
        end
        data.gearGranted = true

        local weapon = RPD.Treasury:getLevelTreasury():random("WEAPON")
        while not RPD.ItemUtils:statueWeaponCandidate(weapon) do
            weapon = RPD.Treasury:getLevelTreasury():random("WEAPON")
        end

        weapon:identify()
        RPD.ItemUtils:enchantStatueWeapon(weapon)
        self:getBelongings():setItemForSlot(weapon, RPD.Slot.WEAPON)
        self:STR(math.max(12, weapon:requiredSTR()))
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
            -- naked enslaved statue: gearGranted blocks refill, plain name
            return self:name()
        end
        return RPD.format("Statue_Desc", item:name())
    end
}
