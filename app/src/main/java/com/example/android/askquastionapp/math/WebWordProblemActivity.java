package com.example.android.askquastionapp.math;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.LatexToSpannedUtils;
import com.example.android.askquastionapp.views.VerticalCenterSpan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.android.askquastionapp.utils.HtmlToSpannedUtils.loadHtmlContent;

public class WebWordProblemActivity extends AppCompatActivity {
    String mainText = "{\n" +
            "    \"options\": [\n" +
            "        \"A.已知<i >x</i>,<i >y</i>满足约束条件<img src='http://47.95.246.224/Uploads/word/2017/0823/599ce98f6dc9e582354.files/image012.png' height='87' width ='127' style='vertical-align:middle;' />则<i >z</i>=<i >x+</i>2<i >y</i>的最大值是的最大值是的最大值是的最大值是的最大值是的最大值是的最大值是\",\n" +
            "        \"B.已知命题<i >p</i>:∀<i >x</i>>0,ln(<i >x+</i>1)>0;命题<i >q</i>:若<i >a</i>><i >b</i>,则<i >a</i><sup>2</sup>><i >b</i><sup>2</sup>.下列命题为真命题的是\",\n" +
            "        \"C.喧<u>阗</u>(tián)      <u>召</u>集(zhào)     <u>怂</u>恿(sǒng)    命途多<u>舛</u>(chuǎn)\"\n" +
            "    ],\n" +
            "    \"testTitle\": \"在平面直角坐标系<i >xOy</i>中,椭圆<i >E</i>:<img src='http://47.95.246.224/Uploads/word/2017/0823/599ce98f6dc9e582354.files/image082.png' height='43' width ='14' style='vertical-align:middle;' /><i >+</i><img src='http://47.95.246.224/Uploads/word/2017/0823/599ce98f6dc9e582354.files/image084.png' height='43' width ='15' style='vertical-align:middle;' />=1(<i >a</i>><i >b</i>>0)的离心率为<i >k</i><sub>2</sub>,且<i >k</i><sub>1</sub><i >k</i><sub>2</sub>=<img src='http://47.95.246.224/Uploads/word/2017/0823/599ce98f6dc9e582354.files/image190.png' height='43' width ='15' style='vertical-align:middle;' />,<i >M</i>是线段<i >OC</i>延长线上一点,且<i >|MC|</i>∶<i >|AB|</i>=2∶3,☉<i >M</i>的半径为<i >|MC|</i>,<i >OS</i>,<i >OT</i>是☉<i >M</i>的两条切线,切点分别为<i >S</i>,<i >T</i>.求∠<i >SOT</i>的最大值,并求取得最大值时直线<i >l</i>的斜率.</p><p><img src='http://47.95.246.224/Uploads/word/2017/0823/599ce98f6dc9e582354.files/image192.jpg' height='145' width ='151' style='vertical-align:middle;'/>\"\n" +
            "}";
    String additionalTest = "{\n" +
            "    \"options\": [\n" +
            "        \"A.求下面函数的结果@math#\\left( \\sum_{k=1}^n a_k b_k \\right)^2 \\leq \\left( \\sum_{k=1}^n a_k^2 \\right) \\left( \\sum_{k=1}^n b_k^2 \\right)@/math#并把答案告诉我\",\n" +
            "        \"B.这个题不需要做,大家看看就行了@math#f(x) = {x^2} - 3x - 18,x \\in \\left[ {1,8} \\right]@/math#因为肯定没人能看懂的\"\n" +
            "    ],\n" +
            "    \"testTitle\": \"求出@math#\\displaystyle= \\left(\\sum_{i=1}^{k}i\\right) +(k+1)@/math#中你觉得最适合变成的那个人，并求出最不合适的那个人()\"\n" +
            "}";
    private String testTitle;

