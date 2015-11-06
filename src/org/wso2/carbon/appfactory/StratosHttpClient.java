package org.wso2.carbon.appfactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by punnadi on 8/12/15.
 */
public class StratosHttpClient {

    private static final Log log = LogFactory.getLog(StratosHttpClient.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";

    public static ServerResponse sendPostRequest(String body, String endPointUrl, String tenantAdmin, String password)
            throws Exception {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        PostMethod postMethod = new PostMethod(endPointUrl);

        // password as garbage value since we authenticate with mutual ssl
        postMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue(tenantAdmin, password));
        StringRequestEntity requestEntity;
        try {
            requestEntity = new StringRequestEntity(body, "application/json", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String msg = "Error while setting parameters";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        postMethod.setRequestEntity(requestEntity);
        return StratosHttpClient.send(httpClient, postMethod);
    }

    public static ServerResponse sendDeleteRequest(String endPointUrl, String tenantAdmin, String password)
            throws Exception {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        DeleteMethod deleteMethod = new DeleteMethod(endPointUrl);
        deleteMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue(tenantAdmin, password));
        return StratosHttpClient.send(httpClient, deleteMethod);
    }

    public static ServerResponse sendGetRequest(String endPointUrl, String tenantAdmin)
            throws Exception {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        GetMethod getMethod = new GetMethod(endPointUrl);
        getMethod.setRequestHeader(AUTHORIZATION_HEADER, getAuthHeaderValue(tenantAdmin, ""));
        return StratosHttpClient.send(httpClient, getMethod);
    }

    private static String getAuthHeaderValue(String username, String password) {
        byte[] getUserPasswordInBytes = (username + ":" + password).getBytes();
        String encodedValue = new String(Base64.encodeBase64(getUserPasswordInBytes));
        return "Basic " + encodedValue;
    }

    private static ServerResponse  send(HttpClient httpClient, HttpMethodBase method) throws Exception {
        int responseCode;
        String responseString = null;
        try {
            responseCode = httpClient.executeMethod(method);
        } catch (IOException e) {
            String msg = "Error occurred while executing method " + method.getName();
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        try {
            responseString = method.getResponseBodyAsString();
        } catch (IOException e) {
            String msg = "error while getting response as String for " + method.getName();
            log.error(msg, e);
            throw new Exception(msg, e);

        } finally {
            method.releaseConnection();
        }

        log.info("Response id: " + responseCode + " message:  " + responseString);
        return new ServerResponse(responseString, responseCode);
    }
}
