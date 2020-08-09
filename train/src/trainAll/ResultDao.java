package trainAll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

public class ResultDao {
	public static int insertResult (JSONObject json) {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		StringBuilder sql=new StringBuilder();
		sql.append("insert into result(userId,number,itemName,itemResult,itemTime) values");
		int i=0;
		String resultCount=json.getString("resultCount");
		while(!(""+i).equals(resultCount)){
			JSONObject result=JSONObject.fromObject(json.getString(""+i));
			
			//{"userId":"1","number":"0","itemName":"2","itemResult":"123","itemTime":"2020-04-07 06:54:52"}
			sql.append("('"+result.getString("userId")+"',");
			sql.append("'"+result.getString("number")+"',");
			sql.append("'"+result.getString("itemName")+"',");
			sql.append("'"+result.getString("itemResult")+"',");
			sql.append("'"+result.getString("itemTime")+"')");
			i++;
			if(!(""+i).equals(resultCount))
				sql.append(",");
		}
		try {
			pre=conn.prepareStatement(sql.toString());
			return pre.executeUpdate();
			
			
		}catch(Exception e) {
			System.out.println("insertResultr:"+e.getMessage());
			return 0;
		}
		finally {
			DbManager.closeAll(conn, null, null);
			
		}	
		
	}
	public static int insertHistroy (JSONObject json) {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		StringBuilder sql=new StringBuilder();
		System.out.println("insertResultr:"+json.toString());
		sql.append("insert into history(userId,number,title,studentRemark) values(?,?,?,?)");
		try {
			
			
			pre=conn.prepareStatement(sql.toString());
			pre.setString(1, json.getString("userId"));
			pre.setString(2, json.getString("number"));
			pre.setString(3, json.getString("title"));
			pre.setString(4, json.getString("studentRemark"));
			return pre.executeUpdate();
			
			
		}catch(Exception e) {
			System.out.println("insertResultr:"+e.getMessage());
			return 0;
		}
		finally {
			DbManager.closeAll(conn, null, null);
			
		}	
	}
	public static JSONObject queryHistory (JSONObject json) {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		
		ResultSet resultSet=null;
		JSONObject result=new JSONObject();
		StringBuilder sql=new StringBuilder();
		sql.append("select * from history where  userId=?");
		try {
			pre=conn.prepareStatement(sql.toString());
			pre.setString(1, json.getString("requireHistory"));
			
			resultSet=pre.executeQuery();
			int i=0;
			while(resultSet.next()) {
				
				JSONObject map= new JSONObject();
				map.put("number", resultSet.getString("number"));
				map.put("title", resultSet.getString("title"));
				map.put("commitTime", resultSet.getString("commitTime"));
				System.out.println(resultSet.getString("title"));
				
				result.put(i, map.toString());
				i++;
			}
			
				result.put("Result","queryHistorySuccess");
				result.put("Count",""+i);
				return result;
			
		}catch(Exception e) {
			System.out.println("queryUser:"+e.getMessage());
			 result.put("Result","queryHistoryFaild");
			return result;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			
		}	
		
		
	}
	
