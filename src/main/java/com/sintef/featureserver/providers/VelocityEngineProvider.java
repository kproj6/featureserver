package com.sintef.featureserver.providers;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.apache.velocity.app.VelocityEngine;


/**
 * A class that has a static reference to VelocityEngine.
 *
 * Used by state-less API classes in the resource package.
 *
 * @author arve
 */
@Provider
public class VelocityEngineProvider implements InjectableProvider<Context, Type> {
    public static VelocityEngine velocityEngine = null;

    @Override
    public ComponentScope getScope() { return ComponentScope.Singleton; }

    @Override
    public Injectable getInjectable(final ComponentContext componentContext, final Context context, final Type type) {
        if (type.equals(VelocityEngine.class)) {
            return new Injectable<VelocityEngine>() {
                @Override
                public VelocityEngine getValue() {
                    return velocityEngine;
                }
            };
        }

        // Asked to provide something other than VelocityEngine. We don't do that.
        return null;
    }
}
