package com.carlos.cposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Created by Carlos on 2019/1/25.
 * Change total money and yesterday profit in wealth tab.
 * Test in Alipay version 10.1.55
 */
class AlipayHook : IXposedHookLoadPackage {

    private val packageName = "com.eg.android.AlipayGphone"
    private val TAG = "AlipayHook-"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (packageName == lpparam.packageName) {
            XposedBridge.log("${TAG}load Alipay")
            val classLoader = lpparam.classLoader
            val hookClass = classLoader.loadClass("com.alipay.android.render.engine.viewbiz.AssetsHeaderV2View")
            val assetsCardModel = classLoader.loadClass("com.alipay.android.render.engine.model.AssetsCardModel")

            dumpClass(hookClass)

            XposedBridge.log("${TAG}start hook")

            if (hookClass == null) return
            XposedHelpers.findAndHookMethod(
                hookClass,
                "setData",
                assetsCardModel,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        super.beforeHookedMethod(param)
                        XposedBridge.log("this is dividing line.")
                        XposedBridge.log("--------------------------------------------")
                        XposedBridge.log("start change data")
                        val data = param.args[0]
                        XposedBridge.log( "${TAG}latestTotalView:"+data.javaClass.getField("latestTotalView").get(data).toString())
                        XposedBridge.log("${TAG}totalYesterdayProfitView:"+data.javaClass.getField("totalYesterdayProfitView").get(data).toString())
                        XposedBridge.log("${TAG}assetDesc:"+data.javaClass.getField("assetDesc").get(data))
                        XposedBridge.log("${TAG}ext:"+data.javaClass.getField("ext").get(data))
                        XposedBridge.log("${TAG}followAction:"+data.javaClass.getField("followAction").get(data))
                        XposedBridge.log("${TAG}userPic:"+data.javaClass.getField("userPic").get(data))

                        data.javaClass.getField("latestTotalView").set(data, "1000000.88")
                        data.javaClass.getField("totalYesterdayProfitView").set(data, "983.25")

                    }
                })

        }
    }

    /**
     * Pring all fileds,methods and classes in class.
     */
    private fun dumpClass(cClass: Class<*>) {
        XposedBridge.log("${TAG}Fileds")
        val cFileds = cClass.declaredFields
        for (filed in cFileds) {
            XposedBridge.log(TAG +filed.toString())
        }

        XposedBridge.log("${TAG}Methods")
        val cMethods = cClass.declaredMethods
        for (method in cMethods) {
            XposedBridge.log(TAG + method.toString())
        }

        XposedBridge.log("${TAG}Classes")
        val childClass = cClass.declaredClasses
        for (cChildClass in childClass) {
            XposedBridge.log(TAG +cChildClass.toString())
        }
    }
}