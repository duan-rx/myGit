package trainAll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;


/**
 * Servlet implementation class DbManager
 */
@WebServlet("/DbManager")
public class DbManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	ServletConfig config;
	private static String dbUserName="root";
	private static String dbUserPassword="1606010038@drxdrx";
	private static String dbUrl="jdbc:mysql://47.96.184.211:3306/train?useSSL=false";
	private static Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DbManager() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		this.config=config;
		//dbUserName=config.getInitParameter("DbUserName");
		//dbUserPassword=config.getInitParameter("DbUserPassword");
		//dbUrl=config.getInitParameter("ConnectionURL");
		
	}
	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance(); 
		}catch(Exception e) {
			System.out.println("jdbc:"+e.getMessage());
		}
		try {
			connection=DriverManager.getConnection(dbUrl,dbUserName,dbUserPassword);
		}catch(Exception e) {
			connection=null;
			System.out.println("connection:"+e.getMessage());
		}
		return connection;
	}
	
	public static void closeAll(Connection conn, Statement state,ResultSet resultSeet) {
		try {
			if(state!=null) {
				state.close();
			}
			if(conn!=null) {
				conn.close();
			}
		}catch(Exception e) {
			
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
