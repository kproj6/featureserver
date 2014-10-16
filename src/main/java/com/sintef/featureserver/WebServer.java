package com.sintef.featureserver;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebServer {
    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());
    private static final String JETTY_RESOURCES
            = "com.sintef.featureserver.rs;"
            + "com.sintef.featureserver.providers";
    private final int port;
    private final org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server();
    private final SocketConnector connector = new SocketConnector();

    /**
     * WebServer constructor.
     * @param port port number
     */
    public WebServer(final int port) {
        this.port = port;
    }

    /**
     * Start the Web server.
     */
    public void start() {
        connector.setPort(port);
        LOGGER.config("Using port " + port);
        server.setConnectors(new Connector[]{connector});

        final List<Handler> contextHandlers = new ArrayList<>();

        final ServletContextHandler idHandler
                = new ServletContextHandler(ServletContextHandler.SESSIONS);
        final ServletHolder idServlet = new ServletHolder(ServletContainer.class);
        // Setting package path where Jersey looks for Providers and Resources
        idServlet.setInitParameter("com.sun.jersey.config.property.packages", JETTY_RESOURCES);
        idHandler.addServlet(idServlet, "/*");
        contextHandlers.add(idHandler);

        final RequestLogHandler requestLogHandler = null;

        final ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(contextHandlers.toArray(new Handler[1]));
        final HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(
                (requestLogHandler == null)
                        ? new Handler[]{contexts}
                        : new Handler[]{contexts, requestLogHandler}
        );
        server.setHandler(handlers);
        try {
            LOGGER.info("Starting feature server on port: " + port);
            server.start();
        } catch (final Exception e) {
            LOGGER.severe("Starting Partner server FAILED on port: " + port + " with reason: " + e);
            throw new RuntimeException(e);
        }
    }
}
