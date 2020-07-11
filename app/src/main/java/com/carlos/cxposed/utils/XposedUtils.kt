package com.carlos.cxposed.utils

import android.util.Log
import android.view.ViewGroup
import androidx.core.view.forEach
import de.robv.android.xposed.XposedBridge

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
 * Created by Carlos on 2020/7/6.
 */

var TAG = "CXposed->"
const val line = "----------------------------------------------"

fun log(string: String) {
    Log.d(TAG, string)
}

fun xlog(string: String) {
    XposedBridge.log(TAG + string)
}

private fun dumpChild(view: ViewGroup) {
    log("${TAG}viewGroup:${view}")
    view.forEach {
        if (it is ViewGroup) dumpChild(it)
        log("${TAG}view:${it}------:${it.id}")
    }
}

/**
 * Pring all fileds, methods and classes in class.
 */
fun dumpClass(cClass: Class<*>) {
    XposedBridge.log("${TAG}Print start$line")
    XposedBridge.log("${TAG}Fileds:")
    val cFileds = cClass.declaredFields
    for (filed in cFileds) {
        XposedBridge.log(TAG + filed.toString())
    }

    XposedBridge.log("${TAG}Methods:")
    val cMethods = cClass.declaredMethods
    for (method in cMethods) {
        XposedBridge.log(TAG + method.toString())
    }

    XposedBridge.log("${TAG}Classes:")
    val childClass = cClass.declaredClasses
    for (cChildClass in childClass) {
        XposedBridge.log(TAG + cChildClass.toString())
    }
    XposedBridge.log("$TAG${line}Print end")
}