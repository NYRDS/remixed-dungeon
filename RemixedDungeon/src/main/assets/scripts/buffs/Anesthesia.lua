local RPD  = require "scripts/lib/commonClasses"

local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 26, -- Using a different icon number
            name          = "AnesthesiaBuff_Name",
            info          = "AnesthesiaBuff_Info",
        }
    end,

    -- This function is called when the buff is attached to a character
    attachTo = function(self, buff, target)
        return true -- Allow the buff to be attached
    end,

    -- This function is called when damage is received by the character
    damage = function(self, buff, damage, src)
        -- This doesn't change the damage value but marks that this damage shouldn't wake the character
        -- The actual prevention of waking up is handled by the modified Sleeping AI
        return damage
    end
}