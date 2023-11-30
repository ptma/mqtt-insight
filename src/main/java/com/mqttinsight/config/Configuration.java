package com.mqttinsight.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.formdev.flatlaf.util.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ptma
 */
public final class Configuration implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    private static final int RECENT_ENTRIES = 10;

    private static class ConfigurationHolder {
        private final static Configuration INSTANCE = new Configuration();
    }

    public static Configuration instance() {
        return ConfigurationHolder.INSTANCE;
    }

    private final String userPath;
    private JSONObject conf = null;
    private boolean changed = false;
    private List<String> recentConnections;
    private List<ConnectionNode> connections;

    public List<String> getRecentConnections() {
        if (recentConnections == null) {
            recentConnections = new ArrayList<>();
        }
        return recentConnections;
    }

    public List<ConnectionNode> getConnections() {
        if (connections == null) {
            connections = new LinkedList<>();
        }
        return connections;
    }

    private Configuration() {
        if (SystemInfo.isWindows) {
            userPath = System.getProperty("user.dir");
        } else {
            userPath = System.getProperty("user.home") + File.separator + "MqttInsight";
        }
        try {
            conf = JSONUtil.readJSONObject(new File(confFilePath()), StandardCharsets.UTF_8);
            connections = conf.getBeanList(ConfKeys.CONNECTIONS, ConnectionNode.class);
            recentConnections = getRecentConnections();
            recentConnections.addAll(conf.getBeanList(ConfKeys.RECENT_CONNECTIONS, String.class));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (conf == null) {
                conf = new JSONObject();
            }
            connections = new LinkedList<>();
        }
        conf.getConfig().setIgnoreNullValue(true);
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

    public void clearTempPath() {
        try {
            FileUtil.clean(new File(getTempPath()));
        } catch (Exception ignored) {
        }
    }

    public void appendRecentConnection(String id) {
        recentConnections.remove(id);
        recentConnections.add(0, id);
        while (recentConnections.size() > RECENT_ENTRIES) {
            recentConnections.remove(RECENT_ENTRIES);
        }
        changed();
    }

    public void changed() {
        this.changed = true;
    }

    public void save() {
        save(false);
    }

    public void save(boolean force) {
        if (changed || force) {

            conf.set(ConfKeys.RECENT_CONNECTIONS, getRecentConnections());
            conf.set(ConfKeys.CONNECTIONS, getConnections());
            File configFile = new File(confFilePath());
            try {
                FileUtil.writeString(conf.toString(), configFile, StandardCharsets.UTF_8);
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
        return conf.getStr(key);
    }

    public String getString(String key, String defaultValue) {
        return conf.getStr(key, defaultValue);
    }

    public Integer getInt(String key, Integer defaultValue) {
        return conf.getInt(key, defaultValue);
    }

    public Integer getInt(String key) {
        return conf.getInt(key);
    }

    public Boolean getBoolean(String key, boolean defaultValue) {
        return conf.getBool(key, defaultValue);
    }

    public Boolean getBoolean(String key) {
        return conf.getBool(key);
    }

}