	public static JSONObject getResult(JSONObject json) {
		Connection conn=DbManager.getConnection();
		Connection conn2=DbManager.getConnection();
		PreparedStatement pre=null;
		PreparedStatement pre2=null;
		PreparedStatement pre3=null;
		ResultSet resultSet=null;
		ResultSet resultSet2=null;
		JSONObject result=new JSONObject();
		StringBuilder sql=new StringBuilder();
		StringBuilder sql2=new StringBuilder();
		StringBuilder sql3=new StringBuilder();
		sql.append("select itemName,itemResult,itemTime from result where  userId=? and number=?");
		sql2.append("select * from history where userId=? and number=?");
		sql3.append("update history set isCheck=1 where  userId=? and number=?");
		try {
			
			pre=conn.prepareStatement(sql.toString());
			pre.setString(1, json.getString("userId"));
			pre.setString(2, json.getString("number"));
			
			pre2=conn.prepareStatement(sql2.toString());
			pre2.setString(1, json.getString("userId"));
			pre2.setString(2, json.getString("number"));
			
			pre3=conn.prepareStatement(sql3.toString());
			pre3.setString(1, json.getString("userId"));
			pre3.setString(2, json.getString("number"));
			
			resultSet=pre.executeQuery();
			resultSet2=pre2.executeQuery();
			pre3.executeUpdate();
			int i=0;
			while(resultSet.next()) {
				
				JSONObject map= new JSONObject();
				map.put("itemName", resultSet.getString("itemName"));
				map.put("itemResult", resultSet.getString("itemResult"));
				map.put("itemTime", resultSet.getString("itemTime"));
	
				result.put(i, map.toString());
				i++;
			}
		
			while(resultSet2.next()) {
				result.put("teacherRemark",resultSet2.getString("teacherRemark"));
				result.put("studentRemark",resultSet2.getString("studentRemark"));
			}
			result.put("Result","getResultSuccess");
			result.put("Count",""+i);
			
			return result;
			
		}catch(Exception e) {
			System.out.println("getResultError:"+e.getMessage());
			 result.put("Result","getResultfaild");
			return result;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			DbManager.closeAll(conn2, pre2, resultSet2);
			
		}	
			
	}
	
	public static JSONObject deleteHistoryAndResult (JSONObject json) {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		PreparedStatement pre2=null;
		ResultSet resultSet=null;
		JSONObject result=new JSONObject();
		StringBuilder sql=new StringBuilder();
		StringBuilder sql2=new StringBuilder();
		sql.append("delete from history where  userId=? and number=?");
		sql2.append("delete from result where  userId=? and number=?");
		try {
			pre=conn.prepareStatement(sql.toString());
			pre.setString(1, json.getString("userId"));
			pre.setString(2, json.getString("number"));
			
			pre2=conn.prepareStatement(sql2.toString());
			pre2.setString(1, json.getString("userId"));
			pre2.setString(2, json.getString("number"));
			if(pre.executeUpdate()!=0&&pre2.executeUpdate()!=0) {
				result.put("Result","deleteSuccess");
	
			}else {
				result.put("Result","deleteFaild");
			}	
			return result;
			
		}catch(Exception e) {
			System.out.println("queryUser:"+e.getMessage());
			 result.put("Result","faild");
			return result;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			
		}			
	}
	
	public static JSONObject resultManager (JSONObject json) {
		int scan=0;
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		
		ResultSet resultSet=null;
		JSONObject result=new JSONObject();
		String sql="";
		switch(json.getString("ResultManager")) {
			case "userName":
				sql="select user.userName,h.userId,h.number,h.title,h.commitTime,h.isCheck  from history as h,user where user.userId=h.userId order by user.userName desc";
				break;
			case "number":
				sql="select user.userName,h.userId,h.number,h.title,h.commitTime ,h.isCheck from history as h,user where user.userId=h.userId order by h.number desc";
			
				break;
			case "commitTime":
				sql="select user.userName,h.userId,h.number,h.title,h.commitTime,h.isCheck  from history as h,user where user.userId=h.userId order by h.commitTime desc";
				
				break;
			case "isCheck":
				sql="select user.userName,h.userId,h.number,h.title,h.commitTime ,h.isCheck from history as h,user where user.userId=h.userId order by h.isCheck";
				
				break;
			default:
				sql="select user.userName,h.userId,h.number,h.title,h.commitTime ,h.isCheck from history as h,user where user.userId=h.userId and user.userName like ? order by h.isCheck";
				scan=1;
				break;
		}
		
		try {
			pre=conn.prepareStatement(sql);
			if(scan==1) {
				pre.setString(1,"%"+json.getString("ResultManager")+"%");
			}
			resultSet=pre.executeQuery();
			int i=0;
			while(resultSet.next()) {
				
				JSONObject map= new JSONObject();
				System.out.println(resultSet.toString());
				map.put("userId", resultSet.getString("userId"));
				map.put("userName", resultSet.getString("userName"));
				map.put("number", resultSet.getString("number"));
				map.put("title", resultSet.getString("title"));
				map.put("commitTime", resultSet.getString("commitTime"));
				map.put("isCheck", resultSet.getString("isCheck"));
				System.out.println(map.toString());
				
				result.put(i, map.toString());
				i++;
			}
			
				result.put("Result","ResultManagerSuccess");
				result.put("Count",""+i);
				return result;
			
		}catch(Exception e) {
			System.out.println("queryUser:"+e.getMessage());
			 result.put("Result","ResultManagerFaild");
			return result;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			
		}	
		
		
	}
	
