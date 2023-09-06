package com.mqttinsight.mqtt;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.util.Const;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 */
@Getter
@Setter
public class MqttProperties implements Serializable, Cloneable {

    protected String id = IdUtil.fastUUID();

    protected String name;

    protected String host = "127.0.0.1";

    protected int port = 1883;

    protected Transport transport = Transport.MQTT;

    protected Version version = Version.MQTT_3_1_1;

    protected String clientId;

    protected boolean randomClientId;

    protected String username;

    protected String password;

    protected WillMessage lastWill = new WillMessage();

    /* ============== MQTT 3 only ============== */
    protected boolean cleanSession = true;

    protected int connectionTimeout = 30;

    protected int keepAliveInterval = 60;

    /* ============== MQTT 5 options ============== */
    protected boolean cleanStart = true;
    protected Long sessionExpiryInterval = null;
    protected Integer receiveMaximum = null; // The Receive Maximum, null defaults to 65,535, cannot be 0.
    protected Long maximumPacketSize = null; // The Maximum packet size, null defaults to no limit.
    protected Integer topicAliasMaximum = null; // The Topic Alias Maximum, null defaults to 0.
    protected boolean requestResponseInfo = false; // Request Response Information, null defaults to false.
    protected boolean requestProblemInfo = true;
    protected List<Property> userProperties = null;
    /* ============== MQTT 5 options end ============== */

    protected SecureSetting secure = new SecureSetting();

    // Other properties
    protected ReconnectionSettings reconnection = new ReconnectionSettings();
    protected List<String> searchHistory;
    protected List<FavoriteSubscription> favoriteSubscriptions;
    protected Integer maxMessageStored = Const.MESSAGES_STORED_MAX_SIZE;
    protected String payloadFormat;
    protected boolean clearUnsubMessage = true;

    /* ============== Getter and Setter ============== */
    public String getUsername() {
        return username == null ? "" : username;
    }

    public String getPassword() {
        return password == null ? "" : password;
    }

    public String getClientId() {
        return randomClientId ? "MqttInsight_" + RandomUtil.randomString(8) : clientId;
    }

    public String getPayloadFormat() {
        return (payloadFormat == null) ? CodecSupport.PLAIN : payloadFormat;
    }

    public SecureSetting getSecure() {
        if (secure == null) {
            secure = new SecureSetting();
        }
        return secure;
    }

    public ReconnectionSettings getReconnection() {
        if (reconnection == null) {
            reconnection = new ReconnectionSettings();
        }
        return reconnection;
    }

    public String completeServerURI() {
        String serverURI = StrUtil.format("{}:{}", host, port);
        boolean sslEnabled = secure != null && secure.isEnable();
        boolean websocket = Transport.WEB_SOCKET.equals(transport);
        if (sslEnabled && websocket) {
            serverURI = "wss://" + serverURI;
        } else if (sslEnabled && !websocket) {
            serverURI = "ssl://" + serverURI;
        } else if (!sslEnabled && websocket) {
            serverURI = "ws://" + serverURI;
        } else {
            serverURI = "tcp://" + serverURI;
        }

        return serverURI;
    }

    public List<FavoriteSubscription> getFavoriteSubscriptions() {
        if (favoriteSubscriptions == null) {
            favoriteSubscriptions = new ArrayList<>();
        }
        return favoriteSubscriptions;
    }

    public boolean isFavorite(String topic) {
        if (favoriteSubscriptions != null) {
            for (int i = 0; i < favoriteSubscriptions.size(); i++) {
                if (favoriteSubscriptions.get(i).getTopic().equals(topic)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addFavorite(String topic, int qos, String format) {
        if (favoriteSubscriptions == null) {
            favoriteSubscriptions = new ArrayList<>();
        }
        favoriteSubscriptions.add(new FavoriteSubscription(topic, qos, format));
    }

    public void removeFavorite(String topic) {
        if (favoriteSubscriptions != null) {
            for (int i = 0; i < favoriteSubscriptions.size(); i++) {
                if (favoriteSubscriptions.get(i).getTopic().equals(topic)) {
                    favoriteSubscriptions.remove(i);
                    return;
                }
            }
        }
    }


    @Override
    public MqttProperties clone() throws CloneNotSupportedException {
        MqttProperties clone = (MqttProperties) super.clone();
        clone.id = IdUtil.fastUUID();
        return clone;
    }
}
