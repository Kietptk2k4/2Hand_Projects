package com.twohands.auth_service.unit.application.auth.common;

import com.twohands.auth_service.application.auth.common.EmailVerificationOtpGenerator;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailVerificationOtpGeneratorTest {

    private static final Pattern OTP_PATTERN = Pattern.compile("^\\d{6}$");

    private final EmailVerificationOtpGenerator generator = new EmailVerificationOtpGenerator();

    @RepeatedTest(20)
    void generate_producesSixDigitNumericOtp() {
        String otp = generator.generate();

        assertTrue(OTP_PATTERN.matcher(otp).matches());
        int value = Integer.parseInt(otp);
        assertTrue(value >= 100_000 && value <= 999_999);
    }

    @Test
    void generate_producesDistinctValuesOverManyCalls() {
        Set<String> values = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            values.add(generator.generate());
        }
        assertTrue(values.size() > 1);
    }
}
