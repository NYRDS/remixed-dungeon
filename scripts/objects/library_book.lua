---
--- LibraryBook, data-served (batch 21). Opens the library window; does not
--- burn (java super.burn was a no-op). Not walkable-over (stepOn false).
---

local RPD = require "scripts/lib/commonClasses"

local object = require "scripts/lib/object"


return object.init{

    interactive = function(self, object)
        return true
    end,

    interact = function(self, object, hero)
        RPD.GameScene:show(luajava.newInstance(RPD.Objects.Ui.WndLibrary))
        return false
    end,

    stepOn = function(self, object, hero)
        return false
    end
}
