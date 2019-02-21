package cn.llwy.com.mydemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Newactivity extends Activity {
    private TextView tv_date;
    private String last_date;
    private EditText et_content;
    private ImageView btn_share;
    private Button set_bg;
    private ImageView btn_ok;
    private ImageView btn_cancel;
    private ImageView btn_delete;
    private DatabaseHelper DB;
    private SQLiteDatabase dbread;
    public static int ENTER_STATE = 0;
    public static String last_content;
    public static String last_time;
    public static int id;
    public static int last_bg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newactivity);

        DB = new DatabaseHelper(this);
        dbread = DB.getReadableDatabase();
        tv_date=(TextView)findViewById(R.id.tv_date);
        tv_date.setText(last_time);

        btn_cancel =findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                finish();
            }
        });

        et_content = (EditText)findViewById(R.id.et_content);
        Bundle myBundle = this.getIntent().getExtras();
        last_content = myBundle.getString("info");
        Log.d("LAST_CONTENT", last_content);
        et_content.setText(last_content);

        // 确认按钮的点击事件

        btn_share=findViewById(R.id.share_button) ;
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = et_content.getText().toString();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,
                        ""+content);
                startActivity(intent);
            }
        });
        btn_ok = findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String content = et_content.getText().toString();
                Log.d("LOG1", content);
                // 获取写日志时间
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
                String dateNum = sdf.format(date);
                String sql;
                String sql_count = "SELECT COUNT(*) FROM note";
                SQLiteStatement statement = dbread.compileStatement(sql_count);
                long count = statement.simpleQueryForLong();
                Log.d("COUNT", count + "");
                Log.d("ENTER_STATE", ENTER_STATE + "");
                if(!content.equals(last_content)){
                    Date date2 = new Date();
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
                    String dateNum2 = sdf2.format(date2);
                    Log.d("执行命令", "执行了该函数");
                    String updatesql = "update note set content='"
                            + content + "' where _id=" + id;
                    String updatesq2 = "update note set date='"
                            + dateNum2 + "' where _id=" + id;

                    dbread.execSQL(updatesql);
                    dbread.execSQL(updatesq2);
                    }
                Intent data = new Intent(Newactivity.this,MainActivity.class);
                startActivityForResult(data, 2);

            }
        });

        btn_delete=findViewById(R.id.delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Newactivity.this);
                builder.setTitle("删除该识别记录");
                builder.setMessage("确认删除吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sql_del = "update note set content='' where _id="
                                + id;
                        dbread.execSQL(sql_del);
                        Intent data = new Intent();
                        setResult(2, data);
                        data.setClass(Newactivity.this,MainActivity.class);
                        startActivityForResult(data, 2);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create();
                builder.show();


            }
        });
    }
}
