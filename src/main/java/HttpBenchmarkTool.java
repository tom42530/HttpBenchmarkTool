import org.apache.commons.cli.*;

import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class HttpBenchmarkTool {


    public static int mNbRequest;
    public static int mConcurrentRequest;

    public static String mUrl;

    public static String[] mHeaders;

    public static boolean mIsRetry;

    public static boolean mIsUnsecure;


    public static void main(String[] args) throws InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        parseArgs(args);

        Benchmark lTest = new Benchmark(mUrl, mNbRequest, mConcurrentRequest, mHeaders, mIsRetry, mIsUnsecure);
        lTest.start();

    }


    private static void parseArgs(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("r").argName("nb request").desc("nb request").hasArgs().longOpt("request").build());
        options.addOption(Option.builder("h").desc("print help").longOpt("help").build());
        options.addOption(Option.builder("c").argName("nb max concurrent request").desc("set max concurent request").hasArgs().longOpt("concurrent").build());
        options.addOption(Option.builder("H").argName("headers").desc("headers list delimited by ;").hasArgs().longOpt("headers").build());
        options.addOption(Option.builder("u").desc("disable ssl verification").longOpt("unsecure").build());
        options.addOption(Option.builder("R").desc("retry on 429 (wait retry after delay").longOpt("retry").build());


        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(HttpBenchmarkTool.class.getSimpleName() + " [OPTION] <url>", options);
                System.exit(0);
            }
            if (line.getArgList().isEmpty()) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(HttpBenchmarkTool.class.getSimpleName() + " [OPTION] <url>", options);
                System.exit(1);
            }

            mNbRequest = Integer.parseInt(line.getOptionValue("r", "10"));
            mConcurrentRequest = Integer.parseInt(line.getOptionValue("c", "1"));
            mHeaders = line.getOptionValues("H");
            mUrl = line.getArgList().get(0);
            mIsRetry = line.hasOption("R");
            mIsUnsecure = line.hasOption("u");


        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.exit(1);
        }

    }


}
