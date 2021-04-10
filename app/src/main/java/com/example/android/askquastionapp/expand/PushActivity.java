package com.example.android.askquastionapp.expand;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.askquastionapp.utils.ToastUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.example.android.askquastionapp.views.ExpandableImageTextView;
import com.example.android.askquastionapp.utils.GsonGetter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PushActivity extends AppCompatActivity {

    //#what is your name  my name is @龙    岸 @hello world 你好世界
    private String[] names = {"@@龙    岸", "#what is your name", "@hello world"};

    private StringBuilder content = new StringBuilder();

    private List<MyTopicAndName> topicAndNames = new ArrayList<>();

    private StringBuilder json = new StringBuilder();
    private ExpandableImageTextView expandClickView;

    public static void start(Context context) {
        Intent intent = new Intent(context, PushActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);
        for (int i = 0; i < 2; i++) {
            FollowBean f = new FollowBean();
            f.userId = "123" + i;
            f.userName = names[0];
            persons.add(f);
        }
        findViewById(R.id.push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content.setLength(0);
                json.setLength(0);
                content.append(addInvisibleCharBeforeWhiteSpace(names[1]))
                        .append(" my name is ")
                        .append(addInvisibleCharBeforeWhiteSpace(names[0]))
                        .append(" ")
                        .append(addInvisibleCharBeforeWhiteSpace(names[2]))
                        .append(" 你好世界");

                Log.i("zune ", "content = " + content);
                String[] split = content.toString().split("@|#| ");
                StringBuilder temp = new StringBuilder();
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    if (s.endsWith(String.valueOf(INVISIBLE_CHAR))) {
                        int index = getIndex(i, split) - 1;
                        char afterChar = content.charAt(index > content.length() - 1 ? content.length() - 1 : index);
                        temp.append(s).append(afterChar);
                        if (i < split.length - 1) {
                            continue;
                        } else {
                            s = temp.toString();
                            temp.setLength(0);
                        }
                    } else if (!TextUtils.isEmpty(temp)) {
                        s = temp.append(s).toString();
                        temp.setLength(0);
                    }
                    if (TextUtils.isEmpty(s.trim())) {
                        continue;
                    }
                    int index = content.toString().indexOf(s);
                    addList(index, s);
                    while (index != -1) {
                        index = content.toString().indexOf(s, index + 1);
                    }
                }
                json.append(GsonGetter.getInstance().getGson().toJson(topicAndNames));
                Log.i("zune ", "getListAndJson: json = " + json);
                expandClickView.setMeasureText(content);
            }
        });

        findViewById(R.id.parse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable.just(content).delay(1, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<StringBuilder, Integer>(1, true) {
                            @Override
                            public void onNext(StringBuilder content, Integer integer2) {
                                String translateContent = addInvisibleCharBeforeWhiteSpace(names[1]) +
                                        " 私の名前は  " +
                                        addInvisibleCharBeforeWhiteSpace(names[0]) +
                                        " " +
                                        addInvisibleCharBeforeWhiteSpace(names[2]) +
                                        " こんにちは、世界";
                                translateText(translateContent);
                            }
                        });
            }
        });
        expandClickView = findViewById(R.id.expandable_click);
        expandClickView.setOnShowExpand(new ExpandableImageTextView.OnShowExpand() {
            @Override
            public void showExpand() {
                ToastUtils.showShort("showExpand");
            }

            @Override
            public void onClickTopic(String topicName) {
                ToastUtils.showShort("onClickTopic = " + topicName);
            }

            @Override
            public void onClickUser(String userName) {
                ToastUtils.showShort("onClickUser = " + userName);
            }
        });
    }

    private void translateText(String translateContent) {
        expandClickView.setMeasureText(translateContent);
    }

    private ArrayList<FollowBean> persons = new ArrayList<>();

    private void addList(int index, String s) {
        Log.i("zune", "checkTintColor: index = " + index + ", s  = " + s);
        MyTopicAndName name = new MyTopicAndName();
        if (s.startsWith("#")) {
            name.type = 2;
            name.content = s;
            topicAndNames.add(name);
        } else if (s.startsWith("@")) {
            name.type = 3;
            name.content = s;
            for (FollowBean person : persons) {
                if (name.content.equals(person.userName)) {
                    name.userId = person.userId;
                    break;
                }
            }
            topicAndNames.add(name);
        } else {
            name.type = 1;
            name.content = " " + s;
            topicAndNames.add(name);
        }
    }

    private int getIndex(int position, String[] split) {
        int index = 0;
        for (int i = 0; i < split.length; i++) {
            index += split[i].length();
            index++;
            if (position == i) {
                return index;
            }
        }
        return index;
    }

    public static char INVISIBLE_CHAR = '\u200b';

    /**
     * 碰到空格,@和#就在其前面加一个不可见字符
     *
     * @param name
     * @return
     */
    public String addInvisibleCharBeforeWhiteSpace(String name) {
        if (TextUtils.isEmpty(name)) {
            return name;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == ' ' || c == '@' || c == '#') {
                stringBuilder.append(INVISIBLE_CHAR);
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static class MyTopicAndName {
        public int type;
        public String content;
        public String userId;
    }

    public class FollowBean {
        public String userId;
        public String userName;
    }
}
