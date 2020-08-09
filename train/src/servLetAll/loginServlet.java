package servLetAll;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import trainAll.UserDao;
import trainAll.DbManager;
import trainAll.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Servlet implementation class loginServlet
 */
@WebServlet("/loginServlet")

public class loginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public loginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub


		 response.setContentType("text/html;charset=utf-8");
		 request.setCharacterEncoding("utf-8");
		 response.setCharacterEncoding("utf-8");
		 JSONObject jsonObject= new JSONObject();
		 try(PrintWriter out=response.getWriter()){
			
			 String userName=new String(request.getParameter("userName").trim().getBytes("iso8859-1"),"UTF-8");
			 String userPassword=new String(request.getParameter("userPassword").trim().getBytes("iso8859-1"),"UTF-8");
			 System.out.println("get="+userName);
			 System.out.println("get="+userPassword);
			 JSONObject params= new JSONObject();
			 //JSONObject params=new JSONObject();
			 User user=UserDao.queryUser(userName);
			 System.out.println("password="+user.userPassword);
			 if(null!=user&&user.userStatus.equals("3")){
				 params.put("userId",user.userId);
				 params.put("userName",user.userName);
				 params.put("userPhone",user.userPhone);
				 params.put("userPower",user.userPower);
				 params.put("userStatus",user.userStatus);
				 jsonObject.put("Result",params.toString());
				 response.getWriter().write(jsonObject.toString());
				 return;
				}
			 if(null!=user&&userPassword.equals(user.userPassword)){
				 params.put("userId",user.userId);
				 params.put("userName",user.userName);
				 params.put("userPhone",user.userPhone);
				 params.put("userPower",user.userPower);
				 params.put("userStatus",user.userStatus);
				 jsonObject.put("Result",params.toString());
				 response.getWriter().write(jsonObject.toString());
				 
			 }
			 else {
				 jsonObject.put("Result","faild");
				
			 }
			
		 }
		 catch(Exception e) {
			 System.out.println("errr="+e.getMessage());
			 
		 }
		 
	}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request,response);
		System.out.println(readFromInputStream(request.getInputStream()));
		
	}

private String readFromInputStream(InputStream in) throws IOException {
		         ByteArrayOutputStream baos = new ByteArrayOutputStream();
		       byte[] buffer = new byte[1024];
		         int len = -1;
		         while ((len = in.read(buffer)) != -1) {
		             baos.write(buffer, 0, len);
		         }
		         baos.close();
		         in.close();
		       
		        byte[] lens = baos.toByteArray();
		        String result = new String(lens,"UTF-8");//ÄÚÈÝÂÒÂë´¦Àí
		        
		        return result;
		    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
