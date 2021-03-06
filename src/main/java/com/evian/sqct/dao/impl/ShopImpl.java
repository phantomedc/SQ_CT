package com.evian.sqct.dao.impl;

import com.evian.sqct.bean.shop.*;
import com.evian.sqct.bean.shop.inputDTO.ProcBackstageAppMerchantTodayStatisticsDTO;
import com.evian.sqct.bean.shop.inputDTO.ProcBackstageShopLeagueClientsSelectDTO;
import com.evian.sqct.bean.shop.inputDTO.ProcBackstageShopSelectDTO;
import com.evian.sqct.bean.shop.inputDTO.ProcDisParkGetTicketAccountDTO;
import com.evian.sqct.bean.shop.writerDTO.ShowShopInfoDTO;
import com.evian.sqct.dao.IBaseDao;
import com.evian.sqct.dao.IShopDao;
import com.evian.sqct.util.DaoUtil;
import com.evian.sqct.util.ResultSetToBeanHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.*;

@Repository("shopDao")
public class ShopImpl implements IShopDao {

	@Autowired
	@Qualifier("primaryJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private IBaseDao baseDao;

	@Override
	public String selectUserAllShop() {
		return null;
	}

	/**
	 * 增加水店
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String addShop(final Integer shopId,final Integer eid, final String shopNo,
			final String shopName, final String address, final String tel,
			final String linkman, final Integer cityId,
			final Integer districtId, final String location,
			final String description, final String pictureUrl,
			final String shopType, final String startTime,
			final String endTime, final String scopeDescription,
			final Integer sendOnTime, final Double minSendPrice,
			final Double freight, final String createUser,
			final Boolean ifLine, final Integer userId) {
		String result = (String)jdbcTemplate
				.execute("{call Proc_Backstage_shop_operat(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}",
						(CallableStatementCallback) cs -> {
							cs.setInt("shopId", shopId);
							cs.setInt("eid", eid);
							cs.setString("shopNo", shopNo);
							cs.setString("shopName", shopName);
							cs.setString("address", address);
							cs.setString("tel", tel);
							cs.setString("linkman", linkman);
							cs.setInt("cityId", cityId);
							cs.setInt("districtId", districtId);
							cs.setString("location", location);
							cs.setString("description", description);
							cs.setString("pictureUrl", pictureUrl);
							cs.setString("shopType", shopType);
							cs.setString("startTime", startTime);
							cs.setString("endTime", endTime);
							cs.setString("scopeDescription",
									scopeDescription);
							cs.setInt("sendOnTime", sendOnTime);
							cs.setDouble("minSendPrice", minSendPrice);
							cs.setDouble("freight", freight);
							cs.setString("createUser", createUser);
							cs.setBoolean("ifLine", ifLine);
							cs.setInt("accountId", userId);
							cs.setString("TAG", "ADD");
							cs.registerOutParameter("shopId", Types.INTEGER);// 注册输出参数的类型
							cs.registerOutParameter("TAG", Types.NVARCHAR);// 注册输出参数的类型
							cs.execute();

							if (cs.getString("TAG") != null
									&& cs.getString("TAG").toString()
									.equals("1")) {
								return String.valueOf(cs.getInt("shopId"));
							}
							return cs.getString("TAG");
						});
		return result;
	}

	/**
	 * 通过企业id和用户id查询水店
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Map<String,Object>> selectShopByEidAndUserId(final Integer accountId,final Integer eid) {
		List<Map<String,Object>> result = (List<Map<String,Object>>)jdbcTemplate.execute(
				// "{call Proc_Backstage_auth_user_shop_select(?,?)}",
				"{call Proc_Backstage_appMerchant_account_shop_select(?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("accountId", accountId);
					cs.setObject("eid", eid);
					// 注册输出参数的类型
					cs.execute();
					ResultSet rs = cs.executeQuery();
					try {
						return ResultSetToBeanHelper.resultSetToList(rs, Map.class);
					} catch (Exception e) {
						return null;
					}finally{
						if(rs!=null){
							rs.close();
						}
					}
				});
		return result;
	}

	/**
	 * 通过企业id和用户id查询水店
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String,Object> selectShopByEidAndShopId(ProcBackstageShopSelectDTO dto) {

		/*Map result = baseDao.agencyDB("Proc_Backstage_shop_select", dto, FindShopModel.class);
		return (List<FindShopModel>)result.get("result0");*/
		Map result = baseDao.agencyDB("Proc_Backstage_shop_select", dto, ShowShopInfoDTO.class);
		return DaoUtil.resultRename(result,"shoplings");
		/*List<FindShopModel> result = (List<FindShopModel>)jdbcTemplate
				.execute(
						"{call Proc_Backstage_shop_select(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}",
						(CallableStatementCallback) cs -> {
							cs.setObject("shopId", shopId);
							cs.setObject("shopNo", null);
							cs.setObject("shopName", null);
							cs.setObject("shopType", null);
							cs.setObject("ifLine", null);
							cs.setObject("cityId", null);
							cs.setObject("districtId", null);
							cs.setObject("eid", eid);
							cs.setObject("ename", null);
							cs.setObject("isRelevance", null);
							cs.setObject("isHaveKeeper", null);
							cs.setObject("shopName1", null);
							cs.setObject("isVendor", null);
							cs.setObject("PageIndex", null);
							cs.setObject("PageSize", null);
							cs.setObject("IsSelectAll", null);
							cs.registerOutParameter("Count", Types.NVARCHAR);// 注册输出参数的类型
							cs.execute();
							ResultSet rs = cs.executeQuery();

							try {
								return ResultSetToBeanHelper
										.resultSetToList(rs,
												FindShopModel.class);
							} catch (Exception e) {
								return null;
							}finally{
								if(rs!=null){
									rs.close();
								}
							}
						});
		return result;*/
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> updateShop(final Shop shop) {
		Map<String, Object> result = (Map<String, Object>)jdbcTemplate
				.execute(
						"{call Proc_Backstage_shop_operat(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}",
						(CallableStatementCallback) cs -> {
							cs.setObject("shopId", shop.getShopId());
							cs.setObject("eid", shop.getEid());
							cs.setObject("shopNo", shop.getShopNo());
							cs.setObject("shopName", shop.getShopName());
							cs.setObject("address", shop.getAddress());
							cs.setObject("tel", shop.getTel());
							cs.setObject("linkman", shop.getLinkman());
							cs.setObject("cityId", shop.getCityId());
							cs.setObject("districtId", shop.getDistrictId());
							cs.setObject("location", shop.getLocation());
							cs.setObject("description",
									shop.getDescription());
							cs.setObject("pictureUrl", shop.getPictureUrl());
							cs.setObject("shopType", shop.getShopType());
							cs.setObject("startTime", shop.getStartTime());
							cs.setObject("endTime", shop.getEndTime());
							cs.setObject("scopeDescription",
									shop.getScopeDescription());
							cs.setObject("sendOnTime", shop.getSendOnTime());
							cs.setObject("minSendPrice",
									shop.getMinSendPrice());
							cs.setObject("freight", shop.getFreight());
							cs.setObject("createUser", shop.getCreateUser());
							cs.setObject("ifLine", shop.getIfLine());
							cs.setObject("staffId", shop.getStaffId());
							cs.setObject("TAG", "EDIT");
							cs.registerOutParameter("shopId", Types.INTEGER);// 注册输出参数的类型
							cs.registerOutParameter("TAG", Types.NVARCHAR);// 注册输出参数的类型
							cs.execute();

							Map<String, Object> resul = new HashMap<String, Object>();
							resul.put("TAG", cs.getString("TAG"));
							resul.put("shopId", cs.getInt("shopId"));
							return resul;
						});
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Map<String, Object>> selectAreaByEid(Integer eid) {
		List<City> list = new ArrayList<City>();
		String sql = "select B.cityId,B.cityName,C.districtId,C.districtName from e_enterprise_city A inner join e_gis_city  B  on A.cityId=B.cityId  inner join e_gis_district C  on B.cityId=C.cityId where eid=?";
		list = jdbcTemplate.query(sql, new Object[] { eid },
				new BeanPropertyRowMapper(City.class));
		List<String> cityName = new ArrayList<String>();
		List<Integer> cityIds = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++) {
			if (cityName.size() == 0) {
				cityName.add(list.get(0).getCityName());
				cityIds.add(list.get(0).getCityId());
			} else {
				boolean name = true;
				for (int j = 0; j < cityName.size(); j++) {
					if (cityName.get(j).equals(list.get(i).getCityName())) {
						name = false;
					}
					if (j == cityName.size() - 1 && name) {
						cityName.add(list.get(i).getCityName());
						cityIds.add(list.get(i).getCityId());
					}
				}
			}

		}

		/*
		 * List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		 * for (int i = 0; i <cityName.size(); i++) { Map<String, Object> map =
		 * new HashMap<String, Object>(); map.put("cityName", cityName.get(i));
		 * List<City> list2 = new ArrayList<City>(); for (int j = 0; j
		 * <list.size(); j++) {
		 * if(list.get(j).getCityName().equals(cityName.get(i))){
		 * list2.add(list.get(j)); } } // map.put(cityName.get(i), list2);
		 * map.put("cityInfos", list2); list1.add(map); }
		 */
		List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < cityName.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("cityName", cityName.get(i));
			map.put("cityId", cityIds.get(i));
			List<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>();
			for (int j = 0; j < list.size(); j++) {
				if (list.get(j).getCityName().equals(cityName.get(i))) {
					Map<String, Object> map3 = new HashMap<String, Object>();
					map3.put("districtId", list.get(j).getDistrictId());
					map3.put("districtName", list.get(j).getDistrictName());

					list2.add(map3);

					// System.out.println(list.get(j));
				}
			}
			// map.put(cityName.get(i), list2);
			map.put("districts", list2);
			list1.add(map);
		}
		return list1;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public City selectCityByCityIdAndDistrictId(Integer cityId,
			Integer districtId) {
		String sql = "select B.cityId,B.cityName,C.districtId,C.districtName from e_gis_city  B inner join e_gis_district C  on B.cityId=C.cityId where B.cityId=? and C.districtId=?";
		City city = new City();
		city = (City)jdbcTemplate.queryForObject(sql, new Object[] { cityId,
				districtId }, new BeanPropertyRowMapper(City.class));
		return city;
	}

	@Override
	public Integer inserteAuthUserShop(Integer userId, Integer eid,
			Integer shopId, Date createTime, String createUser) {
		String sql = "insert into e_auth_user_shop(userId,eid,shopId,createTime,createUser) values(?,?,?,?,?)";
		Object[] args = { userId, eid, shopId, createTime, createUser };
		int count = jdbcTemplate.update(sql, args);
		return count;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<ShopCode> selectShopCode(final Integer eid, final Integer shopId) {
		List<ShopCode> result = (List<ShopCode>)jdbcTemplate
				.execute(
						"{call Proc_Backstage_shop_wechatliteapp_code_select(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}",
						(CallableStatementCallback) cs -> {
							cs.setObject("shopCode", null);
							cs.setObject("eid", eid);
							cs.setObject("shopId", shopId);
							cs.setObject("beginTime", null);
							cs.setObject("endTime", null);
							cs.setObject("managerFullName", null);
							cs.setObject("tuikeFullName", null);
							cs.setObject("shopName", null);
							cs.setObject("eName", null);
							cs.setObject("isEnable", null);
							cs.setObject("managerAccount", null);
							cs.setObject("tuikeAccount", null);
							cs.setObject("PageIndex", null);
							cs.setObject("PageSize", null);
							cs.setObject("IsSelectAll", null);
							cs.registerOutParameter("Count", Types.INTEGER);// 注册输出参数的类型
							cs.execute();
							ResultSet rs = cs.executeQuery();
							try {
								return ResultSetToBeanHelper
										.resultSetToList(rs, ShopCode.class);
							} catch (Exception e) {
								return null;
							}finally{
								if(rs!=null){
									rs.close();
								}
							}
						});
		return result;
	}

	@Override
	public Integer upTimeframe(Integer eid, Integer shopId, String beginHour,
			String endHour) {
		// TODO Auto-generated method stub
		String sql = "insert into e_shop_timeframe(eid,shopId,beginHour,endHour) values(?,?,?,?)";
		int update = jdbcTemplate.update(sql,new Object[]{eid,shopId,beginHour,endHour});
		return update;
	}

	@Override
	public Integer delTimeframe(Integer eid, Integer shopId) {
		// TODO Auto-generated method stub
		String sql="delete from e_shop_timeframe where eid=? and shopId=?";
		int update = jdbcTemplate.update(sql,new Object[]{eid,shopId});
		return update;
	}

	@Override
	public List<ShopTimeframe> selectTimeframe(Integer eid, Integer shopId) {
		// TODO Auto-generated method stub
		String sql="select  id, eid, shopId, beginHour, endHour from e_shop_timeframe where eid=? and shopId=?";
		try {
			List<ShopTimeframe> query = jdbcTemplate.query(sql, new Object[] { eid,shopId }, new BeanPropertyRowMapper<ShopTimeframe>(ShopTimeframe.class));
			return query;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Map<String, Object>> commodityManage(final Integer eid,final Integer shopId) {
		
			List<Map<String, Object>> result = (List<Map<String, Object>>)jdbcTemplate.execute(
					"{call Proc_Backstage_Shop_ManagerProduct(?,?)}",
					(CallableStatementCallback) cs -> {
						cs.setObject("eid", eid);
						cs.setObject("shopId", shopId);
						cs.execute();
						ResultSet rs = cs.executeQuery();

						try {
							return new ResultSetToBeanHelper().extractData(rs);
						} catch (Exception e) {
							return null;
						}finally{
							if(rs!=null){
								rs.close();
							}
						}
					});
			return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String productLine(final Integer pid,final Integer shopId,
			final Boolean recommend,final Integer lineBit) {
		String result = (String)jdbcTemplate.execute(
				"{call Proc_Backstage_Shop_ProductLine(?,?,?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("pid", pid);
					cs.setObject("shopId", shopId);
					cs.setObject("recommend", recommend);
					cs.setObject("lineBit", lineBit);
					cs.registerOutParameter("TAG", Types.NVARCHAR);// 注册输出参数的类型
					cs.execute();
					return cs.getString("TAG");
				});
		return result;
	}
	

	@Override
	public Shop selectEidByShopId(Integer shopId) {
		String sql = "select shopId,eid from e_shop where shopId=?";
		try {
			Shop shop = jdbcTemplate.queryForObject(sql, new Object[] {shopId},  new BeanPropertyRowMapper<>(Shop.class));
			
			return shop;
		} catch (DataAccessException e) {
			
			return null;
		}
	}

	/**
	 * 修改商品序号
	 */
	@Override
	public Integer updateProductSort(Integer sort, Integer pid, Integer eid) {
		String sql = "update e_product set sort = ? where pid=? and eid=?";
		return jdbcTemplate.update(sql,new Object[] {sort,pid,eid});
	}

	/**
	 * 修改商品上下架
	 * @param enabled
	 * @param pid
	 * @param eid
	 * @return
	 */
	@Override
	public Integer updateProductEnabled(Boolean enabled, Integer pid, Integer eid) {
		String sql = "update e_product set enabled = ? where pid=? and eid=?";
		return jdbcTemplate.update(sql,new Object[] {enabled,pid,eid});
	}
	
	/**
	 * 修改店铺商品上下架
	 * @param enabled
	 * @param pid
	 * @param eid
	 * @return
	 */
	@Override
	public Integer updateShopProductEnabled(Boolean enabled, Integer pid, Integer shopId) {
		String sql = "update e_shop_product set isEnabled = ? where pid=? and shopId=?";
		return jdbcTemplate.update(sql,new Object[] {enabled,pid,shopId});
	}

	/**
	 * 修改商品价格
	 * @param vipPrice
	 * @param pid
	 * @param eid
	 * @return
	 */
	@Override
	public Integer updateProductPrice(Double vipPrice, Integer pid, Integer eid) {
		String sql = "update e_product set vipPrice = ? where pid=? and eid=?";
		return jdbcTemplate.update(sql,new Object[] {vipPrice,pid,eid});
	}

	/**
	 * 查询公众号二维码
	 * @return
	 */
	@Override
	public String selectGZHQRCode(Integer eid) {
		String sql = "select publicWxCode as liteappShearPic from e_enterprise where eid=?";
		List<Map<String, Object>> query = jdbcTemplate.query(sql.toString(),new Object[] {eid}, ResultSetToBeanHelper.resultSetToListMap());
		String liteappShearPic = "";
		if(query.size()>0) {
			Map<String, Object> map = query.get(0);
			liteappShearPic =  map.get("liteappShearPic")!=null?(String)map.get("liteappShearPic"):"";
		}
		return liteappShearPic;
	}

	/**
	 * 小程序推客二维码
	 * @param eid
	 * @param clientId
	 * @return
	 */
	@Override
	public String selectXCXQRCode(Integer eid, String account) {
		String sql = "select picUrl from e_shop_wechatliteapp_code where eid=? and tuikeClientId = (select clientId from e_client where account = ?)";
		List<Map<String, Object>> query = jdbcTemplate.query(sql.toString(),new Object[] {eid,account}, ResultSetToBeanHelper.resultSetToListMap());
		String picUrl = "";
		if(query.size()>0) {
			Map<String, Object> map = query.get(0);
			picUrl =  map.get("picUrl")!=null?(String)map.get("picUrl"):"";
		}
		return picUrl;
	}

	/**
	 * 给店铺添加关联商品
	 * @param shopId
	 * @param pid
	 * @param shopPrice
	 * @return
	 */
	@Override
	public Integer insertShopProduct(Integer shopId, Integer pid, Double shopPrice) {
		String sql="insert into e_shop_product(shopId,pid,shopPrice,isEnabled) values(?,?,?,0)";
		
		return jdbcTemplate.update(sql,new Object[] {shopId,pid,shopPrice});
	}

	/**
	 * 删除店铺商品
	 * @param shopId
	 * @param pid
	 * @return
	 */
	@Override
	public Integer removeShopProduct(Integer shopId, Integer pid) {
		String sql="delete from e_shop_product where shopId=? and pid=?";
		
		return jdbcTemplate.update(sql,new Object[] {shopId,pid});
	}
	

	/**
	 * 查询店铺商品
	 * @param shopId
	 * @param pid
	 * @return
	 */
	@Override
	public List<Map<String,Object>> selectShopProduct(Integer shopId, Integer pid) {
		String sql="select pid from e_shop_product where shopId=? and pid=?";
		List<Map<String, Object>> query = jdbcTemplate.query(sql.toString(),new Object[] {shopId,pid}, ResultSetToBeanHelper.resultSetToListMap());
		return query;
	}

	/**
	 * 修改店铺商品价格
	 * @param shopId
	 * @param pid
	 * @param shopPrice
	 * @return
	 */
	@Override
	public Integer updateShopProductPrice(Integer shopId, Integer pid, Double shopPrice) {
		String sql="update e_shop_product set shopPrice=? where shopId=? and pid=?";
		
		return jdbcTemplate.update(sql,new Object[] {shopPrice,shopId,pid});
	}

	@Override
	public List<Map<String, Object>> appMerchantNotify(Integer eid) {
		String sql = "select notifyId, eid, sortId, notifyContent, createTime, creater from e_appMerchant_notify where eid = ? order by sortId";
		List<Map<String, Object>> query = jdbcTemplate.query(sql.toString(),new Object[] {eid}, ResultSetToBeanHelper.resultSetToListMap());
		return query;
	}

	/**
	 * 手机商户端门店今日统计
	 * @param accountId 手机商户端账号ID
	 * @param shopId 店铺ID
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> appMerchantTodayStatistics(ProcBackstageAppMerchantTodayStatisticsDTO dto) {
		return baseDao.agencyDB("Proc_Backstage_appMerchant_Today_statistics",dto);
		/*Map<String,Object> result = (Map<String,Object>)jdbcTemplate.execute(
				"{call Proc_Backstage_appMerchant_Today_statistics(?,?,?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("accountId", accountId);
					cs.setObject("shopId", shopId);
					cs.setObject("beginTime", beginTime);
					cs.setObject("endTime", endTime);
					cs.registerOutParameter("TAG", Types.NVARCHAR);// 注册输出参数的类型
					cs.execute();
					ResultSet rs = cs.executeQuery();
					try {
						Map<String,Object> map = new HashMap<String,Object>();


						List<Map<String,Object>> resul = ResultSetToBeanHelper.resultSetToList(rs, Map.class);
						String tag = cs.getString("TAG");
						map.put("TAG", tag);
						if("1".equals(tag)&&resul.size()>0) {
							map.put("appMerchantTodayStatistics", resul.get(0));
						}

						return map;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}finally{
						if(rs!=null){
							rs.close();
						}
					}
				});
		return result;*/
	}

	/**
	 * 操作店铺上下线
	 * @param isLine
	 * @return
	 */
	@Override
	public Integer updateShopIsLine(Boolean ifLine,Integer eid,Integer shopId) {
		String sql="update e_shop set ifLine=? where eid=? and shopId=?";
		int update = jdbcTemplate.update(sql,new Object[] {ifLine,eid,shopId});
		return update;
	}

	/* (non-Javadoc)
	 * @see com.evian.sqct.dao.IShopDao#shopLeagueClientsSelect(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.Boolean)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> shopLeagueClientsSelect(ProcBackstageShopLeagueClientsSelectDTO dto) {
		Map result = baseDao.agencyDB("Proc_Backstage_shop_league_clients_select", dto);
		return DaoUtil.resultRename(result,"shopClients");


		/*Map<String,Object> result = (Map<String,Object>)jdbcTemplate.execute(
				"{call Proc_Backstage_shop_league_clients_select(?,?,?,?,?,?,?,?,?,?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("beginTime", beginTime);
					cs.setObject("endTime", endTime);
					cs.setObject("shopId", shopId);
					cs.setObject("eid", eid);
					cs.setObject("nickName", nickName);
					cs.setObject("ifTuiYa", ifTuiYa);
					cs.setObject("cellphone", cellphone);
					cs.setObject("noOrderDays", noOrderDays);
					cs.setObject("PageIndex", PageIndex);
					cs.setObject("PageSize", PageSize);
					cs.setObject("IsSelectAll", IsSelectAll);
					cs.registerOutParameter("Count", Types.INTEGER);// 注册输出参数的类型
					cs.execute();
					ResultSet rs = cs.executeQuery();
					try {
						Map<String,Object> map = new HashMap<String,Object>();


						List<Map<String,Object>> resul = ResultSetToBeanHelper.resultSetToList(rs, Map.class);
						Integer Count = cs.getInt("Count");
						map.put("Count", Count);
						map.put("shopClients", resul);
						return map;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}finally{
						if(rs!=null){
							rs.close();
						}
					}
				});
		return result;*/
	}

	/**
	 * 电子票有效剩余量
	 * @param clientId
	 * @param type
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String,Object> getTicketAccount(ProcDisParkGetTicketAccountDTO dto) {
		Map result = baseDao.agencyDB("Proc_DisPark_Get_Ticket_Account", dto);
		return DaoUtil.resultRename(result,"ticketAccount");
		/*List<Map<String,Object>> result = (List<Map<String,Object>>)jdbcTemplate.execute(
				"{call Proc_DisPark_Get_Ticket_Account(?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("clientId", clientId);
					cs.setObject("type", type);
					cs.execute();
					ResultSet rs = null;
					try {

						rs = cs.executeQuery();

						return ResultSetToBeanHelper.resultSetToList(rs, Map.class);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}finally{
						if(rs!=null){
							rs.close();
						}
					}
				});
		return result;*/
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Map<String, Object>> ticketSelectMx(final Integer eid, final Integer clientId) {
		List<Map<String,Object>> result = (List<Map<String,Object>>)jdbcTemplate.execute(
				"{call PROC_LIDAN_Ticket_SELECT_MX(?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("clientID", clientId);
					cs.setObject("eid", eid);
					cs.execute();
					ResultSet rs = null;
					try {

						rs = cs.executeQuery();

						return ResultSetToBeanHelper.resultSetToList(rs, Map.class);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}finally{
						if(rs!=null){
							rs.close();
						}
					}
				});
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Map<String, Object>> selectMyVouchers(final Integer clientId,final Integer eid) {
		List<Map<String,Object>> result = (List<Map<String,Object>>)jdbcTemplate.execute(
				"{call Proc_DisPark_Select_My_Vouchers(?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("clientId", clientId);
					cs.setObject("use_imode", 2);
					cs.setObject("eid", eid);
					cs.execute();
					ResultSet rs = null;
					try {

						rs = cs.executeQuery();

						return ResultSetToBeanHelper.resultSetToList(rs, Map.class);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}finally{
						if(rs!=null){
							rs.close();
						}
					}
				});
		return result;
	}

	@Override
	public Map<String, Object> selectShopByShopId(Integer shopId) {
		String sql = "select shopName,tel,pictureUrl,startTime,endTime from e_shop where shopId=?";
		List<Map<String, Object>> query = jdbcTemplate.query(sql, new Object[] {shopId},ResultSetToBeanHelper.resultSetToListMap());
		if(query.size()>0) {
			return query.get(0);
		}
		return null;
	}

	@Override
	public List<Map<String, Object>> selectShopBrandByShopId(Integer shopId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select  F.brandId,F.brandName,F.pictureUrl from (select C.brandId,C.sort from e_enterprise_brand AS C where C.brandId in(select B.brandId from e_shop_product A inner join e_product B ON A.pid=B.pid ");
		sql.append(" where A.shopId=? and B.brandId>0 and B.[enabled]=1 and B.[status]=1 group by B.brandId) and C.eid=(select Top 1 eid from e_shop where shopId=?)) ");
		sql.append(" AS D inner join e_product_brand F ON D.brandId=F.brandId order by D.sort ");
		List<Map<String, Object>> query = jdbcTemplate.query(sql.toString(), new Object[] {shopId,shopId},ResultSetToBeanHelper.resultSetToListMap());
		return query;
	}

	@Override
	public Integer updateShop(Integer shopId, String pictureUrl, String tel, String shopName,String startTime,String	endTime,String address,String location) {
		StringBuilder sql = new StringBuilder("update e_shop set  ");
		List<Object> args = new ArrayList<Object>();
		
		if(StringUtils.isEmpty(pictureUrl)&&StringUtils.isEmpty(tel)&&StringUtils.isEmpty(shopName)&&StringUtils.isEmpty(startTime)&&StringUtils.isEmpty(endTime)&&StringUtils.isEmpty(address)&&StringUtils.isEmpty(location)) {
			return 0;
		}
		
		if(!StringUtils.isEmpty(pictureUrl)) {
			sql.append(" pictureUrl=?, ");
			args.add(pictureUrl);
		}
		
		if(!StringUtils.isEmpty(address)) {
			sql.append(" address=?, ");
			args.add(address);
		}
		
		if(!StringUtils.isEmpty(location)) {
			sql.append(" location=?, ");
			args.add(location);
		}
		
		if(!StringUtils.isEmpty(tel)) {
			sql.append(" tel=?, ");
			args.add(tel);
		}
		
		if(!StringUtils.isEmpty(shopName)) {
			sql.append(" shopName=?, ");
			args.add(shopName);
		}
		
		if(!StringUtils.isEmpty(startTime)) {
			sql.append(" startTime=?, ");
			args.add(startTime);
		}
		
		if(!StringUtils.isEmpty(endTime)) {
			sql.append(" endTime=?, ");
			args.add(endTime);
		}
		
		// 删除最后一个,
		int lastIndexOf = sql.lastIndexOf(",");
		sql.deleteCharAt(lastIndexOf);
		
		sql.append(" where shopId=?");
		int o = args.size()+1;
		Object[] a = new Object[o];
		
		a[o-1] = shopId;
		for (int i = 0; i < args.size(); i++) {
			a[i]=args.get(i);
		}
		
		int update = jdbcTemplate.update(sql.toString(),a);
		return update;
	}

	/* (non-Javadoc)
	 * @see com.evian.sqct.dao.IShopDao#selectEidProduct(java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Boolean)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Map<String, Object>> selectEidProduct(final Integer eid, final String pcode, final String pname, final String property,
			final String shopName, final Boolean enabled, final Integer shopId, final Integer pid, final Integer PageIndex, final Integer PageSize,
			final Boolean IsSelectAll) {

		List<Map<String, Object>> result = (List<Map<String, Object>>)jdbcTemplate.execute(
				"{call Proc_Backstage_shop_product_select(?,?,?,?,?,?,?,?,?,?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("eid", eid);
					cs.setObject("pcode", pcode);
					cs.setObject("pname", pname);
					cs.setObject("property", property);
					cs.setObject("shopName", shopName);
					cs.setObject("enabled", enabled);
					cs.setObject("shopId", shopId);
					cs.setObject("pid", pid);
					cs.setObject("PageIndex", PageIndex);
					cs.setObject("PageSize", PageSize);
					cs.setObject("IsSelectAll", IsSelectAll);
					cs.registerOutParameter("Count", Types.INTEGER);// 注册输出参数的类型
					cs.execute();
					ResultSet rs = cs.executeQuery();

					try {
						return new ResultSetToBeanHelper().extractData(rs);
					} catch (Exception e) {
						return null;
					}finally{
						if(rs!=null){
							rs.close();
						}
					}
				});
		return result;
	}

	/**
	 * 库存初始化
	 * @param eid
	 * @param pid
	 * @param shopId
	 * @param initStockNum
	 * @param saleStockNum
	 * @param createUser
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String Proc_Backstage_product_stock_num_init(final Integer eid, final Integer pid, final Integer shopId
			, final Integer initStockNum, final Integer saleStockNum, final String createUser) {
		String result = (String)jdbcTemplate.execute(
				"{call Proc_Backstage_product_stock_num_init(?,?,?,?,?,?,?,?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("eid", eid);
					cs.setObject("schemeId", 3);// 3为店铺初始化
					cs.setObject("pid", pid);
					cs.setObject("cityId", 0);
					cs.setObject("districtId", 0);
					cs.setObject("shopId", shopId);
					cs.setObject("initStockNum", initStockNum);
					cs.setObject("saleStockNum", saleStockNum);
					cs.setObject("createUser", createUser);
					cs.registerOutParameter("TAG", Types.NVARCHAR);// 注册输出参数的类型
					cs.execute();
					return cs.getString("TAG");
				});
		return result;
	}

	/**
	 * 商品库存重新初始化
	 * @param eid
	 * @param tsId
	 * @return
	 */
	@Override
	public String Proc_Backstage_product_stock_num_reInit(final Integer eid, final Integer tsId) {
		String result = (String)jdbcTemplate.execute(
				"{call Proc_Backstage_product_stock_num_reInit(?,?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("eid", eid);
					cs.setObject("tsId", tsId);
					cs.setObject("pid", null);
					cs.registerOutParameter("TAG", Types.NVARCHAR);// 注册输出参数的类型
					cs.execute();
					return cs.getString("TAG");
				});
		return result;
	}

	/**
	 * 配送费提现账户总账
	 * @param shopId
	 * @param eid
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> PROC_ZH_DisCosts_SELECT_ShopAccount(final Integer shopId, final Integer eid) {
		Map<String, Object> result = (Map<String, Object>)jdbcTemplate.execute(
				"{call PROC_ZH_DisCosts_SELECT_ShopAccount(?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("ShopID", shopId);
					cs.setObject("eid", eid);
					ResultSet rs = cs.executeQuery();
					Map<String, Object> map = new HashMap<String, Object>();
					try {
						List<Map<String,Object>> resul = ResultSetToBeanHelper.resultSetToList(rs, Map.class);
						map.put("generalLedger", resul);
						if(cs.getMoreResults()) {
							rs = cs.getResultSet();
							List<Map<String,Object>> result2 = ResultSetToBeanHelper.resultSetToList(rs, Map.class);
							map.put("otherCommissions", result2);
						}
						return map;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}finally{
						if(rs!=null){
							rs.close();
						}
					}
				});
		return result;
	}

	/**
	 * 申请提现保存
	 *
	 * @param eid
	 * @param clientID
	 * @param ShopID
	 * @param sAccount  水趣商户帐号
	 * @param nCashOut  提现金额
	 * @param CashOutNO 提现单号
	 * @param Remark    提现说明
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> PROC_ZH_DistributionCosts_CashOut_SAVE(final Integer eid, final Integer clientId
			, final Integer shopId, final String account, final Double cashOut, final String cashOutNo, final String remark, final String APPUserName) {
		Map<String, Object> result = (Map<String, Object>)jdbcTemplate.execute(
				"{call PROC_ZH_DistributionCosts_CashOut_SAVE(?,?,?,?,?,?,?,?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("eid", eid);
					cs.setObject("clientID", clientId);// 3为店铺初始化
					cs.setObject("ShopID", shopId);
					cs.setObject("sAccount", account);
					cs.setObject("nCashOut", cashOut);
					cs.setObject("CashOutNO", cashOutNo);
					cs.setObject("Remark", remark);
					cs.setObject("APPUserName", APPUserName);
					cs.registerOutParameter("CashOutID", Types.INTEGER);// 注册输出参数的类型
					cs.registerOutParameter("RETURN_message", Types.NVARCHAR);// 注册输出参数的类型
					cs.execute();
					String RETURN_message = cs.getString("RETURN_message");
					Integer CashOutID = cs.getInt("CashOutID");
					Map<String,Object> resul = new HashMap<>();
					resul.put("RETURN_message",RETURN_message);
					resul.put("CashOutID",CashOutID);
					return resul;
				});
		return result;
	}

	/**
	 * @param CashOutID 提现单ID
	 * @param CashOutNO 提现单号
	 * @param ifPayment 是否付款成功：1=已付款；0=未付款
	 * @param iStatus   状态：0=正在提现；1=提现完成；-1=支付失败；-2=拒绝（传1或-1）；
	 * @param paymentNo 支付对帐单号
	 * @param SH_remark 出错日志
	 * @return RETURN_message
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String PROC_ZH_DistributionCosts_CashOut_SAVE_Pay(final Integer CashOutID, final String CashOutNO
			, final Boolean ifPayment, final Integer iStatus, final String paymentNo, final String SH_remark) {
		String result = (String)jdbcTemplate.execute(
				"{call PROC_ZH_DistributionCosts_CashOut_SAVE_Pay(?,?,?,?,?,?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("CashOutID", CashOutID);
					cs.setObject("CashOutNO", CashOutNO);// 3为店铺初始化
					cs.setObject("ifPayment", ifPayment);
					cs.setObject("iStatus", iStatus);
					cs.setObject("paymentNo", paymentNo);
					cs.setObject("SH_remark", SH_remark);
					cs.setObject("SH_UserAccount", null);
					cs.registerOutParameter("RETURN_message", Types.NVARCHAR);// 注册输出参数的类型
					cs.execute();
					return cs.getString("RETURN_message");
				});
		return result;
	}

	/**
	 * 查询订单
	 * @param eid
	 * @param shopId
	 * @param begDate
	 * @param endDate
	 * @param RE_TAG
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Map<String, Object>> PROC_ZH_DistributionCosts_order_SELECT(final Integer eid, final Integer shopId
			, final String begDate, final String endDate, final String RE_TAG, final Integer PageIndex, final Integer PageSize) {
		List<Map<String, Object>> result = (List<Map<String, Object>>)jdbcTemplate.execute(
				"{call PROC_ZH_DistributionCosts_order_SELECT(?,?,?,?,?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("eid", eid);
					cs.setObject("shopID", shopId);
					cs.setObject("BegDate", begDate);
					cs.setObject("EndDate", endDate);
					cs.setObject("RE_TAG", RE_TAG);
					cs.setObject("PageIndex", PageIndex);
					cs.setObject("PageSize", PageSize);
					cs.execute();
					ResultSet rs = cs.executeQuery();
					try {
						return new ResultSetToBeanHelper().extractData(rs);
					} catch (Exception e) {
						return null;
					}finally{
						if(rs!=null){
							rs.close();
						}
					}
				});
		return result;
	}


	/**
	 * 查询订单
	 * @param eid
	 * @param shopId
	 * @param begDate
	 * @param endDate
	 * @param RE_TAG
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Map<String, Object>> PROC_ZH_DistributionCosts_CashOut_SELECT(final Integer eid, final Integer shopId
			, final String begDate, final String endDate, final String RE_TAG, final Integer PageIndex, final Integer PageSize) {
		List<Map<String, Object>> result = (List<Map<String, Object>>)jdbcTemplate.execute(
				"{call PROC_ZH_DistributionCosts_CashOut_SELECT(?,?,?,?,?,?,?)}",
				(CallableStatementCallback) cs -> {
					cs.setObject("eid", eid);
					cs.setObject("shopID", shopId);
					cs.setObject("BegDate", begDate);
					cs.setObject("EndDate", endDate);
					cs.setObject("RE_TAG", RE_TAG);
					cs.setObject("PageIndex", PageIndex);
					cs.setObject("PageSize", PageSize);
					cs.execute();
					ResultSet rs = cs.executeQuery();
					try {
						return new ResultSetToBeanHelper().extractData(rs);
					} catch (Exception e) {
						return null;
					}finally{
						if(rs!=null){
							rs.close();
						}
					}
				});
		return result;
	}

}
