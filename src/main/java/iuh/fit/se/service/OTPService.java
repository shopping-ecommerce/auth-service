package iuh.fit.se.service;

import iuh.fit.se.dto.request.VerifyOTPRequest;

public interface OTPService {
    public void createAndSaveOTP(String email);

    public boolean verifyOTP(VerifyOTPRequest verifyOTPRequest);

    public void deleteOTP(String email);
}
