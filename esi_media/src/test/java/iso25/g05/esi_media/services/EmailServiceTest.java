package iso25.g05.esi_media.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.CodigoRecuperacionRepository;
import iso25.g05.esi_media.service.EmailService;
import jakarta.mail.internet.MimeMessage;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private CodigoRecuperacionRepository codigoRecuperacionRepository;

    @InjectMocks
    private EmailService emailService;

    @Test
    void testSend3FAEmail() {
        Usuario user = new Usuario();
        user.setEmail("test@example.com");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.send3FAemail("test@example.com", user);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(codigoRecuperacionRepository, times(1)).save(any());
    }
}
