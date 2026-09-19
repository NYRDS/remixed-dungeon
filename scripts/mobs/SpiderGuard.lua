--
-- SpiderGuard: sungrass medic behavior (see SpiderHealer.lua)
--

local mob = require "scripts/lib/mob"

local healerAct = require "scripts/mobs/SpiderHealer"

return mob.init{
	act = healerAct.act
}
