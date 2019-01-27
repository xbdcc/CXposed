package com.carlos.cposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 *                             _ooOoo_
 *                            o8888888o
 *                            88" . "88
 *                            (| -_- |)
 *                            O\  =  /O
 *                         ____/`---'\____
 *                       .'  \\|     |//  `.
 *                      /  \\|||  :  |||//  \
 *                     /  _||||| -:- |||||-  \
 *                     |   | \\\  -  /// |   |
 *                     | \_|  ''\---/''  |   |
 *                     \  .-\__  `-`  ___/-. /
 *                   ___`. .'  /--.--\  `. . __
 *                ."" '<  `.___\_<|>_/___.'  >'"".
 *               | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *               \  \ `-.   \_ __\ /__ _/   .-` /  /
 *          ======`-.____`-.___\_____/___.-`____.-'======
 *                             `=---='
 *          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 *                     佛祖保佑        永无BUG
 *            佛曰:
 *                   写字楼里写字间，写字间里程序员；
 *                   程序人员写程序，又拿程序换酒钱。
 *                   酒醒只在网上坐，酒醉还来网下眠；
 *                   酒醉酒醒日复日，网上网下年复年。
 *                   但愿老死电脑间，不愿鞠躬老板前；
 *                   奔驰宝马贵者趣，公交自行程序员。
 *                   别人笑我忒疯癫，我笑自己命太贱；
 *                   不见满街漂亮妹，哪个归得程序员？
*/

/**
 * Created by Carlos on 2019/1/25.
 * Change the total money and yesterday profit in wealth tab.
 * Test in Alipay version 10.1.55.
 * Refer to artic:https://juejin.im/post/5c4531c451882524ff640bbc.
 */
class AlipayHook : IXposedHookLoadPackage {

    private val packageName = "com.eg.android.AlipayGphone"
    private val TAG = "AlipayHook-"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (packageName == lpparam.packageName) {
            XposedBridge.log("${TAG}Load Alipay app.")
            val classLoader = lpparam.classLoader
            val hookClass = classLoader.loadClass("com.alipay.android.render.engine.viewbiz.AssetsHeaderV2View")
            val assetsCardModel = classLoader.loadClass("com.alipay.android.render.engine.model.AssetsCardModel")

            dumpClass(hookClass)

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
                        XposedBridge.log("This is dividing line.")
                        XposedBridge.log("----------------------------------------------")
                        XposedBridge.log("Start to change data.")
                        val data = param.args[0]
                        XposedBridge.log( "${TAG}data:"+data.javaClass.getField("latestTotalView").get(data).toString())
                        XposedBridge.log("${TAG}data:"+data.javaClass.getField("totalYesterdayProfitView").get(data).toString())

                        data.javaClass.getField("latestTotalView").set(data, "36162848.69")
                        data.javaClass.getField("totalYesterdayProfitView").set(data, "1003.81")

                    }
                })

        }
    }

    /**
     * Pring all fileds,methods and classes in class.
     */
    private fun dumpClass(cClass: Class<*>) {
        XposedBridge.log("${TAG}Fileds:")
        val cFileds = cClass.declaredFields
        for (filed in cFileds) {
            XposedBridge.log(TAG +filed.toString())
        }

        XposedBridge.log("${TAG}Methods:")
        val cMethods = cClass.declaredMethods
        for (method in cMethods) {
            XposedBridge.log(TAG + method.toString())
        }

        XposedBridge.log("${TAG}Classes:")
        val childClass = cClass.declaredClasses
        for (cChildClass in childClass) {
            XposedBridge.log(TAG +cChildClass.toString())
        }
    }
}