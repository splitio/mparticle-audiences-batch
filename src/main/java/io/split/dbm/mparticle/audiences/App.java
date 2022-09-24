package io.split.dbm.mparticle.audiences;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Hello world!
 *
 */
public class App {

	private static final Logger logger = Logger.getLogger("BatchedAPI");
	 
	static Configuration config;
	
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    static final Instant startTime = Instant.now();
    
	public static void main( String[] args ) throws Exception {
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setLevel(Level.INFO);
		for (Handler h : rootLogger.getHandlers()) {
		    h.setLevel(Level.INFO);
		}
		
		logger.setLevel(Level.INFO);
		
		logger.info("starting server...");
		
		config = Configuration.fromFile(configFile(args));
		logger.info(config.toString());
		
		HttpServer server = HttpServer.create(new InetSocketAddress(config.port), 0);
		logger.info("server listening to port " + config.port);

		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				logger.info("cache - " + mpidCache.values());
				for(Entry<AudienceRequest, Set<String>> entry : mpidCache.entrySet()) {
					AudienceRequest ar = entry.getKey();
					
					if(entry.getValue().size() < 1) {
						logger.fine("cache empty...");
						continue;
					}
					
					String urlVerb = "";
					if(ar.verb.equalsIgnoreCase("add")) {
						urlVerb = "uploadKeys";
					} else if (ar.verb.equalsIgnoreCase("delete")) {
						urlVerb = "removeKeys";
					} else {
						logger.warning("unknown verb for segment " + ar.segment + " , should be \"add\" or \"delete\": " + ar.verb);
						logger.warning("removing corrupt segment from cache");
						mpidCache.remove(ar);
						continue;
					}
					
					final String url = "https://api.split.io/internal/api/v2/segments/" 
	                        + ar.environmentId + '/' + ar.segment + "/" + urlVerb + "?replace=false";
					logger.info("URL: " + url);
					
					String body = "{\"keys\": [ ";
					for(String mpid : entry.getValue()) {
						body += "\"" + mpid + "\", ";
					}
					if(body.indexOf(",") != -1) {
						body = body.substring(0, body.lastIndexOf(","));
					}
					body += "]}";
					
					logger.info("POST body: " + body);
					
	                HttpRequest request = HttpRequest.newBuilder()
	                        .PUT(HttpRequest.BodyPublishers.ofString(body))
	                        .uri(URI.create(url))
	                        .setHeader("Content-Type", "application/json")
	                        .setHeader("Authorization", "Bearer " + ar.apiToken)
	                        .build();

	                HttpResponse<String> response;
					try {
						response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		                logger.info("POST TO SPLIT - " + response.statusCode() + " response body: " + response.body());
		                if(response.statusCode() == 200) {
		                	mpidCache.get(ar).clear();
		                }
					} catch (Exception e) {
						logger.warning("error during POST to Split: " + e.getMessage());
						e.printStackTrace();
					}
						
				}
			}
		}, 0, config.segmentsFlushRateInSeconds, TimeUnit.SECONDS);
		
		server.createContext("/audiences", new MyHandler());
		server.createContext("/uptime", new UptimeHandler());
		server.createContext("/ping", new PingHandler());
		server.setExecutor(null);
		server.start();
	}

	static class UptimeHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			Instant now = Instant.now();
			
			Duration duration = Duration.between(startTime, now);
			String uptime = duration.toDaysPart() + "d:"
					+ duration.toHoursPart() + "h:"
					+ duration.toMinutesPart() + "m:"
					+ duration.toSecondsPart() + "s";

			exchange.sendResponseHeaders(200, uptime.length());
			logger.info("200 - uptime - " + uptime);
		
			OutputStream os = exchange.getResponseBody();
			os.write(uptime.getBytes());
			os.close();					
		}
	}
	
	static class PingHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			String response = "pong";
			exchange.sendResponseHeaders(200, response.length());
			logger.info("200 - pong");
		
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();					
		}
	}
	
	private static Map<AudienceRequest, Set<String>> mpidCache = new HashMap<AudienceRequest, Set<String>>();

	static class MyHandler implements HttpHandler {

		public void handle(HttpExchange exchange) throws IOException {
			logger.info("request method: " + exchange.getRequestMethod());
			String response = "_placeholder_";
			if(exchange.getRequestMethod().equalsIgnoreCase("post")) {
				String authHeader = getHeader(exchange, "Authorization");
//				logger.info(" authHeader: " + authHeader);
				if(authHeader != null && authHeader.equals(config.authToken)) {
					
					AudienceRequest ar = null;
					try {
						InputStream requestBody = exchange.getRequestBody();
						Gson gson = new Gson();
						ar = gson.fromJson(new InputStreamReader(requestBody), AudienceRequest.class);
						logger.info(" parsed post: " + ar);
						
						if(ar != null) {
							Set<String> mpids = mpidCache.get(ar);
							if(mpids == null) {
								mpids = new HashSet<String>();
								mpidCache.put(ar, mpids);
							}
							mpids.addAll(ar.getMpids());
							response = "MPIDs added";
							exchange.sendResponseHeaders(200, response.length());
						} else {
							response = "invalid POST body";
							exchange.sendResponseHeaders(400, response.length());
						}
					
					} catch (Exception e) {
						logger.severe(e.getMessage());
						e.printStackTrace(System.out);
						response = e.getMessage();
						exchange.sendResponseHeaders(500, response.length());
					}
				} else {
					response = "auth token doesn't match";
					exchange.sendResponseHeaders(401, response.length());
					logger.warning("401 - auth token mismatch");
				}
			} else {
				response = "method not allowed";
				exchange.sendResponseHeaders(405, response.length());
				logger.warning("405 - method not allowed");
			}
			
			if(response.equals("_placeholder_")) {
				response = "noop";
				exchange.sendResponseHeaders(200, response.length());
				logger.info("200 - noop");
			}
			
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();			
		}
	}

	private static String configFile(String[] args) {
		if(args.length != 1) {
			logger.severe("first argument should be configuration file  (e.g. java -jar audiences.jar audiences.config)");
			System.exit(1);
		}
		return args[0];
	}
	
	 public static String getHeader(HttpExchange exchange, String key) {
		   List<String> values = exchange.getRequestHeaders().get(key);
		   if (values != null && !values.isEmpty()) {
		     return values.get(0);
		   }
		   return null;	
	}
}
