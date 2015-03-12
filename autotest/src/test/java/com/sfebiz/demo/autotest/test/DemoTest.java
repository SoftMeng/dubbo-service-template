package com.sfebiz.demo.autotest.test;

import com.sfebiz.demo.autotest.test.WebRequestUtil.ResponseFiller;
import com.sfebiz.demo.client.ApiConfig;
import com.sfebiz.demo.client.ApiContext;
import com.sfebiz.demo.client.BaseRequest;
import com.sfebiz.demo.client.ServerResponse;
import com.sfebiz.demo.client.api.request.*;
import com.sfebiz.demo.client.api.resp.Api_DEMO_DemoEntity;
import com.sfebiz.demo.client.util.Base64Util;
import com.sfebiz.demo.client.util.Md5Util;
import com.sfebiz.demo.client.util.RsaHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by guankaiqiang521 on 2014/9/29.
 */
public class DemoTest {
    private static final String url          = ApiConfig.apiUrl;
    private static final long   deviceId     = 1414807058834L;
    private static final String deviceSecret = "581bb3c7f2d09e4d2f07f69706fff13f261f4cfa2038cd2ab7bb46040ca2d568";
    private static final String deviceToken  = "jxpvuVNWcYb75UlLHC3QyptGUwn0V+LDzdi/GMTLcmGN1rmpX80ze7hRE8peb0dbjfUWi52dEoaZy6YCJZcF9L4f+2gJXMjncRCFhGY3AHo=";
    private static final long   userId       = 22L;
    private static final String userToken    = "F3Ul7EEwE0wUwZWlXYqZqf1l476xWq/yzmAFWWnjGchKZBnu2Sgb8dz4VnYgEy3F+Hfp/nxbb3vxlOkPi8Syb+U/BU1h8U+THkNSgHdU9UM=";

    private void initWithDeviceInfo(ApiContext context) {
        context.setDeviceInfo(deviceId, deviceSecret, deviceToken);
    }

    private void initWithUserInfo(ApiContext context) {
        initWithDeviceInfo(context);
        context.setUserInfo(userId, userToken, Long.MAX_VALUE);
    }

    @Test
    public void sayHelloTest() {
        final ApiContext context = new ApiContext("1", 123);
        final Demo_SayHello sayHello = new Demo_SayHello("abc");
        WebRequestUtil.fillResponse(url, context.getParameterString(sayHello), String.valueOf(System.currentTimeMillis()), true,
                                    new ResponseFiller() {
                                        public ServerResponse fill(InputStream is) {
                                            return context.fillResponse(sayHello, is);
                                        }
                                    });
        Api_DEMO_DemoEntity resp = sayHello.getResponse();
        Assert.assertEquals(ApiCode.SUCCESS, sayHello.getReturnCode());
        Assert.assertEquals(resp.id, 1);
        Assert.assertEquals(resp.name, "abc");
    }

    @Test
    public void tryErrorTest() {
        final ApiContext context = new ApiContext("1", 123);
        final Demo_TryError tryError = new Demo_TryError("abc");
        WebRequestUtil.fillResponse(url, context.getParameterString(tryError), String.valueOf(System.currentTimeMillis()), true,
                                    new ResponseFiller() {
                                        public ServerResponse fill(InputStream is) {
                                            return context.fillResponse(tryError, is);
                                        }
                                    });
        Assert.assertEquals(-100, tryError.getReturnCode());
    }

    @Test
    public void testRegistedDevice() {
        final ApiContext context = new ApiContext("1", 123);
        initWithDeviceInfo(context);
        final Demo_TestRegistedDevice regiestedDevice = new Demo_TestRegistedDevice();
        WebRequestUtil.fillResponse(url, context.getParameterString(regiestedDevice), String.valueOf(System.currentTimeMillis()), true,
                                    new ResponseFiller() {
                                        public ServerResponse fill(InputStream is) {
                                            return context.fillResponse(regiestedDevice, is);
                                        }
                                    });
        Assert.assertEquals(ApiCode.SUCCESS, regiestedDevice.getReturnCode());
    }

    @Test
    public void testUserLogin() {
        final ApiContext context = new ApiContext("1", 123);
        initWithUserInfo(context);
        Demo_TestUserLogin userLogin = new Demo_TestUserLogin();
        final BaseRequest[] requests = new BaseRequest[]{userLogin};
        WebRequestUtil.fillResponse(url, context.getParameterString(requests), String.valueOf(System.currentTimeMillis()), true,
                                    new ResponseFiller() {
                                        public ServerResponse fill(InputStream is) {
                                            return context.fillResponse(requests, is);
                                        }
                                    });
        System.out.println(userLogin.getResponse().value);
        Assert.assertEquals(ApiCode.SUCCESS, userLogin.getReturnCode());
    }

    @Test
    public void testReirectUrl() {
        final ApiContext context = new ApiContext("1", 123);
        final Demo_TestRedirect req = new Demo_TestRedirect();
        WebRequestUtil.fillResponse(url, context.getParameterString(req), String.valueOf(System.currentTimeMillis()), true, new ResponseFiller() {
            public ServerResponse fill(InputStream is) {
                return context.fillResponse(req, is);
            }
        });
        System.out.println(req.getResponse().value);
        Assert.assertEquals(ApiCode.SUCCESS, req.getReturnCode());
    }

