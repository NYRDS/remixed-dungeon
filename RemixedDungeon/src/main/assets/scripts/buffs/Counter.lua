-- This is demo buff for charAct

local RPD  = require "scripts/lib/commonClasses"

local buff = require "scripts/lib/buff"
local storage = require "scripts/lib/storage"

return buff.init{
    desc  = function ()
        return {
            icon          = 46,
            name          = "CounterBuff_Name",
            info          = "CounterBuff_Info",
        }
    end,

    attachTo = function(self, buff, target)
        return true
    end,

    act = function(self,buff)
        buff.target:damage(1, buff)
        buff:level(buff:level()-1)
        buff:spend(1)
        if buff:level() <= 0 then
            buff:detach()
        end
    end,

    charAct = function(self,buff)
        self.data.counter = (self.data.counter or 0) + 1
        print(self.data.counter)
        RPD.debug("counter: %d", self.data.counter)
        storage.gamePut("CounterBuffTest", self.data.counter)
        storage.put("CounterBuffTest", self.data.counter)
        buff.target:getSprite():showStatus( 0xFF00FF, tostring(self.data.counter))
    end
}