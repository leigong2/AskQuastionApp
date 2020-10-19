package com.example.android.askquastionapp.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.contacts.ContactBean;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;

import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import kotlin.text.Regex;

public class ContactsUtils {
    private static ContactsUtils contactsUtils;
    public static final String isChinaMobile = "^134[0-8]\\d{7}$|^(?:13[5-9]|147|15[0-27-9]|178|1703|1705|1706|18[2-478])\\d{7,8}$";
    public static final String isChinaUnion = "^(?:13[0-2]|145|15[56]|176|1704|1707|1708|1709|171|18[56])\\d{7,8}$";
    public static final String isChinaTelcom = "^(?:133|153|1700|1701|1702|177|173|18[019])\\d{7,8}$";

    private ContactsUtils() {
    }

    public static ContactsUtils getInstance() {
        if (contactsUtils == null) {
            contactsUtils = new ContactsUtils();
        }
        return contactsUtils;
    }

    public Observable<List<ContactBean>> getFastContacts(Context context) {
        return Observable.just(1).map(integer -> {
            String[] projection = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
            };
            List<ContactBean> contacts = new ArrayList<>();
            long currentTimeMillis = System.currentTimeMillis();
            contacts.clear();
            Cursor cursor = null;
            ContentResolver cr = context.getContentResolver();
            try {
                cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, "sort_key");
                if (cursor != null) {
                    final int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    final int mobileNoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    final int mobileNumNoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
                    String mobileNo, displayName, mobileNormalNo;
                    while (cursor.moveToNext()) {
                        mobileNo = cursor.getString(mobileNoIndex);
                        mobileNormalNo = cursor.getString(mobileNumNoIndex);
                        displayName = cursor.getString(displayNameIndex);
                        ContactBean temp = new ContactBean();
                        String s = replacePublicStr(mobileNormalNo.replaceAll(" ", ""), mobileNo.replaceAll(" ", ""));
                        temp.phone = mobileNo.startsWith("0") || mobileNo.startsWith("+") ? mobileNo : s + " " + mobileNo.replaceAll(" ", "");
                        temp.name = displayName;
                        contacts.add(temp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    Log.d("zune: ", "获取所有联系人耗时: " + (System.currentTimeMillis() - currentTimeMillis) + "，共计：" + cursor.getCount());
                    cursor.close();
                }
            }
            return contacts;
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    private String replacePublicStr(String strOne, String strTwo) {// 参数检查
        if(strOne==null || strTwo == null){
            return null;
        }
        if(strOne.equals("") || strTwo.equals("")){
            return null;
        }
        // 二者中较长的字符串
        String max = "";
        // 二者中较短的字符串
        String min = "";
        if(strOne.length() < strTwo.length()){
            max = strTwo;
            min = strOne;
        } else{
            max = strTwo;
            min = strOne;
        }
        String current = "";
        // 遍历较短的字符串，并依次减少短字符串的字符数量，判断长字符是否包含该子串
        for(int i=0; i<min.length(); i++){
            for(int begin=0, end=min.length()-i; end<=min.length(); begin++, end++){
                current = min.substring(begin, end);
                if(max.contains(current)){
                    return current.equals(strOne) ? "" : strOne.replaceAll(current, "");
                }
            }
        }
        return "";
    }

    public Observable<List<ContactBean>> getContacts(Context context) {
        Observable<List<ContactBean>> listObservable = Observable.just(1).map(new Function<Integer, List<ContactBean>>() {
            @Override
            public List<ContactBean> apply(Integer integer) throws Exception {
                List<ContactBean> contacts = new ArrayList<>();
                Cursor cursor = context.getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                while (cursor.moveToNext()) {
                    //新建一个联系人实例
                    ContactBean temp = new ContactBean();
                    String contactId = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    //获取联系人姓名
                    String name = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    temp.name = name;

                    //获取联系人电话号码
                    Cursor phoneCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
                    while (phoneCursor.moveToNext()) {
                        String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phone = phone.replace("-", "");
                        phone = phone.replace(" ", "");
                        temp.phone = phone;
                    }

                    //获取联系人备注信息
                    Cursor noteCursor = context.getContentResolver().query(
                            ContactsContract.Data.CONTENT_URI,
                            new String[]{ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Nickname.NAME},
                            ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='"
                                    + ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "'",
                            new String[]{contactId}, null);
                    if (noteCursor.moveToFirst()) {
                        do {
                            String note = noteCursor.getString(noteCursor
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
                            temp.note = note;
                            Log.i("note:", note);
                        } while (noteCursor.moveToNext());
                    }
                    contacts.add(temp);
                    //记得要把cursor给close掉
                    phoneCursor.close();
                    noteCursor.close();
                }
                cursor.close();
                return contacts;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        return listObservable;
    }

    public void addContacts(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            int contactPermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_CONTACTS);
            if (contactPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context
                        , new String[]{Manifest.permission.WRITE_CONTACTS}, 1);
                return;
            }
        }
        for (int i = 0; i < 1000; i++) {
            int middle = (int) (Math.random() * 10);
            if (middle == 0 || middle == 1 || middle == 2 || middle == 4 || middle == 6 || middle == 9) {
                middle = 3;
            }
            DecimalFormat decimalFormat = new DecimalFormat("000000000");
            int random = (int) (Math.random() * 999999999);
            String temp = decimalFormat.format(random);
            String number = String.format("%s%s%s", 1, middle, temp);
            ContactBean data = SetDataUtils.getInstance().createData(new ContactBean());
            addContact(context, data.name, number);
        }
    }

    public void addContact(Context context, String name, String phoneNumber) {
        if (Build.VERSION.SDK_INT >= 23) {
            int contactPermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_CONTACTS);
            if (contactPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context
                        , new String[]{Manifest.permission.WRITE_CONTACTS}, 1);
                return;
            }
        }
        // 创建一个空的ContentValues
        ContentValues values = new ContentValues();

        // 向RawContacts.CONTENT_URI空值插入，
        // 先获取Android系统返回的rawContactId
        // 后面要基于此id插入值
        Uri rawContactUri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        // 内容类型
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        // 联系人名字
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
        // 向联系人URI添加联系人名字
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        // 联系人的电话号码
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
        // 电话类型
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        // 向联系人电话号码URI添加电话号码
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        // 联系人的Email地址
        values.put(ContactsContract.CommonDataKinds.Email.DATA, "zhangphil@xxx.com");
        //修改联系人的头像
        int[] avatars = new int[]{R.mipmap.first, R.mipmap.second, R.mipmap.third, R.mipmap.forth, R.mipmap.fifth};
        Bitmap sourceBitmap = BitmapFactory.decodeResource(context.getResources(), avatars[(int) (Math.random() * avatars.length)]);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 将Bitmap压缩成PNG编码，质量为100%存储
        sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        byte[] avatar = os.toByteArray();
        values.put(ContactsContract.Contacts.Photo.PHOTO, avatar);
        // 电子邮件的类型
        values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        // 向联系人Email URI添加Email数据
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        Log.i("zune", "添加成功 name = " + name + ", phone = " + phoneNumber);
    }


    /*zune: 子线程中执行，并且已申请了权限**/
    @SuppressLint("SimpleDateFormat")
    private List<ContactBean> getCallLogs(Context context) {
        Uri callUri = CallLog.Calls.CONTENT_URI;
        String[] columns = {CallLog.Calls.CACHED_NAME// 通话记录的联系人
                , CallLog.Calls.NUMBER// 通话记录的电话号码
                , CallLog.Calls.DATE// 通话记录的日期
                , CallLog.Calls.DURATION// 通话时长
                , CallLog.Calls.TYPE};// 通话类型}
        Cursor cursor = context.getContentResolver().query(callUri, // 查询通话记录的URI
                columns
                , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        );
        Map<String, ContactBean> maps = new HashMap<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));  //姓名
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));  //号码
            long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)); //获取通话日期
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateLong));
            String time = new SimpleDateFormat("HH:mm").format(new Date(dateLong));
            int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));//获取通话时长，值为多少秒
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)); //获取通话类型：1.呼入2.呼出3.未接
            String dayCurrent = new SimpleDateFormat("dd").format(new Date(dateLong));
            String dayRecord = new SimpleDateFormat("dd").format(new Date(dateLong));
            if (TextUtils.isEmpty(number)) {
                continue;
            }
            String realNum = number.replaceAll("\\+86", "")
                    .replaceAll("\\+44", "")
                    .replaceAll(" ", "");
            if ("未知".equals(getRegex(number)) && !realNum.startsWith("1")) {
                continue;
            }
            if (realNum.length() != 11) {
                continue;
            }
            ContactBean bean = new ContactBean();
            bean.name = TextUtils.isEmpty(name) ? "通话:" + date : name;
            bean.phone = number;
            maps.put(number, bean);
        }
        List<ContactBean> temp = new ArrayList<>();
        for (String s : maps.keySet()) {
            ContactBean bean = maps.get(s);
            temp.add(bean);
        }
        return temp;
    }

    /*zune: 子线程中执行，并且已申请了权限**/
    private List<ContactBean> getSmsLog(Context context) {
        Uri SMS_INBOX = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");
        if (null == cur) {
            Log.i("ooc", "************cur == null");
        }
        Map<String, ContactBean> maps = new HashMap<String, ContactBean>();
        while (cur.moveToNext()) {
            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));//短信内容
            //至此就获得了短信的相关的内容, 以下是把短信加入map中，构建listview,非必要。
            if (TextUtils.isEmpty(number)) {
                continue;
            }
            String realNum = number.replaceAll("\\+86", "")
                    .replaceAll("\\+44", "")
                    .replaceAll(" ", "");
            if ("未知".equals(getRegex(number)) && !realNum.startsWith("1")) {
                continue;
            }
            if (realNum.length() != 11) {
                continue;
            }
            ContactBean bean = new ContactBean();
            bean.phone = number;
            bean.name = TextUtils.isEmpty(name) ? "未知" : name;
            maps.put(number, bean);
        }
        List<ContactBean> temp = new ArrayList<>();
        for (String s : maps.keySet()) {
            ContactBean bean = maps.get(s);
            temp.add(bean);
        }
        return temp;
    }

    public void deleteContacts(Context context, List<ContactBean> list) {
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            for (ContactBean model : list) {
                if (model.name.equals(name)) {
                    model.contactId = contactId;
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (int j = 0; j < 100; j++) {
            List<ContactBean> temp = list.subList(j, j * ops.size() / 100);
            for (int i = 0; i < temp.size(); i++) {
                ContactBean model = list.get(i);
                ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, model.contactId))
                        .withYieldAllowed(true)
                        .build());
            }
            try {
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Log.i("zune", "删除成功: " + ops.size());
            } catch (OperationApplicationException e) {
                e.printStackTrace();
                Log.i("zune", "OperationApplicationException = " + e);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.i("zune", "RemoteException = " + e);
            }
        }
    }

    public List<String> getGeo(Context context, String countryCode, List<String> phoneNumber) {
        List<String> strings = new ArrayList<>();
        PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();
        long time = System.currentTimeMillis();
        for (String number : phoneNumber) {
            String[] split = number.split(" : ");
            if (split.length != 2) {
                strings.add("");
                continue;
            }
            Phonenumber.PhoneNumber pn = new Phonenumber.PhoneNumber();
            String[] s = split[1].split(" ");
            long phone = 0;
            try {
                phone = Long.parseLong(s[s.length - 1]);
                pn.setCountryCode(Integer.parseInt(s[0].replaceAll("\\+", "")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            pn.setNationalNumber(phone);
            /*long phone = 0;
            try {
                phone = Long.parseLong(s.replaceAll("\\+86", "")
                        .replaceAll("\\+44", "")
                        .replaceAll(" ", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (s.startsWith("+") && !s.startsWith("+86")) {
                String[] splitCode = s.split(" ");
                if (splitCode.length > 1) {
                    pn.setCountryCode(Integer.parseInt(s.substring(1, splitCode.length + 1)));
                } else {
                    pn.setCountryCode(Integer.parseInt(s.substring(1, 3)));
                }
            } else {
                pn.setCountryCode(ccode);
            }
            pn.setNationalNumber(phone);
            String description = geocoder.getDescriptionForNumber(pn, context.getResources().getConfiguration().locale);
            strings.add(String.format("%s : +%s %s  %s%s", split[0], pn.getCountryCode(), s.startsWith("0") ? "0" + phone : phone, TextUtils.isEmpty(description) ? "未知" : description, getRegex(String.valueOf(phone))));*/
            String description = geocoder.getDescriptionForNumber(pn, context.getResources().getConfiguration().locale);
            strings.add(String.format("%s  %s%s", number, TextUtils.isEmpty(description) ? "未知" : description, getRegex(String.valueOf(phone))));
        }
        Log.i("zune", "获取归属地共耗时 ：" + (System.currentTimeMillis() - time));
        return strings;
    }

    public static String getRegex(String phoneNumber) {
        Regex regex = new Regex(isChinaMobile);
        if (regex.matches(phoneNumber)) {
            return "移动";
        }
        regex = new Regex(isChinaUnion);
        if (regex.matches(phoneNumber)) {
            return "联通";
        }
        regex = new Regex(isChinaTelcom);
        if (regex.matches(phoneNumber)) {
            return "电信";
        }
        return "未知";
    }
}