	public static JSONObject insertHistoryTeacherRemark (JSONObject json) {
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		StringBuilder sql=new StringBuilder();
		JSONObject result= new JSONObject();
		System.out.println("insertResultr:"+json.toString());
		sql.append("update history set teacherId=?, teacherRemark=?,commentTime=? where userId=? and number=?");
		try {
			
			
			pre=conn.prepareStatement(sql.toString());
			pre.setString(1, json.getString("teacherId"));
			pre.setString(2, json.getString("teacherRemark"));
			pre.setString(3, ""+new Timestamp(System.currentTimeMillis()));
			pre.setString(4, json.getString("userId"));
			pre.setString(5, json.getString("number"));
			if(pre.executeUpdate()>0){
				result.put("Result","insertTeacherRemarkSuccess");
				return result;
			}
			else {
				result.put("Result","insertTeacherRemarkFaild");
				return result;
			}
			
		}catch(Exception e) {
			System.out.println("insertHistoryTeacherRemarkError:"+e.getMessage());
			result.put("Result","insertTeacherRemarkFaild");
			return result;
		}
		finally {
			DbManager.closeAll(conn, null, null);
			
		}	
	}
	public static JSONObject shouYeQuery (JSONObject json) {
		int scan=0;
		Connection conn=DbManager.getConnection();
		PreparedStatement pre=null;
		System.out.println("1:"+json.getString("shouYeQuey"));
		ResultSet resultSet=null;
		JSONObject result=new JSONObject();
		String sql="";
		switch(json.getString("shouYeQuey")) {
			case "1":
				sql="select user.userName,h.title,h.commitTime  from history as h,user where user.userId=h.userId and h.isCheck='0' order by user.userName desc";
				break;
			case "0":
				sql="select user.userName,h.title,h.commentTime  from history as h,user where h.isCheck='1' and h.userId=? and user.userId=h.teacherId order by user.userName desc";
				scan=1;
				break;
		}
		
		try {
			pre=conn.prepareStatement(sql);
			if(scan==1) {
				pre.setString(1,json.getString("userId"));
			}
			resultSet=pre.executeQuery();
			int i=0;
			while(resultSet.next()) {
				
				JSONObject map= new JSONObject();
		
				map.put("userName", resultSet.getString("userName"));
				map.put("title", resultSet.getString("title"));
				if(scan==1) {
					System.out.println("1");
					map.put("commentTime", resultSet.getString("commentTime"));
				}
				else {
					System.out.println("0");
					map.put("commitTime", resultSet.getString("commitTime"));
				}
		
				System.out.println(map.toString());
				
				result.put(i, map.toString());
				i++;
			}
			
				result.put("Result","shouYeQuerySuccess");
				result.put("Count",""+i);
				return result;
			
		}catch(Exception e) {
			System.out.println("queryUser:"+e.getMessage());
			 result.put("Result","shouYeQueryFaild");
			return result;
		}
		finally {
			DbManager.closeAll(conn, pre, resultSet);
			
		}	
		
		
	}
}
