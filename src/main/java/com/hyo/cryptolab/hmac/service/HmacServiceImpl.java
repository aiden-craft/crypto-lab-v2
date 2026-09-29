package com.hyo.cryptolab.hmac.service;

import com.hyo.cryptolab.common.constants.CryptoConstants;
import com.hyo.cryptolab.common.util.HexUtil;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class HmacServiceImpl implements HmacService {

    @Override
    public String generate(String message, String secretKey) {
        try {
            Mac mac = Mac.getInstance(CryptoConstants.HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), CryptoConstants.HMAC_SHA256);
            mac.init(keySpec);
            return HexUtil.bytesToHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC generation failed.", e);
        }
    }

    @Override
    public boolean verify(String message, String secretKey, String signature) {
        return generate(message, secretKey).equals(signature);
    }
}
