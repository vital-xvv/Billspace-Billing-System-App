package io.vital.billspace.query;

public class UserQuery {
    public static final String INSERT_USER_QUERY = "INSERT INTO USERS (first_name, last_name, email, password) " +
            "VALUES(:firstName,:lastName,:email,:password);";

    public static final String COUNT_USER_EMAIL_QUERY = "SELECT COUNT(*) FROM USERS WHERE email=:email;";

    public static final String GET_USER_WITH_TOKEN_ACCOUNT_VERIFICATION_QUERY = "SELECT * FROM USERS u INNER JOIN AccountVerifications a on u.ID=a.USER_ID WHERE a.TOKEN=:token;";

    public static final String INSERT_ACCOUNT_VERIFICATION_URL_QUERY = "INSERT INTO AccountVerifications (user_id, token) VALUES(:userId, :token)";

    public static final String USER_VERIFICATION_ACCOUNT_ENABLED_QUERY = "UPDATE USERS SET ENABLED=TRUE WHERE ID=:userId";

}
