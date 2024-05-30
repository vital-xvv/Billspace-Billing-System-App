package io.vital.billspace.query;

public class RoleQuery {
    public static final String INSERT_ROLE_TO_USER_QUERY = "INSERT INTO UserRoles (user_id, role_id) " +
            "VALUES(:userId, :roleId);";
    public static final String SELECT_ROLE_BY_NAME_QUERY = "SELECT * FROM Roles WHERE name=:roleName;";
    public static final String SELECT_ROLE_BY_USER_ID_QUERY = "SELECT * FROM Roles r " +
            "INNER JOIN UserRoles ur on ur.role_id=r.id WHERE ur.user_id=:user_id";
    public static final String SELECT_ROLES_QUERY = "SELECT * FROM Roles";
}
