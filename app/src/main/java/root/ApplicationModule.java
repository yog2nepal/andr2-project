package root;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Binod Nepali on 10/30/2017.
 */
@Module
public class ApplicationModule
{
    private Application application;

    public ApplicationModule(Application application)
    {
        this.application=application;
    }

    @Provides
    @Singleton
    public Context providerContext()
    {
        return this.application;
    }
}
