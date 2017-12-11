package io.ph.bot.audio.stream.radio;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadioData {
    public static RadioData instance;
    
    private static final Logger LOG = LoggerFactory.getLogger(RadioData.class);
    private static final String APIURL = "http://r-a-d.io/api";
    private String nowplaying;
    private Integer listeners;
    private int bitrate;
    private boolean isafkstream;
    private Integer current;
    private Integer start_time;
    private Integer end_time;
    private int trackid;
    private String thread;
    private boolean requesting;
    private String djname;
    private int djid;
    private String djimg;
    private String djcolor;
    private String lastplayed;
    private String next;

    /**
     * @return the nowplaying track
     */
    public String getNowplaying() {
        return nowplaying;
    }

    /**
     * @return the number of listeners
     */
    public String getListeners() {
        return listeners.toString();
    }

    /**
     * @return the bitrate
     */
    public int getBitrate() {
        return bitrate;
    }

    /**
     * @return if the stream is a bot stream
     */
    public boolean getIsafkstream() {
        return isafkstream;
    }

    /**
     * @return the current time
     */
    public Integer getCurrent() {
        return current;
    }
    
    /**
     * @return the start_time
     */
    public Integer getStart_time() {
        return start_time;
    }

    /**
     * @return the end_time
     */
    public Integer getEnd_time() {
        return end_time;
    }

    /**
     * @return the trackid
     */
    public int getTrackid() {
        return trackid;
    }
    /**
     * @return the thread
     */
    public String getThread() {
        return thread;
    }

    /**
     * @return if r/a/dio is taking requests
     */
    public boolean getRequesting(){
        return requesting;
    }

    /**
     * @return the djname
     */
    public String getDjname() {
        return djname;
    }

    /**
     * @return the djid
     */
    public int getDjid() {
        return djid;
    }

    /**
     * @return the djimg
     */
    public String getDjimg() {
        return djimg;
    }

    /**
     * @return the djcolor
     */
    public String getDjcolor() {
        return djcolor;
    }

    /**
     * @return the lastplayed track
     */
    public String getLastplayed() {
        return lastplayed;
    }

    /**
     * @return the next track in the queue
     */
    public String getNext() {
        return next;
    }


    private boolean updateable(){
        if (end_time == null){
            return true;
        } else {
            if(end_time < Instant.now().toEpochMilli()){
                return true;
            } else {
                return false;
            }
        }
    }

    public void update() {
        if(updateable()){
            URL url;
            try {
                url = new URL(APIURL);
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.setRequestMethod("GET");
                request.setRequestProperty("User-Agent", "Momo-2");
                request.setRequestProperty("Content-length", "0");
                request.connect();
                int statusCode = request.getResponseCode();
                String jsonString;
                switch(statusCode){
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
                        jsonString = br.lines().collect(Collectors.joining());
                        break;
                    default: return;
                        
                }
                JsonObject response = Json.parse(jsonString).asObject();
                if(response.get("main") != null){
                    JsonObject data = response.get("main").asObject();
                    nowplaying = data.getString("np", "");
                    listeners = data.getInt("listeners", 0);
                    bitrate = data.getInt("bitrate", 0);
                    isafkstream = data.getBoolean("isafkstream", true);
                    current = data.getInt("current", 0);
                    start_time = data.getInt("start_time", 0);
                    end_time = data.getInt("end_time", 0);
                    trackid = data.getInt("trackid", 0);
                    thread = data.getString("thread", "");
                    requesting = data.getBoolean("requesting", true);
                    JsonObject dj = data.get("dj").asObject();
                    djname = dj.getString("djname", "");
                    djid = dj.getInt("id", 0);
                    djimg = dj.getString("djimage", "");
                    djcolor = dj.getString("djcolor", "");
                    JsonArray queue = data.get("queue").asArray();
                    JsonObject firstTrack = queue.get(0).asObject();
                    next = firstTrack.getString("meta", "");
                    JsonArray previous = data.get("lp").asArray();
                    JsonObject lastTrack = previous.get(0).asObject();
                    lastplayed = lastTrack.getString("meta", "");                    
                } else {
                    LOG.error("Unable to update r/a/dio API data");
                    return;
                }
            } catch (MalformedURLException e) {
                LOG.error(e.getLocalizedMessage());
            } catch (IOException e) {
                LOG.error(e.getLocalizedMessage());
            }
            

        }
    } 

	public static RadioData getInstance() {
		if (instance == null) {
			instance = new RadioData();
		}
		return instance;
	}
}
