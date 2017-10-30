package login;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Binod Nepali on 10/30/2017.
 */

@Module
public class LoginModule
{
    @Provides
    public  LoginActivityMVP.Presenter provideLoginActivityPresenter()
    {
        return new LoginActivityPresenter();
    }
}
