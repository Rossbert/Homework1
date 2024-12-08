<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import = "java.sql.*" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Database SQL Load</title>
    </head>
    <style>
        .error {
            color: red;
        }
        pre {
            color: green;
        }
    </style>
    <body>
        <h2>Database SQL Load</h2>
        <%
            String dbname = "homework1";
            String schema = "ROOT";
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            Connection con = null;

            try {
                con = DriverManager.getConnection("jdbc:derby://localhost:1527/" + dbname, "root", "root");
                Statement stmt = con.createStatement();

                // Check if tables exist; if not, create them
                String[] tableChecks = {
                    "SELECT 1 FROM " + schema + ".TOPIC FETCH FIRST 1 ROW ONLY",
                    "SELECT 1 FROM " + schema + ".COMMENT FETCH FIRST 1 ROW ONLY",
                    "SELECT 1 FROM " + schema + ".CREDENTIALS FETCH FIRST 1 ROW ONLY"
                };

                boolean tablesExist = true;

                for (String checkQuery : tableChecks) {
                    try (ResultSet rs = stmt.executeQuery(checkQuery)) {
                        // Query executed successfully, table exists
                    } catch (SQLException e) {
                        // If the table doesn't exist, set flag to false
                        tablesExist = false;
                        break;
                    }
                }

                if (!tablesExist) {
                    out.println("<span class='error'>Some tables are missing. Creating tables...</span><br>");
                    stmt.executeUpdate("CREATE TABLE " + schema + ".TOPIC (" +
                        "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "name VARCHAR(255) NOT NULL)");
                    
                    stmt.executeUpdate("CREATE TABLE " + schema + ".COMMENT (" +
                        "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "text VARCHAR(255) NOT NULL, " +
                        "topic_id BIGINT, " +
                        "FOREIGN KEY (topic_id) REFERENCES TOPIC(id))");

                    stmt.executeUpdate("CREATE TABLE " + schema + ".CREDENTIALS (" +
                        "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "username VARCHAR(255) NOT NULL, " +
                        "password VARCHAR(255) NOT NULL)");
                    
                    out.println("<span class='error'>Tables created successfully.</span><br>");
                }

                // Insert initial data only if it doesn't already exist
                String[] data = new String[] {
                    "INSERT INTO " + schema + ".TOPIC VALUES (NEXT VALUE FOR TOPIC_GEN, 'Computer Science')",
                    "INSERT INTO " + schema + ".COMMENT VALUES (NEXT VALUE FOR COMMENT_GEN, 'Skeleton code', 1)",
                    "INSERT INTO " + schema + ".COMMENT VALUES (NEXT VALUE FOR COMMENT_GEN, 'for homework1', 1)",
                    "INSERT INTO " + schema + ".CREDENTIALS VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'sob', 'sob')",
                    "INSERT INTO " + schema + ".CREDENTIALS VALUES (NEXT VALUE FOR USER_GEN, 'john_doe', 'john123')",
                    "INSERT INTO " + schema + ".CREDENTIALS VALUES (NEXT VALUE FOR USER_GEN, 'jane_doe', 'jane123')"
                };

                for (String datum : data) {
                    try {
                        stmt.executeUpdate(datum);
                        out.println("<pre> -> " + datum + "</pre>");
                    } catch (SQLException e) {
                        out.println("<span class='error'>Error inserting: " + datum + "</span><br>");
                    }
                }
            } catch (Exception e) {
                out.println("<span class='error'>Failed to connect or initialize database: " + e.getMessage() + "</span><br>");
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        %>
        <button onclick="window.location='<%=request.getSession().getServletContext().getContextPath()%>'">Go home</button>
    </body>
</html>
