package jenkins.scm.impl.mock;

import org.junit.Test;

import java.security.MessageDigest;

import static org.junit.Assert.assertEquals;

public class MockSCMControllerTest {

    @Test
    public void toHexBinary() throws Exception {
        final MessageDigest msg = MessageDigest.getInstance("SHA-1");
        msg.update("blah".getBytes());
        assertEquals("5bf1fd927dfb8679496a2e6cf00cbe50c1c87145", MockSCMController.toHexBinary(msg.digest()));
    }
}
