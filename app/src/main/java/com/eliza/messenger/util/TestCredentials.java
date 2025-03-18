package com.eliza.messenger.util;

/**
 * Utility class containing test credentials for development and testing purposes.
 * These should NEVER be used in production builds.
 */
public class TestCredentials {
    // Flag to enable/disable test credentials
    public static final boolean USE_TEST_CREDENTIALS = true;
    
    // Test phone numbers
    public static final String TEST_PHONE_NUMBER = "+1234567890";
    public static final String TEST_PHONE_NUMBER_2 = "+15555555556";
    public static final String TEST_PHONE_NUMBER_3 = "+15555555557";
    
    // Test verification codes
    public static final String TEST_VERIFICATION_CODE = "123456";
    
    // Test user profile
    public static final String TEST_USER_NAME = "Test User";
    public static final String TEST_USER_BIO = "This is a test account for development purposes";
    public static final String TEST_USER_EMAIL = "test.user@example.com";
    
    // Test user ID (for Firebase)
    public static final String TEST_USER_ID = "test_user_123456";
    
    // Test profile image URL
    public static final String TEST_PROFILE_IMAGE_URL = "https://ui-avatars.com/api/?name=Test+User&background=random";
    
    // Test authentication
    public static final String TEST_EMAIL = "test@elizachat.dev";
    public static final String TEST_PASSWORD = "testuser123!@#";
    
    // Test contacts
    public static final String[][] TEST_CONTACTS = {
        {"John Doe", "+15551234567"},
        {"Jane Smith", "+15559876543"},
        {"Alice Johnson", "+15555551212"}
    };
    
    /**
     * Check if a phone number is a test phone number
     * @param phoneNumber The phone number to check
     * @return true if it's a test phone number
     */
    public static boolean isTestPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !USE_TEST_CREDENTIALS) {
            return false;
        }
        
        return phoneNumber.equals(TEST_PHONE_NUMBER) || 
               phoneNumber.equals(TEST_PHONE_NUMBER_2) || 
               phoneNumber.equals(TEST_PHONE_NUMBER_3) || 
               phoneNumber.endsWith("0000000000") ||
               phoneNumber.endsWith("1234567890");
    }
    
    /**
     * Check if a verification code is a test verification code
     * @param code The verification code to check
     * @return true if it's a test verification code
     */
    public static boolean isTestVerificationCode(String code) {
        if (code == null || !USE_TEST_CREDENTIALS) {
            return false;
        }
        
        return code.equals(TEST_VERIFICATION_CODE) || 
               code.equals("000000") || 
               code.equals("123456");
    }
    
    /**
     * Get a test user profile image URL based on the user's name
     * @param firstName First name of the user
     * @param lastName Last name of the user
     * @return A URL for a generated avatar
     */
    public static String getTestProfileImageUrl(String firstName, String lastName) {
        return "https://ui-avatars.com/api/?name=" + 
               firstName + "+" + lastName + 
               "&background=random&color=fff&size=256";
    }
}
