---
--- SpellBook: an artifact book holding one spell, read from the artifact
--- slot (was artifacts/SpellBook.java). Starts unidentified; casting the
--- stored spell while the book is equipped reveals it (Spell.java). A fresh
--- book resolves its spell on first use - nil rolls a random Common spell
--- and persists the kind (java spell() parity).
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"

local AC_READ = RPD.Actions.read

return itemLib.init{
    desc = function()
        return {
            image      = 3,
            imageFile  = "items/books.png",
            name       = "SpellBook_Name",
            info       = "SpellBook_Info",
            stackable  = false,
            upgradable = false,
            identified = false,
            price      = 500,
            equipable  = RPD.Slots.artifact,
        }
    end,

    -- raw stored spell kind for the java bridges ("" = never rolled)
    getSpellName = function(self, item)
        return self.data.spell or ""
    end,

    setSpellName = function(self, item, spellKind)
        self.data.spell = spellKind
    end,

    activate = function(self, item, hero)
        item:setDefaultAction(AC_READ)
    end,

    deactivate = function(self, item, hero)
        item:setDefaultAction(RPD.Actions.throw)
    end,

    actions = function(self, item, hero)
        if item:isEquipped(hero) then
            return { AC_READ }
        end
        return {}
    end,

    execute = function(self, item, chr, action)
        if action ~= AC_READ then
            return
        end
        local spell = RPD.ItemUtils:spellBookResolveSpell(item)
        chr:nextAction(luajava.newInstance(RPD.Objects.Actions.UseSpell, spell))
    end,

    name = function(self, item)
        local base = RPD.textById("SpellBook_Name")
        if item:isIdentified() then
            local spell = RPD.ItemUtils:spellBookResolveSpell(item)
            return string.format("%s (%s)", base, spell:name())
        end
        return base
    end,

    info = function(self, item)
        if item:isIdentified() then
            local spell = RPD.ItemUtils:spellBookResolveSpell(item)
            return RPD.format(RPD.textById("SpellBook_Info_Identified"), spell:name(), spell:desc())
        end
        return RPD.textById("SpellBook_Info")
    end,

    -- fireproof
    burn = function(self, item, cell)
        return nil
    end,

    bag = function(self, item)
        return "ScrollHolder"
    end,

    loadLegacy = function(self, item, bundle)
        -- pre-migration @Packable spell (null until first resolution)
        local spellKind = bundle:optString("spell", "")
        if spellKind ~= "" then
            self.data.spell = spellKind
        end
    end,
}
