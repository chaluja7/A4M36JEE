package cz.cvut.fel.service;

import cz.cvut.fel.model.FlightStatus;
import cz.cvut.fel.util.ArquillianTest;
import cz.cvut.fel.util.UpdateServiceClient;
import org.testng.annotations.Test;

import java.net.URL;

import static cz.cvut.fel.utils.DateUtils.date;

/** @author Karel Cemus */
public class UpdateServiceTest extends ArquillianTest {

    /** The path of the WSDL endpoint in relation to the deployed web application. */
    private static final String WSDL_PATH = "http://localhost:8080/FlightSystem/UpdateService?wsdl";

    private UpdateServiceClient client;

    @Test
    public void testUpdate() throws Exception {
        client = new UpdateServiceClient( new URL( WSDL_PATH ) );
        client.login( "karel", "cemus" );
        client.update( "1", date( 1, 2, 2013, 10, 20 ), date( 1, 2, 2013, 13, 20 ), FlightStatus.DELAYED );
    }

    @Test( expectedExceptions = Exception.class )
    public void testUpdate_NotInRole() throws Exception {
        client = new UpdateServiceClient( new URL( WSDL_PATH ) );
        client.login( "lubos", "matl" );
        client.update( "1", date( 1, 2, 2013, 10, 20 ), date( 1, 2, 2013, 13, 20 ), FlightStatus.DELAYED );
    }


}
