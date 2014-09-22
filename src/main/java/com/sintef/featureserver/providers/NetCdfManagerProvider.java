package com.sintef.featureserver.providers;

import com.sintef.featureserver.netcdf.NetCdfManager;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 * A class that has a static reference to server config.
 * Used by state-less API classes in this folder.
 *
 * @author gunnaringe
 */
@Provider
public class NetCdfManagerProvider implements InjectableProvider<Context, Type> {
    public static NetCdfManager value = null;

    @Override
    public ComponentScope getScope() {
        return ComponentScope.Singleton;
    }

    @Override
    public Injectable getInjectable(
            final ComponentContext componentContext,
            final Context context,
            final Type type) {
        if (!type.equals(NetCdfManager.class)) {
            return null;
        }
        return new Injectable<NetCdfManager>() {
            @Override
            public NetCdfManager getValue() {
                return value;
            }
        };
    }
}
