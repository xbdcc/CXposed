package com.carlos.cposed

import com.carlos.cposed.util.LogUtils
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Created by Carlos on 2019/1/22.
 */
class TargetHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName.equals("com.carlos.box")) {
            XposedBridge.log("Xposed启动了box软件${lpparam.packageName}" )
            LogUtils.d("Xposed启动了box软件${lpparam.packageName}")
        }
    }

}