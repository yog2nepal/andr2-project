package root;

import android.app.Application;

import login.LoginModule;

/**
 * Created by Binod Nepali on 10/30/2017.
 */

public class App extends Application
{
    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component=DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .loginModule(new LoginModule())
                .build();
    }

    public ApplicationComponent getComponent() {
        return component;
    }
}
