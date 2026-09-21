一、添加工程配置
1、Project 根目录的主 gradle
确认 android studio 的 Project 根目录的主 gradle 中配置了 mavenCentral 支持（新建 project 默认配置就支持），配置华为和 FCM Maven 代码库，,可根据华为和 FCM 发布的版本更新选择最新版本：


        buildscript {
            repositories {
                google()
                mavenCentral()
                // hms， 若不集成华为厂商通道，可直接跳过
                maven { url 'https://developer.huawei.com/repo/' }
                // fcm， 若不集成 FCM 通道，可直接跳过
                maven { url "https://maven.google.com" }
            }

            dependencies {
                // fcm，若不集成 FCM 通道，可直接跳过
                classpath 'com.google.gms:google-services:4.3.8'
                // hms，若不集成华为厂商通道，可直接跳过
                classpath 'com.huawei.agconnect:agcp:1.6.0.300'
            }
        }

        allprojects {
            repositories {
                google()
                mavenCentral()
                //hms，若不集成华为厂商通道，可直接跳过
                maven { url 'https://developer.huawei.com/repo/' }
                //fcm，若不集成 FCM 通道，可直接跳过
                maven { url "https://maven.google.com" }
            }
        }
    
2、module 的 gradle配置
在 module 的 gradle 中添加依赖和 AndroidManifest 的替换变量，集成极光推送SDK和厂商通道SDK，其中厂商组合选择所需的通道即可。


        android {
                    ......
                    defaultConfig {
                        applicationId "com.xxx.xxx" //JPush 上注册的包名.
                        ......

                        ndk {
                            //选择要添加的对应 cpu 类型的 .so 库。
                            abiFilters 'armeabi', 'armeabi-v7a', 'arm64-v8a'
                            // 还可以添加 'x86', 'x86_64', 'mips', 'mips64'
                        }

                        manifestPlaceholders = [
                            JPUSH_PKGNAME : applicationId,
                            JPUSH_APPKEY : "你的 Appkey ", //JPush 上注册的包名对应的 Appkey.
                            JPUSH_CHANNEL : "developer-default", //暂时填写默认值即可.
                            MEIZU_APPKEY : "MZ-魅族的APPKEY",
                            MEIZU_APPID : "MZ-魅族的APPID",
                            XIAOMI_APPID : "小米的APPID",//JPush5.5.3开始，可以不添加MI-前缀.
                            XIAOMI_APPKEY : "小米的APPKEY",//JPush5.5.3开始，可以不添加MI-前缀.
                            OPPO_APPKEY : "OP-oppo的APPKEY",
                            OPPO_APPID : "OP-oppo的APPID",
                            OPPO_APPSECRET : "OP-oppo的APPSECRET",
                            VIVO_APPKEY : "vivo的APPKEY",
                            VIVO_APPID : "vivo的APPID"
                        ]
                        ......
                    }
                    ......
                }


                dependencies {
                    ......

                    implementation 'cn.jiguang.sdk:jpush:5.5.3'  // 此处以JPush 5.5.3 版本为例, 基础库JCore SDK将自动引用最新
                    // 接入华为厂商
                    implementation 'com.huawei.hms:push:6.12.0.300'
                    implementation 'cn.jiguang.sdk.plugin:huawei:5.5.3'// 极光厂商插件版本与接入 JPush 版本保持一致，下同
                    // 接入 FCM 厂商
                    implementation 'com.google.firebase:firebase-messaging:23.2.0'
                    implementation 'cn.jiguang.sdk.plugin:fcm:5.5.3'
                    // 接入魅族厂商
                    implementation 'cn.jiguang.sdk.plugin:meizu:5.5.3'
                    // 接入 VIVO 厂商
                    implementation 'cn.jiguang.sdk.plugin:vivo:5.5.3.a'
                    // 接入 OPPO 厂商
                    implementation 'cn.jiguang.sdk.plugin:oppo:5.5.3'
                    // 接入小米厂商
                    implementation 'cn.jiguang.sdk.plugin:xiaomi:5.5.3'
                    ......
                }

                apply plugin: 'com.google.gms.google-services'
                apply plugin: 'com.huawei.agconnect'
    
3、应用 Module 配置
如果选择的厂商通道包含了HUAWEI厂商通道和FCM厂商通道，则需要额外执行以下操作，若未选择可忽略本步骤。

FCM：在 Firebase 上创建和 JPush 上同包名的待发布应用,创建完成后下载该应用的 google-services.json 配置文件并添加到应用的 module 目录下。

HUAWEI：在 Huawei 上创建和 JPush 上同包名的待发布应用,创建完成后下载该应用的 agconnect-services.json 配置文件并添加到应用的 module 目录下。

二 、配置推送必须组件
从JPush3.0.7<=SDK版本< JPush5.2.0 和 JPush5.4.0及以上，需要配置继承JPushMessageReceiver的广播，原来如果配了MyReceiver现在可以弃用。示例如下。


        <!-- 新的 tag/alias 接口结果返回需要开发者配置一个自定的广播 -->
        <!-- 该广播需要继承 JPush 提供的 JPushMessageReceiver 类, 并如下新增一个 Intent-Filter -->
        <receiver
            android:name="自定义 Receiver"
            android:enabled="true"
            android:exported="false" >
            <intent-filter>
                    <action android:name="cn.jpush.android.intent.RECEIVE_MESSAGE" />
                    <category android:name="您应用的包名" />
            </intent-filter>
        </receiver>
    
从JPush5.2.0<=SDK版本<=JPush5.3.1，AndroidManifest 中配置一个Service，以在更多手机平台上获得更稳定的支持，示例如下：


        <!-- 可配置android:process参数将Service放在其他进程中；android:enabled属性不能是false -->
        <!-- 这个是自定义Service，要继承极光JCommonService，可以在更多手机平台上使得推送通道保持的更稳定 -->
        <service android:name="xx.xx.XService"
                android:enabled="true"
                android:exported="false"
                android:process=":pushcore">
                <intent-filter>
                    <action android:name="cn.jiguang.user.service.action" />
                </intent-filter>
        </service>
    
mavenCentral集成其他注意事项，查看解决方案
若需手动集成，查看集成教程
三 、启用推送服务
JPush SDK 提供的 API 接口，都主要集中在 cn.jpush.android.api.JPushInterface 类里。


        public class ExampleApplication extends Application {
            @Override
            public void onCreate() {
                super.onCreate();
                JPushInterface.setDebugMode(true);
                JPushInterface.init(this);
            }
        }
    
四 、验证推送结果
集成完成后，您可运行工程，然后 创建推送体验通知推送服务
推送完成后，你可以在 推送历史中查看推送状态、推送通道、送达率等详细数据