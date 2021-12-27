package cn.codekong.paddle.sample;

import android.app.Application;
import android.content.Context;

/**
 * Created by linke on 2021/12/27
 */
public class App extends Application {
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
    }

    public static Context getAppContext() {
        return mAppContext;
    }
}
