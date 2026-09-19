---
--- Portal gates, data-served; "target" placement = sender, else receiver.
--- WndPortal confirm calls the "useUp" hook via CustomObject.runScript.
---

local RPD = require "scripts/lib/commonClasses"

local object = require "scripts/lib/object"


local function storeState(self)
    -- restoreData returns the live table; persist mutations made by helpers
    self:storeData(self:restoreData())
end

local function activate(self, object)
    local st = self:restoreData()
    if st.animating or st.activated then
        return
    end
    st.animating = true
    storeState(self)
    object:playObjectAnim("activation", "onActivated")
end

local function openTravelWindow(self, object, hero, st, target)
    local window
    if st.isSender then
        window = RPD.Objects.Ui.WndPortal
    else
        window = RPD.Objects.Ui.WndPortalReturn
    end
    RPD.GameScene:show(luajava.newInstance(window, object, hero, target))
end

return object.init{

    init = function(self, object, level, data, json)
        local st = self:restoreData()

        if json ~= nil and json:has("uses") then
            st.uses = json:getInt("uses")
            st.infiniteUses = false
        else
            st.infiniteUses = true
        end

        if json ~= nil and json:has("target") then
            local t = json:getJSONObject("target")
            st.targetLevelId = t:optString("levelId", "1")
            st.targetX = t:optInt("x", 1)
            st.targetY = t:optInt("y", 1)
            st.isSender = true
        end

        self:storeData(st)
    end,

    interactive = function(self, object)
        return true
    end,

    stepOn = function(self, object, hero)
        return false
    end,

    nonPassable = function(self, object, ch)
        return true
    end,

    bump = function(self, object, presser)
        if not RPD.ItemUtils:isItem(presser) then
            return
        end
        RPD.ItemUtils:throwItemAway(object:getPos())
    end,

    interact = function(self, object, hero)
        local st = self:restoreData()

        if st.isSender then
            if st.used or hero:getBelongings():getItem("Amulet") ~= nil then
                RPD.GLog:w(RPD.textById("PortalGate_Used"), {})
                return false
            end
            if not st.activated then
                activate(self, object)
                return false
            end
            if not st.animating then
                openTravelWindow(self, object, hero, st,
                    RPD.newPosition(st.targetLevelId, st.targetX, st.targetY))
            end
            return false
        end

        local portalPos = hero.portalLevelPos
        if st.used
                or hero:getBelongings():getItem("Amulet") ~= nil
                or portalPos == nil
                or portalPos:equals(object:getPosition()) then
            RPD.GLog:w(RPD.textById("PortalGate_Used"), {})
            return false
        end
        if not st.activated then
            activate(self, object)
            return false
        end
        if not st.animating then
            openTravelWindow(self, object, hero, st, portalPos)
        end
        return false
    end,

    onActivated = function(self, object)
        local st = self:restoreData()
        st.animating = false
        st.activated = true
        storeState(self)
        object:playObjectAnim("activatedLoop", nil)
        RPD.GLog:w(RPD.textById("PortalGate_Activated"), {})
    end,

    -- java-dispatched by WndPortal on confirm
    useUp = function(self, object)
        local st = self:restoreData()
        if not st.infiniteUses then
            st.uses = (st.uses or 0) - 1
            if st.uses < 1 then
                st.used = true
            end
        end
        storeState(self)
    end,

    resetVisualState = function(self, object)
        local st = self:restoreData()
        if st.activated then
            object:playObjectAnim("activatedLoop", nil)
        end
    end,

    info = function(self, object, level)
        local st = self:restoreData()
        if st.activated then
            return RPD.textById("PortalGate_Desc_Activated")
        end
        return RPD.textById("PortalGate_Desc")
    end,

    name = function(self, object, level)
        return RPD.textById("PortalGate_Name")
    end
}
