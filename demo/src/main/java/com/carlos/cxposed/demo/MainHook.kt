package com.carlos.cxposed.demo

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.TextView
import de.robv.android.xposed.*
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
 * Github: https://github.com/xbdcc/.
 * Created by Carlos on 2019/1/22.
 */
class MainHook : IXposedHookLoadPackage {

    private val packageName = "com.carlos.cxposed.demo"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {

        if (packageName == lpparam.packageName) {
            xlog(TAG + "hook an app start:$packageName")

            hookDemoClass(lpparam)

            hookMainActivity(lpparam)
        }
    }

    private fun hookMainActivity(lpparam: XC_LoadPackage.LoadPackageParam) {
        val hookClass = lpparam.classLoader.loadClass("com.carlos.cxposed.demo.MainActivity")

        XposedHelpers.findAndHookMethod(hookClass, "getLog", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                xlog("hook before getLog")
                // 修改方法返回值
                param?.result = "this is a hook message."
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                xlog("hook after getLog")
            }
        })

        XposedHelpers.findAndHookMethod(hookClass, "click", View::class.java,
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam?): Any {
                    xlog("hook replace click")
                    val thisObject = param?.thisObject ?: return ""
                    // 修改textView的显示内容
                    val activity = thisObject as Activity
                    val textView = activity.findViewById<TextView>(R.id.textview)
                    textView.text = "carlos"
                    return ""
                }
            })

    }

    private fun hookDemoClass(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 通过类加载器加载DemoClass类
        val hookClass = lpparam.classLoader.loadClass("com.carlos.cxposed.demo.DemoClass") ?: return
        // 通过XposedHelpers调用静态方法printlnHelloWorld
        XposedHelpers.callStaticMethod(hookClass, "printlnHelloWorld")
        // 获取DemoClass的类对象
        val demoClass = hookClass.newInstance()
        // 获取私有字段name
        val field = hookClass.getDeclaredField("name")
        // 私有字段name访问属性改为公有
        field.isAccessible = true
        // 给字段name赋值为"xbd"
        field.set(demoClass, "xbd")
        // 通过XposedHelpers调用非静态方法printlnName
        XposedHelpers.callMethod(demoClass, "printlnName")
    }

}

const val TAG = "MainHook->"

fun log(string: String) {
    Log.d(TAG, string)
}

fun xlog(string: String) {
    XposedBridge.log(TAG + string)
}
