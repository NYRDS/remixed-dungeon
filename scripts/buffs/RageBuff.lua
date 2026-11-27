---
--- RageBuff: CorpseDust bloodlust - double damage, self-harm ticks (was
--- mechanics/buffs/RageBuff.java). Artifact-sourced instances stick; bare
--- permanentBuff attaches still self-detached like the java ArtifactBuff.
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 32, -- BuffIndicator.BLOODLUST
            name          = "CorpseDustBuff_Name",
            info          = "CorpseDustBuff_Info",
        }
    end,

    attachTo = function(self, buff, target)
        -- java attachTo: an existing rage means pretend-success, keep the old
        return not target:hasBuff("RageBuff")
    end,

    act = function(self, buff)
        local target = buff.target
        if not target:isAlive() then
            buff:deactivateActor()
            buff:spend(1)
            return
        end

        -- java ArtifactBuff.act: an unsourced artifact buff never sticks
        if buff:getSourceId() == -1 then
            buff:detach()
            return
        end

        if target:hp() > target:ht() / 5 and math.random() < 0.1 then
            target:damage(math.floor(math.random() * 5), buff)
            target:getSprite():emitter():burst(RPD.Sfx.ShadowParticle.CURSE, 6)
            RPD.playSound("snd_cursed")
        end
        buff:spend(1)
    end,

    attackProc = function(self, buff, defender, damage)
        return damage * 2
    end,
}
