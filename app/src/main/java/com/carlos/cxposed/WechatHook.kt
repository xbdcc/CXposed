package com.carlos.cxposed

import android.app.Activity
import android.view.View
import com.carlos.cxposed.utils.xlog
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
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
 * Github: https://github.com/xbdcc/.
 * Test in Wechat 7.0.16.
 * Created by Carlos on 2020/7/4.
 */
class WechatHook : IXposedHookLoadPackage {

    private val packageName = "com.tencent.mm"
    private lateinit var classLoader: ClassLoader
    private val wechatPayActivity = "com.tencent.mm.plugin.mall.ui.MallIndexUI"
    private val wechatWalletActivity = "com.tencent.mm.plugin.mall.ui.MallWalletUI"
    private val wechatChangeActivity =
        "com.tencent.mm.plugin.wallet.balance.ui.WalletBalanceManagerUI"
    private val wechatMoneyLoadingView =
        "com.tencent.mm.plugin.wallet_core.ui.view.WcPayMoneyLoadingView"
    private var money = "100000000.00"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (packageName == lpparam.packageName) {
            xlog("Load Wechat app.")
            classLoader = lpparam.classLoader

            hookMoney()

//            hookPayPage()
//            hookPayPage2()
//
//            hookChangePage()

            hookBeat()
        }
    }

    /**
     * 屏蔽拍一拍
     */
    private fun hookBeat() {
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.chatting.view.AvatarImageView",
            classLoader,
            "setOnDoubleClickListener",
            "com.tencent.mm.plugin.story.api.i\$a",
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam?): Any {
                    xlog("replace double click")
                    return ""
                }
            })
    }

    /**
     * 改变自定义的文本控件设置文本方法，终极boss，三个都可以
     */
    private fun hookMoney() {
        val hookClass = classLoader.loadClass(wechatMoneyLoadingView) ?: return
        XposedHelpers.findAndHookMethod(hookClass, "setFirstMoney", String::class.java, replaceStr)
        XposedHelpers.findAndHookMethod(hookClass, "setNewMoney", String::class.java, replaceStr)
    }

    /**
     * 支付页面改变文本
     */
    private fun hookPayPage() = XposedHelpers.findAndHookMethod(
        wechatPayActivity,
        classLoader,
        "dbb",
        replaceViewText
    )

    /**
     * 支付页面改变文本另一种方法
     */
    private fun hookPayPage2() {
        XposedHelpers.findAndHookMethod(wechatMoneyLoadingView,
            classLoader,
            "cc",
            String::class.java,
            Boolean::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    param?.let {
                        val view = param.thisObject as View
                        param.args[0] = money
                    }
                }
            })
    }

    /**
     * 零钱页面改变文本
     */
    private fun hookChangePage() = XposedHelpers.findAndHookMethod(
        wechatChangeActivity,
        classLoader,
        "tk",
        Boolean::class.java,
        replaceViewText
    )

    /**
     * 在方法调用前手动修改值来改变最后显示的金额
     */
    private val replaceStr = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam?) {
            param?.let {
                xlog("")
                val view = param.thisObject as View
                when (view.context.javaClass.name) {
                    wechatWalletActivity -> param.args[0] = "¥$money"
                    wechatPayActivity, wechatChangeActivity -> param.args[0] = money
                }
            }
        }
    }

    /**
     * 通过找到显示金额的控件反射拿到它的赋值方法并调用
     */
    private val replaceViewText = object : XC_MethodReplacement() {
        override fun replaceHookedMethod(param: MethodHookParam?): Any {
            param?.let {
                val activity = param.thisObject as Activity
                var view = activity.findViewById<View>(0x7f091794) //id:dod的id值为0x7f091794
                XposedHelpers.callMethod(view, "setText", money)
                xlog("find view and set text.")
            }
            return ""
        }
    }
}