package aaron.stock;

public class StockConstants {
    public final static String URL_BASE = "https://sandbox.iexapis.com/stable";
    public final static String URL_INTRA_DAY = URL_BASE + "/stock/%s/intraday-prices"; // requires a symbol when String.formatting
    public final static String SYMBOL_TO_READ = "CCC"; // "BTCUSD
    public final static String DATA_FOLDER = "data";
    public static final String PATH_TOKENS = "tokens.json";
    public static final String DATE_TO_START_READING = "20180701";
    public static final String DATE_TO_END_READING = "20200701";
}
