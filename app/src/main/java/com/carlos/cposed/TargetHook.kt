package com.carlos.cposed

import android.widget.EditText
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Created by Carlos on 2019/1/22.
 */
class TargetHook : IXposedHookLoadPackage {

    private val TAG = "TargetHook-"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName.equals("cn.codemao.android.account.demo")) {
            XposedBridge.log(TAG + "Xposed启动了软件")

            val isExists = XposedHelpers.findMethodExactIfExists("cn.codemao.android.account.demo.MainActivity", lpparam.classLoader, "setUsername")
            val isExists2 = XposedHelpers.findMethodExactIfExists("cn.codemao.android.account.demo.MainActivity", lpparam.classLoader, "setUsername", String::class.java)
            XposedBridge.log("method isExists:$isExists")
            XposedBridge.log("method isExists:$isExists2")

            val hookClass = lpparam.classLoader.loadClass("cn.codemao.android.account.demo.MainActivity")
            dumpClass(hookClass)


            XposedHelpers.findAndHookMethod("cn.codemao.android.account.demo.MainActivity", lpparam.classLoader, "onResume", object : XC_MethodHook(){

                override fun beforeHookedMethod(param: MethodHookParam) {
                    super.beforeHookedMethod(param)
                        XposedBridge.log("$TAG---------------------------------------")

                    val editText = hookClass.getDeclaredField("mEtUsername")
                    XposedBridge.log(TAG+ editText.toString())

                    val edittext3 = XposedHelpers.getObjectField(param.thisObject, "mEtUsername")
                    XposedBridge.log(TAG+ edittext3.toString())

                    val editText6 = edittext3 as EditText
                    XposedBridge.log(TAG+ editText6.text)


                }
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
                }
            })

////
//            XposedHelpers.findAndHookMethod("cn.codemao.android.account.demo.MainActivity", lpparam.classLoader, "setUsername", object : XC_MethodHook(){
//
//                override fun beforeHookedMethod(param: MethodHookParam) {
//                    XposedBridge.log("pa:" + param.args[0])
//                    super.beforeHookedMethod(param)
//
//
//                }
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    XposedBridge.log("开始dddd劫持")
//                    super.afterHookedMethod(param)
//                    param.result = "你好"
//                }
//            })
//
//            XposedHelpers.findAndHookMethod("cn.codemao.android.account.demo.MainActivity", lpparam.classLoader, "setUsername", String::class.java, object : XC_MethodHook(){
//
//                override fun beforeHookedMethod(param: MethodHookParam) {
//
//                    XposedBridge.log("" + param.args[0])
//
//                    param.args[0] = "你好"
//                    super.beforeHookedMethod(param)
//                }
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    param.result = "你好"
//                    XposedBridge.log("开始劫持")
//                    super.afterHookedMethod(param)
//                }
//            })

//            XposedHelpers.findAndHookMethod("cn.codemao.android.account.demo.MainActivity", lpparam.classLoader, "setUsername", String::class.java, object : XC_MethodReplacement(){
//                override fun replaceHookedMethod(param: MethodHookParam?): Any {
//                    XposedBridge.log("劫持")
//                    return ""
//                }
//
//            })



        }
    }

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
            XposedBridge.log(TAG + cChildClass.toString())
        }
    }

}