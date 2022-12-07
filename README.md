<h1>HttpBenchmarkTool</h1>


java tool used for benchmarking http server

Can be used for sending several requests with some ajustable settings.

<h2>Usage:</h2>

<pre><code>
 HttpBenchmarkTool [OPTION] <url>
 -c,--concurrent < nb max concurrent request >   set max concurent request
 -h,--help                                     print help
 -H,--headers <headers>                        headers list delimited by ;
 -r,--request < nb request >                     nb request
 -R,--retry                                    retry on 429 (wait retry after delay)
 -u,--unsecure                                 disable ssl verification
 </code></pre>

<h2>example:</h2>

<pre><code>
 java -jar HttpBenchmarkTool.jar -c10 -r10 -H"User-Agent:HttpBenchmarkTool" "other_custom_header:custom_header_value" https://foo.com/test
 </code></pre>


<h2>For building : </h2>

 <pre><code>
 gradlew jar
 </code></pre>