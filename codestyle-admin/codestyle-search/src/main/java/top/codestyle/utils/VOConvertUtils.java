package top.codestyle.utils;

import co.elastic.clients.elasticsearch._types.SortOrder;
import org.springframework.data.domain.Pageable;
import top.codestyle.pojo.dto.HomePageSearchResultDTO;
import top.codestyle.pojo.dto.TimeRangeParamDTO;
import top.codestyle.pojo.entity.CodeStyleTemplateDO;
import top.codestyle.pojo.enums.TemplateSortField;
import top.codestyle.pojo.vo.HomePageSearchDocResultVO;
import top.codestyle.pojo.vo.HomePageSearchPageableResultVO;
import top.codestyle.repository.CodeStyleTemplateRepository;

import java.util.List;

/**
 * @author ChonghaoGao
 * @date 2025/11/12 20:45)
 */
public class VOConvertUtils {
    /**
     * 将 SearchHits 转换为普通 Page 对象
     */
    public static HomePageSearchPageableResultVO searchCovertToHomePageSearchVO(
            Pageable pageable,
            HomePageSearchResultDTO homePageSearchDocResultDTO
    ){

        List<HomePageSearchDocResultVO> voList = homePageSearchDocResultDTO
                .getSearchHitList()
                .stream()
                .map(hit -> convertToDocVO(hit.getContent()))
                .toList();
        return HomePageSearchPageableResultVO.createResponseVO(
                voList,
                pageable,
                homePageSearchDocResultDTO.getAggTags(),
                homePageSearchDocResultDTO.getTotalHits()
        );
    }

    public static HomePageSearchDocResultVO convertToDocVO(CodeStyleTemplateDO doObj) {
        HomePageSearchDocResultVO vo = new HomePageSearchDocResultVO();
        // TODO: 映射字段
        vo.setId(doObj.getId());
        vo.setNameCh(doObj.getNameCh());
        vo.setNameEn(doObj.getNameEn());
        vo.setMemberName(doObj.getMemberNames());
        vo.setMemberAvatarUrls(doObj.getMemberAvatarUrls());
        vo.setCreatorName(doObj.getCreatorName());
        vo.setCreateTime(doObj.getCreateTime());
        vo.setUpdateTime(doObj.getUpdateTime());
        vo.setEditTime(doObj.getEditTime());
        vo.setDescription(doObj.getDescription());
        vo.setTotalLikeCount(doObj.getTotalLikeCount());
        vo.setTotalFavoriteCount(doObj.getTotalFavoriteCount());
        vo.setTags(doObj.getSearchTags());
        vo.setTemplateAvatar(doObj.getAvatar());
        vo.setVersion(doObj.getVersion());
        return vo;
    }

}
