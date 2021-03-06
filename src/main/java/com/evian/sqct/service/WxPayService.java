package com.evian.sqct.service;

import com.evian.sqct.bean.pay.WXPayConstants;
import com.evian.sqct.bean.pay.WXPayConstants.SignType;
import com.evian.sqct.bean.util.WXHB;
import com.evian.sqct.util.HttpClientUtil;
import com.evian.sqct.util.MD5Util;
import com.evian.sqct.wxHB.RequestHandler;
import com.evian.sqct.wxPay.APPWxPayBean;
import com.evian.sqct.wxPay.EnterprisePayByLooseChangeBean;
import com.evian.sqct.wxPay.WXPayXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

/**
 * ClassName:WxPayService
 * Package:com.evian.sqct.service
 * Description:微信payApi服务
 * 包括：
 * 1.app微信支付工具类
 * 2.微信红包
 *
 * @Date:2020/5/8 9:30
 * @Author:XHX
 */
@Service
public class WxPayService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final String ERR_CODE = "err_code";
    public final String ERR_CODE_DES = "err_code_des";
    public final String SUCCESS = "SUCCESS";

    @Autowired
    private BaseOrderManager baseOrderManager;

    /**
     * 微信app支付
     * @param wx
     * @return
     */
    public String pay(APPWxPayBean wx){
        logger.info("=======================================================================微信app支付开始");
        logger.info("微信app支付入口参数："+wx.toString());
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", wx.getAppid());
        packageParams.put("mch_id", wx.getMch_id());
        packageParams.put("nonce_str", wx.getNonce_str());
        packageParams.put("sub_appid", wx.getSub_appid());
        packageParams.put("sub_mch_id", wx.getSub_mch_id());
        packageParams.put("body", wx.getBody());
        //2019-07-08 彭安需要orderId来优化回调速度    DIVISION分隔符  DIVISION前是orderId ， DIVISION后是充值、拼团id
        wx.setAttach(wx.getOrderId()+"DIVISION"+(wx.getAttach()==null?"":wx.getAttach()));
        packageParams.put("attach", wx.getAttach());
        packageParams.put("total_fee", wx.getTotal_fee());
        packageParams.put("out_trade_no", wx.getOut_trade_no());
        packageParams.put("spbill_create_ip", wx.getSpbill_create_ip());
        packageParams.put("notify_url", wx.getNotify_url());
        String trade_type = wx.getTrade_type();
        packageParams.put("trade_type", trade_type);
        if("NATIVE".equals(trade_type)){
            packageParams.put("product_id", wx.getProduct_id());
        }
        RequestHandler reqHandler = new RequestHandler();
        reqHandler.init(wx.getAppKey());
        String sign = reqHandler.createSign(packageParams);
        wx.setSign(sign);
        /** 封装报文 */
        StringBuilder xml = new StringBuilder("<xml>")
                .append("<nonce_str><![CDATA[").append(wx.getNonce_str()).append("]]></nonce_str>")
                .append("<sign><![CDATA[").append(wx.getSign()).append("]]></sign>")
                .append("<appid><![CDATA[").append(wx.getAppid()).append("]]></appid>")
                .append("<mch_id><![CDATA[").append(wx.getMch_id()).append("]]></mch_id>")
                .append("<sub_appid><![CDATA[").append( wx.getSub_appid() ).append( "]]></sub_appid>")
                .append("<sub_mch_id><![CDATA[").append(wx.getSub_mch_id() ).append( "]]></sub_mch_id>")
                .append("<body>").append(wx.getBody() ).append( "</body>")
                .append("<attach>").append(wx.getAttach() ).append( "</attach>")
                .append("<total_fee><![CDATA[").append(wx.getTotal_fee() ).append( "]]></total_fee>")
                .append("<out_trade_no><![CDATA[").append(wx.getOut_trade_no() ).append( "]]></out_trade_no>")
                .append("<spbill_create_ip><![CDATA[").append(wx.getSpbill_create_ip() ).append( "]]></spbill_create_ip>")
                .append("<notify_url><![CDATA[").append(wx.getNotify_url() ).append( "]]></notify_url>")
                .append("<trade_type><![CDATA[").append(trade_type).append( "]]></trade_type>");
                if("NATIVE".equals(trade_type)){
                    xml.append("<product_id><![CDATA[").append(wx.getProduct_id()).append( "]]></product_id>");
                }
                xml.append("</xml>");

        //获取预支付ID

        String createOrderURL = "https://"+ WXPayConstants.DOMAIN_API+WXPayConstants.UNIFIEDORDER_URL_SUFFIX;
        logger.info("微信app支付prepayId请求地址: "+createOrderURL+", 请求数据: "+xml);
        String prepayContent = HttpClientUtil.post(createOrderURL , xml.toString());
        logger.info("微信app支付prepayId请求返回结果: "+prepayContent);
        return prepayContent;
    }


    /**
     * 企业支付到零钱
     * @return
     */
    public String enterprisePayByLooseChange(EnterprisePayByLooseChangeBean wx) throws Exception {
        logger.info("=======================================================================微信支付到零钱开始");
        logger.info("微信支付到零钱入口参数："+wx.toString());
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("mch_appid", wx.getMch_appid());
        packageParams.put("mchid", wx.getMchid());
        packageParams.put("nonce_str", wx.getNonce_str());
        packageParams.put("partner_trade_no", wx.getPartner_trade_no());
        packageParams.put("openid", wx.getOpenid());
        packageParams.put("check_name", wx.getCheck_name());
        packageParams.put("amount", wx.getAmount());
        packageParams.put("desc", wx.getDesc());
        packageParams.put("spbill_create_ip", wx.getSpbill_create_ip());
        RequestHandler reqHandler = new RequestHandler();
        reqHandler.init(wx.getAppKey());
        String sign = reqHandler.createSign(packageParams);
        wx.setSign(sign);
        /** 封装报文 */
        StringBuilder xml = new StringBuilder("<xml>")
                .append("<mch_appid><![CDATA[").append( wx.getMch_appid() ).append("]]></mch_appid>")
                .append("<mchid><![CDATA[").append( wx.getMchid() ).append("]]></mchid>")
                .append("<nonce_str><![CDATA[").append(wx.getNonce_str()).append("]]></nonce_str>")
                .append("<partner_trade_no><![CDATA[").append(wx.getPartner_trade_no()).append("]]></partner_trade_no>")
                .append("<openid><![CDATA[").append( wx.getOpenid() ).append("]]></openid>")
                .append("<check_name><![CDATA[").append( wx.getCheck_name() ).append("]]></check_name>")
                .append("<amount>").append( wx.getAmount() ).append("</amount>")
                .append("<desc><![CDATA[").append( wx.getDesc() ).append("]]></desc>")
                .append("<spbill_create_ip><![CDATA[").append( wx.getSpbill_create_ip() ).append("]]></spbill_create_ip>")
                .append("<sign><![CDATA[").append( wx.getSign() ).append("]]></sign>")
                .append("</xml>");

        //获取预支付ID

        String createOrderURL = "https://"+WXPayConstants.DOMAIN_API+WXPayConstants.MMPAYMKTTRANSFERS_TRANSFERS_URL_SUFFIX;
        logger.info("微信支付到零钱prepayId请求地址: "+createOrderURL+", 请求数据: "+xml);
        String prepayContent = sslPost(createOrderURL , xml.toString(),wx.getMchid());
        logger.info("微信支付到零钱prepayId请求返回结果: "+prepayContent);

        return payApiErrDispose(prepayContent, createOrderURL, xml.toString(), wx.getMchid());
    }

    /**
     * 微信普通红包支付
     * 2018-12-25
     * xhx
     * @throws Exception
     */
    public String sendredpack(WXHB wxhb) throws Exception {
        logger.info("=======================================================================微信普通红包支付开始");
        logger.info("微信普通红包支付入口参数："+wxhb.toString());
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("nonce_str", wxhb.getNonce_str());
        packageParams.put("mch_billno", wxhb.getMchBillno());
        packageParams.put("mch_id", wxhb.getMchId());
        packageParams.put("wxappid", wxhb.getWxappid());
//		packageParams.put("msgappid", wxhb.getMsgappid());
        packageParams.put("send_name", wxhb.getSendName());
        packageParams.put("re_openid", wxhb.getReOpenid());
        packageParams.put("total_amount", wxhb.getTotalAmount());
        packageParams.put("total_num", wxhb.getTotalNum());
        packageParams.put("wishing", wxhb.getWishing());
        packageParams.put("client_ip", wxhb.getClientIp());
        packageParams.put("act_name", wxhb.getActName());
        packageParams.put("remark", wxhb.getRemark());
        RequestHandler reqHandler = new RequestHandler();
        reqHandler.init(wxhb.getAppKey());
        String sign = reqHandler.createSign(packageParams);
        /** 封装报文 */
        StringBuilder xml = new StringBuilder("<xml>")
                .append("<nonce_str><![CDATA[").append( wxhb.getNonce_str() ).append("]]></nonce_str>")
                .append("<sign><![CDATA[").append( sign ).append("]]></sign>")
                .append("<mch_billno><![CDATA[").append(wxhb.getMchBillno()).append("]]></mch_billno>")
                .append("<mch_id><![CDATA[").append( wxhb.getMchId() ).append("]]></mch_id>")
                .append("<wxappid><![CDATA[").append( wxhb.getWxappid() ).append("]]></wxappid>")
                .append("<send_name>").append( wxhb.getSendName() ).append("</send_name>")
                .append("<re_openid><![CDATA[").append( wxhb.getReOpenid() ).append("]]></re_openid>")
                .append("<total_amount><![CDATA[").append( wxhb.getTotalAmount() ).append("]]></total_amount>")
                .append("<total_num><![CDATA[").append( wxhb.getTotalNum() ).append("]]></total_num>")
                .append("<wishing><![CDATA[").append( wxhb.getWishing() ).append("]]></wishing>")
                .append("<client_ip><![CDATA[").append( wxhb.getClientIp() ).append("]]></client_ip>")
                .append("<act_name><![CDATA[").append( wxhb.getActName() ).append("]]></act_name>")
                .append("<remark><![CDATA[").append(wxhb.getRemark()).append("]]></remark>")
                .append("</xml>");

        //获取预支付ID
        String createOrderURL = "https://"+WXPayConstants.DOMAIN_API+WXPayConstants.MMPAYMKTTRANSFERS_SENDREDPACK_URL_SUFFIX;
        logger.info("微信普通红包支付prepayId请求地址: "+createOrderURL+", 请求数据: "+xml);
        String prepayContent = sslPost(createOrderURL , xml.toString(),wxhb.getMchId());
        logger.info("微信普通红包支付prepayId请求返回结果: "+prepayContent);
        return payApiErrDispose(prepayContent, createOrderURL, xml.toString(), wxhb.getMchId());
    }

    /**
     * 微信裂变红包支付
     * 2018-12-25
     * xhx
     * @throws Exception
     */
    public String sendFissionRedpack(WXHB wxhb) throws Exception{
        logger.info("=======================================================================微信裂变红包支付开始");
        logger.info("微信裂变红包支付入口参数："+wxhb.toString());
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("nonce_str", wxhb.getNonce_str());
        packageParams.put("mch_billno", wxhb.getMchBillno());
        packageParams.put("mch_id", wxhb.getMchId());
        packageParams.put("wxappid", wxhb.getWxappid());
//		packageParams.put("msgappid", wxhb.getMsgappid());
        packageParams.put("send_name", wxhb.getSendName());
        packageParams.put("re_openid", wxhb.getReOpenid());
        packageParams.put("total_amount", wxhb.getTotalAmount());
        packageParams.put("total_num", wxhb.getTotalNum());
        packageParams.put("amt_type", wxhb.getAmt_type());
        packageParams.put("wishing", wxhb.getWishing());
        packageParams.put("client_ip", wxhb.getClientIp());
        packageParams.put("act_name", wxhb.getActName());
        packageParams.put("remark", wxhb.getRemark());
        RequestHandler reqHandler = new RequestHandler();
        reqHandler.init(wxhb.getAppKey());
        String sign = reqHandler.createSign(packageParams);
        /** 封装报文 */
        StringBuilder xml = new StringBuilder("<xml>")
                .append("<nonce_str><![CDATA[").append( wxhb.getNonce_str() ).append("]]></nonce_str>")
                .append("<sign><![CDATA[").append( sign ).append("]]></sign>")
                .append("<mch_billno><![CDATA[").append(wxhb.getMchBillno()).append("]]></mch_billno>")
                .append("<mch_id><![CDATA[").append( wxhb.getMchId() ).append("]]></mch_id>")
                .append("<wxappid><![CDATA[").append( wxhb.getWxappid() ).append("]]></wxappid>")
                .append("<send_name>").append( wxhb.getSendName() ).append("</send_name>")
                .append("<re_openid><![CDATA[").append( wxhb.getReOpenid() ).append("]]></re_openid>")
                .append("<total_amount><![CDATA[").append( wxhb.getTotalAmount() ).append("]]></total_amount>")
                .append("<total_num><![CDATA[").append( wxhb.getTotalNum() ).append("]]></total_num>")
                .append("<amt_type><![CDATA[").append( wxhb.getAmt_type() ).append("]]></amt_type>")
                .append("<wishing><![CDATA[").append( wxhb.getWishing() ).append("]]></wishing>")
                .append("<client_ip><![CDATA[").append( wxhb.getClientIp() ).append("]]></client_ip>")
                .append("<act_name><![CDATA[").append( wxhb.getActName() ).append("]]></act_name>")
                .append("<remark><![CDATA[").append( wxhb.getRemark() ).append("]]></remark>")
                .append("</xml>");

        //获取预支付ID
        String createOrderURL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendgroupredpack";
        logger.info("微信裂变红包支付prepayId请求地址: "+createOrderURL+", 请求数据: "+xml);
        String prepayContent = sslPost(createOrderURL , xml.toString(),wxhb.getMchId());
        logger.info("微信裂变红包支付prepayId请求返回结果: "+prepayContent);
        return payApiErrDispose(prepayContent, createOrderURL, xml.toString(), wxhb.getMchId());
    }


    /**
     * 微信红包查询
     * 2018-12-25
     * xhx
     * @throws Exception
     */
    public String gethbinfo(WXHB wxhb) throws Exception {
        logger.info("=======================================================================微信红包查询开始");
        logger.info("微信红包查询入口参数："+wxhb.toString());
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("mch_billno", wxhb.getMchBillno());
        packageParams.put("mch_id", wxhb.getMchId());
        packageParams.put("appid", wxhb.getWxappid());
        packageParams.put("bill_type", wxhb.getBill_type());
        packageParams.put("nonce_str", wxhb.getNonce_str());
        RequestHandler reqHandler = new RequestHandler();
        reqHandler.init(wxhb.getAppKey());
        String sign = reqHandler.createSign(packageParams);
        /** 封装报文 */
        StringBuilder xml = new StringBuilder("<xml>")
                .append("<nonce_str><![CDATA[").append( wxhb.getNonce_str() ).append("]]></nonce_str>")
                .append("<sign><![CDATA[").append( sign ).append("]]></sign>")
                .append("<mch_billno><![CDATA[").append(wxhb.getMchBillno()).append("]]></mch_billno>")
                .append("<mch_id><![CDATA[").append( wxhb.getMchId() ).append("]]></mch_id>")
                .append("<appid><![CDATA[").append( wxhb.getWxappid() ).append("]]></appid>")
                .append("<bill_type>").append( wxhb.getBill_type() ).append("</bill_type>")
                .append("</xml>");

        //获取预支付ID
        String createOrderURL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gethbinfo";
        logger.info("微信红包查询prepayId请求地址: "+createOrderURL+", 请求数据: "+xml);
        String prepayContent = sslPost(createOrderURL , xml.toString(),wxhb.getMchId());
        logger.info("微信红包查询prepayId请求返回结果: "+prepayContent);
        return payApiErrDispose(prepayContent, createOrderURL, xml.toString(), wxhb.getMchId());
    }

    private String sslPost(String requestUrl, String outputStr,String mch_id) throws Exception{
        try {
            return HttpClientUtil.sslPost(requestUrl , outputStr,mch_id);
        } catch (FileNotFoundException e) {
            baseOrderManager.qrcodeVisitReply(mch_id);
            return HttpClientUtil.sslPost(requestUrl , outputStr,mch_id);
        }
    }

    private String payApiErrDispose(String prepayContent,String createOrderURL,String xml,String mch_id) throws Exception {
        if(prepayContent.contains(ERR_CODE)){
            Map<String, String> prepayMap = xmlToMap(prepayContent);
            String errCode = prepayMap.get(ERR_CODE);
            // 证书错误
            if("CA_ERROR".equals(errCode)){
                baseOrderManager.qrcodeVisitReply(mch_id);
                // 再次执行
                prepayContent = HttpClientUtil.sslPost(createOrderURL , xml,mch_id);
                logger.info("微信支付到零钱prepayId请求返回结果: "+prepayContent);
                // 还有错误
                if(prepayContent.contains(ERR_CODE)){
                    prepayMap = xmlToMap(prepayContent);
                    errCode = prepayMap.get(ERR_CODE);
                    /*if(!SUCCESS.equals(errCode)){
                        String errCodeDes = prepayMap.get(ERR_CODE_DES);
                        throw new ResultException(errCodeDes);
                    }*/
                }
            }else if(!SUCCESS.equals(errCode)){
                /*String errCodeDes = prepayMap.get(ERR_CODE_DES);
                throw new ResultException(errCodeDes);*/

            }

        }
        return prepayContent;
    }




    /**
     * 微信订单查询
     * @param wx
     * @return
     */
    public String orderquery(APPWxPayBean wx){
        logger.info("=======================================================================微信订单查询开始");
        logger.info("微信订单查询入口参数："+wx.toString());
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", wx.getAppid());
        packageParams.put("mch_id", wx.getMch_id());
        packageParams.put("sub_appid", wx.getSub_appid());
        packageParams.put("sub_mch_id", wx.getSub_mch_id());
        packageParams.put("nonce_str", wx.getNonce_str());
        packageParams.put("out_trade_no", wx.getOut_trade_no());
        RequestHandler reqHandler = new RequestHandler();
        reqHandler.init(wx.getAppKey());
        String sign = reqHandler.createSign(packageParams);
        wx.setSign(sign);
        /** 封装报文 */
        StringBuilder xml = new StringBuilder("<xml>")
                .append("<appid><![CDATA[").append(wx.getAppid()).append("]]></appid>")
                .append("<mch_id><![CDATA[").append(wx.getMch_id()).append("]]></mch_id>")
                .append("<sub_appid><![CDATA[").append( wx.getSub_appid() ).append("]]></sub_appid>")
                .append("<sub_mch_id><![CDATA[").append( wx.getSub_mch_id() ).append("]]></sub_mch_id>")
                .append("<nonce_str><![CDATA[").append( wx.getNonce_str() ).append("]]></nonce_str>")
                .append("<out_trade_no><![CDATA[").append( wx.getOut_trade_no() ).append("]]></out_trade_no>")
                .append("<sign><![CDATA[").append( wx.getSign() + "]]></sign>")
                .append("</xml>");

        //获取预支付ID
        String createOrderURL = "https://"+WXPayConstants.DOMAIN_API+WXPayConstants.ORDERQUERY_URL_SUFFIX;
        logger.info("微信订单查询prepayId请求地址: "+createOrderURL+", 请求数据: "+xml);
        String prepayContent = HttpClientUtil.post(createOrderURL , xml.toString());
        logger.info("微信订单查询prepayId请求返回结果: "+prepayContent);
        return prepayContent;
    }

    public String create_nonce_str() {
        String guid = UUID.randomUUID().toString();
        return MD5Util.md5(guid).toUpperCase();
    }

    public String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    /**
     * XML格式字符串转换为Map
     *
     * @param strXML
     *            XML字符串
     * @return XML数据转换后的Map
     * @throws Exception
     */
    public Map<String, String> xmlToMap(String strXML) throws Exception {
        try {
            Map<String, String> data = new HashMap<String, String>();
            DocumentBuilder documentBuilder = WXPayXmlUtil.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(
                    strXML.getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
            try {
                stream.close();
            } catch (Exception ex) {
                // do nothing
            }
            return data;
        } catch (Exception ex) {
            logger.error(
                    "[Invalid XML, can not convert to map. Error message: {}. XML content: {}]",
                    new Object[] { ex.getMessage(), strXML });
            throw ex;
        }

    }

    /**
     * 判断签名是否正确，必须包含sign字段，否则返回false。
     *
     * @param data
     *            Map类型数据
     * @param key
     *            API密钥
     * @param signType
     *            签名方式
     * @return 签名是否正确
     * @throws Exception
     */
    public boolean isSignatureValid(Map<String, String> data,
                                           String key, SignType signType) throws Exception {
        if (!data.containsKey(WXPayConstants.FIELD_SIGN)) {
            return false;
        }
        String sign = data.get(WXPayConstants.FIELD_SIGN);
        return generateSignature(data, key, signType).equals(sign);
    }

    /**
     * 生成签名
     *
     * @param data
     *            待签名数据
     * @param key
     *            API密钥
     * @return 签名
     */
    public String generateSignature(final Map<String, String> data,
                                           String key) throws Exception {
        return generateSignature(data, key, SignType.MD5);
    }

    /**
     * 生成签名. 注意，若含有sign_type字段，必须和signType参数保持一致。
     *
     * @param data
     *            待签名数据
     * @param key
     *            API密钥
     * @param signType
     *            签名方式
     * @return 签名
     */
    public String generateSignature(final Map<String, String> data,
                                           String key, SignType signType) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals(WXPayConstants.FIELD_SIGN)) {
                continue;
            }
            if (data.get(k).trim().length() > 0) {// 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.get(k).trim()).append("&");
            }
        }
        sb.append("key=").append(key);
        if (SignType.MD5.equals(signType)) {
            return MD5(sb.toString()).toUpperCase();
        } else if (SignType.HMACSHA256.equals(signType)) {
            return HMACSHA256(sb.toString(), key);
        } else {
            throw new Exception(
                    String.format("Invalid sign_type: %s", signType));
        }
    }

    /**
     * 生成 MD5
     *
     * @param data
     *            待处理数据
     * @return MD5结果
     */
    public String MD5(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100)
                    .substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 生成 HMACSHA256
     *
     * @param data
     *            待处理数据
     * @param key
     *            密钥
     * @return 加密结果
     * @throws Exception
     */
    public String HMACSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"),
                "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100)
                    .substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }
}
