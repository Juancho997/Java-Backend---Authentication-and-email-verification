package com.boninojuan.demo.registration;

import com.boninojuan.demo.appuser.AppUser;
import com.boninojuan.demo.appuser.AppUserRole;
import com.boninojuan.demo.appuser.AppUserService;
import com.boninojuan.demo.registration.token.ConfirmationToken;
import com.boninojuan.demo.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final EmailValidator emailValidator;
    private final ConfirmationTokenService confirmationTokenService;

    @Transactional
    public String confirmToken(String token){
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(()->
                        new IllegalStateException("Token not found"));

        if(confirmationToken.getConfirmedAt() != null){
            throw new IllegalStateException("Email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if(expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Expired token");
        }

        confirmationTokenService.setConfirmedAt(token);

        appUserService.enableAppUser(
                confirmationToken.getAppUser().getEmail());

        return "Confirmed";
    }

    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());

        if(!isValidEmail) {
            throw new IllegalStateException("Invalid email");
        }

        return appUserService.singUp(
                new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        AppUserRole.USER
                )
        );
    }
}
