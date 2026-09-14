local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java ArmoredStatue (batch 14): the armor-clad statue variant. Static
-- dmg 4-8 (json), def/atk formulas differ from the base statue, rolls one
-- armor from the treasury into its ARMOR slot at spawn (one-shot
-- gearGranted). Sprite = hero-layers statue + worn armor (json heroSprite
-- flag: weapon slot empty -> armor branch).

local function depthScaled(self, data)
    local depth = RPD.Dungeon.depth

    self:setBaseDefenseSkill(4 + depth * 2)
    -- java attackSkill(target) = (9+depth)*2 flat; base attack gains +lvl,
    -- compensate to keep the effective roll identical
    self:setBaseAttackSkill((9 + depth) * 2 - self:lvl())
    -- dmg 4-8 static, authored in json

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

        local slot = self:getBelongings():getItemFromSlot(RPD.Slot.ARMOR)
        if slot:valid() then
            -- pre-migration saves carry their java-rolled armor already
            data.gearGranted = true
            return
        end
        data.gearGranted = true

        local armor = RPD.Treasury:getLevelTreasury():random("ARMOR")
        while not RPD.ItemUtils:statueArmorCandidate(armor) do
            armor = RPD.Treasury:getLevelTreasury():random("ARMOR")
        end

        armor:identify()
        RPD.ItemUtils:inscribeStatueArmor(armor)
        self:getBelongings():setItemForSlot(armor, RPD.Slot.ARMOR)
        self:STR(math.max(12, armor:requiredSTR()))
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
        local item = self:getBelongings():getItemFromSlot(RPD.Slot.ARMOR)
        if not item:valid() then
            return self:name()
        end
        return RPD.format("ArmoredStatue_Desc", item:name())
    end
}