    public static final Comparator<String> StringComparator = new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
            int n1 = s1 == null ? 0 : s1.length();
            int n2 = s2 == null ? 0 : s2.length();
            int mn = n1 < n2 ? n1 : n2;
            for (int i = 0; i < mn; i++) {
                int k = s1.charAt(i) - s2.charAt(i);
                if (k != 0) {
                    return k;
                }
            }
            return n1 - n2;
        }
    };
    @Test
    public void testIntegratedWithMd5() throws UnsupportedEncodingException {
        Map<String, String> mapping = new HashMap<String, String>();
        //1.设置请求参数
        mapping.put("_mt", "demo.getResByThirdPartyId");//方法名
        mapping.put("_tpid", "1");//tpid即集成第三方的编号（由网关统一分配），爬虫暂且用1
        mapping.put("in", "abcde xxxx");//其他方法入参
        mapping.put("_sm", "md5");//签名算法,目前支持rsa md5两种方式
        //2.待签数据准备
        StringBuilder sb = new StringBuilder(128);
        String[] array = mapping.keySet().toArray(new String[mapping.size()]);
        if (array.length > 0) {
            Arrays.sort(array, StringComparator);
            for (String key : array) {
                sb.append(key);
                sb.append("=");
                sb.append(mapping.get(key));
            }
        }
        System.out.println("before sig:" + sb.toString());
        //3.构造签名
        String md5Key = "2173c94dfb4ef2da8402f2f0c490995d";
        String sig = Md5Util.computeToHex(sb.append(md5Key).toString().getBytes("utf-8"));
        mapping.put("_sig", sig);
        System.out.println("sig:" + sig);
        StringBuilder req = new StringBuilder();
        for (Entry<String, String> entry : mapping.entrySet()) {
            req.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "utf-8")).append("&");//进行url encoding
        }
        System.out.println("http://115.28.145.123//m.api?" + req.toString());
    }
    @Test
    public void testIntegratedWithRSA() throws UnsupportedEncodingException {
        Map<String, String> mapping = new HashMap<String, String>();
        //1.设置请求参数
        mapping.put("_mt", "demo.getResByThirdPartyId");//方法名
        mapping.put("_tpid", "1");//tpid即集成第三方的编号（由网关统一分配），爬虫暂且用1
        mapping.put("in", "abcde xxxx");//其他方法入参
        mapping.put("_sm", "rsa");//签名算法,目前支持rsa md5两种方式
        //2.待签数据准备
        StringBuilder sb = new StringBuilder(128);
        String[] array = mapping.keySet().toArray(new String[mapping.size()]);
        if (array.length > 0) {
            Arrays.sort(array, StringComparator);
            for (String key : array) {
                sb.append(key);
                sb.append("=");
                sb.append(mapping.get(key));
            }
        }
        System.out.println("before sig:" + sb.toString());
        //3.构造签名
        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyYhw3zrUeFCmvuu82VAkFIX6NKtQPGdKAWVFYhXR9BwFeELmehdEUwcwoHECkzDN4DArsHegWx1nkv4S1+Yz3YIWc0eO2TQgQISw0moj7seqFiAwxzYko5BApabaXQJfR/veGWakEvJCk+jTrH/R6nv1V+8g71HWqnPKBbEdsyQIDAQAB";
        String pri = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALJiHDfOtR4UKa+67zZUCQUhfo0q1A8Z0oBZUViFdH0HAV4QuZ6F0RTBzCgcQKTMM3gMCuwd6BbHWeS/hLX5jPdghZzR47ZNCBAhLDSaiPux6oWIDDHNiSjkEClptpdAl9H+94ZZqQS8kKT6NOsf9Hqe/VX7yDvUdaqc8oFsR2zJAgMBAAECgYBgjYw6hM8x/bXmoWczX98WAOAv5turZM20nSPTp0C7H9yUnrbp4AKgmpk3qLswuDqvos0Sqslh8vtsPmHF4dJzdfXHBHDec93O/b4QTlKr6tTEPdjwkF/JU3mgMQZsNEUdmVHfNG2owsI+0VEfHMfn09VIgs4SQjSbijIQ7Td6VQJBAPbE5m3Q1dUfuDCHuxQrRCIcH8UWTgDLwqvFtfRiD+/C6jpsrarXHUIuxgiJ1jVq1TiE0X/pNc6oUBWJZNXJow8CQQC5DlYH/R573/r2al1y6sYmgGmneHeEbOffzngzxqU+8GNAIhWN1yC2DOdiMUCmgVP34WG4WcIpWHzAkfUnSRKnAkAzizM6cumHR8XYVTGNZ/AmU8uLBjqqzeTOrlBwSF9dzE/SfkrUKXSSE2UH+YqFw9ffo1aDKjoz/VIk/XrTcPefAkEAmN/a+maEVFlH/WEJKfIBF7Vlks/WDDPbqevrKPqlcEUt+MEvhSl/AGXQkDGX8vVL5K7wB1c/KuDKzlrFZ1raaQJBAOrZPzsHcsOS91fwRyVF37vdtRUS0YTMKnFAKI0254UXKbmzbqOSwKC3hkYcu9jIzWkMk8kB2SMFqh9+xPsTNTc=";
        RsaHelper rsaHelper = new RsaHelper(pub, pri);
        String sig = Base64Util.encodeToString(rsaHelper.sign(sb.toString().getBytes("utf-8")));
        mapping.put("_sig", sig);
        System.out.println("sig:" + sig);
        //4.构造请求
        StringBuilder req = new StringBuilder();
        for (Entry<String, String> entry : mapping.entrySet()) {
            req.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "utf-8")).append("&");//进行url encoding
        }
        System.out.println("http://127.0.0.1:8080/m.api?" + req.toString());
    }
}
