package com.jesusthecat.im.pggeog;

import javax.xml.bind.DatatypeConverter;
import java.sql.*;

public class BinaryGeogTest {

    private static final String OperaHouseWkt = "SRID=4326;Point(151.215289 -33.856885)";
    private static final String OperaHouseWkb = "0101000020E610000009C6C1A5E3E662406BB75D68AEED40C0";

    private final String url;
    private final String user;
    private final String pass;

    public BinaryGeogTest(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public void go() throws SQLException {
        Connection conn = getConnection();
        createTestTable(conn);

        insertAsGeogFromWkt(conn, OperaHouseWkt);
        insertAsGeogFromWkb(conn, hexToByteArray(OperaHouseWkb));

        printSummaryTable(conn);
        conn.close();
    }

    // ------------------------------------------------------------------------

    private void printSummaryTable(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("select id, pt, ST_Summary(pt) from px");
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(rs.getLong(1));
            sb.append(", WKB: ").append(rs.getString(2));
            sb.append(", Summary:").append(rs.getString(3));
            System.out.println(sb.toString());
        }
    }

    private void insertAsGeogFromWkt(Connection conn, String wkt) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("insert into px(pt) values(ST_GeographyFromText(?))");
        ps.setString(1, wkt);
        ps.execute();
    }

    private void insertAsGeogFromWkb(Connection conn, byte[] wkb) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("insert into px(pt) values(?)");
        ps.setBytes(1, wkb);
        ps.execute();
    }

    private void createTestTable(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        st.execute("create table px (id SERIAL PRIMARY KEY, pt GEOGRAPHY(Point, 4326));");
        st.close();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }

    private byte[] hexToByteArray(String hex) {
        return DatatypeConverter.parseHexBinary(hex);
    }

    // ------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        new BinaryGeogTest("jdbc:postgresql:DB", "USER", "PASSWORD").go();
    }
}
