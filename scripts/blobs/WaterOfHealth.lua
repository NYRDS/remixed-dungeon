---
--- The well of health: a full heal + uncurse + feed for the drinking hero;
--- a not-full DewVial dropped in gets refilled. Was WaterOfHealth.java.
---
local RPD   = require "scripts/lib/commonClasses"
local well  = require "scripts/lib/wellbase"

local Potion = luajava.bindClass("com.watabou.pixeldungeon.items.potions.Potion")

return well.init{
    desc = function()
        return {
            mode     = "well",
            tileDesc = "WaterOfHealth_Info",
        }
    end,

    after = function(self, water)
        if RPD.Dungeon:isCellVisible(water:pos()) then
            RPD.Journal:add(RPD.Journal.Feature.WELL_OF_HEALTH:desc())
        end
    end,

    affectHero = function(self, water, hero)
        RPD.playSound("snd_drink")
        Potion:heal(hero, 1)
        hero:getBelongings():uncurseEquipped()
        hero:hunger():satisfy(RPD.Buffs.Hunger.STARVING)
        RPD.Sfx.CellEmitter:get(water:pos()):start(RPD.Sfx.ShaftParticle.FACTORY, 0.2, 3)
        hero:interrupt()
        RPD.glogp(RPD.textById("WaterOfHealth_Procced"))
        return true
    end,

    affectItem = function(self, water, item)
        if item:getEntityKind() == "DewVial" and not item:itemBooleanHook("isFull", false) then
            item:itemHook("fill")
            return item
        end
        return nil
    end,
}
