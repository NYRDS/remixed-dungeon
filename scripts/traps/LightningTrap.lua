local RPD = require "scripts/lib/commonClasses"
local trap = require "scripts/lib/trap"

-- java LightningTrap: shock whoever is on the cell for a fraction of their hp,
-- shake the camera and drain wands when the hero is hit, custom death report
-- when it kills (Electricity is not a Doom source - without the explicit
-- fail the rankings cause would be lost), two crossing bolt visuals.
return trap.init(
    function (cell, char, data)
        if char == nil then
            char = RPD.Actor:findChar(cell)
        end
        if char ~= nil then
            local hp = char:hp()
            -- java Random.Int(a,b) is [a,b)
            local dmg = math.max(1, RPD.Random:Int(math.floor(hp / 3), math.floor(2 * hp / 3)))
            char:damage(dmg, luajava.new(RPD.Electricity))
            if char:getEntityKind() == "Hero" then
                RPD.shakeCamera(2, 0.3)
                if not char:isAlive() then
                    RPD.Dungeon:fail(RPD.JavaUtils:format(
                            RPD.ResultDescriptions:getDescription(RPD.ResultReason.TRAP),
                            { RPD.textById("LightningTrap_Name"), RPD.Dungeon.depth }))
                    RPD.glogn(RPD.textById("LightningTrap_Desc"))
                else
                    char:getBelongings():charge(false)
                end
            end
            RPD.Lightning:spawnBolts(cell)
        end

        RPD.Sfx.CellEmitter:center(cell):burst(RPD.Sfx.SparkParticle.FACTORY, RPD.Random:IntRange(3, 4))
    end
)
