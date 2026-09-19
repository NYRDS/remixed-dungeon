---
--- Sign, data-served; text rides the object's "data" field.
---

local RPD = require "scripts/lib/commonClasses"

local object = require "scripts/lib/object"


return object.init{

    interactive = function(self, object)
        return true
    end,

    stepOn = function(self, object, hero)
        return true
    end,

    interact = function(self, object, hero)
        if not hero:getHeroClass():forbidden(RPD.CommonActions.AC_READ) then
            if hero:hasBuff("Blindness") then
                RPD.GLog:w(RPD.textById("Codex_Blinded"), {})
            else
                RPD.GameScene:show(luajava.newInstance(RPD.Objects.Ui.WndMessage, object:getData()))
            end
        else
            RPD.GameScene:show(luajava.newInstance(RPD.Objects.Ui.WndMessage, RPD.textById("Sign_CantRead")))
        end
        return true
    end,

    bump = function(self, object, presser)
        RPD.ItemUtils:throwItemAway(object:getPos())
    end,

    burn = function(self, object)
        local pos = object:getPos()
        object:remove()
        object:level():set(pos, RPD.Terrain.EMBERS)
        RPD.GameScene:discoverTile(pos)
    end,

    image = function(self, object, level)
        return 16 * 1 + level:objectsKind()
    end,

    name = function(self, object, level)
        return level:tileName(RPD.Terrain.SIGN)
    end,

    info = function(self, object, level)
        return level:tileDesc(RPD.Terrain.SIGN)
    end
}
