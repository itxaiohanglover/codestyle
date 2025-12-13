/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.codestyle.utils;

import org.springframework.data.domain.Pageable;
import top.codestyle.pojo.dto.HomePageSearchResultDTO;
import top.codestyle.pojo.entity.CodeStyleTemplateDO;
import top.codestyle.pojo.vo.HomePageSearchDocResultVO;
import top.codestyle.pojo.vo.HomePageSearchPageableResultVO;

import java.util.List;

/**
 * @author ChonghaoGao
 * @date 2025/11/12 20:45)
 */
public class VOConvertUtils {
    /**
     * 将 SearchHits 转换为普通 Page 对象
     */
    public static HomePageSearchPageableResultVO searchCovertToHomePageSearchVO(Pageable pageable,
                                                                                HomePageSearchResultDTO homePageSearchDocResultDTO) {

        List<HomePageSearchDocResultVO> voList = homePageSearchDocResultDTO.getSearchHitList()
            .stream()
            .map(hit -> convertToDocVO(hit.getContent()))
            .toList();
        return HomePageSearchPageableResultVO.createResponseVO(voList, pageable, homePageSearchDocResultDTO
            .getAggTags(), homePageSearchDocResultDTO.getTotalHits());
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
