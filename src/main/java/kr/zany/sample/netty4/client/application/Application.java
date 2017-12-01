package kr.zany.sample.netty4.client.application;

import kr.zany.sample.netty4.client.client.ClientRunner;
import kr.zany.sample.netty4.client.common.data.*;
import kr.zany.sample.netty4.client.common.listener.ShutdownHook;
import kr.zany.sample.netty4.client.common.util.Functions;
import kr.zany.sample.netty4.client.common.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;

/**
 * <p><b>Class Description</b></p>
 * <p>Copyright ⓒ 2016 kt corp. All rights reserved.</p>
 *
 * @author Lee Sang-Hyun (zanylove@gmail.com)
 * @since 2016-01-04 17:42
 */
@Slf4j
@EnableAutoConfiguration
@EnableConfigurationProperties
@ComponentScan(basePackages = "${app.base-package}")
public class Application implements CommandLineRunner {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Inject Beans
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final Options options;

    private final ArgumentVo argumentVo;

    private final CommandLineParser commandLineParser;

    private final AppBaseSettings appBaseSettings;

    private final ClientRunner clientRunner;

    private final ThreadSettings threadSettings;

    private final ThreadPoolTaskExecutor asyncExecutor;


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Member Variables
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructor
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Inject
    public Application(
            Options options,
            ArgumentVo argumentVo,
            CommandLineParser commandLineParser,
            AppBaseSettings appBaseSettings,
            ClientRunner clientRunner,
            ThreadSettings threadSettings,
            ThreadPoolTaskExecutor asyncExecutor
    ) {
        this.options = options;
        this.argumentVo = argumentVo;
        this.commandLineParser = commandLineParser;
        this.appBaseSettings = appBaseSettings;
        this.clientRunner = clientRunner;
        this.threadSettings = threadSettings;
        this.asyncExecutor = asyncExecutor;
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Public Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        new SpringApplicationBuilder(Application.class).run(args);
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Protected, Private Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void showHelp() {
        showHelp(null, true);
    }

    private void showHelp(String message) {
        showHelp(message, true);
    }

    private void showHelp(String message, boolean afterTerminate) {

        if (!StringUtils.isBlank(message)) {
            System.out.println(message);
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(appBaseSettings.getFullName(), options);
        System.out.println("");

        if (afterTerminate) {
            System.exit(0);
        }
    }

    private boolean hasOptions(CommandLine cmd, String[] options) {
        for (String opt : options) {
            if (cmd.hasOption(opt)) {
                return true;
            }
        }
        return false;
    }

    private void setMinMax(MinMaxAvg minMaxAvg, long compareValue) {

        if (compareValue > 0) {

            minMaxAvg.setSum(minMaxAvg.getSum() + compareValue);

            if (compareValue < minMaxAvg.getMin()) {
                minMaxAvg.setMin(compareValue);
            }

            if (compareValue > minMaxAvg.getMax()) {
                minMaxAvg.setMax(compareValue);
            }
        }
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Override Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void run(String... args) throws Exception {

        /** parse arguments */

        CommandLine cmd = null;

        try {
            cmd = commandLineParser.parse(options, args);
        } catch (UnrecognizedOptionException e) {
            log.info(String.format("UnrecognizedOptionException '%s', do nothing.", e.getMessage()));
        } catch (MissingArgumentException e) {
            System.out.println(e.getMessage());
            showHelp();
        }

        assert cmd != null;

        /** check mandatory arguments and processing. */

        if (hasOptions(cmd, CliStatics.OPT_SHOW_HELP)) {
            showHelp();
        }


        /** ---------------------------------------------------------------- */
        /** argument processing. */
        /** ---------------------------------------------------------------- */

        String shortOptArg, longOptArg, currArg;

        if (hasOptions(cmd, CliStatics.OPT_HOST)) {
            shortOptArg = cmd.getOptionValue(CliStatics.OPT_HOST[0]);
            longOptArg  = cmd.getOptionValue(CliStatics.OPT_HOST[1]);
            currArg = StringUtils.isBlank(shortOptArg) ? longOptArg : shortOptArg;

            if (StringUtils.isBlank(currArg)) {
                showHelp(String.format("Missing argument for option '%s'", CliStatics.optionsAsString(CliStatics.OPT_HOST)));
            } else {
                argumentVo.setHost(currArg);
            }
        }

        if (hasOptions(cmd, CliStatics.OPT_PORT)) {
            shortOptArg = cmd.getOptionValue(CliStatics.OPT_PORT[0]);
            longOptArg  = cmd.getOptionValue(CliStatics.OPT_PORT[1]);
            currArg = StringUtils.isBlank(shortOptArg) ? longOptArg : shortOptArg;

            if (StringUtils.isBlank(currArg)) {
                showHelp(String.format("Missing argument for option '%s'", CliStatics.optionsAsString(CliStatics.OPT_PORT)));
            } else if (!StringUtils.isNumeric(currArg)) {
                showHelp(String.format("Only numeric value allowed for option '%s'", CliStatics.optionsAsString(CliStatics.OPT_PORT)));
            } else {
                argumentVo.setPort(Integer.parseInt(currArg));
            }
        }

        if (hasOptions(cmd, CliStatics.OPT_THREAD_CNT)) {
            shortOptArg = cmd.getOptionValue(CliStatics.OPT_THREAD_CNT[0]);
            longOptArg  = cmd.getOptionValue(CliStatics.OPT_THREAD_CNT[1]);
            currArg = StringUtils.isBlank(shortOptArg) ? longOptArg : shortOptArg;

            if (StringUtils.isBlank(currArg)) {
                showHelp(String.format("Missing argument for option '%s'", CliStatics.optionsAsString(CliStatics.OPT_THREAD_CNT)));
            } else if (!StringUtils.isNumeric(currArg)) {
                showHelp(String.format("Only numeric value allowed for option '%s'", CliStatics.optionsAsString(CliStatics.OPT_THREAD_CNT)));
            } else {
                argumentVo.setThreadCount(Integer.parseInt(currArg));
            }
        }

        if (hasOptions(cmd, CliStatics.OPT_RAMP_SEC)) {
            shortOptArg = cmd.getOptionValue(CliStatics.OPT_RAMP_SEC[0]);
            longOptArg  = cmd.getOptionValue(CliStatics.OPT_RAMP_SEC[1]);
            currArg = StringUtils.isBlank(shortOptArg) ? longOptArg : shortOptArg;

            if (StringUtils.isBlank(currArg)) {
                showHelp(String.format("Missing argument for option '%s'", CliStatics.optionsAsString(CliStatics.OPT_RAMP_SEC)));
            } else if (!StringUtils.isNumeric(currArg)) {
                showHelp(String.format("Only numeric value allowed for option '%s'", CliStatics.optionsAsString(CliStatics.OPT_RAMP_SEC)));
            } else {
                argumentVo.setRampUpSeconds(Integer.parseInt(currArg));
            }
        }

        if (hasOptions(cmd, CliStatics.OPT_LOOP_CNT)) {
            shortOptArg = cmd.getOptionValue(CliStatics.OPT_LOOP_CNT[0]);
            longOptArg  = cmd.getOptionValue(CliStatics.OPT_LOOP_CNT[1]);
            currArg = StringUtils.isBlank(shortOptArg) ? longOptArg : shortOptArg;

            if (StringUtils.isBlank(currArg)) {
                showHelp(String.format("Missing argument for option '%s'", CliStatics.optionsAsString(CliStatics.OPT_LOOP_CNT)));
            } else if (!StringUtils.isNumeric(currArg)) {
                showHelp(String.format("Only numeric value allowed for option '%s'", CliStatics.optionsAsString(CliStatics.OPT_LOOP_CNT)));
            } else {
                argumentVo.setLoopCount(Integer.parseInt(currArg));
            }
        }

        /** redefine, core pool size (active thread) */

        //        int threadCount = argumentVo.getThreadCount();
        //
        //        if (threadCount > 0) {
        //
        //            log.info("---------------------------------------------------");
        //            log.info(String.format("Redefine core pool size : %d", threadCount));
        //            log.info("---------------------------------------------------");
        //
        //            asyncExecutor.setCorePoolSize(threadCount);
        //        }


        /** ---------------------------------------------------------------- */
        /** run thread(s)... */
        /** ---------------------------------------------------------------- */

        long startTime = System.currentTimeMillis();

        System.out.println("--------------------------------------------------------");
        System.out.println(String.format(">>> TCP Load Tester started at %s", formatter.print(startTime)));
        System.out.println(String.format("    - Send to       - %s:%d", argumentVo.getHost(), argumentVo.getPort()));
        System.out.println("--------------------------------------------------------");
        System.out.println(String.format("    - Thread        - %d", argumentVo.getThreadCount()));
        System.out.println(String.format("    - Loop          - %d", argumentVo.getLoopCount()));
        System.out.println(String.format("    - Ramp Up (sec) - %d", argumentVo.getRampUpSeconds()));
        System.out.println(String.format("    (total %d requests in %d seconds)", argumentVo.getThreadCount() * argumentVo.getLoopCount(), argumentVo.getRampUpSeconds()));
        System.out.println("--------------------------------------------------------");
        System.out.println(String.format("    - Connection Timeout millis - %s", argumentVo.getConnectionTimeoutMillis()));
        System.out.println(String.format("    - Send delay millis         - %s", argumentVo.getSendDelayMillis()));
        System.out.println("--------------------------------------------------------");
        System.out.println(String.format("    - Disconnect after complete - %s", argumentVo.isDisconnectAfterComplete()));
        System.out.println(String.format("    - Disconnect delay millis   - %d", argumentVo.getDisconnectDelayMillis()));
        System.out.println("--------------------------------------------------------");

        Collection<Future<SocketThreadInfoVo>> allThreads = new ArrayList<>();

        if (!appBaseSettings.isTestMode()) {

            long sleepMillis = 0L;
            if (argumentVo.getRampUpSeconds() > 0) {
                sleepMillis = (argumentVo.getRampUpSeconds() * 1000L) / (long)argumentVo.getThreadCount();
            }

            for (int threadNo = 0; threadNo < argumentVo.getThreadCount(); threadNo++) {

                Collection<Future<SocketThreadInfoVo>> chunkThreads = new ArrayList<>();

                for (int loopNo = 0; loopNo < argumentVo.getLoopCount(); loopNo++) {
                    Future<SocketThreadInfoVo> thread = clientRunner.createThreads(threadNo, loopNo);
                    chunkThreads.add(thread);
                }

                if (chunkThreads.size() > 0) {
                    allThreads.addAll(chunkThreads);
                }

                if (argumentVo.getLoopCount() > 1) {
                    System.out.println(String.format("THREAD GROUP CREATED [%04d] [%04d/%04d] (%s)",
                            chunkThreads.size(),
                            allThreads.size(),
                            argumentVo.getLoopCount() * argumentVo.getThreadCount(),
                            Functions.formatElapsedTime(System.currentTimeMillis() - startTime)
                    ));
                }

                if (sleepMillis > 0) {
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
        }

        System.out.println(String.format("ALL THREADS ARE CREATED [%d] (%s)",
                allThreads.size(),
                Functions.formatElapsedTime(System.currentTimeMillis()-startTime)
        ));

        /** awaiting threads... */

        if (threadSettings.isBaseSyncThread()) {

            SummaryVo summaryVo = SummaryVo.builder().build();

            MinMaxAvg threadPoolActive = MinMaxAvg.builder().min(Long.MAX_VALUE).max(Long.MIN_VALUE).build();

            MinMaxAvg connectedTime    = MinMaxAvg.builder().min(Long.MAX_VALUE).max(Long.MIN_VALUE).build();
            MinMaxAvg sentTime         = MinMaxAvg.builder().min(Long.MAX_VALUE).max(Long.MIN_VALUE).build();
            MinMaxAvg receivedTime     = MinMaxAvg.builder().min(Long.MAX_VALUE).max(Long.MIN_VALUE).build();
            MinMaxAvg disconnectedTime = MinMaxAvg.builder().min(Long.MAX_VALUE).max(Long.MIN_VALUE).build();

            Collection<CommonResultVo> results = ThreadUtils.awaitAsyncTasks(allThreads);

            for (CommonResultVo result : results) {

                summaryVo.setTotalRequest(summaryVo.getTotalRequest()+1);

                if ((result.getResultObject() != null) && (result.getResultObject() instanceof SocketThreadInfoVo)) {

                    SocketThreadInfoVo socketThreadInfoVo = (SocketThreadInfoVo) result.getResultObject();

                    // 접속, 전송, 응답, 끊김 시간이 없으면 에러로 간주한다.

                    if ((socketThreadInfoVo.getConnectedTime() <= 0) ||
                            (socketThreadInfoVo.getSentTime() <= 0) ||
                            (socketThreadInfoVo.getReceivedTime() <= 0) ||
                            (socketThreadInfoVo.getDisconnectedTime() <= 0)) {
                        summaryVo.setErrorCount(summaryVo.getErrorCount()+1);
                    }

                    // min, max, sum, average

                    setMinMax(threadPoolActive, socketThreadInfoVo.getThreadPoolActive());
                    setMinMax(connectedTime,    socketThreadInfoVo.getConnectedTime());
                    setMinMax(sentTime,         socketThreadInfoVo.getSentTime());
                    setMinMax(receivedTime,     socketThreadInfoVo.getReceivedTime());
                    setMinMax(disconnectedTime, socketThreadInfoVo.getDisconnectedTime());

                } else {
                    summaryVo.setErrorCount(summaryVo.getErrorCount()+1);
                }

                log.info(ToStringBuilder.reflectionToString(result));
            }

            long spentTime = System.currentTimeMillis() - startTime;
            float spentSec = spentTime / 1000.0f;

            System.out.println("--------------------------------------------------------");
            System.out.println(String.format("- TOTAL REQ    : %d", summaryVo.getTotalRequest()));
            System.out.println(String.format("- ERR COUNT    : %d", summaryVo.getErrorCount()));
            System.out.println(String.format("- TPS          : %-8.3f", (float)summaryVo.getTotalRequest() / spentSec));
            System.out.println("--------------------------------------------------------");
            System.out.println(String.format("- THREAD POOL  : %6d    %7d    %11.3f   ", threadPoolActive.getMin(), threadPoolActive.getMax(), threadPoolActive.getSum() / summaryVo.getTotalRequest()));
            System.out.println("--------------------------------------------------------");
            System.out.println(String.format("- CONNECTED    : %6d ms %7d ms %11.3f ms", connectedTime.getMin(),    connectedTime.getMax(),    connectedTime.getSum() / summaryVo.getTotalRequest()));
            System.out.println(String.format("- SENT         : %6d ms %7d ms %11.3f ms", sentTime.getMin(),         sentTime.getMax(),         sentTime.getSum() / summaryVo.getTotalRequest()));
            System.out.println(String.format("- RECEIVED     : %6d ms %7d ms %11.3f ms", receivedTime.getMin(),     receivedTime.getMax(),     receivedTime.getSum() / summaryVo.getTotalRequest()));
            System.out.println(String.format("- DISCONNECTED : %6d ms %7d ms %11.3f ms", disconnectedTime.getMin(), disconnectedTime.getMax(), disconnectedTime.getSum() / summaryVo.getTotalRequest()));
            System.out.println("--------------------------------------------------------");
            System.out.println(String.format("DONE (%s)", Functions.formatElapsedTime(spentTime)));
        }

        //System.exit(0);
    }
}
