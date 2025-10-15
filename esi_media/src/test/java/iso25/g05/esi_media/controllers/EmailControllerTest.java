package iso25.g05.esi_media.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import iso25.g05.esi_media.services.EmailService;

@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController emailController;

    @Test
    void testActivate() {
        // No business logic in the controller method, just testing the call
        emailController.activate("test_token");
        // If there were logic, we would verify service calls, like:
        // verify(emailService, times(1)).activateAccount("test_token");
    }
}
