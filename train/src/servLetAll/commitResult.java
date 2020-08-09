package servLetAll;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import constant.convertStream;
import net.sf.json.JSONObject;
import trainAll.ResultDao;
import trainAll.User;
import trainAll.UserDao;

/**
 * Servlet implementation class commitResult
 */
@WebServlet("/commitResult")
public class commitResult extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public commitResult() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
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
		String s=convertStream.readFromInputStream(request.getInputStream());
		
		
		JSONObject json=JSONObject.fromObject(s);
		if(json.has("shouYeQuey")) {
			System.out.println(json.toString());
			response.getWriter().write(ResultDao.shouYeQuery(json).toString());	
		}
		if(json.has("teacheWriteRemark")) {
			System.out.println(json.toString());
			response.getWriter().write(ResultDao.insertHistoryTeacherRemark(json).toString());	
		}
		if(json.has("ResultManager")) {
			System.out.println(json.toString());
			response.getWriter().write(ResultDao.resultManager(json).toString());	
		}
		if(json.has("getResult")) {
			System.out.println(json.toString());
			response.getWriter().write(ResultDao.getResult(json).toString());	
		}
		if(json.has("deleteResult")) {
			System.out.println(json.toString());
			response.getWriter().write(ResultDao.deleteHistoryAndResult(json).toString());
		}
		if(json.has("requireHistory")) {
			System.out.println(json.toString());
			response.getWriter().write(ResultDao.queryHistory(json).toString());
		}
		if(json.has("uploadeCommitMsg")) {
			JSONObject json2=JSONObject.fromObject(json.getString("uploadeCommitMsg"));		
			if(ResultDao.insertHistroy(json2)==0) {
				JSONObject jsonReturn=new JSONObject();
				jsonReturn.put("Result","failed");
				response.getWriter().write(jsonReturn.toString());
			
			}
		}
		if(json.has("uploadeResult")) {
			JSONObject json2=JSONObject.fromObject(json.getString("uploadeResult"));
			if(ResultDao.insertResult(json2)!=0) {
				JSONObject jsonReturn=new JSONObject();
				jsonReturn.put("Result","success");
				response.getWriter().write(jsonReturn.toString());
			}
			else {
				JSONObject jsonReturn=new JSONObject();
				jsonReturn.put("Result","failed");
				response.getWriter().write(jsonReturn.toString());
			}
			
		}
		
		
	}
	

}
