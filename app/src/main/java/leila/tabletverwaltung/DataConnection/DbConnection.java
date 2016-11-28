package leila.tabletverwaltung.DataConnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import leila.tabletverwaltung.SettingsActivity;


/**
 * Created by a.moszczynski on 15.11.2016.
 */
public class DbConnection {
    private Connection mConnection;
    private ResultSet mResult;
    private Statement mStatement;

    private static DbConnection mInstance;

    private static String mUrl;
    private static String mUserName;
    private static String mPassWord;

    private DbConnection(String url, String userName, String passWord){
        mUrl = "jdbc:mysql://"+url+"/tabletverwaltung";
        mUserName = userName;
        mPassWord = passWord;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try{
            DriverManager.setLoginTimeout(5);
            mConnection = (Connection) DriverManager.getConnection
                    (mUrl,mUserName,mPassWord);
        }catch (SQLException e){
            Log.e("SQL",e.getMessage());
            Log.i("DRIVERMANAGER", "here");
        }
    }

    public static DbConnection CreateInstance(String url, String userName, String passWord){
        if(mInstance == null)
            mInstance = new DbConnection(url,userName,passWord);
        return mInstance;
    }


    public static DbConnection CreateInstance(Context context){
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        if(mInstance == null)
            mInstance = new DbConnection(sp.getString(SettingsActivity.SP_URL, null), sp.getString(SettingsActivity.SP_BENUTZER, null), sp.getString(SettingsActivity.SP_PASSWORT, null));
        return mInstance;
    }


    public static DbConnection GetInstance(){
        if(mInstance == null)
            mInstance = new DbConnection(mUrl, mUserName,mPassWord);
        return  mInstance;
    }

    public ResultSet Select(String query){
        try{
            mStatement = (Statement) mConnection.createStatement();
            mResult = mStatement.executeQuery(query);
        }catch (SQLException e){
            Log(e);
        }
        return  mResult;
    }

    public void Update(String sql){
        try {
            ExecuteStatement(sql);
        }
        catch (SQLException exception){
            Log(exception);
        }
    }

    public void Insert(String sql){
        try {
            ExecuteStatement(sql);
        }
        catch (SQLException exception){
            Log(exception);
        }

    }

    private boolean ExecuteStatement (String sql) throws SQLException {
        mStatement = (Statement)mConnection.createStatement();
        return mStatement.execute(sql);
    }


    public void close(){
        try {
            if(this.mConnection != null){
                if(mResult != null) this.mResult.close();
                if(mStatement != null) this.mStatement.close();
                if(mConnection != null) this.mConnection.close();
                this.mInstance = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void Log(Exception e){
        Log.e("SQLException", e.getMessage());
    }

    public Connection getmConnection() {
        return mConnection;
    }
}









