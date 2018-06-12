--
-- User: mike
-- Date: 03.06.2018
-- Time: 22:51
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

local mob = require "scripts/lib/mob"

local storage = require "scripts/lib/storage"

local latest_kill_index = "__latest_dead_mob"

local function updateLatestDeadMob(mob)
    storage.put(latest_kill_index, {class = mob:getMobClassName(), pos = mob:getPos()})
end

mob.installOnDieCallback(updateLatestDeadMob)

return spell.init{
    desc  = function ()
        return {
            image         = 2,
            imageFile     = "spellsIcons/necromancy.png",
            name          = "Raise Dead",
            info          = [[Attempt to raise latest slain creature.
 When using this dark art remember: almost any creature can be raised, but not everyone will is easy to bend]],
            magicAffinity = "Necromancy",
            targetingType = "none",
            spellCost     = 15,
            castTime      = 3,
            level         = 4
        }
    end,
    cast = function(self, spell, chr)
        local latestDeadMob = storage.get(latest_kill_index) or {}

        if latestDeadMob.class ~= nil then
            local mob = RPD.MobFactory:mobByName(latestDeadMob.class)
            storage.put(latest_kill_index, {})

            local level = RPD.Dungeon.level
            local mobPos = latestDeadMob.pos

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
