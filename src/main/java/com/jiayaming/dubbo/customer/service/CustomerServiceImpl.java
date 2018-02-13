package com.jiayaming.dubbo.customer.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.jiayaming.dubbo.customer.CustomerService;
import com.jiayaming.dubbo.customer.dao.CustomerInfoMapper;
import com.jiayaming.dubbo.customer.dao.Test01Mapper;
import com.jiayaming.dubbo.customer.model.CustomerInfo;
import com.jiayaming.dubbo.customer.model.Test01;
import com.jiayaming.dubbo.sdk.BeanUtil;
import com.jiayaming.dubbo.sdk.MD5Util;
import com.jiayaming.dubbo.sdk.PatternUtil;
import com.jiayaming.dubbo.sdk.SecurityCode;
import com.jiayaming.dubbo.sdk.SecurityCode.SecurityCodeLevel;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@Service("customerService")
public class CustomerServiceImpl implements CustomerService {

	@Resource
	Test01Mapper test01Mapper;
	@Resource
	CustomerInfoMapper customerInfoMapper;
	
	@Override
	public Map<String, Object> getCustomerInfoByMap(Map<String, Object> map) throws Exception {
		Integer id=Integer.valueOf(map.get("id").toString());
		Test01 test01 = test01Mapper.selectByPrimaryKey(id);
		Map<String,Object> returnMap =  BeanUtil.beanToMap(test01);
		return returnMap;
	}
	/**
	 * 校验用户信息
	 */
	@Override
	public Map<String, Object> validatePasswordByUserInfo(Map<String, Object> param) throws Exception {
		Map<String,Object> returnMap = new HashMap<>();
		//如果参数对象为空,就不做查询
		if (param.get("customerLoginName")==null || param.get("customerLoginName").toString().equals("")) {
			returnMap.put("state", "failed");
	    	returnMap.put("message", "亲，请输入您的登陆信息哦。");
			return returnMap;
		}
		if (param.get("password")==null || param.get("password").toString().equals("")) {
			returnMap.put("state", "failed");
	    	returnMap.put("message", "亲，请输入您的登陆信息哦。");
			return returnMap;
		}
		//校验密码是否是以字母和数字组成的6到16位字符串
		if(!PatternUtil.isPassword(param.get("password").toString())){
	    	returnMap.put("state", "failed");
	    	returnMap.put("message", "亲，请输入您正确的密码信息哦。");
			return returnMap;
		}
		
		//定义需要查询的登陆用户信息
		Map<String,Object> customerLoginInfo=new HashMap<String,Object>();
		//获取密码
		String password =param.get("password").toString();
	    //加密
	    customerLoginInfo.put("password", MD5Util.MD5Encode(password));
	    //获取未检验的账户信息
		String customerLoginName=param.get("customerLoginName").toString();
		if(PatternUtil.isUsername(customerLoginName)){
			customerLoginInfo.put("loginName", customerLoginName);
		}else if (PatternUtil.isEmail(customerLoginName)){
			customerLoginInfo.put("email", customerLoginName);
		}else if(PatternUtil.isMobile(customerLoginName)){
			customerLoginInfo.put("mobile", customerLoginName);
		}else{
			returnMap.put("state", "failed");
	    	returnMap.put("message", "亲，请输入您正确的登录信息哦。");
			return returnMap;
		}
		
	    CustomerInfo customerInfo= customerInfoMapper.selectCustomerByMap(customerLoginInfo);
	    if(customerInfo != null) {
	    	returnMap =  BeanUtil.beanToMap(customerInfo);
	    	returnMap.put("state", "successe");
	    	returnMap.put("message", "亲，您的信息已经验证通过，请开始尽情的玩耍吧！");
	    	return returnMap;
	    }else {
	    	returnMap.put("state", "failed");
	    	returnMap.put("message", "亲，系统没有找到您的信息，请先确认账号和密码是否正确。");
	    	return returnMap;
	    }
	}
	/**
	 * 生成验证码图片
	 */
	@Override
	public Map<String, Object> getValidateCodePicture() throws Exception {
		//验证码图片宽高
		int  width = 75;
		int height = 30;
		BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = buffImg.createGraphics();
		//创建一个随机数生成器
		Random random = new Random();
		//设置图像背景色因为是背景色所以偏淡
		g.setColor((new Color(180+random.nextInt(50), 180+random.nextInt(50), 180+random.nextInt(50))));
		g.fillRect(0, 0, width, height);
		//创建字体字体的大小应该更具图片的高度来定
		Font font = new Font("Times", Font.HANGING_BASELINE, 28);
		//设置字体
		g.setFont(font);
		//画边框
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, width-1, height-1);
		
		g.setColor(new Color(200, 200, 200));
		//产生25条随机干扰线，是图片不易被机器识别
		for (int i = 0; i < 25; i++) {
			int x1 = random.nextInt(width);
			int y1 = random.nextInt(height);
			int x2 = random.nextInt(width);
			int y2 = random.nextInt(height);
			g.drawLine(x1, y1, x2, y2);
		}
		//产生验证码
		String randomCode = SecurityCode.getSecurityCode(4, SecurityCodeLevel.Hard, false);
		for (int i = 0; i < randomCode.length(); i++) {
			String strRand = randomCode.substring(i,i+1);
			//用随机产生的颜色将字符绘制到图片中
			//产生随机颜色
			g.setColor(new Color(20+random.nextInt(150), 20+random.nextInt(150), 20+random.nextInt(150)));
			//绘制字符到图片
			g.drawString(strRand, 15*i+6, 24);
		}
		//图像生成
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(buffImg, "jpeg", out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] bytes = out.toByteArray();
		//base64处理
		BASE64Encoder encoder = new BASE64Encoder();
		//使用base64转码否则在页面中不能直接显示
		String validateCodePicture = encoder.encode(bytes);
		Map<String,Object> map = new HashMap<>();
		map.put("validateCodePicture", validateCodePicture);
		map.put("validateCode", randomCode.toString());
		return map;
	}
	@Override
	public Map<String, Object> saveRegisterInfo(Map<String, Object> map) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		
		String username = map.get("username").toString();
		String password = map.get("password").toString();
		String uuid = UUID.randomUUID().toString();
		Date date = new Date();
		
		Map<String, Object> selectMap = new HashMap<>();
		selectMap.put("email", username);
		CustomerInfo selectCustomerInfo =  customerInfoMapper.selectCustomerByMap(selectMap);
		if(selectCustomerInfo != null) {
			returnMap.put("state", 0);
			returnMap.put("message", "您的账号已经注册");
			return returnMap;
		}
		CustomerInfo customerInfo = new CustomerInfo();
		customerInfo.setEmail(username);
		customerInfo.setPassword(MD5Util.MD5Encode(password));
		customerInfo.setUuid(uuid);
		customerInfo.setUpdateTime(date);
		customerInfo.setCreateTime(date);
		customerInfoMapper.insertSelective(customerInfo);
		
		
		returnMap.put("state", 1);
		return returnMap;
	}

}
