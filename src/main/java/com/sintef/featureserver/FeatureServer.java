package com.sintef.featureserver;

import org.cloudname.flags.Flag;
import org.cloudname.flags.Flags;

public class FeatureServer {
    @Flag(name = "webserver-port")
    public static int webserverPort = 10100;

    private WebServer webServer;

    public static void main(String[] args) throws Exception {
        Flags flags = new Flags()
                .loadOpts(FeatureServer.class)
                .parse(args);

        //quit if help has been called
        if (flags.helpFlagged()) {
            return;
        }
        final FeatureServer featureServer = new FeatureServer();
        featureServer.start();
    }

    public void start() {
        webServer = new WebServer(webserverPort);
        webServer.start();
    }



}
