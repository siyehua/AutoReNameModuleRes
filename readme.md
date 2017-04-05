# AutoReNameModuleRes
自动为module的资源添加前缀

[组件化](http://www.jianshu.com/p/186fa07fc48a)的概念提出来有一段的时间,也确实带来许多好处,但在对项目进行组件化的同时也凸显了一些问题<br>
其中一个资源问题是子module的资源可能会能其他的module重复,网上一些做法是,通过为gradle的添加来实现.

```
resourcePrefix "module1_"
```

实际上这个实现并不可靠,编译的时候也不会报错,创建资源的时候也不会自动添加前缀.

# 问题原因
Android支持不同的module可以共享使用相同的资源名字,是因为Gradle在打包编译的过程中,会自动对同样的资源名字进行合并.<br>
例如假设子module命名了app的名字为"app_testname",而主module命名为"app_name",则编译合并之后,会以主module为准.

详细规则可点击官网[链接](https://developer.android.com/studio/build/index.html?hl=zh-cn)

# 解决方案
自动合并同样名字的资源的好处是巨大的,可以让我们配置多样化的APP,但有时候我们并不希望它自动合并,因为我们只是不小心
将资源名字与其他module的资源名字命名为一致,但目前来说Android Studio并没有一个有效的有段了检测或提醒,这个时候
就需要我们对module的资源名字注意检查,非常麻烦.通过此项目可以帮助你解决这个问题.

# 使用
 * 新建一个java module,复制[MyClass](./rename/src/main/java/com/example/MyClass)到这个module中
 * 在当前module的gradle中添加添加一个[配置](./rename/build.gradle)(修改包名为MyClass的包名)
 * 修改[MyClass](./rename/src/main/java/com/example/MyClass)中的resourcePrefix,srcPath
 * 在Terminal中执行"gradlew run -Pmain=MyClass"(第一次运行需要下载Gradle配置)

