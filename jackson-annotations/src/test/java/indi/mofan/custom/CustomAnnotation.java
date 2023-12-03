package indi.mofan.custom;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 * 自定义 Jackson 注解。
 * {@code @JacksonAnnotationsInside} 注解能够将所有被 {@code @JacksonAnnotation} 元注解标记
 * 的 Jackson 注解合并成一个组合注解
 * </p>
 *
 * @author mofan
 * @date 2023/12/3 16:03
 */
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "id", "dateCreated"})
public @interface CustomAnnotation {
}
