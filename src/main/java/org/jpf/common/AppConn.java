package org.jpf.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AppConn {
	private AppConn() {
	}

	public static AppConn GetInstance() {
		return new AppConn();
	}

	public Connection GetConn(String dbconection) {
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/samp_db";
		String username = "root";
		String password = "123456";
		Connection conn = null;
		try {
			Class.forName(driver); // classLoader,加载对应驱动
			conn = (Connection) DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

}
