# Xposed系列之微信屏蔽拍一拍(三)

原文链接：https://github.com/xbdcc/CXposed/blob/master/beat.md

Xposed系列之前文章如下：
- [Xposed系列之Demo上手指南及源码解析(一)](https://github.com/xbdcc/CXposed/blob/master/demo/README.md)
- [Xposed系列之微信装X指南(二)](https://github.com/xbdcc/CXposed/blob/master/README.md)


## 需求
需求来自于看到的热搜，微信拍一拍好多人说不支持关闭，容易误触。所以想着Hook下可以屏蔽自己的拍一拍


<figure>
<img src="http://xbdcc.cn/image/CXposed/wechat/beat_background1.jpg" height="600"/>&nbsp;&nbsp;&nbsp;&nbsp;
<img src="http://xbdcc.cn/image/CXposed/wechat/beat_background2.jpg" height="600"/>
</figure>


## 结果
最后代码实现的效果如下，屏蔽了双击"拍一拍"：

![](http://xbdcc.cn/image/CXposed/wechat/beat.gif)


## 分析UI
可以拦截的地方有很多处，这里就从最直观容易分析Hook的地方入手，分析方法上篇讲了很多了，这里就不再详细介绍了。
找到拍一拍双击点，可以看到id为`aku`的控件就是显示的头像，而它的点击和长按属性都为true，这个应该也是双击的控件
![](/images/beat_ui.jpg)

然后通过通过`adb shell dumpsys activity top > activity_top.txt`，找到id为`aku`的那一行信息如下：
```xml
com.tencent.mm.ui.chatting.view.AvatarImageView{8c74400 V.ED..CL. ........ 0,0-122,122 #7f090708 app:id/aku}
```
可以看到该ImageView为自定义控件`AvatarImageView`，所以我们就可以去到这个自定义控件里面去看它的代码了

## 分析代码
反编译APK查看`AvatarImageView`类的完整代码如下：
```java
public class AvatarImageView extends AppCompatImageView implements m {
    private boolean Ihh;
    private final String TAG;
    private int pageType;
    private i yXY;
    private String zkN;

    public AvatarImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AvatarImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        AppMethodBeat.i(36689);
        this.TAG = "MicroMsg.AvatarImageView";
        this.pageType = -1;
        this.yXY = null;
        this.zkN = "";
        this.Ihh = true;
        this.yXY = ((e) g.ad(e.class)).getStoryUIFactory().gq(context);
        this.yXY.aZ(this);
        setLayerType(1, (Paint) null);
        AppMethodBeat.o(36689);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        AppMethodBeat.i(36690);
        super.onDraw(canvas);
        if (this.Ihh) {
            this.yXY.a(canvas, true, 0);
            AppMethodBeat.o(36690);
            return;
        }
        this.yXY.a(canvas, false, 0);
        AppMethodBeat.o(36690);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        AppMethodBeat.i(36691);
        super.onMeasure(i, i2);
        AppMethodBeat.o(36691);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        AppMethodBeat.i(36692);
        super.setOnClickListener(this.yXY.dUH());
        this.yXY.setOnClickListener(onClickListener);
        AppMethodBeat.o(36692);
    }

    public void setOnDoubleClickListener(i.a aVar) {
        AppMethodBeat.i(36693);
        this.yXY.setOnDoubleClickListener(aVar);
        AppMethodBeat.o(36693);
    }

    public void setShowStoryHint(boolean z) {
        AppMethodBeat.i(36694);
        this.yXY.setShowStoryHint(z);
        AppMethodBeat.o(36694);
    }

    public final void eM(String str, int i) {
        AppMethodBeat.i(36695);
        this.yXY.eM(str, i);
        this.zkN = str;
        AppMethodBeat.o(36695);
    }

    public void setChattingBG(boolean z) {
        this.Ihh = z;
    }

    public final void bO(String str, boolean z) {
        AppMethodBeat.i(36696);
        if (TextUtils.isEmpty(str) || getContext() == null) {
            AppMethodBeat.o(36696);
            return;
        }
        if (str.equals(this.zkN)) {
            setShowStoryHint(!z);
        }
        AppMethodBeat.o(36696);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        AppMethodBeat.i(36697);
        super.onDetachedFromWindow();
        if (this.pageType != -1) {
            a.b(this.pageType, this.zkN, this);
        }
        AppMethodBeat.o(36697);
    }
}
```

可以看到其中有个`setOnDoubleClickListener`方法，很明显这个就是我们双击事件的监听，所以我们可以Hook这个方法做拦截，
让他后面拍一拍的操作都不执行就可以了，当然你还可以继续往里面分析拍一拍执行网络请求或数据库操作显示UI等的地方做拦截。
我们看到`setOnDoubleClickListener`方法里面传了参数`i.a aVar`，需要先知道下这个参数是什么，继续看`i`的代码如下：
```java
public interface i {

    public interface a {
        boolean fl(View view);
    }

    void a(Canvas canvas, boolean z, int i);

    void aZ(View view);

    View.OnClickListener dUH();

    void eM(String str, int i);

    void setOnClickListener(View.OnClickListener onClickListener);

    void setOnDoubleClickListener(a aVar);

    void setShowStoryHint(boolean z);

    void setWeakContext(Context context);
}
```
查看代码得知`i`为一个接口，并且内部有一个接口`a`，因为是内部类，所以要加`$`，找到后就可以开始写Hook代码了

## 编写代码
最终实现代码如下：
```kotlin
    private fun hookBeat() {
        val hookClass = classLoader.loadClass("com.tencent.mm.plugin.story.api.i\$a")
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.chatting.view.AvatarImageView",
            classLoader,
            "setOnDoubleClickListener",
            hookClass,
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam?): Any {
                    xlog("replace double click")
                    return ""
                }
            })
    }
```
到这里其实已经结束了，但是代码还可以做下优化，我们查看`findAndHookMethod`方法源码及注释，可以看到注释里有例子`parameterTypesAndCallback`参数也可以直接传如`"com.example.MyClass"`，
所以内部提供了这种方法我们就不用外部去loadClass，直接把类名传进来就是了。仔细看源码注释也会发现这个例子有个小bug，Hook的时候少传了方法名`doSomething`。
```java
	/**
	 * Look up a method and hook it. The last argument must be the callback for the hook.
	 *
	 * <p>This combines calls to {@link #findMethodExact(Class, String, Object...)} and
	 * {@link XposedBridge#hookMethod}.
	 *
	 * <p class="warning">The method must be declared or overridden in the given class, inherited
	 * methods are not considered! That's because each method implementation exists only once in
	 * the memory, and when classes inherit it, they just get another reference to the implementation.
	 * Hooking a method therefore applies to all classes inheriting the same implementation. You
	 * have to expect that the hook applies to subclasses (unless they override the method), but you
	 * shouldn't have to worry about hooks applying to superclasses, hence this "limitation".
	 * There could be undesired or even dangerous hooks otherwise, e.g. if you hook
	 * {@code SomeClass.equals()} and that class doesn't override the {@code equals()} on some ROMs,
	 * making you hook {@code Object.equals()} instead.
	 *
	 * <p>There are two ways to specify the parameter types. If you already have a reference to the
	 * {@link Class}, use that. For Android framework classes, you can often use something like
	 * {@code String.class}. If you don't have the class reference, you can simply use the
	 * full class name as a string, e.g. {@code java.lang.String} or {@code com.example.MyClass}.
	 * It will be passed to {@link #findClass} with the same class loader that is used for the target
	 * method, see its documentation for the allowed notations.
	 *
	 * <p>Primitive types, such as {@code int}, can be specified using {@code int.class} (recommended)
	 * or {@code Integer.TYPE}. Note that {@code Integer.class} doesn't refer to {@code int} but to
	 * {@code Integer}, which is a normal class (boxed primitive). Therefore it must not be used when
	 * the method expects an {@code int} parameter - it has to be used for {@code Integer} parameters
	 * though, so check the method signature in detail.
	 *
	 * <p>As last argument to this method (after the list of target method parameters), you need
	 * to specify the callback that should be executed when the method is invoked. It's usually
	 * an anonymous subclass of {@link XC_MethodHook} or {@link XC_MethodReplacement}.
	 *
	 * <p><b>Example</b>
	 * <pre class="prettyprint">
	 * // In order to hook this method ...
	 * package com.example;
	 * public class SomeClass {
	 *   public int doSomething(String s, int i, MyClass m) {
	 *     ...
	 *   }
	 * }
	 *
	 * // ... you can use this call:
	 * findAndHookMethod("com.example.SomeClass", lpparam.classLoader, String.class, int.class, "com.example.MyClass", new XC_MethodHook() {
	 *   &#64;Override
	 *   protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	 *     String oldText = (String) param.args[0];
	 *     Log.d("MyModule", oldText);
	 *
	 *     param.args[0] = "test";
	 *     param.args[1] = 42; // auto-boxing is working here
	 *     setBooleanField(param.args[2], "great", true);
	 *
	 *     // This would not work (as MyClass can't be resolved at compile time):
	 *     //   MyClass myClass = (MyClass) param.args[2];
	 *     //   myClass.great = true;
	 *   }
	 * });
	 * </pre>
	 *
	 * @param className The name of the class which implements the method.
	 * @param classLoader The class loader for resolving the target and parameter classes.
	 * @param methodName The target method name.
	 * @param parameterTypesAndCallback The parameter types of the target method, plus the callback.
	 * @throws NoSuchMethodError In case the method was not found.
	 * @throws ClassNotFoundError In case the target class or one of the parameter types couldn't be resolved.
	 * @return An object which can be used to remove the callback again.
	 */
	public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
		return findAndHookMethod(findClass(className, classLoader), methodName, parameterTypesAndCallback);
	}
```

优化后代码如下：
```kotlin
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
```

接下来分析下为什么也可以直接传String类型的类的名称，我们跟进`findAndHookMethod`找用到`parameterTypesAndCallback`参数的方法，
会发现它最终又会调用`findMethodExact`如下：
```java
	public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
		if (parameterTypesAndCallback.length == 0 || !(parameterTypesAndCallback[parameterTypesAndCallback.length-1] instanceof XC_MethodHook))
			throw new IllegalArgumentException("no callback defined");

		XC_MethodHook callback = (XC_MethodHook) parameterTypesAndCallback[parameterTypesAndCallback.length-1];
		Method m = findMethodExact(clazz, methodName, getParameterClasses(clazz.getClassLoader(), parameterTypesAndCallback));

		return XposedBridge.hookMethod(m, callback);
	}
```

继续看`getParameterClasses`方法如下，可以看到首先判断了如果type如果为空抛异常，如果为`XC_MethodHook`则不往下执行，
如果为`Class`则强转为Class，如果为`String`则调用`findClass((String) type, classLoader)`找到Class，方法最后返回Class，
所以`parameterTypesAndCallback`也可以直接传包名+类名：
```java
	private static Class<?>[] getParameterClasses(ClassLoader classLoader, Object[] parameterTypesAndCallback) {
		Class<?>[] parameterClasses = null;
		for (int i = parameterTypesAndCallback.length - 1; i >= 0; i--) {
			Object type = parameterTypesAndCallback[i];
			if (type == null)
				throw new ClassNotFoundError("parameter type must not be null", null);

			// ignore trailing callback
			if (type instanceof XC_MethodHook)
				continue;

			if (parameterClasses == null)
				parameterClasses = new Class<?>[i+1];

			if (type instanceof Class)
				parameterClasses[i] = (Class<?>) type;
			else if (type instanceof String)
				parameterClasses[i] = findClass((String) type, classLoader);
			else
				throw new ClassNotFoundError("parameter type must either be specified as Class or String", null);
		}

		// if there are no arguments for the method
		if (parameterClasses == null)
			parameterClasses = new Class<?>[0];

		return parameterClasses;
	}
```


