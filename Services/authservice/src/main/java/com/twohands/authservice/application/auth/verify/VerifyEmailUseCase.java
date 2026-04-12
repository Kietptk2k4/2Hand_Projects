// package com.twohands.authservice.application.auth.verify;

// import com.twohands.authservice.application.auth.port.AttemptStore;
// import com.twohands.authservice.application.auth.port.OtpStore;
// import com.twohands.authservice.domain.user.User;
// import com.twohands.authservice.domain.user.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;

// @Service
// @RequiredArgsConstructor
// public class VerifyEmailUseCase {
//     private final UserRepository userRepository;
//     private final OtpStore otpStore;
//     private final AttemptStore attemptStore;

//     @Value("${auth.otp.ttl-seconds}")
//     private long otpTtl;

//     @Value("${auth.otp.max-attempts}")
//     private int maxAttempts;

//     public void execute(String email, String otp){
//         String emailNorm = normalize(email);
//         User user = userRepository.findByEmailNormalized(emailNorm).orElseThrow(()-> new RuntimeException("User not found"));

//         if(user.getEmailVerified()){
//             throw new RuntimeException("User is already verified");
//         }

//         String otpKey = "auth:otp:" + emailNorm;
//         String storedOtp = otpStore.get(otpKey);

//         if(storedOtp == null){
//             throw new RuntimeException("OTP expired");
//         }

//         if(!storedOtp.equals(otp)){
//             String failKey = "auth:otp:fail:" + emailNorm;
//             long attempts = attemptStore.increment(failKey,otpTtl);

//             // if()
//         }

//     }    
// }
