package com.mqttinsight.config;

import cn.hutool.core.io.FileUtil;
import com.formdev.flatlaf.util.SystemInfo;
import com.mqttinsight.codec.DynamicCodec;
import com.mqttinsight.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author ptma
 */
public final class Configuration implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    private static class ConfigurationHolder {
        private final static Configuration INSTANCE = new Configuration();
    }

    public static Configuration instance() {
        return ConfigurationHolder.INSTANCE;
    }

    private final String userPath;
    private Conf conf = null;
    private boolean changed = false;

    public List<ConnectionNode> getConnections() {
        return conf.getConnections();
    }

    public List<DynamicCodec> getDynamicCodecs() {
        return conf.getDynamicCodecs();
    }

    private Configuration() {
        if (SystemInfo.isWindows) {
            userPath = System.getProperty("user.dir");
        } else {
            userPath = System.getProperty("user.home") + File.separator + "MqttInsight";
        }
        try {
            conf = Utils.JSON.readObject(new File(confFilePath()), Conf.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (conf == null) {
                conf = new Conf();
            }
        }
    }

    private String confFilePath() {
        return userPath + File.separator + "conf" + File.separator + "config.json";
    }

    public String getUserPath() {
        return userPath;
    }

    public String getTempPath() {
        return userPath + File.separator + "temp";
    }

    public String getCodecsPath() {
        return userPath + File.separator + "codecs";
    }

    public void clearTempPath() {
        try {
            FileUtil.clean(new File(getTempPath()));
        } catch (Exception ignored) {
        }
    }

    public void changed() {
        this.changed = true;
    }

    public void save() {
        save(false);
    }

    public void save(boolean force) {
        if (changed || force) {
            File configFile = new File(confFilePath());
            try {
                FileUtil.writeString(Utils.JSON.toString(conf), configFile, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("Can not write configuration to file: {}", configFile.getAbsolutePath(), e);
            }
            changed = false;
        }
    }

    public void set(String key, Object value) {
        changed();
        conf.set(key, value);
    }

    public String getString(String key) {
        return (String) conf.get(key);
    }

    public String getString(String key, String defaultValue) {
        return (String) conf.getOrDefault(key, defaultValue);
    }

    public Integer getInt(String key, Integer defaultValue) {
        return (Integer) conf.getOrDefault(key, defaultValue);
    }

    public Integer getInt(String key) {
        return (Integer) conf.get(key);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return (Boolean) conf.getOrDefault(key, defaultValue);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) conf.get(key);
    }

}
