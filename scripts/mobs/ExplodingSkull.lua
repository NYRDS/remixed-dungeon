local mob = require "scripts/lib/mob"

return mob.init{
    -- kamikaze: bursts after a successful hit; attackProc only runs on hit,
    -- matching the java attack() override that died on super.attack()==true
    attackProc = function(self, enemy, dmg)
        self:die(self)
        return dmg
    end
}
