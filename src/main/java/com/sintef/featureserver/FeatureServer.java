package com.sintef.featureserver;

import com.sintef.featureserver.netcdf.NetCdfManager;
import com.sintef.featureserver.providers.NetCdfManagerProvider;
import org.cloudname.flags.Flag;
import org.cloudname.flags.Flags;

public class FeatureServer {
    @Flag(name = "webserver-port",
          description = "whicb port to run the API server on")
    public static int webserverPort = 10100;

    @Flag(name = "netcdf-file",
          description = "Temporary flag. Which file are we using as data source",
          required = true)
    public static String netCdfFile;


    public static void main(final String[] args) throws Exception {
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
        // Initialize providers
        NetCdfManagerProvider.value = new NetCdfManager();
        final WebServer webServer = new WebServer(webserverPort);
        webServer.start();
    }



}
