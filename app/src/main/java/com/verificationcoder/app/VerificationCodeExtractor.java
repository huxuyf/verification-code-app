package com.verificationcoder.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerificationCodeExtractor {

    /**
     * 从短信内容中提取验证码
     * 提取逻辑：提取4-8位连续数字，优先取最后出现的
     *
     * @param message 短信内容
     * @return 验证码，如果未找到则返回null
     */
    public static String extractCode(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        // 提取4-8位连续数字
        Pattern pattern = Pattern.compile("\\d{4,8}");
        Matcher matcher = pattern.matcher(message);

        String lastCode = null;
        while (matcher.find()) {
            lastCode = matcher.group();
        }

        return lastCode;
    }

    /**
     * 提取所有可能的验证码（用于调试）
     *
     * @param message 短信内容
     * @return 所有匹配的数字序列
     */
    public static java.util.List<String> extractAllCodes(String message) {
        java.util.List<String> codes = new java.util.ArrayList<>();
        if (message == null || message.isEmpty()) {
            return codes;
        }

        Pattern pattern = Pattern.compile("\\d{4,8}");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            codes.add(matcher.group());
        }

        return codes;
    }
}
