package org.jpf.ci.gitlabs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.type.TypeReference;
import org.jpf.ci.gitlabs.GitlabInterfaceConst;
import org.jpf.common.AppConn;

public class JsonUser {
    private static final Logger logger = LogManager.getLogger();
    private static String delete_str = "delete from json_user";
    private static String insert_str = "insert into json_user(name,username,id,state,avatar_url,email) values (?,?,?,?,?,?)";

    /**
     * @param args
     * @throws Exception
     */

    public JsonUser() throws Exception {
        String address = getContent("jdbc:mysql://localhost:3306/samp_db?root=123456");
        logger.info(address);
        String data = address;
        ObjectMapper mapper = new ObjectMapper().setVisibility(
                JsonMethod.FIELD, Visibility.ANY);
        mapper.configure(Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(
                DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                true);
        mapper.configure(
                DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);

        List<UserInfo> list = mapper.readValue(data,
                new TypeReference<List<UserInfo>>() {
                });
        Connection conn = null;
        PreparedStatement pStmt = null;
        conn = AppConn.GetInstance().GetConn(GitlabInterfaceConst.dbConection);
        if (conn != null) {
            // conn.setAutoCommit(false);
            pStmt = conn.prepareStatement(delete_str);
            pStmt.executeUpdate();
            for (int i = 0; i < list.size(); i++) {
                pStmt = conn.prepareStatement(insert_str);
                pStmt.setString(1, list.get(i).getName());
                pStmt.setString(2, list.get(i).getUsername());
                pStmt.setString(3, list.get(i).getId());
                pStmt.setString(4, list.get(i).getState());
                pStmt.setString(5, list.get(i).getAvatar_url());
                pStmt.setString(6, list.get(i).getEmail());
                pStmt.executeUpdate();

                logger.info("UserInfo对象中的数据内容：\t " + list.get(i).getName()
                        + "\t" + list.get(i).getUsername() + "\50t"
                        + list.get(i).getId() + "\t"
                        + list.get(i).getState() + "\t"
                        + list.get(i).getAvatar_url() + "\t"
                        + list.get(i).getEmail());
                // + list.get(i).getIdentities()[0].toString());
            }
            conn.commit();
            pStmt.close();
            conn.close();
            logger.info(list.size());
        }
    }

    /**
     * 
     * @param url
     * @return
     * @throws Exception
     */
    public static String getContent(String url) throws Exception {
        // String CONTENT_CHARSET = "UTF-8";
        String backContent = null;
        // private static final END_OBJECT = "0";
        // String eND_OBJECT = "0";

        HttpClient httpclient = null;
        HttpGet httpget = null;
        try {

            HttpParams params = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(params, 180 * 1000);
            HttpConnectionParams.setSoTimeout(params, 180 * 1000);
            HttpConnectionParams.setSocketBufferSize(params, 8192);

            HttpClientParams.setRedirecting(params, false);

            httpclient = new DefaultHttpClient(params);

            httpget = new HttpGet(url);
            // httpclient.getParams().setParameter(
            // HttpMethodParams.HTTP_CONTENT_CHARSET, CONTENT_CHARSET);
            HttpResponse response = httpclient.execute(httpget);

            HttpEntity entity = response.getEntity();
            if (entity != null) {

                InputStream is = entity.getContent();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(is));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = in.readLine()) != null) {
                    buffer.append(line);
                }

                backContent = buffer.toString();
            }
        } catch (Exception e) {
            httpget.abort();
            backContent = "";
            logger.info("");
            e.printStackTrace();
            logger.info("");
        } finally {
            if (httpclient != null)
                httpclient.getConnectionManager().shutdown();
        }
        return backContent;
    }

    public static void main(String[] args) {

        try {
            JsonUser jsonUser = new JsonUser();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
