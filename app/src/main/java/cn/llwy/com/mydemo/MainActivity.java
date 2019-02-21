package cn.llwy.com.mydemo;

        import android.app.Activity;
        import android.app.AlertDialog;
        import android.content.ContentResolver;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteStatement;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.net.Uri;
        import android.os.Environment;
        import android.provider.MediaStore;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.TextUtils;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.SearchView;
        import android.widget.SimpleAdapter;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.baidu.ocr.sdk.OCR;
        import com.baidu.ocr.sdk.OnResultListener;
        import com.baidu.ocr.sdk.exception.OCRError;
        import com.baidu.ocr.sdk.model.AccessToken;
        import com.baidu.ocr.sdk.model.GeneralBasicParams;
        import com.baidu.ocr.sdk.model.GeneralParams;
        import com.baidu.ocr.sdk.model.GeneralResult;
        import com.baidu.ocr.sdk.model.Word;
        import com.baidu.ocr.sdk.model.WordSimple;

        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private Button btn;
    private ImageView im2;
    private ImageView im3;
    private Uri uri;
    //private TextView txt;
    private Uri imageUri;
    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;
    public static final int SELECT_PHOTO=3;
    public static final int CROP_PHOTO_1 = 4;
    Bitmap bitmap;
    private boolean hasGotToken = false;
    private  Button btn_new;
    private Context mContext;
    private ListView listview;
    private SimpleAdapter simp_adapter;
    private List<Map<String, Object>> dataList;
    private TextView tv_content;
    private DatabaseHelper DB;
    private SQLiteDatabase dbread;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mSearchView = (SearchView)findViewById(R.id.search_view);
        mSearchView.setIconifiedByDefault(true);
        //mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchView.setIconified(false);
            }
        });


        tv_content=(TextView)findViewById(R.id.tv_content);
        listview=(ListView)findViewById(R.id.list_view);
        dataList=new ArrayList<Map<String, Object>>();
        mContext=this;
        DB = new DatabaseHelper(this);
        dbread = DB.getReadableDatabase();
        // 清空数据库中表的内容
        //dbread.execSQL("delete from note");
        RefreshNotesList();

        listview.setTextFilterEnabled(true);
        listview.setOnItemClickListener(this);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s)){
                    search_action(s);
                }
                else{
                    RefreshNotesList();
                }
                return true;
            }
        });
        im2=findViewById(R.id.im2);
        im3=findViewById(R.id.im3);
        im2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File outputImage = new File(Environment.getExternalStorageDirectory(),
                        "tempImage" + ".jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });
        im3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                File outputImage = new File(Environment.getExternalStorageDirectory(),
                        "outputImage" + ".jpg");
                imageUri = Uri.fromFile(outputImage);
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent1 = new Intent(Intent.ACTION_PICK,null);
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent1.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent1,SELECT_PHOTO);//打开相册
            }
        });
        initAccessTokenWithAkSk();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, CROP_PHOTO); // 启动裁剪程序
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver()
                                .openInputStream(imageUri));
                        Currency();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    String text = imageUri.toString();
                    intent.setDataAndType(data.getData(), "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent,CROP_PHOTO);
                }
                break;
            case CROP_PHOTO_1:
                if(resultCode == RESULT_OK){
                    File outputImage = new File(Environment.getExternalStorageDirectory(),
                            "tempImage" + ".jpg");
                    try {
                        if (outputImage.exists()) {
                            outputImage.delete();
                        }
                        outputImage.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    uri = Uri.fromFile(outputImage);
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver()
                                .openInputStream(uri));
                        Currency();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void initAccessTokenWithAkSk() {
        OCR.getInstance(MainActivity.this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
                                                                       @Override
                                                                       public void onResult(AccessToken result) {
                                                                           String token = result.getAccessToken();
                                                                           Log.e("TGA","token:"+token);
                                                                           hasGotToken = true;
                                                                       }

                                                                       @Override
                                                                       public void onError(OCRError error) {
                                                                           Log.e("TGA","AK，SK方式获取token失败");
//                error.printStackTrace();
//                Toast.makeText(MainActivity.this,"AK，SK方式获取token失败",Toast.LENGTH_LONG).show();
                                                                       }
                                                                   }, getApplicationContext(), "CcyW4En7kmM8XwUQxv3qOmAO",
                "Kc5E2nBx09BGGCnXUpQZBuGxYhtHmaQa");
    }


    /**
     * uri 转相对路径
     * @param uri
     * @return
     */
    public static String getRealFilePath(final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    public void Currency(){
        final StringBuffer sb=new StringBuffer();
        // 通用文字识别参数设置
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        String str=getRealFilePath(this,imageUri);
        Log.e("TGA",str+"------str-------------");
        param.setImageFile(new File(getRealFilePath(this,imageUri)));
// 调用通用文字识别服务
        OCR.getInstance(this).recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                // 调用成功，返回GeneralResult对象
                for (WordSimple wordSimple : result.getWordList()) {
                    // wordSimple不包含位置信息
                    sb.append(wordSimple.getWords());
                    sb.append("\n");
                }
                String content = sb.toString();
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
                String dateNum = sdf.format(date);
                String sql;
                String sql_count = "SELECT COUNT(*) FROM note";
                SQLiteStatement statement = dbread.compileStatement(sql_count);
                long count = statement.simpleQueryForLong();
                if (!content.equals("")) {
                    sql = "insert into " + DatabaseHelper.TABLE_NAME_NOTES
                            + " values(" + count + "," + "'" + content
                            + "'" + "," + "'" + dateNum +
                            "')";
                    Log.d("LOG", sql);
                    dbread.execSQL(sql);
                }
                RefreshNotesList();

            }
            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放内存资源
        OCR.getInstance(this).release();
    }

    public void RefreshNotesList(){
        int size = dataList.size();
        if (size > 0) {
            dataList.removeAll(dataList);
            simp_adapter.notifyDataSetChanged();
            listview.setAdapter(simp_adapter);
        }
        simp_adapter = new SimpleAdapter(this, getData(), R.layout.item,
                new String[] { "tv_content", "tv_date" }, new int[] {
                R.id.tv_content, R.id.tv_date});
        listview.setAdapter(simp_adapter);
    }//,"bg" ,R.id.image

    private List<Map<String, Object>> getData() {

        Cursor cursor = dbread.query("note", null, "content!=\"\"", null, null,
                null, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("content"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tv_content", name);
            map.put("tv_date", date);
            dataList.add(map);
        }
        cursor.close();
        return dataList;
    }

    public void search_action(String s){
        int size = dataList.size();
        if (size > 0) {
            dataList.removeAll(dataList);
            simp_adapter.notifyDataSetChanged();
            listview.setAdapter(simp_adapter);
        }
        simp_adapter = new SimpleAdapter(this, getData2(s), R.layout.item,
                new String[] { "tv_content", "tv_date" }, new int[] {
                R.id.tv_content, R.id.tv_date});
        listview.setAdapter(simp_adapter);
    }

    private List<Map<String, Object>> getData2(String s) {

        Cursor cursor = dbread.query("note", null, "content!=\"\"", null, null,
                null, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("content"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            Map<String, Object> map = new HashMap<String, Object>();
            if(name.contains(s)){
                map.put("tv_content", name);
                map.put("tv_date", date);
                dataList.add(map);
            }
        }
        cursor.close();
        return dataList;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String content = listview.getItemAtPosition(position) + "";
        String content1 = content.substring(content.indexOf("=") + 1,
                content.indexOf(","));
        Log.d("CONTENT", content1);
        Cursor c = dbread.query("note", null,
                "content=" + "'" + content1 + "'", null, null, null, null);
        while (c.moveToNext()) {
            String No = c.getString(c.getColumnIndex("_id"));
            String time = c.getString(c.getColumnIndex("date"));
            Newactivity.last_time=time;
            Log.d("TEXT", No);
            Intent myIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("info", content1);
            Newactivity.id = Integer.parseInt(No);
            myIntent.putExtras(bundle);
            myIntent.setClass(MainActivity.this, Newactivity.class);
            startActivityForResult(myIntent, 1);
        }
    }

}
