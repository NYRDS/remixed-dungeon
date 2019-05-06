-- This is demo buff for charAct

local RPD  = require "scripts/lib/commonClasses"

local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 46,
            name          = "CounterBuff_Name",
            info          = "CounterBuff_Info",
        }
    end,

    attachTo = function(self, buff, target)
        self.counter = 0
        return true
    end,

    charAct = function(self,buff)
        self.counter = self.counter + 1
        buff.target:getSprite():showStatus( 0xFF00FF, tostring(self.counter))
    end
}