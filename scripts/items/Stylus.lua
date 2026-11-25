---
--- Stylus: inscribes armor with a fresh random glyph, or turns a blank
--- scroll into a random one (was items/Stylus.java).
---
local RPD     = require "scripts/lib/commonClasses"
local itemSelector = require "scripts/lib/itemSelector"
local itemLib = require "scripts/lib/item"

local AC_INSCRIBE = "Stylus_ACInscribe"

return itemLib.init{
    desc = function()
        return {
            image     = 80, -- ItemSpriteSheet.STYLUS
            imageFile = "items.png",
            name      = "Stylus_Name",
            info      = "Stylus_Info",
            stackable = true,
            price     = 50,
        }
    end,

    actions = function(self, item)
        return { AC_INSCRIBE }
    end,

    execute = function(self, item, chr, action)
        if action == AC_INSCRIBE then
            itemSelector.selectItem(function(selected, selector)
                if selected == nil then
                    return
                end

                -- the inscribe effect (detach stylus, operate, purple sparks,
                -- burning sound) runs once something was actually inscribed
                local function inscribeEffect()
                    item:detach(item:getOwner():getBelongings().backpack)
                    item:getOwner():doOperate(2) -- TIME_TO_INSCRIBE
                    item:getOwner():getSprite():centerEmitter():start(
                        RPD.Sfx.PurpleParticle.BURST, 0.05, 10)
                    RPD.playSound("snd_burning")
                end

                if RPD.ItemUtils:inscribeRandomGlyph(selected) then
                    inscribeEffect()
                elseif selected:getEntityKind() == "BlankScroll" then
                    selected:detach(item:getOwner():getBelongings().backpack)

                    local inscribed = RPD.Scroll.createRandomScroll()
                    selector:collect(inscribed)
                    selector:itemPickedUp(inscribed)

                    inscribeEffect()
                end
            end, RPD.BackpackMode.INSCRIBABLE, RPD.textById("Stylus_SelectArmor"), chr)
        end
    end,
}
