[Android][动态使用点9图NinePatchDrawable]

lgy_gg
0.242019.05.25 10:03:48字数 1,143阅读 2,105
1.什么是点9图
.9.PNG是安卓开发里面的一种特殊的图片，这种格式的图片通过ADT自带的编辑工具生成，使用九宫格切分的方法，使图片支持在android 环境下的自适应展示。
.9图片的作用就是在图片拉伸的时候保证其不会失真。所以我们使用.9图片，让图片在指定的位置拉伸和在指定的位置显示内容，这样图片的边边角角就不会出现失真了。
2.动态使用外部点9图
.9.png图片的使用，只需要简单的放到res目录下，在xml布局文件里当作普通的图片来调用即可。下图是我要调用的图片，放在src的drawable文件夹里

image.png
只需要在布局文件里当作普通的图片使用即可。


image.png
效果如下：


image.png
但是如果你要调用的点9图不是在你打包的apk内部，而是位于sd卡上，这时候你直接调用它的话是得不到你想要的拉伸效果的


image.png
效果如下：


image.png
工程里面用的.9.png在打包的时候，经过了aapt的处理，成为了一张包含有特殊信息的.png图片。而不是直接加载的.9.png这种图片。所以一般情况下我们需要自己手动的调用aapt命令来处理点9图。假设该路径C:\Users\LGY\Desktop\fd\9下有一张待处理的点9图


image.png
首先来到找到自己的Android SDK 路径，例如我的sdk路径是
C:\Users\LGY\AppData\Local\Android\android-sdk
那么我就到这个路径下（build-tools下有很多个版本，选哪个都可以，这里选了21.1.0这个版本），在下面这个路径里可以看到aapt.exe文 件C:\Users\LGY\AppData\Local\Android\android-sdk\build-tools\21.1.0
然后，
在命令窗口里输入 cd C:\Users\LGY\AppData\Local\Android\android-sdk\build-tools\21.1.0


image.png
然后就是处理图片的命令了，命令如下：
aapt.exe c -v -S C:\Users\LGY\Desktop\fd\9 -C C:\Users\LGY\Desktop\fd
C:\Users\LGY\Desktop\fd\9是待处理的点9图所在的文件夹，
C:\Users\LGY\Desktop\fd是处理完成后，会生成一个同名的点9图片，这张图片就是我们需要的。


image.png
下图是生成的图片


image.png
然后在apk里我们就可以如下使用了。
Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());NinePatchDrawable npd = new NinePatchDrawable(TestActivity.this.getResources(), bitmap, bitmap.getNinePatchChunk(), new Rect(), null);
textView.setBackground(npd);
这样的操作真的很繁琐，直到我在网上看到一篇文章，介绍得很好，还给出了为什么直接调用外部点9图会得不到我们想要得效果（原因主要是NinePatchChunk的信息有无），它还说有个现成的包可以使用。下面给出这个包的地址：
https://github.com/Anatolii/NinePatchChunk
这个包里的API我没有仔细研究过，但是我试了一下确实有用，代码如下：
/**
 * 調用sd卡的.9圖
 */private void setSdNinePatchDrawable(final TextView textView2){
    new Thread(new Runnable() {
        @Override
        public void run() {

            try {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"balloon_r_pressed.9.png");
                if (file.exists()) {
                    final NinePatchDrawable drawable =
                            NinePatchChunk.create9PatchDrawable(
                                    TestActivity.this,
                                    BitmapFactory.decodeFile(file.getAbsolutePath()),
                                    null);
                    new Handler(TestActivity.this.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            textView2.setBackground(drawable);
                        }
                    });

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }).start();}
效果如下:


image.png
3.如何创建点9图
这个在谷歌的官方文档里有介绍，地址如下：
https://developer.android.com/studio/write/draw9patch.html
下面我也演示一下如何制作点9图。在Android Studio里你的项目里边，
右击要制作成点9图的图片，点击Create 9-Patch file

image.png
输入要创建的点9图名字


image.png
双击您的新 NinePatch 文件，将其在 Android Studio 中打开。您的工作区现在将打开。左侧窗格是您的绘制区域，您可以在其中编辑可拉伸配线和内容区域的线条。右侧窗格是预览区域，您可以在其中预览拉伸的图形。


image.png
具体操作请查看官方的文档。下面主要看一下效果。
如上图是我制作的图片，但是会报错too many padding sections on bottom border，按照网上说把右下角的黑线去掉就没问题了。


image.png
效果如下：


image.png
可以看到中间的头像没有拉伸，实现了我们要的效果。
4.参考文章
https://blog.csdn.net/tencent_bugly/article/details/52414034
5.源码地址
https://download.csdn.net/download/lgywsdy/11200737
