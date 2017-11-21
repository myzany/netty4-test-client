package kr.zany.sample.netty4.client.common.config;

import kr.zany.sample.netty4.client.common.data.CliStatics;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p><b>Apache Commons Cli, Bean Configuration</b></p>
 *
 * <p>Copyright ⓒ 2015 kt corp. All rights reserved.</p>
 *
 * @author Lee Sang-Hyun (zanylove@gmail.com)
 * @since 2015-11-19 16:00
 */
@Configuration
public class CliBeanConfig {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Inject Beans
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Bean
    public Options options() {

        Options options = new Options();

        options.addOption(CliStatics.OPT_SHOW_HELP[0],  CliStatics.OPT_SHOW_HELP[1],  false, "show help.");

        options.addOption(CliStatics.OPT_HOST[0],       CliStatics.OPT_HOST[1],       true,  "host ip address.");
        options.addOption(CliStatics.OPT_PORT[0],       CliStatics.OPT_PORT[1],       true,  "host port number.");

        options.addOption(CliStatics.OPT_THREAD_CNT[0], CliStatics.OPT_THREAD_CNT[1], true,  "thread count.");
        options.addOption(CliStatics.OPT_RAMP_SEC[0],   CliStatics.OPT_RAMP_SEC[1],   true,  "ramp up seconds.");
        options.addOption(CliStatics.OPT_LOOP_CNT[0],   CliStatics.OPT_LOOP_CNT[1],   true,  "loop count.");

        return options;
    }

    @Bean
    public CommandLineParser commandLineParser() {
        return new IgnoreUnrecognizedOptionParser(true);
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Member Variables
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructor
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Public Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Protected, Private Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Override Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

}
