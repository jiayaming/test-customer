package com.jiayaming.dubbo.customer.service;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.jiayaming.dubbo.customer.CustomerService;
import com.jiayaming.dubbo.customer.dao.Test01Mapper;
import com.jiayaming.dubbo.customer.model.Test01;
import com.jiayaming.dubbo.sdk.BeanUtil;

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

}
