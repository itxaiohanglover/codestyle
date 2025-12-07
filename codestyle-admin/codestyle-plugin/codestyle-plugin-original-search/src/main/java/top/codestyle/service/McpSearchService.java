package top.codestyle.service;

import top.codestyle.entity.es.vo.McpSearchResultVO;

import java.io.File;
import java.io.IOException;

/**
 * 用于mcp检索
 */
public interface McpSearchService {


    McpSearchResultVO searchMcp(String templateKeyword);

    boolean saveMetaJsonToElasticsearch(File tempFile) throws IOException;
}
