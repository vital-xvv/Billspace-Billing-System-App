package io.vital.billspace.query;

public class UserQuery {
    public static final String INSERT_USER_QUERY = "INSERT INTO Users (first_name, last_name, email, password) " +
            "VALUES(:firstName,:lastName,:email,:password);";

    public static final String COUNT_USER_EMAIL_QUERY = "SELECT COUNT(*) FROM Users WHERE email=:email;";

    public static final String GET_USER_WITH_TOKEN_ACCOUNT_VERIFICATION_QUERY = "SELECT * FROM Users u " +
            "INNER JOIN AccountVerifications a on u.ID=a.USER_ID WHERE a.TOKEN=:token;";

    public static final String INSERT_ACCOUNT_VERIFICATION_URL_QUERY = "INSERT INTO AccountVerifications (user_id, url) " +
            "VALUES(:userId, :url)";

    public static final String USER_VERIFICATION_ACCOUNT_ENABLED_QUERY = "UPDATE Users SET ENABLED=TRUE WHERE ID=:userId";

    public static final String GET_USER_BY_EMAIL_QUERY = "SELECT * FROM Users WHERE email=:email";

    public static final String SELECT_USER_BY_USER_CODE_QUERY = "SELECT * FROM Users u JOIN TwoFactorVerifications tfv " +
            "on tfv.user_id=u.id WHERE tfv.code=:code && u.email=:email";
}
