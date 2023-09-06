package com.mqttinsight.mqtt.security;

import com.mqttinsight.exception.SslException;
import com.mqttinsight.mqtt.SecureProtocol;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;
import java.security.Security;

public class SecureSocketFactoryBuilder {

    public static SSLSocketFactory getSocketFactory(final SecureProtocol protocol) throws SslException {
        return getSocketFactory(protocol, null, null, null);
    }

    public static SSLSocketFactory getSocketFactory(final SecureProtocol protocol, final String caCertificateFile) throws SslException {
        try {
            Security.addProvider(new BouncyCastleProvider());

            final TrustManager[] tm = SecureSocketUtils.getTrustManagerFactory(caCertificateFile).getTrustManagers();

            return getSocketFactory(protocol, null, tm, null);
        } catch (Exception e) {
            throw new SslException("Cannot create TLS/SSL connection", e);
        }
    }

    public static SSLSocketFactory getSocketFactory(final SecureProtocol protocol,
                                                    final String caKeyStoreFile, final String caKeyStorePassword) throws SslException {
        try {
            Security.addProvider(new BouncyCastleProvider());

            final TrustManager[] tm = SecureSocketUtils.getTrustManagerFactory(
                    caKeyStoreFile, caKeyStorePassword, SecureSocketUtils.getTypeFromFilename(caKeyStoreFile))
                .getTrustManagers();

            return getSocketFactory(protocol, null, tm, null);
        } catch (Exception e) {
            throw new SslException("Cannot create TLS/SSL connection", e);
        }
    }

    public static SSLSocketFactory getSocketFactory(final SecureProtocol protocol,
                                                    final String serverCrtFile, final String clientCrtFile, final String clientKeyFile, final String clientKeyPassword,
                                                    final boolean pemFormat) throws SslException {
        try {
            Security.addProvider(new BouncyCastleProvider());

            final KeyManager[] km = SecureSocketUtils.getKeyManagerFactory(clientCrtFile, clientKeyFile, clientKeyPassword, pemFormat).getKeyManagers();
            final TrustManager[] tm = SecureSocketUtils.getTrustManagerFactory(serverCrtFile).getTrustManagers();

            return getSocketFactory(protocol, km, tm, null);
        } catch (Exception e) {
            throw new SslException("Cannot create TLS/SSL connection", e);
        }
    }

    public static SSLSocketFactory getSocketFactory(final SecureProtocol protocol,
                                                    final String caKeyStoreFile, final String caKeyStorePassword,
                                                    final String clientKeyStoreFile, final String clientKeyStorePassword, final String clientKeyPassword)
        throws SslException {
        try {
            Security.addProvider(new BouncyCastleProvider());

            final KeyManager[] km = SecureSocketUtils.getKeyManagerFactory(
                    clientKeyStoreFile, clientKeyStorePassword, clientKeyPassword, SecureSocketUtils.getTypeFromFilename(clientKeyStoreFile))
                .getKeyManagers();

            final TrustManager[] tm = SecureSocketUtils.getTrustManagerFactory(
                    caKeyStoreFile, caKeyStorePassword, SecureSocketUtils.getTypeFromFilename(caKeyStoreFile))
                .getTrustManagers();

            return getSocketFactory(protocol, km, tm, null);
        } catch (Exception e) {
            throw new SslException("Cannot create TLS/SSL connection", e);
        }
    }

    private static SSLSocketFactory getSocketFactory(final SecureProtocol protocol,
                                                     final KeyManager[] km, final TrustManager[] tm, final SecureRandom random) throws SslException {
        try {
            Security.addProvider(new BouncyCastleProvider());

            // Create SSL/TLS socket factory
            final SSLContext context = SSLContext.getInstance(protocol.getValue());

            context.init(km, tm, random);

            return context.getSocketFactory();
        } catch (Exception e) {
            throw new SslException("Cannot create TLS/SSL connection", e);
        }
    }
}
