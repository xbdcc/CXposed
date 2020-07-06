package com.carlos.cxposed.old

import android.widget.TextView
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
 * Created by Carlos on 2019/1/27.
 * Change the change and chang wallet in change page.
 * Test in Wechat version 6.7.3.
 */
class Wechat6_7_3Hook : IXposedHookLoadPackage {

    private val packageName = "com.tencent.mm"
    private val TAG = "WechatHook-"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (packageName == lpparam.packageName) {
            XposedBridge.log("${TAG}Load Wechat app.")

            val classLoader = lpparam.classLoader
            val hookClass =
                classLoader.loadClass("com.tencent.mm.plugin.wallet.balance.ui.WalletBalanceManagerUI") ?: return

            dumpClass(hookClass)

            getData(hookClass)

            XposedHelpers.findAndHookMethod(hookClass, "aZ", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("This is dividing line.")
                    XposedBridge.log("----------------------------------------------")
                    XposedBridge.log("Start to change data.")
                    var textview = XposedHelpers.getObjectField(param.thisObject, "qha") as TextView
                    textview.text = "67348723.06"
                    textview = XposedHelpers.getObjectField(param.thisObject, "qhi") as TextView
                    XposedBridge.log("${TAG}data:" + textview.text)
                    textview.text = "-6.66"
                    super.afterHookedMethod(param)
                }
            })
        }
    }

    private fun getData(hookClass: Class<*>) {
        XposedHelpers.findAndHookMethod(hookClass, "onResume", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                XposedBridge.log("This is dividing line.")
                XposedBridge.log("----------------------------------------------")
                XposedBridge.log("Start to get data")
                var textview = XposedHelpers.getObjectField(param.thisObject, "qgx") as TextView
                XposedBridge.log("${TAG}data:" + textview.text)
                // This is amount of money.
                textview = XposedHelpers.getObjectField(param.thisObject, "qha") as TextView
                XposedBridge.log("${TAG}data:" + textview.text)
                textview = XposedHelpers.getObjectField(param.thisObject, "qhe") as TextView
                XposedBridge.log("${TAG}data:" + textview.text)
                textview = XposedHelpers.getObjectField(param.thisObject, "qhh") as TextView
                XposedBridge.log("${TAG}data:" + textview.text)
                // This is amount of money pass .
                textview = XposedHelpers.getObjectField(param.thisObject, "qhi") as TextView
                XposedBridge.log("${TAG}data:" + textview.text)
                super.afterHookedMethod(param)
            }
        })
    }


    private fun dumpClass(cClass: Class<*>) {
        XposedBridge.log("${TAG}Fileds")
        val cFileds = cClass.declaredFields
        for (filed in cFileds) {
            XposedBridge.log(TAG + filed.toString())
        }

        XposedBridge.log("${TAG}Methods")

        val cMethods = cClass.declaredMethods
        for (method in cMethods) {
            XposedBridge.log(TAG + method.toString())
        }

        XposedBridge.log("${TAG}Classes")

        val childClass = cClass.declaredClasses
        for (cChildClass in childClass) {
            XposedBridge.log(TAG + cChildClass.toString())
        }
    }
}