package io.split.dbm.mparticle.audiences;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

public class Configuration {
    
    public String authToken;
    public int port;
    public int segmentsFlushRateInSeconds;
    public String keyFile;
    
    public static Configuration fromFile(String configFilePath) throws IOException {
        String configContents = Files.readString(Paths.get(configFilePath));
        return new Gson().fromJson(configContents, Configuration.class);
    }

	@Override
	public String toString() {
		return "Configuration [authToken=" + authToken + ", port=" + port + ", segmentsFlushRateInSeconds="
				+ segmentsFlushRateInSeconds + ", keyFile=" + keyFile + "]";
	}    
    
}
