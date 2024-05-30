package io.vital.billspace.query;

public class ResetPasswordVerificationQuery {
    public static final String DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY =
            "DELETE FROM ResetPasswordVerifications WHERE user_id = :user_id";

    public static final String INSERT_PASSWORD_VERIFICATION_QUERY =
            "INSERT INTO ResetPasswordVerifications (user_id, url, expiration_date) " +
                    "VALUES(:id, :url, :expirationDate)";

    public static final String SELECT_EXPIRATION_DATE_BY_CODE_QUERY = "SELECT expiration_date " +
            "FROM ResetPasswordVerifications WHERE url = :url";

    public static final String DELETE_PASSWORD_VERIFICATION_BY_URL_QUERY = "DELETE FROM ResetPasswordVerifications" +
            " WHERE url = :url";

}
