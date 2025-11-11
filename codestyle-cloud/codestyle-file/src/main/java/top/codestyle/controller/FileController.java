package top.codestyle.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.codestyle.service.FileService;
import top.codestyle.model.dto.file.FileReq;
import top.codestyle.model.query.FileQuery;
import top.codestyle.model.vo.FileResp;
import top.codestyle.model.vo.FileStatisticsResp;
import top.codestyle.model.vo.FileUploadResp;
import top.continew.starter.core.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;


/**
 * 公共 API
 *
 * @author GALAwang
 * @since 2023/1/22 21:48
 */
@Tag(name = "文件 API")
@Validated
@RequiredArgsConstructor
@RestController("/file")
@CrudRequestMapping(value = "/file", api = {Api.PAGE, Api.UPDATE, Api.DELETE})
public class FileController extends BaseController<FileService, FileResp, FileResp, FileQuery, FileReq>{

    private final FileService fileService;

    @Operation(summary = "上传文件", description = "上传文件")
    @PostMapping("/upload")
    public FileUploadResp upload(@NotNull(message = "文件不能为空")
                                 @RequestBody MultipartFile file) {
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        FileInfo fileInfo = fileService.upload(file);
        return FileUploadResp.builder().url(fileInfo.getUrl()).build();
    }


    @Operation(summary = "查询文件资源统计", description = "查询文件资源统计")
    @SaCheckPermission("file:list")
    @GetMapping("/statistics")
    public FileStatisticsResp statistics() {
        return baseService.statistics();
    }

    @Operation(summary = "下载本地文件并打包", description = "下载本地文件并打包")
    @SaCheckPermission("file:load")
    @PostMapping("/load")
    public  byte[] load(FileQuery query, HttpServletResponse response) {
//        FileQuery query = new FileQuery();
//        query.setPaths(Collections.singletonList(filePath));
        if (query.getPaths() == null){
            return null;
        }
        byte[] zipdata = baseService.load(query);
        // 设置响应头
        response.setHeader("attachment", "files.zip");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return zipdata;

    }
}