package com.jiayaming.dubbo.customer.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.jiayaming.dubbo.customer.CustomerService;
import com.jiayaming.dubbo.customer.dao.Test01Mapper;
import com.jiayaming.dubbo.customer.model.Test01;
import com.jiayaming.dubbo.sdk.BeanUtil;
import com.jiayaming.dubbo.sdk.MD5Util;
import com.jiayaming.dubbo.sdk.PatternUtil;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@Service("customerService")
public class CustomerServiceImpl implements CustomerService {

	@Resource
	Test01Mapper test01Mapper;
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
		Map<String,Object> message=null;
		Map<String,Object> user = null;
		//如果参数对象为空,就不做查询
		if(param.get("onCheckInfo")!=null&&param.get("password")!=null){
			//定义需要查询的登陆用户信息
			Map<String,Object> userLoginInfo=new HashMap<String,Object>();
			//获取密码
			String password =param.get("password").toString();
			//校验密码格式,如果不正确直接 返回
		    if(!PatternUtil.isPassword(param.get("password").toString())){
			 return message;
			}
		    
		    //加密
		    userLoginInfo.put("password", MD5Util.MD5Encode(password));
		    //获取未检验的账户信息
			String Info=param.get("onCheckInfo").toString();
			if(PatternUtil.isUsername(Info)){
				userLoginInfo.put("loginName", Info);
			  }else if (PatternUtil.isEmail(Info)){
				userLoginInfo.put("email", Info);
			  }else if(PatternUtil.isMobile(Info)){
				userLoginInfo.put("mobile", Info);
			  }else{return  message;}
			
			//user= userMapper.selectByPasswordAndEmailOrPhoneOrUserName(userLoginInfo);
		    //用户对象存在的情况下
			
		}
		
		return user;
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
		//用来保存产生的验证码
		StringBuffer randomCode = new StringBuffer();
		int length = 4;
		String base="aaaaaaaaaaaaaaaaaaaaaaaaa";
		for (int i = 0; i < length; i++) {
			int start = random.nextInt(randomCode.length());
			String strRand = base.substring(start,start+1);
			//用随机产生的颜色将字符绘制到图片中
			//产生随机颜色
			g.setColor(new Color(20+random.nextInt(150), 20+random.nextInt(150), 20+random.nextInt(150)));
			//绘制字符到图片
			g.drawString(strRand, 15*i+6, 24);
			randomCode.append(strRand);
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
		return null;
	}

}
