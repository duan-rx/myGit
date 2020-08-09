package servLetAll;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import trainAll.User;
import trainAll.UserDao;

/**
 * Servlet implementation class changUserMsg
 */
@WebServlet("/changUserMsg")
public class changUserMsg extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public changUserMsg() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html;charset=utf-8");
		 request.setCharacterEncoding("utf-8");
		 response.setCharacterEncoding("utf-8");
		//{"Register":"{userPassword=1, userStatus=1, userPhone=1, userName=1, userId=1, userPower=0}"}
		String s=readFromInputStream(request.getInputStream());
		
		System.out.println(s);
		JSONObject json=JSONObject.fromObject(s);
		
		JSONObject json2=JSONObject.fromObject(json.getString("UserMsg"));
		System.out.println(json2.toString());
		User user=new User();
		user.userId=json2.getString("userId");
		user.userName=json2.getString("userName");
		user.userPhone=json2.getString("userPhone");
		user.userPassword=json2.getString("userPassword");
		user.userPower=json2.getString("userPower");
		user.userStatus=json2.getString("userStatus");
		
		
		if(UserDao.changeUsrMsg(user)!=0) {
			JSONObject jsonReturn=new JSONObject();
			jsonReturn.put("Result","success");
			response.getWriter().write(jsonReturn.toString());
			System.out.println("success");
		}
		else {
			
			JSONObject jsonReturn=new JSONObject();
			jsonReturn.put("Result","failed");
			response.getWriter().write(jsonReturn.toString());
			System.out.println("failed");
			
		}
		
	
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
