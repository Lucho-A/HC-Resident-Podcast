package com.lucho.hc_resident_podcast;

import android.os.StrictMode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL_HOME {
    private static Connection connection = null;
    private static String currentVersion="1.1";

    public MySQL_HOME() throws SQLException, ClassNotFoundException{
        try {
            connection=createConn();
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    public Connection createConn() throws SQLException, ClassNotFoundException {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String connURL="jdbc:mysql://lucho-alfie.ddns.net:6033/hc-resident-podcast";
            connection = DriverManager.getConnection(connURL,"hc-resident-podcast","lsjtndj7439nfcd'!??Dksljal$5");
        } catch (SQLException e) {
            throw e;
        }
        if (connection != null) return connection;
        return null;
    }

    public ResultSet mssql_query(Connection mssqlConn, String q) throws SQLException {
        try {
            Statement stmt = mssqlConn.createStatement();
            ResultSet rs = stmt.executeQuery(q);
            if(rs!=null) return rs;
            return null;
        } catch (SQLException e) {
            throw e;
        }
    }

    public boolean isUpdated() throws SQLException{
        ResultSet rs=mssql_query(connection,"SELECT idVersion FROM Versions WHERE date = (SELECT max(date) from Versions);");
        rs.first();
        if(currentVersion.equals(rs.getString(1))) return true;
        return false;
    }
}