    public static void start(Context context) {
        Intent intent = new Intent(context, WebWordProblemActivity.class);
        context.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_word_problem);
        startFirstQuestion();
        startSecondQuestion();
        WebView webView = findViewById(R.id.webview);
        StringBuilder stringBuilder = initWebViewSetting(webView, mainText);
        webView.loadDataWithBaseURL(null, stringBuilder.toString(), "text/html", "utf-8", null);
    }

    /**
     * zune:html适配
     **/
    private StringBuilder initWebViewSetting(WebView web, String data) {
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setBlockNetworkImage(false);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        web.setSaveEnabled(false);
        web.setWebChromeClient(new WebChromeClient());
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"utf-8\"/>");
        html.append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0," +
                "maximum-scale=1.0,user-scalable=no\"/>");
        html.append("<style> img { padding: 0px;max-width:100%;}.box3{overflow-x:hidden}" +
                "p{color: #333333;}</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='box3' style=\"width: 100%;padding:0px 6px 10px 6px;" +
                "box-sizing: border-box;\">");
        html.append(data);
        html.append("</div>");
        html.append("</body>");
        html.append("<script type=\"text/javascript\"> window.onload = function imgcenter() " +
                "{ var box = document.getElementsByClassName(\"box3\"); " +
                "var img = box[0].getElementsByTagName(\"img\"); " +
                "for(i = 0; i < img.length; i++) { " +
                "var imgParentNodes = img[i].parentNode;console.log(imgParentNodes) " +
                "imgParentNodes.style.textIndent = 'inherit';}}</script>");
        html.append("</html>");
        return html;
    }

    private void startSecondQuestion() {
        StringBuilder src = new StringBuilder();
        for (int i = 0; i < additionalTest.length(); i++) {
            char c = additionalTest.charAt(i);
            if (c == '\\') {
                src.append("\\\\");
            } else {
                src.append(c);
            }
        }
        try {
            JSONObject jsonObject = new JSONObject(src.toString());
            String testTitle = (String) jsonObject.get("testTitle");
            ((TextView) findViewById(R.id.title2)).setText(getSpanFromString(testTitle));
            JSONArray jsonArray = (JSONArray) jsonObject.get("options");
            for (int i = 0; i < jsonArray.length(); i++) {
                String o = (String) jsonArray.get(i);
                switch (i) {
                    case 0:
                        ((TextView) findViewById(R.id.first_text2)).setText(getSpanFromString(o));
                        break;
                    case 1:
                        ((TextView) findViewById(R.id.second_text2)).setText(getSpanFromString(o));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SpannableStringBuilder getSpanFromString(String testTitle) {
        String regex = "(?<=@math#).*?(?=@/math#)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(testTitle);
        Map<String, Bitmap> maps = new HashMap<>();
        while (matcher.find()) {
            String group = matcher.group(0);
            Bitmap bitmap = LatexToSpannedUtils.mathToBitmap(14, group);
            maps.put("@math#" + group + "@/math#", bitmap);
        }
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(testTitle);
        for (String s : maps.keySet()) {
            int start = stringBuilder.toString().indexOf(s);
            int end = start + s.length();
            stringBuilder.replace(start, end, " ");
            BitmapDrawable drawable = new BitmapDrawable(getResources(), maps.get(s));
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            stringBuilder.setSpan(new VerticalCenterSpan(drawable), start, start + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return stringBuilder;
    }

    private void startFirstQuestion() {
        try {
            JSONObject jsonObject = new JSONObject(mainText);
            testTitle = (String) jsonObject.get("testTitle");
            loadHtmlContent(findViewById(R.id.title), testTitle);
            JSONArray jsonArray = (JSONArray) jsonObject.get("options");
            for (int i = 0; i < jsonArray.length(); i++) {
                String o = (String) jsonArray.get(i);
                switch (i) {
                    case 0:
                        loadHtmlContent(findViewById(R.id.first_text), o);
                        break;
                    case 1:
                        loadHtmlContent(findViewById(R.id.second_text), o);
                        break;
                    case 2:
                        loadHtmlContent(findViewById(R.id.third_text), o);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
