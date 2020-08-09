package trainAll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

public class UserDao {
	public static User queryUser (String userName) {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		
		ResultSet resultSet=null;
		
		StringBuilder sql=new StringBuilder();
		sql.append("select * from user where userName=? or userPhone=?");
		User user=new User();
		try {
			pre=conn.prepareStatement(sql.toString());
			pre.setString(1, userName);
			pre.setString(2, userName);
			resultSet=pre.executeQuery();
			if(resultSet.next()) {
				user.userId=resultSet.getString("userId");
				user.userPassword=resultSet.getString("userPassword");	
				
				System.out.println(resultSet.getString("userPassword"));
				
				user.userName=resultSet.getString("userName");
				user.userPhone=resultSet.getString("userPhone");	
				user.userPower=resultSet.getString("userPower");
				user.userStatus=resultSet.getString("userStatus");	
			}
			return user;
		}catch(Exception e) {
			System.out.println("queryUser:"+e.getMessage());
			return user;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			
		}	
		
	}
	
	public static Boolean queryMsg(String userId,String userName ,String userPhone) {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		
		ResultSet resultSet=null;
		
		StringBuilder sql=new StringBuilder();
		sql.append("select * from user where  userId=? or userName=? or userPhone=?");
		try {
			pre=conn.prepareStatement(sql.toString());
			pre.setString(1, userId);
			pre.setString(2, userName);
			pre.setString(3, userPhone);
			
			System.out.println(pre.toString());
			System.out.println(userPhone);
			
			System.out.println("row:"+pre.execute());
			if(pre.executeQuery().getRow()>0) {
				return true;
			}
			else {
				return false;
			}
		}catch(Exception e) {
			System.out.println("queryUser:"+e.getMessage());
			return false;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			
		}	
		
	}
	
	
	public static int insertUser(User user) {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		
		ResultSet resultSet=null;
		
		StringBuilder sql=new StringBuilder();
		sql.append("insert into user(userId,userName,userPhone,userPassword,userPower,userStatus) values(?,?,?,?,?,?) ");
		try {
			pre=conn.prepareStatement(sql.toString());
			pre.setString(1, user.userId);
			pre.setString(2, user.userName);
			pre.setString(3, user.userPhone);
			pre.setString(4, user.userPassword);
			pre.setString(5, user.userPower);
			pre.setString(6, user.userStatus);
			return pre.executeUpdate();
			
		}catch(Exception e) {
			System.out.println("insertUser:"+e.getMessage());
			return 0;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			
		}	
		
	}
	
	public static int changePassword(User user) {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		
		ResultSet resultSet=null;
		
		StringBuilder sql=new StringBuilder();
		sql.append("update user set userPassword=? where userId=? ");
		try {
			pre=conn.prepareStatement(sql.toString());
			pre.setString(2, user.userId);
			pre.setString(1, user.userPassword);
			return pre.executeUpdate();
			
		}catch(Exception e) {
			System.out.println("insertUser:"+e.getMessage());
			return 0;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			
		}	
		
	}
	
	public static int changeUsrMsg(User user) {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		
		ResultSet resultSet=null;
		
		StringBuilder sql=new StringBuilder();
		sql.append("update user set userName=?, userPhone=?, userStatus=?, userPower=? where userId=? ");
		try {
			pre=conn.prepareStatement(sql.toString());
			pre.setString(1, user.userName);
			pre.setString(2, user.userPhone);
			pre.setString(3, user.userStatus);
			pre.setString(4, user.userPower);
			pre.setString(5, user.userId);
			
			return pre.executeUpdate();
			
		}catch(Exception e) {
			System.out.println("insertUser:"+e.getMessage());
			return 0;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			
		}	
		
	}
	
	public static JSONObject queryUser () {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		
		ResultSet resultSet=null;
		
		StringBuilder sql=new StringBuilder();
		sql.append("select * from user");
		JSONObject json= new JSONObject();
		
		try {
			pre=conn.prepareStatement(sql.toString());
			resultSet=pre.executeQuery();
			int i=0;
			while(resultSet.next()) {
				Map<String,String> map= new HashMap<>();
				map.put("userId",resultSet.getString("userId"));
				map.put("userName",resultSet.getString("userName"));
				map.put("userPhone",resultSet.getString("userPhone"));
				map.put("userPassword",resultSet.getString("userPassword"));	
				map.put("userStatus",resultSet.getString("userStatus"));	
				map.put("userPower",resultSet.getString("userPower"));
				json.put(i, map.toString());
				i++;
					
			}
			json.put("Result",i);
			return json;
		}catch(Exception e) {
			System.out.println("queryUser:"+e.getMessage());
			json.put("Result","faild");
			return json;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			
		}	
		
	}
	
	
	
	
	
	
}
