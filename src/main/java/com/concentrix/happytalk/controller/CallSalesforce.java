package com.concentrix.happytalk.controller;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "happytalk")
public class CallSalesforce {

    private String access_token;
    private static final Logger log = LoggerFactory.getLogger(CallSalesforce.class);
    
    // @RequestMapping(value = "/test")
    @GetMapping(value = "/test")
    public String log4jTest() {

        log.trace("TRACE!!");
        log.debug("DEBUG!!");
        log.info("INFO!!");
        log.warn("WARN!!");
        log.error("ERROR!!");

        return "index";
    }

    // @RequestMapping(value = "/roomstatus")
    @PostMapping(value = "/roomstatus")
    public String oauth(@RequestBody String Response) throws IOException, InterruptedException {

        // logger.info(Response);

        //추가
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(Response);
        JsonNode dataNode = rootNode.get("data");
        String getStatus = dataNode.get("status").asText();
        log.info("status : " + getStatus);

        if(getStatus.equals("RECEIPT_COUNSEL")){
            log.info("1초후 실행!!");
            Thread.sleep(2000);
        }
        //여기까지
        log.info("Response : " + Response);

        //System.out.println("데이터 잘 받았나? : " + Response);

        String getToken = getToken("authorized");
        String callApi = callApi(getToken, Response);

      if(callApi == "Unauthorized"){

        getToken = getToken(callApi);
        callApi(getToken, Response);
        // return callApi(getToken);
        return "Response : " + Response;
      }

    //   return callApi;
    return "Response : " + Response;
    }
 
    //SFDC Token 발급
    @SuppressWarnings("deprecation")
    public String getToken(String getToken) throws IOException{
        log.info("getToken() =>  getToken : " + getToken);
        Properties properties = new Properties();
        FileInputStream input = null;
        try {
            System.out.println("현재 작업 디렉토리: " + System.getProperty("user.dir"));
            properties.load(new FileInputStream("OrgAccess.properties"));

        } catch (IOException ex) {
            log.error("Error => " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // 리소스 해제  
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("Error => " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        String SF_CLIENT_ID     = properties.getProperty("SF_CLIENT_ID");
        String SF_CLIENT_SECRET = properties.getProperty("SF_CLIENT_SECRET");
        String SF_AUTH_ENDPOINT = properties.getProperty("SF_DEVAUTH_ENDPOINT");
        String SF_USERNAME      = properties.getProperty("SF_USERNAME");
        String SF_PASSWORD      = properties.getProperty("SF_PASSWORD");
        String SF_ACCESS_TOKEN  = properties.getProperty("SF_ACCESS_TOKEN");

        log.info("SF_ACCESS_TOKEN : " + SF_ACCESS_TOKEN);
        log.info("SF_ACCESS_TOKEN.length() : " + SF_ACCESS_TOKEN.length());

        if(SF_ACCESS_TOKEN.length() == 0 || getToken == "Unauthorized"){
            CloseableHttpClient client = HttpClients.createDefault();
            
            HttpPost httpPost = new HttpPost(SF_AUTH_ENDPOINT + "/token"); // Request for TOKEN
        
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("grant_type", "password"));
            params.add(new BasicNameValuePair("client_id", SF_CLIENT_ID));
            params.add(new BasicNameValuePair("client_secret", SF_CLIENT_SECRET));
            params.add(new BasicNameValuePair("username", SF_USERNAME));
            params.add(new BasicNameValuePair("password", SF_PASSWORD));
            
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            log.info("params : " + params);
        
            CloseableHttpResponse response = client.execute(httpPost);

            HttpEntity entity = response.getEntity();
            // Read the contents of an entity and return it as a String.
            String content = EntityUtils.toString(entity);
            log.info("### BODY: "+content);
            client.close();

            JsonParser jsonParser = new BasicJsonParser();
            Map<String, Object> jsonMap = jsonParser.parseMap(content);
            this.access_token  = (String)jsonMap.get("access_token");

            properties.setProperty("SF_ACCESS_TOKEN", this.access_token);
            properties.save( new FileOutputStream("OrgAccess.properties"),"this is comment!!");
        }
        
        return properties.getProperty("SF_ACCESS_TOKEN");
    }

    //SFDC Api 호출
    public String callApi(String getToken, String Response) throws IOException{
        log.info("callApi() =>  getToken : " + getToken + ", Response : " + Response);
        Properties properties = new Properties();
        properties.load(new FileInputStream("OrgAccess.properties"));

        String SF_APEX_MAPPING  = properties.getProperty("SF_APEX_MAPPING");

        StringEntity postData = new StringEntity(Response, StandardCharsets.UTF_8);

        

        CloseableHttpClient clientpost = HttpClients.createDefault();
        HttpPost httpPost =  new HttpPost(SF_APEX_MAPPING + "/services/apexrest/happytalk/create");
        log.info("callApi() Call =>" + SF_APEX_MAPPING + "/services/apexrest/happytalk/create");
  
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setHeader("Authorization", "Bearer " + getToken);
        httpPost.setEntity(postData);

        CloseableHttpResponse responsePost = clientpost.execute(httpPost);
  
        log.info("SatusCode : " + responsePost.getStatusLine().getStatusCode());
        int statusCode = responsePost.getStatusLine().getStatusCode();
  
        HttpEntity entityget = responsePost.getEntity();
        log.info("entityget : " + entityget);
        // Read the contents of an entity and return it as a String.
        String contentget = EntityUtils.toString(entityget);
  
        clientpost.close();
        String result = (statusCode == 401 || statusCode == 302) ? "Unauthorized" : contentget;
        log.info("callApi() =>  result : " + result);
        return result;
    }

}
