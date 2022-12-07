import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Benchmark {
    private final String mUrl;
    private final int mNbRequest;
    private final int mMaxConcurrentRequest;
    private final String[] mHeaders;

    private final boolean mIsRetry;

    private final boolean mIsUnsecure;


    HttpClient mHttpClient;//.newHttpClient().;

    HttpRequest mRequest;


    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };


    private HttpClient buildHttpClient(boolean aIsUnsecure) throws NoSuchAlgorithmException, KeyManagementException {
        if (aIsUnsecure) {
            final Properties props = System.getProperties();
            props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());


            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return HttpClient.newBuilder().sslContext(sslContext).build();

        } else {
            return HttpClient.newHttpClient();
        }
    }


    public Benchmark(String aUrl, int aNbRequest, int aMaxConcurrentRequest, String[] aHeaders, boolean aIsRetry, boolean aIsUnsecure) throws NoSuchAlgorithmException, KeyManagementException {

        this.mUrl = aUrl;
        this.mNbRequest = aNbRequest;
        this.mMaxConcurrentRequest = aMaxConcurrentRequest;
        this.mHeaders = aHeaders;
        this.mIsRetry = aIsRetry;
        this.mIsUnsecure = aIsUnsecure;


        mHttpClient = buildHttpClient(mIsUnsecure);


        HttpRequest.Builder lBuilder = HttpRequest.newBuilder()
                .uri(URI.create(mUrl));

        if (mHeaders != null) {
            Arrays.stream(mHeaders).forEach(new Consumer<>() {
                @Override
                public void accept(String o) {
                    lBuilder.setHeader(o.split(":")[0], o.split(":")[1]);
                }
            });
        }
        mRequest = lBuilder.build();
        //   .setHeader("X-Player-Mac-Address","12345678")
    }


    public void start() throws InterruptedException {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(mMaxConcurrentRequest);
        for (int i = 0; i < mNbRequest; i++) {
            executor.execute(new HttpRunnable(i));
        }
        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.MINUTES);
    }


    private class HttpRunnable implements Runnable {

        private final int mId;

        public HttpRunnable(int aId) {
            mId = aId;
        }

        @Override
        public void run() {
            try {
                RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
                long start = bean.getUptime();

                HttpResponse<String> lResponse = executeRequest();//mHttpClient.send(mRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("#" + mId + " code : " + lResponse.statusCode() + " duration : " + (bean.getUptime() - start) + " start " + start);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        private HttpResponse<String> executeRequest() throws IOException, InterruptedException {
            HttpResponse<String> lResponse = mHttpClient.send(mRequest, HttpResponse.BodyHandlers.ofString());

            if (mIsRetry && lResponse.statusCode() == 429) {
                if (lResponse.headers().firstValue("Retry-After").isPresent())
                    Thread.sleep(Integer.parseInt(lResponse.headers().firstValue("Retry-After").get()) * 1000L);
                else
                    Thread.sleep(120L * 1000L);
                lResponse = executeRequest();
            }
            return lResponse;
        }

    }
}
