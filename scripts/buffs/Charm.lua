---
--- Charm: pacifies the target (was actors/buffs/Charm.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

local CharSprite = luajava.bindClass("com.watabou.pixeldungeon.sprites.CharSprite")

return buff.init{
    desc  = function ()
        return {
            icon          = 21, -- BuffIndicator.HEART
            name          = "CharmBuff_Name",
            info          = "CharmBuff_Info",
        }
    end,

    attachTo = function(self, buff, target)
        return not target:hasBuff("OneWayLoveBuff")
    end,

    attached = function(self, buff)
        local target = buff.target
        if target:isOnStage() then
            target:getSprite():centerEmitter():start(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.HEART), 0.2, 5)
            RPD.playSound("snd_charms")
        end
        target:setPacified(true)
    end,

    detach = function(self, buff)
        buff.target:setPacified(false)
    end
}
