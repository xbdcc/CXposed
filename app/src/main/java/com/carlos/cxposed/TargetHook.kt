package com.carlos.cxposed

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Created by Carlos on 2019/1/22.
 */
class TargetHook : IXposedHookLoadPackage {

    private val packageName = "com.carlos.cxposed"
    private val TAG = "TargetHook-"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log(TAG + lpparam.packageName)

        if (packageName == lpparam.packageName) {
            XposedBridge.log(TAG + "Xposed启动了软件")


            val hookClass = lpparam.classLoader.loadClass("com.carlos.cxposed.MainActivity")
            dumpClass(hookClass)



//            XposedHelpers.findAndHookMethod("com.carlos.cutils.demo.MainActivity", lpparam.classLoader, "click", View::class.java, object : XC_MethodHook(){
//                override fun afterHookedMethod(param: MethodHookParam) {
////                    super.afterHookedMethod(param)
////                    XposedHelpers.callMethod(param.thisObject, "showToast", "hello")
//                }
//            })

            XposedHelpers.findAndHookMethod(hookClass, "click", View::class.java, object : XC_MethodReplacement(){
                override fun replaceHookedMethod(param: MethodHookParam): Any {
                    XposedBridge.log(TAG + "before")
                    val cc = param.thisObject
                    XposedBridge.log(TAG + cc)
                    XposedBridge.log(TAG + cc.javaClass)
                    val activity = cc as Activity
                    val textView = activity.findViewById<TextView>(R.id.textview)
                    XposedBridge.log(TAG + textView.text)
                    textView.text = "cc"
                    XposedBridge.log(TAG + textView.text)
                    return ""
                }
            })


            XposedHelpers.findAndHookMethod("com.carlos.cxposed.MainActivity", lpparam.classLoader, "click", View::class.java, object : XC_MethodReplacement(){
                override fun replaceHookedMethod(param: MethodHookParam): Any {

                    val cc = param.thisObject
                    XposedBridge.log(TAG + cc)
                    XposedBridge.log(TAG + cc.javaClass)

                    return ""
                }
            })
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