package com.shinkte.common;

/**
 * 返回工具类
 *
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        /*public static <T> BaseResponse<T> success(T data, String message)
        * 公共类可访问，且不用实例化对象，通过类名.方法名调用
        * <T>:泛型声明，表示该方法可以处理任意类型的数据
        * BaseResponse<T>:返回值类型，表示该方法返回一个BaseResponse对象，其泛型类型为T的数据，T可以是任意类型
        * success(T data):方法名，表示该方法是一个成功的返回方法，其参数data表示返回的数据，参数是一个泛型类型T的数据，T可以是任意类型。
        * */
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code
     * @param message
     * @param description
     * @return
     */
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code, null, message, description);
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getCode(), null, message, description);
    }


    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getCode(), errorCode.getMessage(), description);
    }
}

