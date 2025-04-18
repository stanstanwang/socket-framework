package com.zeewain.socket.netty.doc;

import com.zeewain.socket.netty.doc.dto.ApiInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author songlongyue
 * @version 2022/8/17
 * @since
 **/
@Slf4j
public class DocGenerator {


    /**
     * 要解析的路径
     */
    private String parsePattern = "classpath*:com/zeewain/**/processor/**/*.class";

    /**
     * 要输出的路径
     */
    private String outputPath = "src/main/resources/docGenerator.md";

    /**
     * 要过滤的组
     */
    private Set<String> includeGroups = Collections.emptySet();

    /**
     * 要过滤了的组
     */
    private Set<String> excludeGroups = Collections.emptySet();

    /**
     * 要过滤的路径
     */
    private Set<String> includePaths = Collections.emptySet();


    public DocGenerator() {
    }

    public DocGenerator(String parsePattern, String outputPath) {
        this.parsePattern = parsePattern;
        this.outputPath = outputPath;
    }

    /**
     * 文档生成器
     */
    // private DocRender render;


    /**
     * 解析器
     */
    // private DocParser parser;




    public void generate() {
        DocParser parser = new DocParser(parsePattern);
        parser.setIncludeGroups(includeGroups);
        parser.setIncludePaths(includePaths);
        parser.setExcludeGroups(excludeGroups);

        DocRender render = new MarkdownRender(outputPath);

        List<ApiInfo> apiInfos = parser.parsePackages();
        render.render(apiInfos);
    }


    public void setParsePattern(String parsePattern) {
        this.parsePattern = parsePattern;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setIncludeGroups(Set<String> includeGroups) {
        this.includeGroups = includeGroups;
    }

    public void setIncludePaths(Set<String> includePaths) {
        this.includePaths = includePaths;
    }

    public void setExcludeGroups(Set<String> excludeGroups) {
        this.excludeGroups = excludeGroups;
    }
}
