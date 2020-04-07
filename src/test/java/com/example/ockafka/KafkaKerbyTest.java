package com.example.ockafka;
import java.io.File;

import org.apache.kerby.kerberos.kerb.KrbException;
import org.apache.kerby.kerberos.kerb.server.SimpleKdcServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Create a KDC for use with securing Kafka. It contains the following principals:
 * a) zookeeper/localhost
 * b) kafka/localhost
 * c) client (used for both consumers + producers)
 *
 * The krb.conf file with the (random) port that the KDC is running on is written out to target/krb5.conf
 *
 * Comment out the org.junit.Ignore annotation on the test below to run this KDC.
 */
public class KafkaKerbyTest extends org.junit.Assert {

    private static SimpleKdcServer kerbyServer;

    @BeforeClass
    public static void setUp() throws Exception {

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File(".").getCanonicalPath();
        }

        System.setProperty("sun.security.krb5.debug", "true");
        //System.setProperty("java.security.auth.login.config", basedir + "/target/test-classes/kerberos/kerberos.jaas");

        kerbyServer = new SimpleKdcServer();
       // kerbyServer.setKdcUdpPort(64132);
        kerbyServer.setKdcRealm("kafka.apache.org");
        kerbyServer.setAllowUdp(false);
        kerbyServer.setWorkDir(new File(basedir + "/target"));

        //kerbyServer.setInnerKdcImpl(new NettyKdcServerImpl(kerbyServer.getKdcSetting()));

        kerbyServer.init();

        // Create principals
        String zookeeper = "zookeeper/localhost@kafka.apache.org";
        String kafka = "kafka/localhost@kafka.apache.org";
        String client = "client@kafka.apache.org";

        kerbyServer.createPrincipal(zookeeper, "zookeeper");
        File keytabFile = new File(basedir + "/target/zookeeper.keytab");
        kerbyServer.exportPrincipal(zookeeper, keytabFile);

        kerbyServer.createPrincipal(kafka, "kafka");
        keytabFile = new File(basedir + "/target/kafka.keytab");
        kerbyServer.exportPrincipal(kafka, keytabFile);

        kerbyServer.createPrincipal(client, "client");
        keytabFile = new File(basedir + "/target/client.keytab");
        kerbyServer.exportPrincipal(client, keytabFile);

        kerbyServer.start();
    }

    @AfterClass
    public static void tearDown() throws KrbException {
        if (kerbyServer != null) {
            kerbyServer.stop();
        }
    }

    @Test
    @org.junit.Ignore
    public void testKerberos() throws Exception {
        System.out.println("KDC ready on port: " + kerbyServer.getKdcSetting().getKdcTcpPort());
        Thread.sleep(2 * 100 * 60 * 1000);
    }

}