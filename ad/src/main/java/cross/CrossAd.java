package cross;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*




 */

public class CrossAd {
    private Context mContext;

    public static CrossAd init(Context context) {
        return new CrossAd(context);
    }

    public static VNCross getACrossAd(Context context) {
        ArrayList<VNCross> vnCrosses = getCrossAd(context);
        return vnCrosses.get(new Random().nextInt(vnCrosses.size()));
    }

    public static ArrayList<VNCross> getCrossAd(Context context) {


        return new Gson().fromJson(readFromFile(context), new TypeToken<List<VNCross>>() {
        }.getType());
    }

    ArrayList<VNCross> vnCrosses;

    public CrossAd(final Context context) {
        mContext = context;
        vnCrosses = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    String csv = run("https://doan-xeom.herokuapp.com/vncross");
                    Log.e("hehe", csv);
                    CSVParser csvParser = CSVParser.parse(csv, CSVFormat.DEFAULT
                            .withHeader("title_tag", "des_tag", "link_icon_tag", "id_app_android_tag", "link_tracking_tag", "age_tag")
                            .withIgnoreHeaderCase()
                            .withTrim());
                    for (CSVRecord csvRecord : csvParser.getRecords()) {

                        String title_tag = csvRecord.get("title_tag");
                        String des_tag = csvRecord.get("des_tag");
                        String link_icon_tag = csvRecord.get("link_icon_tag");
                        String id_app_android_tag = csvRecord.get("id_app_android_tag");
                        String link_tracking_tag = csvRecord.get("link_tracking_tag");
                        String age_tag = csvRecord.get("age_tag");

                        System.out.println("Record No - " + csvRecord.getRecordNumber());
                        System.out.println("Name : " + title_tag);
                        System.out.println("Email : " + des_tag);
                        System.out.println("Phone : " + link_icon_tag);
                        System.out.println("Country : " + id_app_android_tag);
                        System.out.println("Country : " + link_tracking_tag);
                        System.out.println("Country : " + age_tag);
                        System.out.println("---------------\n\n");
                        if (id_app_android_tag.endsWith(context.getPackageName())) continue;
                        vnCrosses.add(new VNCross(title_tag, des_tag, link_icon_tag, id_app_android_tag, link_tracking_tag, age_tag));
                    }
                    vnCrosses.remove(0);
                    String[] lines = csv.split(System.getProperty("line.separator"));
                    Log.e("hehe", "lines " + lines.length);
                    Log.e("hehe", "lines " + lines[0]);
                    writeToFile(new Gson().toJson(vnCrosses), mContext);

                    Log.e("hehe", "readFromFile " + readFromFile(mContext));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);


            }
        }.execute();

    }

    public class VNCross {
        @Override
        public String toString() {
            return "VNCross{" +
                    "title_tag='" + title_tag + '\'' +
                    ", des_tag='" + des_tag + '\'' +
                    ", link_icon_tag='" + link_icon_tag + '\'' +
                    ", id_app_android_tag='" + id_app_android_tag + '\'' +
                    ", link_tracking_tag='" + link_tracking_tag + '\'' +
                    ", age_tag='" + age_tag + '\'' +
                    '}';
        }

        public String getTitle_tag() {
            return title_tag;
        }

        public void setTitle_tag(String title_tag) {
            this.title_tag = title_tag;
        }

        public String getDes_tag() {
            return des_tag;
        }

        public void setDes_tag(String des_tag) {
            this.des_tag = des_tag;
        }

        public String getLink_icon_tag() {
            return link_icon_tag;
        }

        public void setLink_icon_tag(String link_icon_tag) {
            this.link_icon_tag = link_icon_tag;
        }

        public String getId_app_android_tag() {
            return id_app_android_tag;
        }

        public void setId_app_android_tag(String id_app_android_tag) {
            this.id_app_android_tag = id_app_android_tag;
        }

        public String getLink_tracking_tag() {
            return link_tracking_tag;
        }

        public void setLink_tracking_tag(String link_tracking_tag) {
            this.link_tracking_tag = link_tracking_tag;
        }

        public String getAge_tag() {
            return age_tag;
        }

        public void setAge_tag(String age_tag) {
            this.age_tag = age_tag;
        }

        public VNCross(String title_tag, String des_tag, String link_icon_tag, String id_app_android_tag, String link_tracking_tag, String age_tag) {

            this.title_tag = title_tag;
            this.des_tag = des_tag;
            this.link_icon_tag = link_icon_tag;
            this.id_app_android_tag = id_app_android_tag;
            this.link_tracking_tag = link_tracking_tag;
            this.age_tag = age_tag;
        }

        String title_tag;
        String des_tag;
        String link_icon_tag;
        String id_app_android_tag;
        String link_tracking_tag;
        String age_tag;
    }

    private static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    OkHttpClient client = new OkHttpClient();

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
