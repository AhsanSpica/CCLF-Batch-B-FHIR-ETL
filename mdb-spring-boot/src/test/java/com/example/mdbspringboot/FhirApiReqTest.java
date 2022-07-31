package com.example.mdbspringboot;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class FhirApiReqTest {

    @Test
    void apiGet() {
        String result ="1";
        boolean found =false;
        int total=0;
        try{
            HttpClient httpClient    = HttpClientBuilder.create().build();
            HttpGet getreq = new HttpGet("http://fhir.dataqhealth.com/fhir/Practitioner?identifier=1437117322");
            // StringEntity postingString = new StringEntity(serOrg, ContentType.APPLICATION_JSON);
            // getreq.setEntity(postingString);
            HttpResponse response = httpClient.execute(getreq);
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
            System.out.println("success after api request: "+tempbuilder);
            total=Integer.parseInt(myResponse.getString("total"));
            if (total>0)
            {
                found=true;
            }
            else { found=false;}
            if (found==false) {
            System.out.println("success after api request id if found: "+total+", found : "+found);}

        }
        catch (ClientProtocolException | JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
       // return result;
    }
}