import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;


public class ApiRequest
{
    public ApiRequest()
    {
        super();
    }

    public String ApiPost(String serOrg,String ResourceType )
    {
        String result ="1";
       try{
           HttpClient httpClient    = HttpClientBuilder.create().build();
           HttpPost postreq = new HttpPost("http://fhir.dataqhealth.com/fhir/"+ResourceType);
           StringEntity postingString = new StringEntity(serOrg, ContentType.APPLICATION_JSON);
           postreq.setEntity(postingString);
           HttpResponse response = httpClient.execute(postreq);
        InputStream is = response.getEntity().getContent();
        Reader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();
        while (true) {
            try {
                String line = bufferedReader.readLine();
                if (line != null) {
                    builder.append(line);
                } else {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String tempbuilder= builder.toString();
        JSONObject myResponse =  new JSONObject(tempbuilder.substring(tempbuilder.indexOf("{"), tempbuilder.lastIndexOf("}") + 1));

           result=myResponse.getString("id");

        }
       catch (ClientProtocolException | JSONException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
       return result;
    }
}
