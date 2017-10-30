package root;

import javax.inject.Singleton;

import dagger.Component;
import login.LoginActivity;
import login.LoginModule;

/**
 * Created by Binod Nepali on 10/30/2017.
 */
@Singleton
@Component(modules = {ApplicationModule.class, LoginModule.class})
public interface ApplicationComponent
{
    void inject(LoginActivity loginActivity);
}
