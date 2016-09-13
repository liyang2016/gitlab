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
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jpf.common.AppConn;

public class JsonProject {
	private final Logger logger = LogManager.getLogger();
	private String DELETE_Owners = "delete from json_project_owners";
	private String DELETE_Projects = "delete from json_projects";
	private String INSERT_Owners = "insert into json_projects (id,description,default_branch,public,archived,visibility_level,ssh_url_to_repo,http_url_to_repo,web_url,name_with_namespace,path_with_namespace,created_at,last_activity_at,creator_id)             values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private String INSERT_Projects = "insert into json_project_owners (name,username,id,state,avatar_url) values(?,?,?,?,?)";

	/**
	 * @param args
	 * @throws Exception
	 */
	public JsonProject() throws Exception {

		String address = getContent("http://10.1.130.29/api/v3/projects/all?private_token=eeb1WbPDCVchnr9RxuDE");
		logger.info(address);
		String data = address;

		ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
		mapper.configure(Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<ProjecInfo> list = mapper.readValue(data, new TypeReference<List<ProjecInfo>>() {
		});

		Connection conn = null;
		PreparedStatement pStmt = null;
		PreparedStatement pStmt1 = null;
		conn = AppConn.GetInstance().GetConn(GitlabInterfaceConst.dbConection);
		if (conn != null) {
			// conn.setAutoCommit(false);
			pStmt = conn.prepareStatement(DELETE_Owners);
			pStmt1 = conn.prepareStatement(DELETE_Projects);
			logger.debug(DELETE_Owners);
			pStmt.executeUpdate();
			pStmt1.executeUpdate();
			for (int i = 0; i < list.size(); i++) {
				logger.info(list.get(i));
				pStmt = conn.prepareStatement(INSERT_Owners);
				pStmt.setString(1, list.get(i).getId());
				pStmt.setString(2, list.get(i).getDescription());
				pStmt.setString(3, list.get(i).getDefault_branch());
				pStmt.setString(4, list.get(i).getPublic());
				pStmt.setString(5, list.get(i).getArchived());
				pStmt.setString(6, list.get(i).getVisibility_level());
				pStmt.setString(7, list.get(i).getHttp_url_to_repo());
				pStmt.setString(8, list.get(i).getSsh_url_to_repo());
				pStmt.setString(9, list.get(i).getWeb_url());
				pStmt.setString(10, list.get(i).getName_with_namespace());
				pStmt.setString(11, list.get(i).getPath_with_namespace());
				pStmt.setString(12, list.get(i).getCreated_at());
				pStmt.setString(13, list.get(i).getLast_activity_at());
				pStmt.setString(14, list.get(i).getCreator_id());
				if (list.get(i).getOwner() != null) {
					pStmt1 = conn.prepareStatement(INSERT_Projects);
					pStmt1.setString(1, list.get(i).getOwner()[0].getName());
					pStmt1.setString(2, list.get(i).getOwner()[0].getUsername());
					pStmt1.setString(3, list.get(i).getOwner()[0].getId());
					pStmt1.setString(4, list.get(i).getOwner()[0].getState());
					pStmt1.setString(5, list.get(i).getOwner()[0].getAvatar_url());

				}
				pStmt.executeUpdate();
				pStmt1.executeUpdate();
				logger.info("onwers={" + list.get(i).getOwner()[0].toString() + "}");

			}
			conn.commit();
			pStmt.close();
			pStmt1.close();
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
		// String END_OBJECT = "0";

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
			HttpResponse response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();
			if (entity != null) {

				InputStream is = entity.getContent();
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
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
			e.printStackTrace();

		} finally {
			if (httpclient != null)
				httpclient.getConnectionManager().shutdown();
		}
		return backContent;
	}

	public static void main(String[] args) {

		try {
			JsonProject jsonproject = new JsonProject();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
