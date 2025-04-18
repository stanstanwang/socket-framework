package com.zeewain.socket.core.doc;

import ch.qos.logback.core.util.Loader;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import com.zeewain.socket.core.NettyMapping;
import com.zeewain.socket.core.doc.dto.ApiInfo;
import com.zeewain.socket.core.doc.dto.TypeInfo;
import com.zeewain.socket.protocol.NettyResponse;
import com.zeewain.socket.protocol.base.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Promise;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 *
 */
@Slf4j
public class DocParser {


    // TODO sly 2022/8/23 缓存 map 信息， 避免多次解析
    private static Map<String, TypeInfo> typeInfoCache = new HashMap<>();

    private static ResourceLoader resourceLoader;

    private final String dir;

    /**
     * 要过滤的组
     */
    @Setter
    private Set<String> includeGroups = Collections.emptySet();
    @Setter
    private Set<String> excludeGroups = Collections.emptySet();

    /**
     * 要过滤的路径
     */
    @Setter
    private Set<String> includePaths = Collections.emptySet();

    public DocParser(String dir) {
        this.dir = dir;
    }


    /**
     * 解析包信息
     */
    public List<ApiInfo> parsePackages() {
        List<ApiInfo> list = new ArrayList<>();
        for (Class<?> clazz : parseClasses(dir)) {
            list.addAll(parseApis(clazz));
        }
        return list;
    }

    /**
     * 根据路径解析类型信息
     */
    public List<Class<?>> parseClasses(String locationPattern) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory();

        List<Class<?>> list = new ArrayList<>();
        try {
            for (Resource resource : resolver.getResources(locationPattern)) {
                MetadataReader reader = metaReader.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                list.add(Loader.loadClass(className));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }


    public List<ApiInfo> parseApis(Class<?> clazz) {
        Api api = clazz.getAnnotation(Api.class);
        if (api == null) {
            log.warn("没有@Api注解 {}", clazz.getName());
            return Collections.emptyList();
        }


        String parentPath = Optional.ofNullable(clazz.getAnnotation(NettyMapping.class))
                .map(it -> it.value()[0]).orElse("");

        String group = StringUtils.hasLength(api.tags()[0])
                ? api.tags()[0] : api.value();

        // 过滤组信息， 调试的时候方便
        if (!excludeGroups.isEmpty() && excludeGroups.contains(group)) {
            return Collections.emptyList();
        }

        if (!includeGroups.isEmpty() && !includeGroups.contains(group)) {
            return Collections.emptyList();
        }

        List<ApiInfo> list = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            ApiInfo apiInfo = parseApi(method);
            if (apiInfo != null) {
                // 设置组的信息
                apiInfo.setGroup(group);
                // 重新更新 path 信息
                apiInfo.setPath(parentPath + apiInfo.getPath());
                list.add(apiInfo);
            }
        }
        return list;
    }


    public @Nullable ApiInfo parseApi(Method method) {
        NettyMapping mapping = method.getAnnotation(NettyMapping.class);
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (mapping == null) {
            return null;
        }
        if (apiOperation == null || apiOperation.hidden()) {
            log.warn("没有@ApiOperation注解 {}", method);
            return null;
        }


        String path = mapping.value()[0];
        // 如果
        if (!includePaths.isEmpty() && !includePaths.contains(path)) {
            return null;
        }
        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setPath(path);
        apiInfo.setName(apiOperation.value());
        apiInfo.setDesc(apiOperation.notes());

        // 解析参数
        for (Type type : method.getGenericParameterTypes()) {
            Class<?> parameterType = TypeUtil.getClass(type);
            if (!parameterType.isAssignableFrom(ChannelHandlerContext.class)
                    && !parameterType.isAssignableFrom(Channel.class)
                    && !parameterType.isAssignableFrom(Duration.class)
            ) {
                apiInfo.setParameter(parseParameter(type));
                break;
            }
        }

        // 解析响应值
        Type type = method.getGenericReturnType();
        apiInfo.setResult(parseReturnValue(type));
        return apiInfo;
    }


    /**
     * 传类过来。分析参数
     */
    private TypeInfo parseParameter(Type type) {
        return parseTypeInfo(type);
    }


    private Class<?> getListType(Type genericType) {
        return TypeUtil.getClass(TypeUtil.getTypeArgument(genericType));
    }


