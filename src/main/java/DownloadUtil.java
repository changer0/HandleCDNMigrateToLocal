

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

public class DownloadUtil implements X509TrustManager {
    public static void main(String[] args) throws Exception {
        /*从https下载文件,并保存到桌面,文件名字段获取*/
        String path = "C:\\Users\\zll88\\Desktop\\MD Test 文档";
        downLoadFromUrlHttps(
                "https://gitee.com/luluzhang/ImageCDN/raw/master/blog/202202192128613.png",
//                "https://www.keaidian.com/uploads/allimg/190424/24110307_8.jpg",
                "202202192128613.png",
                path,
                "user_locale=zh-CN; oschina_new_user=false; tz=Asia/Shanghai; sajssdk_2015_cross_new_user=1; remember_user_token=BAhbCFsGaQMEVxBJIiIkMmEkMTAkRTB5ZG1kSDVTNmUxVDNVbEVDQXFSZQY6BkVUSSIVMTY1MzM3OTcxNy44MDMxMgY7AEY=--d5799984152c4c292e34ac48111e4134fbd67171; gitee_user=true; remote_way=http; sensorsdata2015jssdkcross={\"distinct_id\":\"1070852\",\"first_id\":\"180f51c1b209b5-08b7ded9951ae6-4c647e53-2073600-180f51c1b21d2c\",\"props\":{},\"$device_id\":\"180f51c1b209b5-08b7ded9951ae6-4c647e53-2073600-180f51c1b21d2c\"}; close_wechat_tour=true; gitee-session-n=V2xJTkFBMGM5SEk4dkNDemRYN1FZVWpoTksxZG95UWw5eis5SUlkSkVrSWJnd0RwQmZQcnFoK0pQWDJnREMwWmxlcDVOWkhBM3RVUmpmRjhCcGZkRnRGMkZUR2sxanhMMjUzeERUQTYrVnFzSHJtSGQ2bXhZUzlBUFRCVEZXRXViL1Y5TlZMQkh6TlI2cTEyaDJqMnllUFljL3RSai9wNm9BcjNXU0QzZnd5QmtXNFVjM1FxeW9lOE9LSjV5Qi9NM0txdk9Mc0Vlb09uQjN3eks0RzUyVDJLZUFrSTJIVWE0UGNCYm10SmlCM0dYVTlYL0paSmNXSFp4dVpNdko1elFBcW0yd2RWb0ZGaTFtMTVoUzVGWk56YWFDQ1BWbnlZNzU5cDVSRFhIK2pZcnFGUUNEclNobDlOaTllOXdwdXh5NDBjR2k0Q0Z5ck9TVkxRYjBJMkNZdVBiellCd2pEcnFiOHgrM0RnOEZXczdsZTJ1dTR2bkRsNGF6engvYmplYkZrZXVDZHZQcGpSSGxtM2RPdzdLOFVDZXhuMXhNK2lmTFNCd20xaXJ4TmVzMEhTbmhzNUFML1ErWkJWdDZsMUhzNlJmOENwZlhhV2VXL3puRmhYS0E9PS0tdVdCK3MyRm9QM3dDSngwV1FYa3JXQT09--fe624c6cbf2d2a59d9221abb193e5eb6b1bf71b8"
        );
    }

    /*
     * 处理https GET/POST请求 请求地址、请求方法、参数
     */
    public static String httpsRequest(String requestUrl, String requestMethod,
                                      String outputStr) {
        StringBuffer buffer = null;
        try {
            // 创建SSLContext
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] tm = {new DownloadUtil()};
            // 初始化
            sslContext.init(null, tm, new java.security.SecureRandom());

            // 获取SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            // url对象
            URL url = new URL(requestUrl);
            // 打开连接
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            /**
             * 这一步的原因: 当访问HTTPS的网址。您可能已经安装了服务器证书到您的JRE的keystore
             * 但是服务器的名称与证书实际域名不相等。这通常发生在你使用的是非标准网上签发的证书。
             *
             * 解决方法：让JRE相信所有的证书和对系统的域名和证书域名。
             *
             * 如果少了这一步会报错:java.io.IOException: HTTPS hostname wrong: should be localhost
             */
            conn.setHostnameVerifier(new DownloadUtil().new TrustAnyHostnameVerifier());
            // 设置一些参数
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(requestMethod);
            // 设置当前实例使用的SSLSoctetFactory
            conn.setSSLSocketFactory(ssf);
            conn.connect();
            // 往服务器端的参数
            if (null != outputStr) {
                OutputStream os = conn.getOutputStream();
                os.write(outputStr.getBytes("utf-8"));
                os.close();
            }
            // 读取服务器端返回的内容
            InputStream is = conn.getInputStream();
            //读取内容
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            buffer = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }


    public static void downLoadFromUrlHttps(String urlStr, String fileName,
                                            String savePath, String cookie) throws Exception {
        // 创建SSLContext
        SSLContext sslContext = SSLContext.getInstance("SSL");
        TrustManager[] tm = {new DownloadUtil()};
        // 初始化
        sslContext.init(null, tm, new java.security.SecureRandom());
        // 获取SSLSocketFactory对象
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        // url对象
        URL url = new URL(urlStr);
        // 打开连接
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        /**
         * 这一步的原因: 当访问HTTPS的网址。您可能已经安装了服务器证书到您的JRE的keystore
         * 但是服务器的名称与证书实际域名不相等。这通常发生在你使用的是非标准网上签发的证书。
         *
         * 解决方法：让JRE相信所有的证书和对系统的域名和证书域名。
         *
         * 如果少了这一步会报错:java.io.IOException: HTTPS hostname wrong: should be <localhost>
         */
        conn.setHostnameVerifier(new DownloadUtil().new TrustAnyHostnameVerifier());
        conn.setRequestProperty("Cookie", cookie);
        //conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.64 Safari/537.36 Edg/101.0.1210.53");

        // 设置一些参数
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        // 设置当前实例使用的SSLSoctetFactory
        conn.setSSLSocketFactory(ssf);
        conn.connect();


        // 得到输入流
        InputStream inputStream = conn.getInputStream();
        byte[] getData = readInputStream(inputStream);
        // 文件保存位置
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        //输出流
        File file = new File(saveDir + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if (fos != null) {
            fos.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }


    /**
     * 从网络http类型Url中下载文件
     *
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static void downLoadFromUrlHttp(String urlStr, String fileName,
                                           String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // 设置超时间为3秒
        conn.setConnectTimeout(3 * 1000);
        // 防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        conn.connect();

        // 得到输入流
        InputStream inputStream = conn.getInputStream();
        byte[] getData = readInputStream(inputStream);
        // 文件保存位置
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        // 输出流
        File file = new File(saveDir + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if (fos != null) {
            fos.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }


    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream)
            throws IOException {
        byte[] b = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(b)) != -1) {
            bos.write(b, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }


    /***
     * 校验https网址是否安全
     *
     * @author solexit06
     *
     */
    public class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            // 直接返回true:默认所有https请求都是安全的
            return true;
        }
    }


    /*
     * 里面的方法都是空的，当方法为空是默认为所有的链接都为安全，也就是所有的链接都能够访问到 当然这样有一定的安全风险，可以根据实际需要写入内容
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }


    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }


    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
