package aaron.stock;

import aaron.stock.utils.Pair;
import aaron.stock.utils.UrlParameters;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static aaron.stock.StockConstants.*;

public class StockMain {
    private static final String PUBLISHABLE_TOKEN;
    private static final String SECRET_TOKEN;
    private static final SimpleDateFormat simpleDateFormatter = new SimpleDateFormat();
    private static final long SECONDS_IN_A_DAY = 60 * 60 * 24;
    private static final OkHttpClient urlReader = new OkHttpClient();
    private static final Calendar calendar = Calendar.getInstance();

    static {
        Gson gson = new Gson();
        BufferedReader tokensFile = null;
        try {
            tokensFile = new BufferedReader(new FileReader(PATH_TOKENS));
        } catch (FileNotFoundException e) {
            System.exit(1);
        }
        JsonArray tokens = gson.fromJson(tokensFile, JsonArray.class);
        PUBLISHABLE_TOKEN = tokens.get(0).getAsString();
        SECRET_TOKEN = tokens.get(1).getAsString();
        simpleDateFormatter.applyPattern("yyyyMMdd");
    }


    /*
    5,000,000 messages + 1,000,000/$1
    2 years
    2 years * 365 days/year
    730 days
    730 days * 24 hours/day
    17520 hours
    17520 hours * 60 minutes/hour
    1,051,200 minutes
     */
    public static void main(String[] args) throws IOException, ParseException {
        File dataFolder = new File(DATA_FOLDER, SYMBOL_TO_READ);
        if (!dataFolder.exists()) dataFolder.mkdir();
        System.out.println(PUBLISHABLE_TOKEN);
        System.out.println(SECRET_TOKEN);
        Date currentDayReading = findNextDayToRead(dataFolder);
        Date endDate = simpleDateFormatter.parse(DATE_TO_END_READING);
        while (!endDate.before(currentDayReading)) {
            String currentDayReadingString = simpleDateFormatter.format(currentDayReading);
            try {
                readDay(currentDayReadingString, dataFolder);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int dayOfWeek;
            do {
                currentDayReading = Date.from(currentDayReading.toInstant().plusSeconds(SECONDS_IN_A_DAY));
                calendar.setTime(currentDayReading);
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            } while (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY);
        }
    }

    private static void readDay(String currentDayReadingString, File dataFolder) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        File fileToWrite = new File(dataFolder, currentDayReadingString + ".json");
        if (fileToWrite.exists()) return;// skip this file because we already have it in memory
        int fails = 0;
        // this is only a loop on a fail of a run. if a fail occurs, we try again in a second, up to 100 times. if we fail 100 times, execution stops
        while (true) {
            Request request = new Request.Builder()
                    .url(UrlParameters.buildURL(String.format(URL_INTRA_DAY, SYMBOL_TO_READ), new Pair[]{new Pair<>("token", SECRET_TOKEN)}))
                    .build();
            Response s = urlReader.newCall(request).execute();
            if (s.code() != 200) {
                //noinspection BusyWait
                Thread.sleep(1000);
                if (++fails == 100) {
                    // stop execution of everything
                    System.out.println("fail");
                    System.exit(1);
                }
                continue;
            }
            BufferedReader response = new BufferedReader(new InputStreamReader(s.body().byteStream()));
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileToWrite));
            int charRead;
            while ((charRead = response.read()) != -1) {
                fileWriter.write(charRead);
            }
            response.close();
            fileWriter.close();
            System.out.println("sleep " + Math.max(12 - (System.currentTimeMillis() - start), 3));
            System.out.print(start + ", ");
            System.out.println(System.currentTimeMillis());
            Thread.sleep(Math.max(12 - (System.currentTimeMillis() - start), 3));
            break;
        }
    }

    private static Date findNextDayToRead(File dataFolder) throws ParseException {
        String[] dayReadPaths = dataFolder.list();
        assert dayReadPaths != null;
        Date bigDate = simpleDateFormatter.parse(DATE_TO_START_READING);
        for (String dayReadPath : dayReadPaths) {
            Date date = simpleDateFormatter.parse(dayReadPath.substring(0, dayReadPath.length() - 5));
            if (bigDate.before(date)) {
                bigDate = date;
            }
        }
        Date currentDayReading = Date.from(bigDate.toInstant().plusSeconds(SECONDS_IN_A_DAY));
        calendar.setTime(currentDayReading);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        while (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
            currentDayReading = Date.from(currentDayReading.toInstant().plusSeconds(SECONDS_IN_A_DAY));
            calendar.setTime(currentDayReading);
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        }
        return currentDayReading;
    }
}
