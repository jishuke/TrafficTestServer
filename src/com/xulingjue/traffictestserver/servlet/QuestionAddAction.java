package com.xulingjue.traffictestserver.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.*;
import org.hibernate.cfg.*;
import org.apache.jasper.tagplugins.jstl.core.Set;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.xulingjue.traffictestserver.servlet.dao.Question;
import com.xulingjue.traffictestserver.servlet.dao.QuestionDAO;
import com.xulingjue.traffictestserver.servlet.dao.Selection;

public class QuestionAddAction extends ActionSupport {
	private String act;
	private String title;// 问题
	private String option[];// 选项
	private File upload;
	private String uploadContentType;
	private String uploadFileName;
	private String savePath;
	private String isRight[];
	private QuestionDAO questionDAO;
	
	public void setQuestionDAO(QuestionDAO questionDAO) {
		this.questionDAO = questionDAO;
	}

	public String[] getIsRight() {
		return isRight;
	}

	public void setIsRight(String[] isRight) {
		this.isRight = isRight;
	}

	public String[] getOption() {
		return option;
	}

	public void setOption(String[] option) {
		this.option = option;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public File getUpload() {
		return upload;
	}

	public void setUpload(File upload) {
		this.upload = upload;
	}

	public String getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}

	public String getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	public String getSavePath() {
		// 从配置文件中取出
		return ServletActionContext.getServletContext().getRealPath(
				"WEB-INF/" + savePath);
		//return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public String getAct() {
		return act;
	}

	public void setAct(String act) {
		this.act = act;
	}

	// 处理action请求
	public String execute() {
		questionDAO.add();
		if (act == null || act.trim().equals("")) {
			return "showForm";
		}

		
		/*
		 * 插入数据库部分
		 */
		// 实例化Configuration，这行代码默认加载hibernate.cfg.xml文件
		Configuration conf = new Configuration().configure();
		// 以Configuration创建SessionFactory
		SessionFactory sf = conf.buildSessionFactory();
		// 实例化Session
		Session sess = sf.openSession();
		// 开始事务
		Transaction tx = sess.beginTransaction();

		HashSet selections = new HashSet();
		// 读取选项部分
		if (option != null) {
			for (int i = 0; i < option.length; i++) {
				Selection s = new Selection();
				s.setContent(option[i]);
				s.setIsRight(Integer.parseInt(isRight[i]));
				selections.add(s);
			}

		}

		Question q = new Question();
		q.setContent(title);
		q.setSelections(selections);
		// 文件上传部分
		if (upload != null) {
			q.setImg(_uploadFile());
		}else{
			q.setImg("");
		}
		sess.save(q);

		// 批量保存
		Iterator selectionsIter = selections.iterator();
		while (selectionsIter.hasNext()) {
			sess.save((Selection) selectionsIter.next());
		}

		// 提交事务
		tx.commit();
		// 关闭Session
		sess.close();

		return SUCCESS;
	}

	private String _uploadFile() {
		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			fos = new FileOutputStream(getSavePath() + "\\"
					+ getUploadFileName());
			fis = new FileInputStream(getUpload());

			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

			fos.close();
			fis.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// 出错跳转到error界面
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("fos close error!");
				}
			}

			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("fis close error!");
				}
			}
		}
		return savePath+"/"+getUploadFileName();
	}

}
