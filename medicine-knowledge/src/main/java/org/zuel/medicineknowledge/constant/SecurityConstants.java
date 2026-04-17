package org.zuel.medicineknowledge.constant;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * SecurityConstants
 *
 * @author star
 **/
public final class SecurityConstants {

    public static final String AUTH_GET_ARTICLES_URL = "/articles/get";

    public static final String AUTH_LIS_CATEGORY_URL = "/category/list";

    private SecurityConstants() {
        throw new IllegalStateException("Cannot create instance of static constant class");
    }

    /**
     * 用于登录的 url
     */
    public static final String AUTH_LOGIN_URL = "/user/login";
    public static final String AUTH_LOGIN_ADMIN_URL = "/user/login/admin";

    /**
     * 用于注册的 url
     */
    public static final String AUTH_REGISTER_URL = "/user/register";
    public static final String AUTH_GET_CODE_URL = "/user/getCode";
    public static final String AUTH_GET_EMAIL_URL = "/user/getEmail";
    public static final String AUTH_LIST_ARTICLES_URL = "/articles/list/page";

    public static final String AUTH_FORGETPASSWORD_URL = "/user/forgetPassword";

    /**
     * JWT签名密钥，这里使用 HS512 算法的签名密钥
     * <p>
     * 注意：最好使用环境变量或 .properties 文件的方式将密钥传入程序
     * 密钥生成地址：http://www.allkeysgenerator.com/
     */
    public static final String JWT_SECRET_KEY = Dotenv.load().get("JWT_SECRET_KEY");

    /**
     * 一般是在请求头里加入 Authorization，并加上 Bearer 标注
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Authorization 请求头
     */
    public static final String TOKEN_HEADER = "token";

    /**
     * token 类型
     */
    public static final String TOKEN_TYPE = "JWT";

    public static final String TOKEN_ROLE_CLAIM = "role";
    public static final String TOKEN_ISSUER = "security";
    public static final String TOKEN_AUDIENCE = "security-all";

    /**
     * 当 Remember 是 false 时，token 有效时间 2 小时
     */
    public static final long EXPIRATION_TIME = 60 * 60 * 2L;

    /**
     * 当 Remember 是 true 时，token 有效时间 7 天
     */
    public static final long EXPIRATION_REMEMBER_TIME = 60 * 60 * 24 * 7L;


}