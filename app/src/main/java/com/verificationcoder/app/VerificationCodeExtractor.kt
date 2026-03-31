package com.verificationcoder.app

object VerificationCodeExtractor {

    /**
     * 从短信内容中提取验证码
     * 提取逻辑：提取4-8位连续数字，优先取最后出现的
     *
     * @param message 短信内容
     * @return 验证码，如果未找到则返回null
     */
    fun extractCode(message: String?): String? {
        if (message.isNullOrEmpty()) {
            return null
        }

        // 提取4-8位连续数字，优先取最后出现的
        val pattern = Regex("\\d{4,8}")
        val matches = pattern.findAll(message).toList()

        return if (matches.isNotEmpty()) {
            matches.last().value
        } else {
            null
        }
    }

    /**
     * 提取所有可能的验证码（用于调试）
     *
     * @param message 短信内容
     * @return 所有匹配的数字序列
     */
    fun extractAllCodes(message: String?): List<String> {
        if (message.isNullOrEmpty()) {
            return emptyList()
        }

        val pattern = Regex("\\d{4,8}")
        return pattern.findAll(message).map { it.value }.toList()
    }
}
