---
--- TomeOfKnowledge: +1 skill level on read (was books/TomeOfKnowledge.java).
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 1,
            imageFile  = "items/books.png",
            name       = "TomeOfKnowledge_Name",
            info       = "TomeOfKnowledge_Info",
            stackable  = true,
            upgradable = false,
            identified = true,
            price      = 100,
        }
    end,

    actions = function(self, item, hero)
        return { RPD.CommonActions.AC_READ }
    end,

    execute = function(self, item, hero, action)
        if action == RPD.CommonActions.AC_READ then
            if hero:hasBuff("Blindness") then
                RPD.GLog:w(RPD.textById("Codex_Blinded"), {})
                return
            end
            item:detach(hero:getBelongings().backpack)
            RPD.Sfx.SpellSprite:show(hero, RPD.Sfx.SpellSprite.MASTERY)
            hero:getSprite():emitter():burst(RPD.Speck:factory(RPD.Speck.MAGIC), 8)
            hero:showStatus(RPD.CharSprite.BLUE, "+ 1")
            RPD.playSound("snd_read")
            hero:spend(2)
            hero:skillLevelUp()
        end
    end,

    burn = function(self, item, cell)
        return nil
    end,
}
