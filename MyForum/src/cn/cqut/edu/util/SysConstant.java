package cn.cqut.edu.util;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

public class SysConstant {

	// �û��洢�û��ϴ���ͷ����ļ���·��
	public static String HEAD_PATH = "D:\\recruitPortalFile\\head";
	// ����Ŀ¼
	public static String UPLOAD_TEMP = "D:\\recruitPortalFile\\temp";

	static {
		File f = new File(HEAD_PATH);
		if (!f.exists()) {
			f.mkdirs();
		}

		File ft = new File(UPLOAD_TEMP);
		if (!ft.exists()) {
			ft.mkdirs();
		}
	}

	public static int DEFAULT_PAGE_LIMIT = 10;

	private static final String serverName = "recruitPortal";

	// �ҵ����ڿͷ�����˵Servet�ľ���·��?
	public static String serverBasePath(HttpServletRequest request) {
		return request.getScheme() + "://" + request.getServerName() + ":"
				+ request.getServerPort() + "/" + serverName;
	}

}
