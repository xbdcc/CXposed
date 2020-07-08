# Xposed系列之Demo上手指南及源码解析(一)

## Xposed简介
百度百科介绍：
> Xposed框架(Xposed Framework)是一套开源的、在Android高权限模式下运行的框架服务，可以在不修改APK文件的情况下影响程序运行(修改系统)的框架服务，基于它可以制作出许多功能强大的模块，且在功能不冲突的情况下同时运作。`

## Xposed相关工具
- [Xposed Installer](https://github.com/rovo89/XposedInstaller/)：为安装在手机上的Xposed的主体运行框架，
手机需要Root权限安装Xposed框架，可以管理Xposed模块
- [VirtualXposed](https://github.com/android-hacker/VirtualXposed)：一个简单的应用程序，无需root用户即可使用Xposed，解锁引导程序或修改系统映像等
- [太极](https://github.com/taichi-framework/TaiChi)：一个带有或不带有Root/BL锁的Xposed模块框架，支持Android 5.0〜10

三个工具首页分别长这样：

Xposed Installer|VirtualXposed|太极
:-:|:-:|:-:
![](http://xbdcc.cn/image/CXposed/xposed.png)|![](http://xbdcc.cn/image/CXposed/virtual_xposed.png)|![](http://xbdcc.cn/image/CXposed/taichi.png)
## Xposed Demo

### 新建项目配置Xposed环境
- 首先创建创建一个Android Studio项目，然后New一个Module，我们要使用Xposed就需要引入Xposed库，在`build.gradle`加入
```
    compileOnly 'de.robv.android.xposed:api:82'
```
- 然后在`AndroidManifest.xml`的`application`节点下加入如下配置

```xml
       <meta-data
                android:name="xposedmodule"
                android:value="true" />
        <meta-data
                android:name="xposeddescription"
                android:value="A Xposed demo." />
        <meta-data
                android:name="xposedminversion"
                android:value="89" />
```

### 编写Hook代码

- 新建一个类，里面写一些方法和变量后面使用，如`DemoClass`：
```kotlin
class DemoClass {

    private val name = "carlos"

    fun printlnName() {
        log("name is:$name")
    }

    companion object {

        @JvmStatic
        fun printlnHelloWorld() {
            log("hello world!")
        }

    }

}
```

- 接下来新建一个继承自`IXposedHookLoadPackage`的类，如：
```kotlin
class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
    }

}

```

- 使用`XposedHelpers`调用APP的方法，使用到的就是反射，Java反射也能实现该功能
```kotlin
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
        XposedHelpers.callMethod(hookClass.newInstance(), "printlnName")
    }
```

- 然后在`MainActivity`中增加方法被Hook，如：
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        log(getLog())
    }

    private fun getLog() : String {
        return "hello world"
    }

    fun click(view: View) {
        log("click")
    }

}
```

- 在`MainHook`中增加如下代码对`MainActivity`的`getLog`和`click`方法进行Hook:
```kotlin
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
```

- 最后在`main`下新建`assets`目录，在里面创建`xposed_init`文件（文件名必须是这个，后面介绍为什么必须为这个），文件里面就可以添加我们刚刚的类了，如：
```
com.carlos.cxposed.demo.MainHook
```

