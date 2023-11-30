package com.mqttinsight.mqtt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 * @see SecureMode
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecureSetting implements Serializable, Cloneable {

    protected boolean enable;

    protected SecureMode mode;

    protected SecureProtocol protocol;

    /**
     * <ul>
     *     <li>SERVER_ONLY</li>
     *     <li>SERVER_AND_CLIENT</li>
     * <ul/>
     */
    protected String serverCertificateFile;

    /**
     * <ul>
     *     <li>SERVER_KEYSTORE</li>
     *     <li>SERVER_AND_CLIENT_KEYSTORES</li>
     * </ul>
     */
    protected String serverKeyStoreFile;

    /**
     * <ul>
     *     <li>SERVER_KEYSTORE</li>
     *     <li>SERVER_AND_CLIENT_KEYSTORES</li>
     * </ul>
     */
    protected String serverKeyStorePassword;

    /**
     * <ul>
     *     <li>SERVER_AND_CLIENT</li>
     * </ul>
     */
    protected String clientCertificateFile;

    /**
     * <ul>
     *     <li>SERVER_AND_CLIENT</li>
     * </ul>
     */
    protected String clientKeyFile;

    /**
     * <ul>
     *     <li>SERVER_AND_CLIENT</li>
     * </ul>
     */
    protected String clientKeyPassword;

    /**
     * <ul>
     *     <li>SERVER_AND_CLIENT</li>
     * </ul>
     */
    protected boolean clientKeyPEM;


    /**
     * <ul>
     *     <li>SERVER_AND_CLIENT_KEYSTORES</li>
     * </ul>
     */
    protected String clientKeyStoreFile;

    /**
     * <ul>
     *     <li>SERVER_AND_CLIENT_KEYSTORES</li>
     * </ul>
     */
    protected String clientKeyStorePassword;

    /**
     * <ul>
     *     <li>PROPERTIES</li>
     * </ul>
     */
    protected List<Property> properties;


    public SecureMode getMode() {
        if (mode == null) {
            mode = SecureMode.BASIC;
        }
        return mode;
    }

    public SecureProtocol getProtocol() {
        if (protocol == null) {
            protocol = SecureProtocol.TLS_1_2;
        }
        return protocol;
    }

    public List<Property> getProperties() {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        return properties;
    }

    @Override
    public SecureSetting clone() throws CloneNotSupportedException {
        return (SecureSetting) super.clone();
    }
}
