package kr.zany.sample.netty4.client.common.util;

import kr.zany.sample.netty4.client.common.data.CommonResultVo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p><b></b></p>
 * <p/>
 * <p>Copyright ⓒ 2016 kt corp. All rights reserved.</p>
 *
 * @author Lee Sang-Hyun (zanylove@gmail.com)
 * @since 2016-05-02 10:53
 */
@Slf4j
public final class ThreadUtils {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Inject Beans
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Member Variables
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructor
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Public Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static <T> Collection<CommonResultVo> awaitAsyncTasks(Collection<Future<T>> threads) {
        return awaitAsyncTasks(threads, 0);
    }

    public static <T> Collection<CommonResultVo> awaitAsyncTasks(Collection<Future<T>> threads, int timeoutSec) {

        Collection<CommonResultVo> results = new ArrayList<>();

        for (Future<?> future : threads) {

            CommonResultVo result = Functions.buildCommonResult(0, "OK");

            try {

                Object object = (timeoutSec > 0) ? future.get(timeoutSec, TimeUnit.SECONDS) : future.get();

                if (object == null) {
                    result.setResultCode(-100);
                    result.setResultMsg("result is null.");
                } else {
                    result.setResultCode(0);
                    result.setResultMsg("OK");
                    result.setResultObject(object);
                }

            } catch (TimeoutException e) {
                result.setResultCode(-200);
                result.setResultMsg(String.format("timeout, %s", e.getMessage()));
            } catch (InterruptedException|ExecutionException e) {
                result.setResultCode(-300);
                result.setResultMsg(String.format("exception, %s", e.getMessage()));
            }

            results.add(result);
        }

        return results;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Protected, Private Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Override Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

}