- 模拟器安装好`Xposed Installer`后，运行项目，可以看出来弹出框：
![](http://xbdcc.cn/image/CXposed/xposed_reboot.webp)<br>
点击重启或软重启生效，然后执行操作可以看到打印日志如下，Hook成功：
```
07-06 22:28:43.470 3965-3965/? I/Xposed: MainHook->MainHook->hook an app start:com.carlos.cxposed.demo
07-06 22:28:43.472 3965-3965/? D/MainHook->: hello world!
07-06 22:28:43.472 3965-3965/? D/MainHook->: name is:xbd
07-06 22:28:45.352 3965-3965/com.carlos.cxposed.demo I/Xposed: MainHook->hook before getLog
07-06 22:28:45.352 3965-3965/com.carlos.cxposed.demo I/Xposed: MainHook->hook after getLog
07-06 22:28:45.352 3965-3965/com.carlos.cxposed.demo D/MainHook->: this is a hook message.
07-06 22:28:51.193 3965-3965/com.carlos.cxposed.demo I/Xposed: MainHook->hook replace click
```

## Xposed原理
Xposed还有C库，我们这里简单分析下我们引用的他的Java层`de.robv.android.xposed:api:82`，看下我们用到的两个类`XposedHelpers`和`XposedBridge`的源码

### XposedBridge解析

- 首先找个入口，就从我们Hook类实现的`IXposedHookLoadPackage`接口开始吧，我们查看到该接口被`Xposed`自己的jar包调用的有如下几个地方：
![](http://xbdcc.cn/image/CXposed/xposed_code1.webp)

- 跟进去new出这个接口的地方，调用到的代码块如下：
```java
    hookLoadPackage(new IXposedHookLoadPackage.Wrapper((IXposedHookLoadPackage) moduleInstance));
```

- 跟进`hookLoadPackage`方法，可以看到这里将该接口添加到集合里存起来了
```java
	public static void hookLoadPackage(XC_LoadPackage callback) {
		synchronized (sLoadedPackageCallbacks) {
			sLoadedPackageCallbacks.add(callback);
		}
	}
```

- 继续回来看`hookLoadPackage`方法，可以看到他是被`XposedBridge`的`loadModule`方法调用：
```java
	/**
	 * Load a module from an APK by calling the init(String) method for all classes defined
	 * in <code>assets/xposed_init</code>.
	 */
	private static void loadModule(String apk) {
		log("Loading modules from " + apk);

		if (!new File(apk).exists()) {
			log("  File does not exist");
			return;
		}

		ClassLoader mcl = new PathClassLoader(apk, BOOTCLASSLOADER);
		InputStream is = mcl.getResourceAsStream("assets/xposed_init");
		if (is == null) {
			log("assets/xposed_init not found in the APK");
			return;
		}

		BufferedReader moduleClassesReader = new BufferedReader(new InputStreamReader(is));
		try {
			String moduleClassName;
			while ((moduleClassName = moduleClassesReader.readLine()) != null) {
				moduleClassName = moduleClassName.trim();
				if (moduleClassName.isEmpty() || moduleClassName.startsWith("#"))
					continue;

				try {
					log ("  Loading class " + moduleClassName);
					Class<?> moduleClass = mcl.loadClass(moduleClassName);

					if (!IXposedMod.class.isAssignableFrom(moduleClass)) {
						log ("    This class doesn't implement any sub-interface of IXposedMod, skipping it");
						continue;
					} else if (disableResources && IXposedHookInitPackageResources.class.isAssignableFrom(moduleClass)) {
						log ("    This class requires resource-related hooks (which are disabled), skipping it.");
						continue;
					}

					final Object moduleInstance = moduleClass.newInstance();
					if (isZygote) {
						if (moduleInstance instanceof IXposedHookZygoteInit) {
							IXposedHookZygoteInit.StartupParam param = new IXposedHookZygoteInit.StartupParam();
							param.modulePath = apk;
							param.startsSystemServer = startsSystemServer;
							((IXposedHookZygoteInit) moduleInstance).initZygote(param);
						}

						if (moduleInstance instanceof IXposedHookLoadPackage)
							hookLoadPackage(new IXposedHookLoadPackage.Wrapper((IXposedHookLoadPackage) moduleInstance));

						if (moduleInstance instanceof IXposedHookInitPackageResources)
							hookInitPackageResources(new IXposedHookInitPackageResources.Wrapper((IXposedHookInitPackageResources) moduleInstance));
					} else {
						if (moduleInstance instanceof IXposedHookCmdInit) {
							IXposedHookCmdInit.StartupParam param = new IXposedHookCmdInit.StartupParam();
							param.modulePath = apk;
							param.startClassName = startClassName;
							((IXposedHookCmdInit) moduleInstance).initCmdApp(param);
						}
					}
				} catch (Throwable t) {
					log(t);
				}
			}
		} catch (IOException e) {
			log(e);
		} finally {
			try {
				is.close();
			} catch (IOException ignored) {}
		}
	}
```

- 通过这里可以得知，他是会找APK下`assets/xposed_init`是否存在，如果不存在则会打印日志并且`return`返回不往下执行了，所以前面说的`文件名必须为xposed_init`是因为这里做了判断，如果不按规则来则Hook都会无效
```java
		InputStream is = mcl.getResourceAsStream("assets/xposed_init");
		if (is == null) {
			log("assets/xposed_init not found in the APK");
			return;
		}
```

- 继续往上跟会发现`loadModule`方法被`loadModules`调用，而`loadModules`被`main`函数调用，也就是Java的主函数入口，代码如下：
```java
	protected static void main(String[] args) {
		// Initialize the Xposed framework and modules
		try {
			SELinuxHelper.initOnce();
			SELinuxHelper.initForProcess(null);

			runtime = getRuntime();
			if (initNative()) {
				XPOSED_BRIDGE_VERSION = getXposedVersion();
				if (isZygote) {
					startsSystemServer = startsSystemServer();
					initForZygote();
				}

				loadModules();
			} else {
				log("Errors during native Xposed initialization");
			}
		} catch (Throwable t) {
			log("Errors during Xposed initialization");
			log(t);
			disableHooks = true;
		}

		// Call the original startup code
		if (isZygote)
			ZygoteInit.main(args);
		else
			RuntimeInit.main(args);
	}
```

- 接下来我们看`MainHook`里我们用到的`XposedHelpers.callStaticMethod`和`XposedHelpers.callMethod`，
可以看到他们调用到的方法都是`callMethod`，而在其内部调用了`findMethodBestMatch`方法，
最终通过`invoke(obj, args)`反射来执行Hook的方法：
```java
	public static Object callMethod(Object obj, String methodName, Object... args) {
		try {
			return findMethodBestMatch(obj.getClass(), methodName, args).invoke(obj, args);
		} catch (IllegalAccessException e) {
			// should not happen
			XposedBridge.log(e);
			throw new IllegalAccessError(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw new InvocationTargetError(e.getCause());
		}
	}
```

- 我们继续看`XposedHelpers.findAndHookMethod`，可以看到调用的是`XposedHelpers`的findAndHookMethod方法，将方法传入的最后一个对象转为`XC_MethodHook`，
接着通过`findMethodExact`方法传入最后一个参数前的所有参数，在`findMethodExact`方法内部又通过反射`clazz.getDeclaredMethod`找出具体的方法，
并且设置该方法访问权限为公有，所有`XposedHelpers.findAndHookMethod`公有私有方法都能Hook
```java
	public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
		if (parameterTypesAndCallback.length == 0 || !(parameterTypesAndCallback[parameterTypesAndCallback.length-1] instanceof XC_MethodHook))
			throw new IllegalArgumentException("no callback defined");

		XC_MethodHook callback = (XC_MethodHook) parameterTypesAndCallback[parameterTypesAndCallback.length-1];
		Method m = findMethodExact(clazz, methodName, getParameterClasses(clazz.getClassLoader(), parameterTypesAndCallback));

		return XposedBridge.hookMethod(m, callback);
	}
```

- 接着来看看刚刚的`XposedBridge.hookMethod`，可以看到该方法内部最终通过`hookMethodNative`调用了Native层的Hook方法。
继续跟`XposedBridge.hookMethod`，会发现他又是被`hookResources`方法调用，而`hookResources`是被`initForZygote`方法调用，
而刚刚我们知道在`main`函数里调用了`initForZygote`方法，接下来我们就再看下`initForZygote`方法

- 首先我们看到该类中有一个main方法入口，主要看其中的`initForZygote`和`loadModules`方法，我们看下其中这段代码，
可以看到他主要是Hook了`ActivityThread`类的`handleBindApplication`这个方法，
而我们看下`ActivityThread`的源码就会发现`handleBindApplication`里面调用了`mInstrumentation.callApplicationOnCreate(app)`，
其实就是`Application`的`onCreate`，所以在`Application`的`onCreate`方法前Xposed就做了Hook拦截执行自己的方法

```java
	// normal process initialization (for new Activity, Service, BroadcastReceiver etc.)
		findAndHookMethod(ActivityThread.class, "handleBindApplication", "android.app.ActivityThread.AppBindData", new XC_MethodHook() {
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				ActivityThread activityThread = (ActivityThread) param.thisObject;
				ApplicationInfo appInfo = (ApplicationInfo) getObjectField(param.args[0], "appInfo");
				String reportedPackageName = appInfo.packageName.equals("android") ? "system" : appInfo.packageName;
				SELinuxHelper.initForProcess(reportedPackageName);
				ComponentName instrumentationName = (ComponentName) getObjectField(param.args[0], "instrumentationName");
				if (instrumentationName != null) {
					XposedBridge.log("Instrumentation detected, disabling framework for " + reportedPackageName);
					disableHooks = true;
					return;
				}
				CompatibilityInfo compatInfo = (CompatibilityInfo) getObjectField(param.args[0], "compatInfo");
				if (appInfo.sourceDir == null)
					return;

				setObjectField(activityThread, "mBoundApplication", param.args[0]);
				loadedPackagesInProcess.add(reportedPackageName);
				LoadedApk loadedApk = activityThread.getPackageInfoNoCheck(appInfo, compatInfo);
				XResources.setPackageNameForResDir(appInfo.packageName, loadedApk.getResDir());

				LoadPackageParam lpparam = new LoadPackageParam(sLoadedPackageCallbacks);
				lpparam.packageName = reportedPackageName;
				lpparam.processName = (String) getObjectField(param.args[0], "processName");
				lpparam.classLoader = loadedApk.getClassLoader();
				lpparam.appInfo = appInfo;
				lpparam.isFirstApplication = true;
				XC_LoadPackage.callAll(lpparam);

				if (reportedPackageName.equals(INSTALLER_PACKAGE_NAME))
					hookXposedInstaller(lpparam.classLoader);
			}
		});
```
