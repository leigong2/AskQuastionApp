package com.example.jsoup.jsoup;


import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://my.oschina.net/haitaohu/blog/1941179
public class HlsDownloader {

    private String preUrl;
    private String rootDir;
    private String fileName;

    private static HlsDownloader hlsDownloader;
    private HlsDownloader(){}
    public static HlsDownloader getInstance() {
        if (hlsDownloader == null) {
            hlsDownloader = new HlsDownloader();
        }
        return hlsDownloader;
    }

    public void download(String fullUrl, String dir, String path) {
        String preUrlPath = fullUrl.substring(0, fullUrl.lastIndexOf("/")+1);
        this.preUrl = preUrlPath;
        this.rootDir = dir;
        this.fileName = path + ".mp4";
        File file = new File(dir + File.separator + path);
        if (!file.exists()) {
            file.mkdirs();
        }
        List<String> urlList = getAnalysisIndex(fullUrl);
        downLoadFastFile(urlList);
    }

    /* 解析索引文件 */
    private List<String> getAnalysisIndex(String fullUrl) {
        List<String> list = new ArrayList<String>();
        try {
            URL url = new URL(fullUrl);
            //下载资源
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line).append("\n");
            }
            in.close();
            Pattern pattern = Pattern.compile(".*ts");
            Matcher ma = pattern.matcher(content);
            while (ma.find()) {
                list.add(ma.group());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /* 下载视频 */
    private void downLoadFastFile(List<String> urlList) {
        try {
            File file = new File(rootDir + File.separator + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(file);
            // 下载网络文件
            int byteread = 0;
            for (String s : urlList) {
                URL url = new URL(preUrl + s);
                URLConnection conn = url.openConnection();
                InputStream inStream = conn.getInputStream();
                byte[] buffer = new byte[1204];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}