package com.mqttinsight.mqtt.security;

import cn.hutool.core.io.FileUtil;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Utility class for handling SSL/TLS connections.
 */
public class SecureSocketUtils {
    private final static Logger logger = LoggerFactory.getLogger(SecureSocketUtils.class);

    private final static String ALGORITHM = "RSA";

    /**
     * Loads a PEM file from the specified location.
     *
     * @param file Location of the file to load
     * @return Content of the PEM file
     * @throws IOException Thrown when cannot read the file
     */
    public static byte[] loadPemFileAsBytes(final String file) throws IOException {
        try (PemReader pemReader = new PemReader(new FileReader(file))) {
            final PemObject pemObject = pemReader.readPemObject();
            final byte[] content = pemObject.getContent();
            logger.debug("Reading PEM file {}, type = {}", file, pemObject.getType());
            pemReader.close();
            return content;
        }
    }

    /**
     * Loads a key file from the specified location.
     *
     * @param file Location of the file to load
     * @return Content of the key file
     * @throws IOException Thrown when cannot read the file
     */
    public static byte[] loadBinaryFileAsBytes(final String file) throws IOException {
        return FileUtil.readBytes(file);
    }

    /**
     * Loads a private key from the specified location.
     */
    public static PrivateKey loadPrivateKeyFromPemFile(final String keyFile) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(loadPemFileAsBytes(keyFile));
        return KeyFactory.getInstance(ALGORITHM).generatePrivate(privateKeySpec);
    }

    /**
     * Loads a private key from the specified location.
     */
    public static PrivateKey loadPrivateKeyFromBinaryFile(final String keyFile) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(loadBinaryFileAsBytes(keyFile));
        return KeyFactory.getInstance(ALGORITHM).generatePrivate(privateKeySpec);
    }

    /**
     * Loads an X509 certificate from the given location.
     */
    public static X509Certificate loadX509Certificate(final String certificateFile) throws IOException, CertificateException {
        try (InputStream inputStream = FileUtil.getInputStream(certificateFile)) {
            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(inputStream);
        }
    }

    /**
     * Creates a trust manager factory.
     */
    public static TrustManagerFactory getTrustManagerFactory(final String caCertificateFile)
        throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        // Load CA certificate
        final X509Certificate caCertificate = (X509Certificate) loadX509Certificate(caCertificateFile);

        // CA certificate is used to authenticate server
        final KeyStore keyStore = getKeyStoreInstance(KeyStoreTypeEnum.DEFAULT);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca-certificate", caCertificate);

        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        return tmf;
    }

    /**
     * Creates a trust manager factory.
     */
    public static TrustManagerFactory getTrustManagerFactory(final String keyStoreFile, final String keyStorePassword,
                                                             final KeyStoreTypeEnum keyStoreType)
        throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        // Load key store
        final KeyStore keyStore = loadKeystore(keyStoreFile, keyStorePassword, keyStoreType);

        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        return tmf;
    }

    /**
     * Creates a key manager factory using a key store.
     */
    public static KeyManagerFactory getKeyManagerFactory(final String keyStoreFile, final String keyStorePassword,
                                                         final String keyPassword, final KeyStoreTypeEnum keyStoreType)
        throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException {
        // Load key store
        final KeyStore keyStore = loadKeystore(keyStoreFile, keyStorePassword, keyStoreType);

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword.toCharArray());

        return kmf;
    }

    /**
     * Creates a key manager factory.
     */
    public static KeyManagerFactory getKeyManagerFactory(
        final String clientCertificateFile, final String clientKeyFile, final String clientKeyPassword, final boolean pemFormat)
        throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, InvalidKeySpecException {
        // Load client certificate
        final X509Certificate clientCertificate = loadX509Certificate(clientCertificateFile);

        // Load private client key
        final PrivateKey privateKey = pemFormat ? loadPrivateKeyFromPemFile(clientKeyFile) : loadPrivateKeyFromBinaryFile(clientKeyFile);

        // Client key and certificate are sent to server
        final KeyStore keyStore = getKeyStoreInstance(KeyStoreTypeEnum.DEFAULT);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("certificate", clientCertificate);
        keyStore.setKeyEntry("private-key", privateKey, clientKeyPassword.toCharArray(), new Certificate[]{clientCertificate});

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, clientKeyPassword.toCharArray());

        return kmf;
    }

    /**
     * Loads a key store from the specified location and using the given password.
     */
    public static KeyStore loadKeystore(final String keyStoreFile, final String keyStorePassword, final KeyStoreTypeEnum keyStoreType)
        throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        KeyStore keyStore;
        try (final FileInputStream inputStream = new FileInputStream(keyStoreFile)) {
            keyStore = getKeyStoreInstance(keyStoreType);
            keyStore.load(inputStream, keyStorePassword.toCharArray());
        }
        return keyStore;
    }

    public static KeyStore getKeyStoreInstance(final KeyStoreTypeEnum type) throws KeyStoreException {
        if (type == null || KeyStoreTypeEnum.DEFAULT.equals(type)) {
            return KeyStore.getInstance(KeyStore.getDefaultType());
        }
        return KeyStore.getInstance(type.value());
    }

    public static KeyStore getKeyStoreInstance(final KeyStoreTypeEnum type, final Provider provider) throws KeyStoreException {
        if (type == null || KeyStoreTypeEnum.DEFAULT.equals(type)) {
            return KeyStore.getInstance(KeyStore.getDefaultType());
        }

        return KeyStore.getInstance(type.value(), provider);
    }

    public static KeyStoreTypeEnum getTypeFromFilename(final String filename) {
        if (filename == null || filename.isEmpty()) {
            return KeyStoreTypeEnum.DEFAULT;
        } else if (filename.toLowerCase().endsWith("jks")) {
            return KeyStoreTypeEnum.JKS;
        } else if (filename.toLowerCase().endsWith("jceks")) {
            return KeyStoreTypeEnum.JCEKS;
        } else if (filename.toLowerCase().endsWith("p12")) {
            return KeyStoreTypeEnum.PKCS_12;
        } else if (filename.toLowerCase().endsWith("pfx")) {
            return KeyStoreTypeEnum.PKCS_12;
        } else if (filename.toLowerCase().endsWith("bks")) {
            return KeyStoreTypeEnum.BKS;
        } else {
            return KeyStoreTypeEnum.DEFAULT;
        }
    }
}
