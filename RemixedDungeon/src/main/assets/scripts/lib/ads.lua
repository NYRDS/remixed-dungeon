---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by mike.
--- DateTime: 10/17/21 12:13 PM
---

rewardVideo =  luajava.newInstance("com.nyrds.pixeldungeon.support.AdsRewardVideo")
interstitial = luajava.newInstance("com.nyrds.pixeldungeon.support.AdsInterstitial")

ads = {
    rewardVideoReady = function()
        return rewardVideo:isReady()
    end,

    rewardVideoShow = function(prize)
        rewardVideo:show(prize)
    end,

    interstitialShow = function() --may fail to show it
        interstitial:show()
    end
}


return ads
