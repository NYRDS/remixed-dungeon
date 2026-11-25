---
--- NecromancerArmor: class-armor data kind (was armor/NecromancerArmor.java). The special action id,
--- equip gate and visual flags live in the desc; the special itself is the
--- framework flag ("leaper"/"teleporter"/...) or a hook below.
---
local RPD     = require "scripts/lib/commonClasses"
local NecrotismDuration = 3 -- Necrotism.duration constant
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 22,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "Necrotism_ACSpecial",
            gateClass = "NECROMANCER",
            hasHelmet = true,
        }
    end,
    doSpecial = function(self, item, user)
        -- Necrotism self-buff, one charge (was NecromancerArmor.doSpecial)
        user:doOperate(RPD.Actor:TICK())
        local necro = RPD.affectBuff(user, "Necrotism")
        if necro ~= nil then necro:set(NecrotismDuration, 1) end
        user:getSprite():burst(0x6935a5, 3)
        RPD.playSound("snd_read")
    end,
}
