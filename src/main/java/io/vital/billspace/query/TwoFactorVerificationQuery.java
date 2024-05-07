package io.vital.billspace.query;

public class TwoFactorVerificationQuery {
    public static final String DELETE_VERIFICATION_CODE_BY_USER_ID_QUERY = "DELETE FROM TwoFactorVerifications " +
            "WHERE user_id=:id";

    public static final String INSERT_VERIFICATION_CODE_QUERY = "INSERT INTO TwoFactorVerifications " +
            "(user_id, code, expiration_date) VALUES(:user_id, :code, :expiration_date)";

    public static final String DELETE_CODE = "DELETE FROM TwoFactorVerifications WHERE code=:code";

    public static final String SELECT_EXPIRATION_DATE_BY_USER_CODE_QUERY = "SELECT t.expiration_date" +
            " FROM TwoFactorVerifications t JOIN Users u on u.id=t.user_id WHERE u.email=:email AND t.code=:code";
}
