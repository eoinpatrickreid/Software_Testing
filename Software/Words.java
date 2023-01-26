package uk.ac.ed.inf;
import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Words {
    private final String port;
    public Words(String p){
        port = p;
    }


    /**
     * This location from words method will take a what3words string containing three period separated words
     * and from this connect to the webserver location corresponding to that address, it will retrieve the coordinates
     * of the location and then return this as a longlat object.
     * @param input a string containing a what4words location
     * @return      the longlat location corresponding to the string.
     */
    public LongLat locationFromWords(String input){
        String[] words = input.split("[.]");
        String httpString2 = String.format("http://localhost:%s/words/%s/%s/%s/details.json", port, words[0], words[1], words[2]);
        HttpResponse<String> response;
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder().uri(URI.create(httpString2)).build();
            response = Client.client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Uncaught", e);
        }
        String orders_json = response.body();

        Word word = new Gson().fromJson(orders_json, Word.class);
        return new LongLat(word.coordinates.lng, word.coordinates.lat);
        }
    }



