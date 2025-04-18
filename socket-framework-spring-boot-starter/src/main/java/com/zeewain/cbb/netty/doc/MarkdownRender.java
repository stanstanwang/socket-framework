package com.zeewain.cbb.netty.doc;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zeewain.cbb.netty.doc.dto.ApiInfo;
import com.zeewain.cbb.netty.doc.dto.TypeInfo;
import lombok.extern.slf4j.Slf4j;
import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.text.code.CodeBlock;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;
import net.steppschuh.markdowngenerator.text.heading.Heading;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static net.steppschuh.markdowngenerator.table.Table.*;


/**
 * @author stan
 * @date 2022/8/23
 */
@Slf4j
public class MarkdownRender implements DocRender {

    private final String outputPath;


    // list 里边是基本类型
    List<String> basicTypes = Arrays.asList(Integer.class.getSimpleName(),
            Long.class.getSimpleName(),
            Float.class.getSimpleName(),
            Double.class.getSimpleName(),
            Boolean.class.getSimpleName(),
            String.class.getSimpleName());

    public MarkdownRender(String outputPath) {
        this.outputPath = outputPath;
    }


    @Override
    public void render(List<ApiInfo> apis) {
        StringBuilder sb = new StringBuilder();
        Map<String, List<ApiInfo>> group2Apis = apis.stream()
                .collect(Collectors.groupingBy(ApiInfo::getGroup, TreeMap::new, Collectors.toList()));

        group2Apis.entrySet().stream()
                .map(e -> renderByGroup(e.getKey(), e.getValue()))
                .forEach(sb::append);

        File file = new File(outputPath);
        FileUtil.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8), file);
        log.info("接口文档生成， 路径 {}", file.getAbsolutePath());
    }

    /**
     * 根据组来渲染
     */
    // @Override
    private StringBuilder renderByGroup(String group, List<ApiInfo> apis) {
        StringBuilder sb = new StringBuilder();
        // 二级标题，类的注释
        sb.append("\n").append(new Heading(group, 2)).append("\n");

        // 根据标题的名称排序后输出
        apis.sort(Comparator.comparingInt(it -> {
            String name = it.getName();
            int idx = name.indexOf('.');
            if (idx != -1) {
                return Integer.parseInt(name.split("\\.")[0].trim());
            }
            // 没有数字的不排序
            return 99;
        }));

        for (ApiInfo api : apis) {
            //三级标题，方法的注释
            sb.append(new Heading(api.getName() + "（" + api.getPath() + "）", 3)).append("\n");

            if (StringUtils.hasLength(api.getDesc())) {
                sb.append("\n" + "接口说明:" + "\n");
                sb.append("\n").append(new BoldText(api.getDesc())).append("\n");
            }
            sb.append("\n" + "参数说明:" + "\n");
            if (api.getParameter() != null) {
                sb.append(renderType(api.getParameter()));
            } else {
                sb.append("无\n");
            }
            sb.append("\n" + "返回值说明:" + "\n");
            if (api.getResult() != null) {
                sb.append(renderType(api.getResult())).append("\n");
            } else {
                sb.append("无\n\n");
            }
        }
        return sb;
    }


    public StringBuilder renderType(TypeInfo typeInfo) {
        StringBuilder requestTableAndCode = new StringBuilder();
        Table table = toTable(typeInfo);
        requestTableAndCode.append("\n")
                .append(table).append("\n");
        CodeBlock codeBlock = toCodeBlock(typeInfo);
        if (codeBlock != null) {
            requestTableAndCode.append(codeBlock).append("\n");
        }
        return requestTableAndCode;
    }


    private Table toTable(TypeInfo typeInfo) {
        // markdown 表格参数说明
        Builder tableBuilder = new Builder()
                .withAlignments(ALIGN_LEFT, ALIGN_CENTER, Table.ALIGN_LEFT, ALIGN_RIGHT)
                // .withRowLimit(typeInfos.size() + 1)
                .addRow("字段名称", "字段类型", "说明", "是否必须");
        iterByObject(typeInfo, tableBuilder, "");
        return tableBuilder.build();
    }


    /**
     * 递归参数， 渲染成 table 类型
     */
    private void iterByObject(TypeInfo parent, Builder tableBuilder, String namePrefix) {
        for (TypeInfo child : parent.getTypeInfos()) {
            // 确定真实名称
            String actualName;
            if (StringUtils.hasLength(namePrefix)) {
                actualName = namePrefix + "." + child.getName();
            } else {
                actualName = child.getName();
            }

            tableBuilder.addRow(actualName, child.getType(), child.getDesc(), child.getRequired());
            if (child.isObject()) {
                iterByObject(child, tableBuilder, actualName);
            } else if (child.isList() && !isListBasic(child)) {
                iterByObject(child, tableBuilder, actualName);
            }
        }
    }


    private @Nullable CodeBlock toCodeBlock(TypeInfo typeInfo) {
        Object code = null;
        if (typeInfo.isList()) {
            code = iterByList(typeInfo);
        } else if (typeInfo.isObject()) {
            code = iterByObject(typeInfo);
        } else if (typeInfo.isBasic()) {
            code = DocRender.randomByType(typeInfo.getType());
        }

        if (code != null) {
            return new CodeBlock(JSONObject.toJSONString(code, SerializerFeature.PrettyFormat), "Json");
        } else {
            return null;
        }
    }


    /**
     * 递归解析list类型
     */
    private List<?> iterByList(TypeInfo parent) {
        if (isListBasic(parent)) {
            List<Object> simpleList = new ArrayList<>(1);
            simpleList.add(DocRender.randomByType(parent.getTypeInfos().get(0).getType()));
            return simpleList;
        }

        // list 里边是对象
        List<Map<String, Object>> objList = new ArrayList<>(1);
        objList.add(iterByObject(parent));
        return objList;
    }

    private boolean isListBasic(TypeInfo typeInfo) {
        return typeInfo.isList() &&
                typeInfo.getTypeInfos().size() == 1
                && basicTypes.contains(typeInfo.getTypeInfos().get(0).getType());
    }


    /**
     * 递归解析 map 类型
     */
    private Map<String, Object> iterByObject(TypeInfo parent) {
        Map<String, Object> map = new HashMap<>();
        for (TypeInfo child : parent.getTypeInfos()) {
            String name = child.getName();
            if (child.isObject()) {
                Map<String, Object> childMap = iterByObject(child);
                map.put(name, childMap);
            } else if (child.isList()) {
                Object childList = iterByList(child);
                map.put(name, childList);
            } else {
                if (child.getDefaultValue() != null) {
                    map.put(name, child.getDefaultValue());
                } else {
                    map.put(name, DocRender.randomByType(child.getType()));
                }
            }
        }
        return map;
    }


}
