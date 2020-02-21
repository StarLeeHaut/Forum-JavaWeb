package cn.cqut.edu.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BaseDao {

	// ����URL
	private static final String url = "jdbc:mysql://localhost:3306/supermarket?useUnicode=true&characterEncoding=UTF-8";//"jdbc:mysql://127.0.0.1:3306/supermarket?";
	private static final String username = "root";
	private static final String password = "456123";
	private static final String jdbcDriver = "com.mysql.jdbc.Driver";
	protected boolean pmdKnownBroken = false;

	public BaseDao() {
		System.out.println("construct success!");
	}

	public BaseDao(boolean pmdKnownBroken) {
		this.pmdKnownBroken = pmdKnownBroken;
	}
	public Connection getConnetion() {
		Connection conn = null;
		try {
			System.out.println("before the driver!");
			Class.forName("com.mysql.jdbc.Driver"); //com.mysql.cj.jdbc.Driver
			System.out.println("MySQL Driver success!");
			conn = DriverManager.getConnection(url, "root","456123");
			System.out.println("DataBase success!");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return conn;
	}

	/** ���ݸ����Ĳ��� ִ��Sql ��ѯ��䣬�ѽ�����Ϸ���һ�� List<Map<String,Object>> ���� */
	public List<Map<String, Object>> executeQuery(String sql, Object[] params) {
		return (List<Map<String, Object>>) this.excuteQuery(sql, params, new ListMapHander());
	}

	/** �鴫������sql���, ��������Ҫ�Ľӿڵķ�����������,��������Ҫ�Ľ�����ĸ�ʽ */
	public Object excuteQuery(String sql, Object[] params, ResultSetHander rsh) {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		Connection con = this.getConnetion();
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		try {
			stmt = con.prepareStatement(sql);

			System.out.println("SQL:" + sql + "; Parameters:" + Arrays.deepToString(params));
			// ���Statement�Ĳ���
			fillStatement(stmt, params);
			// ִ�в�ѯ
			rs = stmt.executeQuery();
			Object obj = rsh.doHander(rs);
			return obj;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(con, stmt, rs);// �ر����ݿ�����
		}
		return resultList;
	}

	/**
	 * ���²���  ����ֵ1���ɹ�
	 */
	public int executeUpdate(String sql, Object[] params) {
		PreparedStatement stmt = null;
		Connection con = this.getConnetion();

		int rs = 0;
		try {
			con.setAutoCommit(false);
			// ����PreparedStatement����
			stmt = con.prepareStatement(sql);
			// ���Statement�Ĳ���
			fillStatement(stmt, params);
			System.out.println("SQL:" + sql + "; Parameters:"
					+ Arrays.deepToString(params));
			// ִ�в�ѯ
			rs = stmt.executeUpdate();//�ٶ���˵sql��executeUpdate�����������Ƿ���0
			// �ύ����
			con.commit();
			// ����������Ϊԭ����״̬
			con.setAutoCommit(true);
		} catch (SQLException e) {
			// �ڲ����쳣��ʱ������ع�
			e.printStackTrace();
			try {
				con.rollback();
				if (!con.getAutoCommit()) {
					con.setAutoCommit(true);
				}
			} catch (SQLException e1) {
				e.printStackTrace();
				System.out.println("update database error");
			}
			System.out.println("update database error");
		} finally {
			// �ر����ݿ�����
			close(con, stmt, null);
		}
		return rs;
	}

	/*
	 * @Title: fillStatement
	 * @Description: ���SQL����
	 * @param stmt
	 * @param params
	 * @throws SQLException
	 * @return void
	 */
	private void fillStatement(PreparedStatement stmt, Object[] params)
			throws SQLException {

		/**
		 * �������ĸ����Ƿ�Ϸ��������е����ݿ�������֧�� stmt.getParameterMetaData()���������
		 * ���������һ��һ��pmdKnownBroken ��������ʶ��ǰ���������Ƿ�֧�ָ÷����ĵ��á�
		 */
		ParameterMetaData pmd = null;
		if (!pmdKnownBroken) {
			pmd = stmt.getParameterMetaData();
			int stmtCount = pmd.getParameterCount();
			int paramsCount = params == null ? 0 : params.length;

			if (stmtCount != paramsCount) {
				System.out.println("stmtCount:" + stmtCount + ",paramsCount:"
						+ paramsCount);
				throw new SQLException("Wrong number of parameters: expected "
						+ stmtCount + ", was given " + paramsCount);
			}
		}

		// ��� ���� Ϊ null ֱ�ӷ���
		if (params == null) {
			return;
		}

		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {
				stmt.setObject(i + 1, params[i]);
			} else {
				int sqlType = Types.VARCHAR;
				if (!pmdKnownBroken) {
					try {
						sqlType = pmd.getParameterType(i + 1);
					} catch (SQLException e) {
						pmdKnownBroken = true;
					}
				}
				stmt.setNull(i + 1, sqlType);
			}
		}
	}

	/**
	 * �ر����ݿ�����
	 */
	private void close(Connection con, Statement stmt, ResultSet rs) {

		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						if (con != null) {
							try {
								con.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
}