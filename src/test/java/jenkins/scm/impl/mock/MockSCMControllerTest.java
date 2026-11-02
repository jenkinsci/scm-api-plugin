package jenkins.scm.impl.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.MessageDigest;
import org.junit.jupiter.api.Test;

class MockSCMControllerTest {

    @Test
    void toHexBinary() throws Exception {
        final MessageDigest msg = MessageDigest.getInstance("SHA-1");
        msg.update("blah".getBytes());
        assertEquals("5bf1fd927dfb8679496a2e6cf00cbe50c1c87145", MockSCMController.toHexBinary(msg.digest()));
    }
}
