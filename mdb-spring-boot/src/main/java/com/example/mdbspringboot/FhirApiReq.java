package com.example.mdbspringboot;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class FhirApiReq {

    String uri = "http://192.168.10.44:8080/fhir/";
    public String ApiPost(String serOrg,String ResourceType )
    {
        String result ="1";
        try{
            HttpClient httpClient    = HttpClientBuilder.create().build();
            //  http://192.168.10.44:8080/

            HttpPost postreq = new HttpPost(uri+ResourceType);
            // HttpPost postreq = new HttpPost("http://fhir.dataqhealth.com/fhir/"+ResourceType);
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
            System.out.println("success after api request: "+tempbuilder);

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

    public String ApiGetid(String ident,String ResourceType )
    {
        String result ="1";
        boolean found =false;
        int total=0;
        int id=0;

        try{
            HttpClient httpClient    = HttpClientBuilder.create().build();
            HttpGet getreq = new HttpGet(uri+ResourceType+"?identifier="+ident);
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
            String substring = tempbuilder.substring(tempbuilder.indexOf("entry"), tempbuilder.lastIndexOf("}") + 1);
            //  String substring2 = substring.substring(substring.indexOf("{"), tempbuilder.lastIndexOf("}") + 1);
            // System.out.println("substring: "+substring);
            JSONObject myResponse =  new JSONObject(substring.substring(substring.indexOf("{"), substring.lastIndexOf("}") + 1));
            //  System.out.println("success after api GET request: "+myResponse.toString());
            System.out.println(myResponse.getString("resource"));
            String jsonresult=myResponse.getString("resource");
            JSONObject myResponse2 = new JSONObject(jsonresult);
            System.out.println("success after api GET request: "+myResponse2.toString());
            result = myResponse2.getString("id");
        }
        catch (ClientProtocolException | JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public boolean ApiGet(String ident,String ResourceType )
    {
        String result ="1";
        boolean found =false;
        int total=0;
        int id=0;
        try{
            HttpClient httpClient    = HttpClientBuilder.create().build();
            HttpGet getreq = new HttpGet(uri+ResourceType+"?identifier="+ident);
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
            System.out.println("success after api GET request: "+tempbuilder);
            JSONObject myResponse =  new JSONObject(tempbuilder.substring(tempbuilder.indexOf("{"), tempbuilder.lastIndexOf("}") + 1));

//            String substring = tempbuilder.substring(tempbuilder.indexOf("entry"), tempbuilder.lastIndexOf("}") + 1);
//            JSONObject myResponse3 =  new JSONObject(substring.substring(substring.indexOf("{"), substring.lastIndexOf("}") + 1));
//            String jsonresult=myResponse3.getString("resource");
//            JSONObject myResponse2 = new JSONObject(jsonresult);
//            System.out.println("success after api GET request: "+myResponse2.toString());
//            result = myResponse2.getString("id");

            total=Integer.parseInt(myResponse.getString("total"));


            if (total>0)
            {
                found=true;
            }
            else { found=false;}
        }
        catch (ClientProtocolException | JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return found;
    }

}
