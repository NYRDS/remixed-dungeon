---
--- SpiderCharm: smash it to bleed and call a pet spider servant
--- (was items/SpiderCharm.java, a UsableArtifact).
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"

local AC_USE = "SpiderCharm_Use"

return itemLib.init{
    desc = function()
        return {
            image     = 146, -- ItemSpriteSheet.SPIDER_CHARM
            imageFile = "items.png",
            name      = "SpiderCharm_Name",
            info      = "SpiderCharm_Info",
            price     = 0,
        }
    end,

    glowing = function(self, item)
        return itemLib.makeGlowing(0xFFFFFF, 1) -- was Glowing(0xFFFFFF) = period 1
    end,

    actions = function(self, item)
        return { AC_USE }
    end,

    execute = function(self, item, ch, action)
        if action ~= AC_USE then
            return
        end

        RPD.Sfx.Wound:hit(ch)
        ch:damage(math.floor(ch:ht() / 4), item)
        RPD.removeBuff(ch, "Health") -- BuffFactory.HEALTH

        local level = ch:level()
        local spawnPos = level:getEmptyCellNextTo(ch:getPos())

        if level:cellValid(spawnPos) then
            local pet = RPD.Mob:makePet(RPD.MobFactory:mobByName("SpiderServant"), ch:getId())
            pet:setPos(spawnPos)

            level:spawnMob(pet)
            RPD.ItemUtils:pushMob(pet, ch:getPos(), pet:getPos())
        end
    end,
}
