package com.mqttinsight.scripting.modules;

import cn.hutool.core.util.StrUtil;
import com.mqttinsight.util.Utils;

/**
 * @author ptma
 */
public class ToastWrapper {

    public void info(String message) {
        Utils.Toast.info(message);
    }

    public void info(String format, Object... arguments) {
        if (arguments.length == 0) {
            Utils.Toast.info(format);
        } else {
            Utils.Toast.info(StrUtil.format(format, arguments));
        }
    }

    public void success(String message) {
        Utils.Toast.success(message);
    }

    public void success(String format, Object... arguments) {
        if (arguments.length == 0) {
            Utils.Toast.success(format);
        } else {
            Utils.Toast.success(StrUtil.format(format, arguments));
        }
    }

    public void warn(String message) {
        Utils.Toast.warn(message);
    }

    public void warn(String format, Object... arguments) {
        if (arguments.length == 0) {
            Utils.Toast.warn(format);
        } else {
            Utils.Toast.warn(StrUtil.format(format, arguments));
        }
    }

    public void error(String message) {
        Utils.Toast.error(message);
    }

    public void error(String format, Object... arguments) {
        if (arguments.length == 0) {
            Utils.Toast.error(format);
        } else {
            Utils.Toast.error(StrUtil.format(format, arguments));
        }
    }
}