    /**
     * 解析响应值
     */
    public TypeInfo parseReturnValue(Type type) {
        // 忽略无响应值的情况
        if (void.class.isAssignableFrom(TypeUtil.getClass(type))) {
            return null;
        }

        // 忽略 promise
        if (Promise.class.isAssignableFrom(TypeUtil.getClass(type))
                || Publisher.class.isAssignableFrom(TypeUtil.getClass(type))) {
            type = TypeUtil.getTypeArgument(type);
        }

        // 忽略 netty response
        // 另外 promise 可能内嵌 response， 所以这里分开2步些
        if (NettyResponse.class.isAssignableFrom(TypeUtil.getClass(type))) {
            type = TypeUtil.getTypeArgument(type);
        }
        if (Response.class.isAssignableFrom(TypeUtil.getClass(type))) {
            type = TypeUtil.getTypeArgument(type);
        }

        // 解析
        TypeInfo typeInfo = parseTypeInfo(type);

        // 重新嵌套一层做响应值使用
        TypeInfo wrapper = new TypeInfo();
        wrapper.setObject(true);
        wrapper.setTypeInfos(Arrays.asList(
                new TypeInfo("code", "状态码", Integer.class.getSimpleName(), 0, true),
                new TypeInfo("message", "成功或失败的信息", String.class.getSimpleName(), "成功", true),
                typeInfo.clone().setName("data").setDesc("数据") // .setObject(true)
        ));

        return wrapper;
    }


    /**
     * 解析参数类型， 默认最外层有 list/object/basic 这3种类型
     */
    TypeInfo parseTypeInfo(Type type) {
        TypeInfo typeInfo = new TypeInfo();
        Class<?> parameterType = TypeUtil.getClass(type);
        if (isBasicType(parameterType)) {
            typeInfo.setBasic(true);
            typeInfo.setType(parameterType.getSimpleName());
            return typeInfo;
        } else if (Collection.class.isAssignableFrom(parameterType)) {
            typeInfo.setList(true);
            parameterType = getListType(type);
        } else {
            typeInfo.setObject(true);
        }

        assert parameterType != null;
        List<TypeInfo> list = new ArrayList<>();
        // 非基本类型，才展开
        if (!isBasicType(parameterType)) {
            Field[] allFields = ReflectUtil.getFields(parameterType);
            for (Field field : allFields) {
                TypeInfo filedInfo = parseField(field);
                if (filedInfo != null) {
                    list.add(filedInfo);
                }
            }
        } else {
            // 基本类型，直接取基本类型便可
            if (typeInfo.isList()) {
                list.add(new TypeInfo("List", parameterType.getSimpleName()));
            } else {
                list.add(new TypeInfo(parameterType.getSimpleName()));
            }
        }

        typeInfo.setTypeInfos(list);
        return typeInfo;
    }


    /**
     * 递归遍历参数
     */
    private TypeInfo parseField(Field field) {
        // 判断属性是否常量
        if (Modifier.isFinal(field.getModifiers())) {
            return null;
        }
        if (Modifier.isStatic(field.getModifiers())) {
            return null;
        }
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setName(field.getName());
        Class<?> filedType = field.getType();
        typeInfo.setType(filedType.getSimpleName());

        // 当前只有标注才
        ApiModelProperty apiModelProperty = field.getAnnotation(ApiModelProperty.class);
        if (apiModelProperty == null) {
            log.warn("{}的@ApiModelProperty为空", field);
            // return null;
        } else {
            typeInfo.setRequired(apiModelProperty.required());
            typeInfo.setDesc(apiModelProperty.value());
        }
        // 如果是基本类型或者字符串
        if (isBasicType(filedType)) {
            typeInfo.setBasic(true);
            return typeInfo;
        }

        // 非基本类型， 递归解析出真正类型
        Class<?> childType;
        if (Collection.class.isAssignableFrom(filedType)) {
            typeInfo.setList(true);
            childType = getListType(field.getGenericType());
            // 可能 list 里边是简单类型
            if (isBasicType(childType)) {
                typeInfo.setTypeInfos(Collections.singletonList(new TypeInfo(childType.getSimpleName())));
                return typeInfo;
            }
        } else {
            typeInfo.setObject(true);
            childType = filedType;
        }

        assert childType != null;
        Field[] fields = ReflectUtil.getFields(childType);
        for (Field child : fields) {
            TypeInfo childInfo = parseField(child);
            if (childInfo != null) {
                typeInfo.getTypeInfos().add(childInfo);
            }
        }
        return typeInfo;
    }

    public static boolean isBasicType(Class<?> clazz) {
        return ClassUtil.isBasicType(clazz) || String.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz) ||
                LocalDate.class.isAssignableFrom(clazz) || LocalDateTime.class.isAssignableFrom(clazz);
    }


}
