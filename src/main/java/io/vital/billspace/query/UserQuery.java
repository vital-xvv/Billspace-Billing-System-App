package io.vital.billspace.query;

public class UserQuery {
    public static final String INSERT_USER_QUERY = "INSERT INTO Users (first_name, last_name, email, password) " +
            "VALUES(:firstName,:lastName,:email,:password);";

    public static final String COUNT_USER_EMAIL_QUERY = "SELECT COUNT(*) FROM Users WHERE email=:email;";

    public static final String GET_USER_WITH_TOKEN_ACCOUNT_VERIFICATION_QUERY = "SELECT * FROM Users u " +
            "INNER JOIN AccountVerifications a on u.id=a.user_id WHERE a.url=:url;";

    public static final String INSERT_ACCOUNT_VERIFICATION_URL_QUERY = "INSERT INTO AccountVerifications (user_id, url) " +
            "VALUES(:userId, :url)";

    public static final String USER_VERIFICATION_ACCOUNT_ENABLED_QUERY = "UPDATE Users SET ENABLED=TRUE WHERE ID=:userId";

    public static final String GET_USER_BY_EMAIL_QUERY = "SELECT * FROM Users WHERE email=:email";

    public static final String SELECT_USER_BY_USER_CODE_QUERY = "SELECT * FROM Users u JOIN TwoFactorVerifications tfv " +
            "on tfv.user_id=u.id WHERE tfv.code=:code && u.email=:email";

    public static final String SELECT_USER_BY_PASSWORD_URL_QUERY = "SELECT * FROM Users u JOIN " +
            "ResetPasswordVerifications r on u.id=r.user_id WHERE r.url = :url";

    public static final String UPDATE_USER_PASSWORD_BY_VERIFICATION_URL_QUERY = "UPDATE Users SET password = :password " +
            "WHERE id = (SELECT user_id FROM ResetPasswordVerifications WHERE url = :url)";

    public static final String UPDATE_USER_DETAILS_QUERY = "UPDATE Users SET first_name=:firstName, last_name=:lastName," +
            " email=:email, phone=:phone, address=:address, title=:title, bio=:bio WHERE id=:id;";

    public static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM Users WHERE id=:id";

    public static final String UPDATE_USER_PASSWORD_BY_ID_QUERY = "UPDATE Users SET password=:newPassword WHERE id=:userId";

    public static final String UPDATE_USER_ROLE_BY_ROLENAME_USER_ID_QUERY = "UPDATE UserRoles SET role_id=(SELECT id FROM Roles WHERE name=:roleName) WHERE user_id=:userId";

    public static final String UPDATE_USER_ACCOUNT_SETTINGS_QUERY = "UPDATE Users SET enabled=:enabled, non_locked=:nonLocked WHERE id=:userId";

    public static final String UPDATE_MFA_QUERY = "UPDATE Users SET using_mfa=:mfa WHERE id=:userId";

    public static final String UPDATE_USER_IMAGE_QUERY = "UPDATE Users SET image_url=:imageUrl WHERE id=:userId";
}
