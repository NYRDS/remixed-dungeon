--
-- User: mike
-- Date: 03.06.2018
-- Time: 22:51
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

local mob = require "scripts/lib/mob"


local latestDeadMobClass

local function updateLatestDeadMob(mob)
    latestDeadMobClass = mob:getMobClassName()
end

mob.installOnDieCallback(updateLatestDeadMob)

return spell.init{
    desc  = function ()
        return {
            image         = 2,
            imageFile     = "spellsIcons/necromancy.png",
            name          = "Raise Dead",
            info          = "Attempt to raise latest slain creature",
            magicAffinity = "Necromancy",
            targetingType = "none"
        }
    end,
    cast = function(self, spell, chr)
        if latestDeadMobClass ~= nil then
            local mob = RPD.MobFactory:mobByName(latestDeadMobClass)
            latestDeadMobClass = nil

            local level = RPD.Dungeon.level
            local mobPos = level:getEmptyCellNextTo(chr:getPos())
            if level:cellValid(mobPos) then
                mob:setPos(mobPos)
                mob:loot(RPD.ItemFactory:itemByName("Gold"))
                RPD.Mob:makePet(mob, chr)
                level:spawnMob(mob)
                chr:getSprite():emitter():burst( RPD.Sfx.ShadowParticle.CURSE, 6 )
                mob:getSprite():emitter():burst( RPD.Sfx.ShadowParticle.CURSE, 6 )
                RPD.playSound( "snd_cursed.mp3" )

                return true
            else
                RPD.glog("Raise Dead failed because no free space nearby")
                return false
            end
        end

        RPD.glog("You need to kill someone before attempt to raise it")
        return false
    end
}
