---
--- LibraryBook, data-served; opens the library window.
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
